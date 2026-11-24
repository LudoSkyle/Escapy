## Module `data`

Le module `data` est le seul à parler à Firebase : Auth, Firestore, Cloud Functions et Storage, par
le SDK Kotlin de GitLive, en code commun
([ADR-0003](../adr/0003-firebase-en-kotlin-commun-avec-gitlive.md)). Il n'a pas de base locale : le
cache hors ligne de Firestore garde ce qui a été lu.

Il ne connaît pas `domain` (voir [ADR-0001](../adr/0001-le-domaine-depend-de-la-donnee.md)) : il
rend des modèles simples, au plus près de ce que Firebase donne, et c'est `domain` qui les
interprète.

```
data/src/
├── commonMain/kotlin/fr/skyle/escapy/data/
│   ├── di/
│   │   └── DataModule.kt          (dataModule, expect platformDataModule)
│   ├── firebase/
│   │   └── FirebaseStatus.kt      (Firebase a-t-il démarré ?)
│   ├── model/
│   │   └── StoredSession.kt       (modèles exposés par les Repositories)
│   └── repository/
│       └── session/
│           ├── api/
│           │   └── SessionRepository.kt
│           └── SessionRepositoryImpl.kt
├── androidMain/kotlin/fr/skyle/escapy/data/
│   └── di/DataModule.android.kt   (FirebaseStatus d'après Firebase.apps(context))
└── iosMain/kotlin/fr/skyle/escapy/data/
    └── di/DataModule.ios.kt       (FirebaseStatus d'après Firebase.apps())
```

À venir, avec les lots qui en ont besoin : `document/` (les documents Firestore, `@Serializable`,
`internal`), `function/` (requêtes et réponses des callables), `mapper/` (document → modèle exposé),
`exception/`.

### Repositories

- **Chaque Repository a son interface (`api/`) et son implémentation**, dans le même package de
  feature.
- L'interface est publique (`domain` l'utilise), l'implémentation est `internal`.
- **Un modèle exposé et le document Firestore sont deux types distincts**, reliés par un mapper : les
  annotations de sérialisation et les noms de champs Firestore restent dans le module.
- Une observation renvoie un `Flow` (`watchSession`, `watchGame`), une action une fonction `suspend`.
  Pas de `runBlocking`, jamais.
- Une action qui change l'état d'une partie **appelle une callable** ; elle n'écrit jamais elle-même
  dans Firestore. Seul l'envoi d'un message de chat écrit directement, ce que les règles permettent
  (voir [ADR-0004](../adr/0004-firestore-et-le-serveur-source-de-verite.md)).
- **Sans Firebase, un Repository ne l'appelle pas.** `FirebaseStatus.isConfigured()` dit si Firebase
  a démarré : un build sans le sous-module privé (fork, CI) n'a pas Firebase, et l'app démarre
  déconnectée. `SessionRepositoryImpl` rend alors `SignedOut`.

`repository/session/api/SessionRepository.kt`

```kotlin
interface SessionRepository {

    /** The current session, then each change: sign-in, sign-out, account deleted elsewhere. */
    fun watchSession(): Flow<StoredSession>
}
```

`repository/session/SessionRepositoryImpl.kt` (extrait)

```kotlin
internal class SessionRepositoryImpl(
    private val firebaseStatus: FirebaseStatus,
) : SessionRepository {

    override fun watchSession(): Flow<StoredSession> {
        if (!firebaseStatus.isConfigured()) {
            return flowOf(StoredSession.SignedOut)
        }

        // Firebase.auth.authStateChanged, mapped to StoredSession, then distinctUntilChanged()
    }
}
```

`di/DataModule.kt`

```kotlin
val dataModule = module {
    includes(platformDataModule)

    single<SessionRepository> { SessionRepositoryImpl(firebaseStatus = get()) }
}

// What each platform answers differently, such as whether Firebase started
internal expect val platformDataModule: Module
```

### Firestore

- **Écouter avant d'écrire.** `add()` et `set()` suspendent jusqu'à l'accusé du serveur : un code
  qui écrit puis ouvre son écoute ne voit jamais l'état local, « en attente ». Pour montrer un
  message dès son envoi, l'écoute est ouverte avant l'écriture ; c'est le cas naturel d'un écran de
  chat, qui écoute ses messages tant qu'il est affiché.
- Une écoute qui doit distinguer l'affichage local de l'accusé du serveur demande
  `includeMetadataChanges` et lit `hasPendingWrites`.
- **Horodatage serveur.** Un champ d'heure s'écrit avec `Timestamp.ServerTimestamp`, jamais avec
  l'heure de l'appareil (voir [Common/02_CONVENTIONS.md](../Common/02_CONVENTIONS.md)). Tant que
  l'écriture attend l'accusé, le champ se lit selon le `ServerTimestampBehavior` demandé : `NONE` et
  `PREVIOUS` rendent `null`, `ESTIMATE` une heure estimée localement. Les messages du chat se lisent
  avec `ESTIMATE`, pour s'afficher et se trier sans sauter ; après l'accusé, ils portent l'heure du
  serveur.
- Le tri d'une liste se fait par la requête (`orderBy` sur l'horodatage serveur), pas sur l'appareil.
- Les documents se décodent avec kotlinx-serialization, dont R8 n'a pas besoin de règle (voir
  [Common/04_BUILD.md](../Common/04_BUILD.md)). Un nom de champ ou de type écrit dans Firestore
  vient d'un `@SerialName` ou d'une constante, jamais d'un nom de classe, obfusqué en release.
- Le cache hors ligne de Firestore tient lieu de base locale : un écran rouvert s'affiche depuis le
  cache, puis se met à jour.

### Cloud Functions

- Une callable s'appelle par `httpsCallable`, avec une requête et une réponse `@Serializable`
  (`function/`), dans la région des fonctions (`europe-west1`).
- **Un délai s'impose avec `withTimeoutOrNull`**, jamais `withTimeout` dans un `try`/`catch` : le
  délai dépassé disparaîtrait sans message (voir
  [Common/02_CONVENTIONS.md](../Common/02_CONVENTIONS.md)).
- Une erreur rendue par une fonction devient une exception typée : la fonction refuse avec une raison
  stable dans `details.reason` de la `FirebaseFunctionsException`, que `data` traduit en
  `<Problème>Exception` ; une raison inconnue devient une erreur générique
  ([ADR-0005](../adr/0005-le-contrat-des-callables.md), raisons dans
  [Backend/README.md](../Backend/README.md)). Le code de cette traduction s'écrit avec la première
  callable de l'app (lot 5).
- Un premier appel après un temps de calme peut prendre 1 à 2 s (démarrage à froid) : l'écran montre
  l'attente. Les fonctions de partie gardent une instance chaude (`minInstances`, lot 6).

### Storage

- Un indice image ou audio s'envoie dans Storage, sous un chemin contrôlé par les règles ; la
  callable `sendHint` ne reçoit que ce chemin. L'envoi expose sa progression (`putDataResumable` rend un flux de progression)
  pour l'écran du maître du jeu.
- Les joueurs reçoivent l'indice par Firestore, puis téléchargent le fichier.

### Émulateur Firebase

`scripts/emulators.sh` lance les émulateurs sur le projet `demo-escapy` (voir
[Backend/README.md](../Backend/README.md)) ; l'app saura s'y brancher en debug au lot 5. En le visant depuis Android, l'hôte est `localhost`,
avec `adb reverse` ; `10.0.2.2` est refusé.
