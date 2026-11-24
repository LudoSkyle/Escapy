## Build et dépendances

### Catalogue de versions

Toutes les versions et dépendances sont déclarées dans `gradle/libs.versions.toml`. Aucune version
en dur dans un `build.gradle.kts`.

- Une entrée inutilisée est supprimée du catalogue : elle fait croire que la techno est en place.
- Un commentaire accompagne toute version contrainte, avec la raison : Material 3 en alpha, alignée
  sur Compose Multiplatform ; GitLive épinglé en bêta
  ([ADR-0003](../adr/0003-firebase-en-kotlin-commun-avec-gitlive.md)).
- La cible Java/Kotlin est alignée sur **tous** les modules (JVM 17, par le `build.gradle.kts`
  racine et les `compileOptions` de `androidApp`).
- Le bloc `dependencies` d'un module est trié et commenté par famille (Modules, Compose, Koin,
  Firebase, Logs, Tests…).

### Plugins par module

AGP 9 interdit d'appliquer `com.android.application` à un module Kotlin Multiplatform : l'app
Android est un module à part, qui ne fait qu'héberger `shared`.

Les plugins Gradle de tous les modules sont sur le classpath de `buildSrc`
(`buildSrc/build.gradle.kts`), avec leur version venue du catalogue. Un module les applique par leur
id, sans version, et le `build.gradle.kts` racine n'a pas de bloc `plugins` : un plugin déclaré à la
fois à la racine et dans `buildSrc` ne se charge pas.

La configuration commune des modules KMP passe par deux plugins de convention de `buildSrc` :

- `escapy-kmp-library` : `org.jetbrains.kotlin.multiplatform`,
  `com.android.kotlin.multiplatform.library`, cibles `iosArm64` et `iosSimulatorArm64`,
  `compileSdk` et `minSdk` du catalogue ;
- `escapy-compose-library` : la même chose, plus `org.jetbrains.compose`,
  `org.jetbrains.kotlin.plugin.compose`, les ressources Android (Compose Resources en a besoin), le
  fichier de stabilité et `ui-tooling` pour les previews d'Android Studio.

| Module | Plugins |
|---|---|
| `androidApp` | `com.android.application`, `org.jetbrains.kotlin.plugin.compose`, `com.google.gms.google-services` (seulement avec le fichier Firebase) |
| `shared` | `escapy-compose-library`, `org.jetbrains.kotlin.plugin.serialization` |
| `designSystem` | `escapy-compose-library` |
| `domain` | `escapy-kmp-library` |
| `data` | `escapy-kmp-library` |

Une configuration qui se répète dans plusieurs modules va dans un plugin de convention plutôt que
d'être copiée. Les fonctions `catalogVersion` et `catalogLibrary` (`EscapyCatalog.kt`) y lisent le
catalogue, dont les accesseurs typés n'existent pas dans `buildSrc`.

### Configuration d'un module KMP

- Cibles : `android`, `iosArm64()` (iPhone) et `iosSimulatorArm64()` (simulateur sur Mac Apple
  Silicon). Pas d'`iosX64()` : les Mac Intel ne sont pas une cible de développement.
- `minSdk` (26) et `compileSdk` (37) viennent du catalogue.
- Une dépendance disponible sur toutes les plateformes se déclare dans `commonMain`, les autres dans
  le source set de leur plateforme (`androidMain`, `iosMain`).
- Les tests hôtes se déclarent dans le module (`withHostTest { }`) : AGP n'accepte qu'un seul appel,
  un plugin de convention ne peut pas le faire à sa place.
- Les modules Compose déclarent le package de leur classe `Res`
  (`compose.resources { packageOfResClass = "..." }` : `fr.skyle.escapy.resources`,
  `fr.skyle.escapy.designsystem.resources`), qui reste `internal` au module.
- Le fichier de stabilité partagé, `compose-stability.conf` à la racine, est branché par
  `escapy-compose-library` (`composeCompiler { stabilityConfigurationFiles.add(...) }`).

### Android (`androidApp`)

- `applicationId` : `fr.skyle.escapy` ; la variante debug ajoute `.debug` et le suffixe de version
  `-debug`. `targetSdk` 37.
- La version (`APP_VERSION_NAME`, `versionName`) est déclarée dans `androidApp/build.gradle.kts`,
  et se change par une pull request. `versionCode` en découle, le même à chaque build : majeure ×
  10 000 + mineure × 100 + correctif, `2.0` donnant 20000, `2.1.3` 20103. iOS suit cette version.
- L'app est en portrait, sombre seulement : `MainActivity` pose des barres système transparentes à
  icônes claires (`enableEdgeToEdge`), et l'écran de démarrage (`core-splashscreen`,
  `Theme.Escapy.Starting`, `installSplashScreen()`) montre l'icône sur le fond de l'app.
- La release est minifiée (`isMinifyEnabled`, `isShrinkResources`). `proguard-rules.pro` n'a
  aucune règle : une release minifiée a été vérifiée contre Firebase (Auth, Firestore, Functions,
  Storage) avec kotlinx-serialization ([ADR-0003](../adr/0003-firebase-en-kotlin-commun-avec-gitlive.md)).
  Toute classe instanciée par réflexion s'y ajoute, avec la raison. Aucun code ne s'appuie sur un
  nom de classe, obfusqué en release (voir [02_CONVENTIONS.md](./02_CONVENTIONS.md)).

### Le sous-module privé et le build sans lui

`private/` est un sous-module privé : il porte les clés de signature et les fichiers Firebase. Son
contenu n'est jamais affiché ni décrit (le dépôt est public), et il n'est modifié qu'à la demande de
l'utilisateur.

Avec le sous-module :

- la debug **et** la release sont signées avec ses clés. **La clé API Android du projet Firebase
  n'accepte que les apps signées avec les clés du sous-module** : une debug signée avec la clé debug
  par défaut de la machine est refusée par Firebase. Les builds debug locaux doivent donc être signés
  avec le keystore debug du sous-module, ce que le build fait seul quand `private/` est là ;
- `google-services.json` est copié depuis le sous-module dans `androidApp/` (tâche
  `clonePrivateFiles`, après `preBuild`), où il est ignoré par git, et le plugin Google Services
  s'applique : Firebase démarre avec l'app ;
- sur iOS, une phase de build copie `GoogleService-Info.plist` dans l'app (voir
  [iosApp/CLAUDE.md](../../iosApp/CLAUDE.md)).

Sans le sous-module (un fork, la CI) :

- le debug est signé avec la clé Android par défaut, la release n'est pas signée ;
- Google Services ne s'applique pas, aucun fichier Firebase n'est copié : **Firebase est absent**, et
  l'app démarre déconnectée. `FirebaseStatus` (`data`) le constate, et `SessionRepository` rend
  `SignedOut` sans appeler Firebase ;
- le build se configure, se teste et produit les deux apps quand même.

### iOS (`iosApp`)

- `shared` produit un framework **statique** nommé `Shared` pour `iosArm64` et `iosSimulatorArm64` :
  Firebase est lié une fois, par l'app.
- Le projet Xcode le construit et l'intègre par une phase de build qui lance
  `./gradlew :shared:embedAndSignAppleFrameworkForXcode`.
- Le projet Xcode est généré par XcodeGen depuis `iosApp/project.yml`, seul fichier à éditer. Le
  projet généré est commité.
- Firebase iOS arrive par le paquet de liaison SwiftPM généré par le plugin Kotlin
  (`iosApp/KotlinMultiplatformLinkedPackage`), commité et régénéré après tout changement de module
  GitLive. Le détail, la commande et ses pièges sont dans [iosApp/CLAUDE.md](../../iosApp/CLAUDE.md).
- Bundle ID `fr.skyle.escapy`, cible minimale iOS 18.5, iPhone en portrait. Aucune équipe Apple
  n'est commitée : elle se déclare dans `iosApp/Configuration/Local.xcconfig`, ignoré par git.

### Intégration continue

GitHub Actions, `.github/workflows/ci.yml` : à chaque pull request, à chaque push sur `develop` et
`main`, et à la main. Un nouveau push sur une pull request arrête le passage précédent. Sur un runner
`macos-26` (le Xcode du projet, le SDK Android), Java 17 :

1. `:data:allTests`, `:domain:allTests`, `:shared:allTests` (JVM et simulateur iOS) et
   `:androidApp:assembleDebug` ;
2. le paquet de liaison SwiftPM régénéré (`:shared:integrateLinkagePackage`), puis
   `git diff --exit-code` : un module GitLive ajouté ou retiré sans régénérer le paquet fait échouer
   la CI ;
3. l'app iOS : `xcodebuild build` sur le simulateur iPhone 17, sans signature
   (`CODE_SIGNING_ALLOWED=NO`) ;
4. en cas d'échec, les rapports de tests sont gardés.

- Les paquets Swift (le SDK Firebase iOS et ses dépendances) sont mis en cache, d'après les deux
  `Package.resolved`.
- La CI ne clone pas le sous-module privé, qu'un fork n'atteint pas : elle construit sans lui (voir
  ci-dessus). Aucun secret n'y est lu.
- Une pull request n'est fusionnée qu'une fois la CI verte.
- Un second workflow, `jekyll-gh-pages.yml`, publie le site public de `docs/` à chaque push sur
  `main` ; `docs/_config.yml` en exclut la documentation interne.

### Commandes utiles

```bash
./gradlew :domain:allTests                       # tests d'un module (JVM et simulateur iOS)
./gradlew :androidApp:assembleDebug              # app Android
./gradlew :androidApp:assembleRelease            # release minifiée (signée seulement avec private/)
./gradlew :shared:linkDebugFrameworkIosSimulatorArm64   # vérifie que le framework iOS compile
```
