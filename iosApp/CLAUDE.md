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
  Firebase Auth ne garde pas la session d'un lancement à l'autre. Pour lancer l'app, on laisse Xcode
  signer localement (le simulateur signe sans équipe).
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
  autorisation (caméra pour le QR code, micro pour un indice audio, photos) déclarera sa clé d'usage
  ici, traduite dans chaque langue (`<langue>.lproj/InfoPlist.strings`).
- **Pas encore d'icône d'app iOS**, et l'écran de démarrage (`UILaunchScreen`) n'est que le fond
  sombre : l'icône arrive avec le design system (lot 4).
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
