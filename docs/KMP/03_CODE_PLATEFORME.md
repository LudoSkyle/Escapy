## Code propre à chaque plateforme

Toute l'UI est commune (voir [ADR-0002](../adr/0002-ui-commune-en-compose-multiplatform.md)), et
Firebase aussi, par GitLive ([ADR-0003](../adr/0003-firebase-en-kotlin-commun-avec-gitlive.md)). Ce
document dit où et comment écrire ce qui ne peut pas l'être.

### Ce qui diffère aujourd'hui

| Quoi | Android | iOS |
|---|---|---|
| Démarrage de Firebase | Automatique, par le plugin Google Services, appliqué seulement avec le fichier Firebase | `startFirebase()` (`shared/iosMain/.../firebase/FirebaseStarter.kt`), appelé par l'app SwiftUI avant Koin ; ne fait rien si `GoogleService-Info.plist` n'est pas dans l'app |
| Firebase a-t-il démarré ? | `platformDataModule` : `Firebase.apps(context).isNotEmpty()` | `platformDataModule` : `Firebase.apps().isNotEmpty()` |
| Démarrage de Koin | `EscapyApplication`, avec `androidContext(...)` | `initKoin()` (`shared/iosMain/.../di/KoinInitializer.kt`), appelé `doInitKoin()` depuis Swift |
| Affichage de `EscapyApp` | `MainActivity.setContent` | `MainViewController()` (`ComposeUIViewController`), dans `ContentView.swift` |
| Logs | Logcat en debug, rien en release | Écrivain par défaut de Kermit |
| Jetons de connexion Google | `rememberGoogleSignInLauncher()` : Credential Manager, qui ne donne que le jeton d'identité, avec le client web `default_web_client_id` écrit par le plugin Google Services (gardé de la réduction des ressources par `androidApp/src/main/res/raw/keep.xml`) | `rememberGoogleSignInLauncher()` : `GIDSignIn`, avec `CLIENT_ID` du fichier Firebase, qui donne aussi le jeton d'accès ; l'app transmet ses URL par `handleGoogleSignInUrl` |
| SDK Firebase natif | Dépendances Maven de GitLive | Paquet de liaison SwiftPM (voir [iosApp/CLAUDE.md](../../iosApp/CLAUDE.md)) |

`FirebaseProvider` est une `fun interface` commune, `internal` à `data` ; chaque plateforme la fournit
dans son `platformDataModule` (`DataModule.android.kt`, `DataModule.ios.kt`) : elle seule sait
lister et créer les apps Firebase (le `Context` sur Android). Le choix des émulateurs vient de l'hôte :
`BuildConfig.USE_FIREBASE_EMULATOR` sur Android, la variable `ESCAPY_FIREBASE_EMULATOR` d'un binaire
debug sur iOS, passés à `startEscapyKoin(useFirebaseEmulator = …)`.

La connexion Google passe par un `expect` composable, pas par une interface fournie par Koin :
Credential Manager a besoin de l'activité, et GoogleSignIn du contrôleur affiché au moment du clic.
La Route obtient les jetons (`requestTokensOrNull`, un `GoogleTokens`) et les donne au ViewModel, qui
fait la connexion Firebase et se teste sans le natif. Le SDK Firebase d'iOS refuse un identifiant
Google sans le jeton d'accès (GitLive lève une exception avant tout appel) : `GoogleTokens` le porte,
nul sur Android, où Credential Manager ne le donne pas et Firebase s'en passe. Sans la configuration
Google, `isAvailable` est faux et l'écran n'affiche pas le bouton.

### Avant d'écrire du code de plateforme

Chercher d'abord l'API commune :

- Firebase passe par GitLive, en commun, dans `data`. Une API Firebase que GitLive n'expose pas
  s'obtient par sa propriété `android` ou `ios` (l'objet natif), dans `androidMain` ou `iosMain` de
  `data`, derrière la même interface de Repository.
- L'heure locale passe par `kotlin.time.Clock`, jamais par `java.util` ou `NSDate` ; l'heure d'une
  partie vient du serveur.
- Ouvrir une URL passe par `LocalUriHandler`, faire vibrer par `LocalHapticFeedback`, copier par
  `LocalClipboard` : ce sont des API de Compose, communes.

### Trois façons d'écrire du natif

| Le besoin… | Se déclare… | Utilisé par |
|---|---|---|
| passe par une API de Compose (lien, vibration, presse-papiers) | rien à déclarer : `LocalUriHandler`, `LocalHapticFeedback`, `LocalClipboard` | la `Route`, en réaction à un clic ou à un event du ViewModel |
| est une action système sans lien avec la composition (partager le lien d'une partie, jouer un son) | une **interface** en `commonMain`, une implémentation par plateforme, fournies par le module Koin de plateforme | le ViewModel, qui la reçoit par constructeur (et un fake dans ses tests) |
| a besoin de la composition (aperçu caméra pour le QR code, demande de permission, sélecteur d'image) | une fonction `@Composable` `expect`, avec un `actual` par plateforme | l'écran, via ses composants, ou la `Route` |

- Un `expect` porte sur une fonction ou une propriété, jamais sur une classe : les classes
  `expect`/`actual` sont encore en bêta. Un état natif à exposer se décrit par une interface
  commune, rendue par une fonction `expect`.
- Le code de plateforme des écrans vivra dans `shared`, sous `platform/<capacité>/`
  (`platform/camera/`, `platform/share/`…), dans le source set de sa plateforme. Le fichier d'un
  `actual` est suffixé par sa plateforme : `DataModule.android.kt`, `DataModule.ios.kt`.
- Le module Koin de plateforme est un `internal expect val platform<Module>Module: Module`, inclus
  par le module commun ; `platformDataModule` est le seul aujourd'hui.

### Règles par plateforme

Android :

- Une action système part du `Context` de l'application, avec `FLAG_ACTIVITY_NEW_TASK` : une
  implémentation ne garde jamais de référence à une `Activity`.
- Ce qui attend un résultat d'activité (permission, sélecteur d'image) passe par
  `rememberLauncherForActivityResult`, donc par un `expect` composable.

iOS :

- Le contrôleur depuis lequel présenter une feuille (partage) se cherche **au moment de l'action**,
  jamais pendant la composition.
- Les vues natives (aperçu caméra) s'intègrent par `UIKitView`.
- Les appels UIKit se font sur le thread principal.
- Un `@OptIn(ExperimentalForeignApi::class)` se place au plus près de l'appel qui l'exige, pas sur
  un fichier entier.
- Toute API qui demande une autorisation déclare sa clé d'usage dans l'`Info.plist` de `iosApp`,
  traduite en anglais et en français.
- Le code natif iOS s'écrit de préférence en Kotlin, dans `iosMain`. Du Swift n'est écrit que pour
  une API inaccessible depuis Kotlin, fourni à `shared` au démarrage derrière une interface commune.

### Matrice des capacités

Mécanisme visé pour chaque capacité native. Une ligne est confirmée (✅) quand son implémentation est
livrée et vérifiée ; le tableau est mis à jour dans le même commit.

| Capacité | Android | iOS | Forme | Lot |
|---|---|---|---|---|
| Démarrage de Firebase, seulement avec son fichier | ✅ plugin Google Services | ✅ `startFirebase()` | build + `platformDataModule` | 2 |
| Scanner le QR code d'une partie | CameraX + ML Kit Barcode Scanning | AVFoundation (`AVCaptureMetadataOutput`) | `expect` composable | 6 |
| Permission caméra | `RequestPermission` | `AVCaptureDevice.requestAccess` | `expect` composable | 6 |
| Ouvrir l'app par le lien d'une partie | App Links | Universal Links | hôtes + route | 6 |
| Partager le lien d'une partie | `ACTION_SEND` | `UIActivityViewController` | interface | 6 |
| Vibration à la réception d'un indice | `LocalHapticFeedback` | `LocalHapticFeedback` | Compose | 9 |
| Choisir une image pour un indice | Photo Picker | `PHPickerViewController` | `expect` composable | 9 |
| Jetons de connexion Google | Credential Manager (`GetSignInWithGoogleOption`) | GoogleSignIn, importé dans `iosMain` par SwiftPM | `expect` composable | 5 |
| Enregistrer et jouer un indice audio | `MediaRecorder`, lecteur | `AVAudioRecorder`, `AVPlayer` | interface | 12 |
| Notifications push | Firebase Messaging | Firebase Messaging, APNs | à décider | 12 |
