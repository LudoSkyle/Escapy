## Modules et bibliothèques

### Modules

| Module | Type | Contenu |
|---|---|---|
| `androidApp` | App Android | `EscapyApplication` (logs, démarrage de Koin), `MainActivity` qui affiche `EscapyApp()`, manifest, icône, écran de démarrage, signature |
| `iosApp` | Projet Xcode | L'app SwiftUI qui démarre Firebase et Koin, et affiche le `ComposeUIViewController` de `shared` ; `Info.plist` ; le paquet de liaison SwiftPM qui apporte Firebase. Voir [iosApp/CLAUDE.md](../../iosApp/CLAUDE.md) |
| `shared` | KMP + Compose Multiplatform | `EscapyApp`, navigation (`ui/main/navigation`), `MainViewModel`, écrans (`ui/screen/<feature>`), module Koin racine, démarrage de Firebase et de Koin sur iOS (`iosMain`). Produit le framework iOS `Shared` |
| `designSystem` | KMP + Compose Multiplatform | `ESTheme`, composants génériques (`ESText`, `ESScreenStructure`), police Exo 2 |
| `domain` | KMP | Modèles métier (`Session`), mappers, UseCases |
| `data` | KMP | Firebase par GitLive, Repositories (interfaces et implémentations), modèles exposés (`StoredSession`) |

Le nom de package suit le module : `fr.skyle.escapy` pour `shared` et les hôtes,
`fr.skyle.escapy.designsystem`, `fr.skyle.escapy.domain`, `fr.skyle.escapy.data`.

Le dossier `firebase/` (règles, Cloud Functions en TypeScript) n'est pas un module Gradle : il a
son propre outillage (npm) et ses règles (voir [firebase/CLAUDE.md](../../firebase/CLAUDE.md)).

### Graphe de dépendances

```
androidApp ──► shared ◄── iosApp (framework Shared)
                 │
         ┌───────┴────────┐
         ▼                ▼
   designSystem         domain
                          │
                          ▼
                        data ──► Firebase (GitLive)
```

- Toutes les dépendances entre modules sont en `implementation`. `shared` ne voit donc pas `data` :
  il passe par les UseCases de `domain` (voir
  [ADR-0001](../adr/0001-le-domaine-depend-de-la-donnee.md)).
- **Seul `data` appelle Firebase.** Une seule exception : `shared/iosMain` dépend de
  `firebase-app`, le temps de démarrer Firebase au lancement de l'app iOS (`startFirebase()`). Voir
  [ADR-0003](../adr/0003-firebase-en-kotlin-commun-avec-gitlive.md).
- `designSystem` ne dépend que de Compose et de Material 3 : le compilateur garantit qu'un composant
  générique ne connaît ni un écran, ni un modèle métier.
- `domain` et `data` n'ont aucune dépendance UI.
- Les hôtes ne dépendent que de `shared`. Ils ne contiennent aucun écran.

### Visibilité entre modules

- `internal` par défaut. Est public uniquement ce qu'un autre module utilise.
- `data` garde `internal` ses implémentations de Repository, ses documents Firestore et ses mappers,
  et ce qui ne sert qu'à lui (`FirebaseProvider`). Sont publics : les interfaces de Repository, les
  modèles qu'elles exposent, le module Koin.
- `domain` garde `internal` ses implémentations de UseCase et ses mappers. Sont publics : les
  interfaces de UseCase, les modèles métier, les exceptions métier, le module Koin.
- `designSystem` expose ses composants et `ESTheme`.

### Injection de dépendances

Koin, en DSL (`module { }`, `singleOf`, `factoryOf`, `viewModelOf`).

- Chaque module déclare son module Koin dans un fichier unique `di/<Module>Module.kt`
  (`dataModule`, `domainModule`, `sharedModule`). Les classes reçoivent leurs dépendances par
  constructeur, sans annotation : **dans `data` et `domain`, seul le package `di/` importe Koin**.
- Chaque module Koin inclut celui du module dont il dépend (`includes(...)`) : les hôtes ne
  démarrent que `sharedModule`.
- Ce qui diffère par plateforme est déclaré dans un module Koin de plateforme, `expect`/`actual`,
  inclus par le module commun : aujourd'hui `platformDataModule`, qui dit si Firebase a démarré
  (voir [KMP/03_CODE_PLATEFORME.md](../KMP/03_CODE_PLATEFORME.md)).
- Android démarre Koin dans `EscapyApplication` avec `androidContext(...)` ; iOS appelle `initKoin()`
  (dans `shared/iosMain`) au démarrage de l'app SwiftUI, après `startFirebase()`.

### Bibliothèques

Les versions sont dans `gradle/libs.versions.toml`, jamais dans la documentation.

Communes (`commonMain`), en place :

| Besoin | Bibliothèque |
|---|---|
| UI | Compose Multiplatform (`ui`, `foundation`, `components-resources`, `ui-tooling-preview`) |
| Composants Material | Material 3, en alpha alignée sur Compose Multiplatform, **dans `designSystem` seulement**, sans API Expressive |
| Textes, polices | Compose Resources |
| Navigation | Navigation 3 (artefacts multiplateformes), navigationevent |
| ViewModel, cycle de vie | Lifecycle multiplateforme (`lifecycle-viewmodel`, `-runtime-compose`, `-viewmodel-navigation3`) |
| Injection de dépendances | Koin (`koin-core`, `koin-compose-viewmodel`, `koin-android` dans les hôtes et `data/androidMain`) |
| Firebase | firebase-kotlin-sdk de GitLive : `firebase-app`, `-auth`, `-firestore`, `-functions`, `-storage`, **dans `data` seulement** ([ADR-0003](../adr/0003-firebase-en-kotlin-commun-avec-gitlive.md)) |
| Sérialisation | kotlinx-serialization (routes de navigation ; requêtes et réponses des callables, documents Firestore à venir) |
| Coroutines | kotlinx-coroutines |
| Logs | Kermit (`Logger`). Sur Android, `EscapyApplication` n'écrit dans logcat qu'en debug, et rien en release tant que la remontée des crashs n'est pas branchée ; sur iOS, l'écrivain par défaut de Kermit |

Android (`androidApp`) : `activity-compose`, `core-splashscreen`, le plugin Google Services (appliqué
seulement avec le fichier Firebase).

Prévues, ajoutées avec le lot qui en a besoin (et retirées du catalogue si elles n'y servent plus) :

| Besoin | Bibliothèque envisagée | Lot |
|---|---|---|
| Préférences locales | DataStore Preferences (multiplateforme) | quand un réglage local apparaît |
| Dates, durées | kotlinx-datetime, `kotlin.time` | 6, 7 |
| Images (avatars, indices) | Coil 3 | 5, 9 |
| Appel d'une API HTTP (le dépôt GitHub de l'À propos) | Ktor | 5 |
| Affichage d'un QR code | qrose | 6 |
| Scan d'un QR code | CameraX + ML Kit sur Android, AVFoundation sur iOS | 6 |
| Remontée des crashs | Crashlytics, branché sur Kermit | 11 |

### Ce qu'on n'utilise pas

- **Room**, ou toute base locale : le cache hors ligne de Firestore garde les données lues. Pas de
  DAO, pas d'entités, pas de synchronisation à écrire.
- **Les SDK Firebase natifs dans le code commun** : Firebase passe par GitLive, dans `data`. Un
  repli vers les SDK natifs (plan B de l'[ADR-0003](../adr/0003-firebase-en-kotlin-commun-avec-gitlive.md))
  ne toucherait que ce module.
- **Firebase déclaré dans le projet Xcode** : il arrive par le paquet de liaison SwiftPM, jamais par
  un paquet ajouté à la main (symboles en double).
- **Hilt**, **Retrofit**, **Timber** : n'existent que sur Android.
- **Un type `Either` ou `Response` sealed** pour les erreurs : exceptions typées
  ([ADR-0001](../adr/0001-le-domaine-depend-de-la-donnee.md)).
- **`material-icons-extended`** : n'est plus maintenu pour Compose Multiplatform. Les icônes seront
  des Material Symbols en vectoriel, dans Compose Resources.
