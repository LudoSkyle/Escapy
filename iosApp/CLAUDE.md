# CLAUDE.md — hôte iOS

Périmètre : l'hôte iOS (`iosApp/`) — projet Xcode, app SwiftUI, `Info.plist`, réglages de build,
paquet de liaison SwiftPM qui apporte Firebase. Ne couvre ni les écrans (dans `shared`, voir
[../docs/SOMMAIRE.md](../docs/SOMMAIRE.md)), ni le code natif iOS (dans `shared/src/iosMain` et
`data/src/iosMain`, voir [../docs/KMP/03_CODE_PLATEFORME.md](../docs/KMP/03_CODE_PLATEFORME.md)).

## Règles

- **Le projet Xcode est généré par XcodeGen.** Seul `project.yml` s'édite, puis
  `xcodegen generate` se lance depuis `iosApp/`. Le `.pbxproj` ne se modifie jamais à la main ni par
  l'interface de Xcode : il serait écrasé à la génération suivante. Le projet généré est commité,
  pour s'ouvrir directement, et régénéré dans le commit qui change `project.yml`. Son schéma
  `iosApp` est partagé (`scheme: {}`) : la ligne de commande et la CI le trouvent dans un clone neuf.
- **Aucun écran en SwiftUI** ([ADR-0002](../docs/adr/0002-ui-commune-en-compose-multiplatform.md)).
  `iOSApp.swift` démarre Firebase (`FirebaseStarterKt.startFirebase()`), puis Koin
  (`KoinInitializerKt.doInitKoin()`, la fonction Kotlin `initKoin`), dans cet ordre : Koin lit si
  Firebase a démarré. `ContentView.swift` affiche `MainViewControllerKt.MainViewController()`, qui
  porte `EscapyApp`, sous les barres système (`ignoresSafeArea`) : Compose gère les insets. Du Swift
  ne s'ajoute que pour une API inaccessible depuis Kotlin, fournie à `shared` au démarrage derrière
  une interface commune.
- La phase de build « Compile Kotlin Framework » lance
  `./gradlew :shared:embedAndSignAppleFrameworkForXcode` : elle construit le framework **statique**
  `Shared` et copie les Compose Resources dans l'app. Elle a besoin de
  `ENABLE_USER_SCRIPT_SANDBOXING = NO`. Le framework doit rester statique : Firebase n'est lié qu'une
  fois, par l'app.
- **GoogleSignIn** est déclaré dans `shared/build.gradle.kts` (`swiftPMDependencies`, version au
  catalogue) et appelé en Kotlin depuis `iosMain` (`swiftPMImport.fr.skyle.escapy.shared.*`) : il
  arrive dans l'app par le même paquet de liaison. Ses dépendances (app-check, gtm-session-fetcher,
  GoogleUtilities) doivent rester dans les plages que résout `firebase-ios-sdk`.
- **Schéma d'URL de Google** : la phase « Add the URL scheme of Google Sign-In » ajoute au
  `Info.plist` construit le `REVERSED_CLIENT_ID` du fichier Firebase de `private/`, et ne l'ajoute
  pas sans lui : aucun identifiant du projet dans le dépôt. Elle déclare l'`Info.plist` construit
  comme fichier d'entrée : sans lui, Xcode écrivait l'`Info.plist` après la phase et effaçait le
  schéma. `iOSApp.swift` transmet les URL reçues à `handleGoogleSignInUrl`.
- **Firebase arrive uniquement par le paquet de liaison SwiftPM**, `KotlinMultiplatformLinkedPackage`,
  généré par le plugin Kotlin à partir des modules GitLive du code Kotlin, et déclaré dans
  `project.yml` (`packages:` et `dependencies:` de la cible). **Ne jamais redéclarer Firebase dans
  `project.yml`** : ses symboles seraient liés deux fois. Un produit Firebase de plus (Crashlytics,
  par exemple) passe par le code Kotlin, jamais par un paquet ajouté à la main.
  - Il ne lie que FirebaseAuth, FirebaseCore, FirebaseFirestore, FirebaseFunctions et
    FirebaseStorage. La résolution télécharge tout le graphe de `firebase-ios-sdk`, Analytics
    compris, mais Analytics n'est pas lié à l'app. **Il ne s'ajoute jamais sans une décision
    explicite** : il apporterait l'identifiant publicitaire, et ce qu'il implique pour la
    confidentialité et les stores.
  - **Régénération**, après tout ajout ou retrait d'un module GitLive, depuis la racine du dépôt :
    `XCODEPROJ_PATH="$PWD/iosApp/iosApp.xcodeproj" ./gradlew :shared:integrateLinkagePackage`. Le
    chemin doit être **absolu** : un chemin relatif a généré le paquet dans `shared/iosApp/`. Le
    paquet régénéré se commite dans le même commit que le changement de module. La tâche n'ajoute au
    projet Xcode que la référence au paquet, déjà déclarée dans `project.yml`.
  - La CI régénère le paquet et échoue s'il diffère de celui du dépôt.
  - Si le paquet change pendant un build Xcode (un module ajouté sans régénération), ce build échoue
    une fois (« Synthetic linkage package files changed during the build ») et le suivant passe.
  - `.swiftpm-locks/` à la racine du dépôt est le verrou des versions résolues côté Gradle
    (`swiftImport/Package.resolved`, `firebase-ios-sdk` 13.2.0 aujourd'hui) : il est commité, comme
    le `Package.resolved` du projet Xcode. Le dossier `swiftPMCheckout/` qu'il contient et le
    `.swiftpm/` du paquet sont ignorés par git.
- **Fichier Firebase** : la phase « Copy the Firebase file » copie `GoogleService-Info.plist` depuis
  `private/` dans l'app s'il existe, et le retire de l'app sinon. `startFirebase()` (Kotlin, dans
  `shared/src/iosMain`) ne démarre Firebase que si le fichier est dans l'app : sans lui (fork, CI),
  l'app démarre déconnectée et n'appelle jamais Firebase.
- **Un build non signé n'a pas accès au trousseau.** Avec `CODE_SIGNING_ALLOWED=NO`, comme en CI,
  Firebase Auth ne peut pas enregistrer l'utilisateur (erreur de trousseau -34018) et la connexion
  échoue. Pour lancer l'app, on laisse Xcode signer localement (le simulateur signe sans équipe).
- **Signature** : aucune équipe Apple dans le dépôt (`CODE_SIGN_STYLE: Automatic`). Pour un iPhone,
  l'équipe (`DEVELOPMENT_TEAM = ...`, un Apple ID gratuit suffit) se déclare dans
  `Configuration/Local.xcconfig`, ignoré par git, inclus par `Configuration/Escapy.xcconfig`.
- `MARKETING_VERSION` et `CURRENT_PROJECT_VERSION` de `project.yml` suivent `versionName` et
  `versionCode` de `androidApp` (2.0 et 20000 aujourd'hui).
- Bundle ID `fr.skyle.escapy`, nom affiché « Escapy ». Cible minimale : **iOS 18.5**.
- iPhone seulement (`TARGETED_DEVICE_FAMILY: "1"`), en portrait, comme l'app Android.
- `Info.plist` : `CADisableMinimumFrameDurationOnPhone` reste à `true` (Compose Multiplatform
  s'arrête sans elle) ; `UIUserInterfaceStyle` à `Dark`, l'app étant sombre seulement ;
  `CFBundleLocalizations` liste les langues de l'app (`en`, `fr`). Toute API qui demande une
  autorisation déclare sa clé d'usage ici, traduite dans chaque langue
  (`iosApp/<langue>.lproj/InfoPlist.strings`, un groupe de variantes du projet) : la caméra, pour le
  QR code d'une partie (`NSCameraUsageDescription`) ; plus tard le micro et les photos.
- `options.xcodeVersion` de `project.yml` fixe la version d'Xcode écrite dans le projet généré : sans
  elle, XcodeGen la remplace par celle de la machine, et chaque génération change le `.pbxproj`.
- **Caméra simulée** : le simulateur n'a pas de caméra. Un build Debug lancé avec
  `ESCAPY_SIMULATED_QR` (`SIMCTL_CHILD_ESCAPY_SIMULATED_QR=ABC234 xcrun simctl launch booted
  fr.skyle.escapy`) lit ce contenu comme un QR code (voir
  [../docs/KMP/03_CODE_PLATEFORME.md](../docs/KMP/03_CODE_PLATEFORME.md)).
- **Icône et démarrage** : `iosApp/Assets.xcassets` porte `AppIcon` (une image de 1024 px, pleine
  page, sans transparence : le trou de serrure de l'icône Android sur son fond), `LaunchBackground`
  (le fond de l'app, `ESColors.background`) et `LaunchLogo` (le trou de serrure seul), que
  `UILaunchScreen` de l'`Info.plist` affiche au démarrage.
- **Catalogue du design system** : un build Debug lancé avec la variable d'environnement
  `ESCAPY_CATALOG=1` (`SIMCTL_CHILD_ESCAPY_CATALOG=1 xcrun simctl launch booted fr.skyle.escapy`)
  affiche les composants au lieu de l'app ; un build Release ne la lit jamais.
- Pas encore de cible de tests d'interface : rien ne se lance avec `xcodebuild test`.

## Commandes

```bash
cd iosApp && xcodegen generate     # après toute modification de project.yml
XCODEPROJ_PATH="$PWD/iosApp/iosApp.xcodeproj" ./gradlew :shared:integrateLinkagePackage   # depuis la racine, après un changement de module GitLive
xcodebuild build -project iosApp/iosApp.xcodeproj -scheme iosApp \
  -destination 'platform=iOS Simulator,name=iPhone 17' CODE_SIGNING_ALLOWED=NO   # depuis la racine, comme la CI
```

La CI construit pour un simulateur nommé, pas pour un simulateur générique : celui-ci compilerait
aussi pour x86_64, que le framework ne cible pas.
