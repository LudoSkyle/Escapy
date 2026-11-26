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
│   │   └── DataModule.kt          (dataModule, expect platformDataModule, FIREBASE_EMULATOR_PROPERTY)
│   ├── document/                  (documents Firestore, @Serializable, internal : ProfileDocument)
│   ├── exception/                 (exceptions de data, traduction des erreurs de Firebase)
│   ├── firebase/
│   │   └── FirebaseServices.kt    (FirebaseServices, FirebaseProvider, l'app des émulateurs)
│   ├── function/                  (requêtes et réponses des callables, internal)
│   ├── model/                     (modèles exposés : StoredSession, StoredProfile)
│   └── repository/
│       ├── account/               (AccountRepository : suppression du compte, puis déconnexion)
│       ├── auth/                  (AuthRepository : connexion par e-mail ou Google, inscription, invité, liaison, réauthentification, e-mail, mot de passe, déconnexion)
│       ├── profile/               (ProfileRepository : profil observé, ensureProfile, updateProfile)
│       └── session/               (SessionRepository : la session observée)
├── androidMain/kotlin/fr/skyle/escapy/data/
│   └── di/DataModule.android.kt   (FirebaseProvider : émulateurs, app par défaut, ou rien)
└── iosMain/kotlin/fr/skyle/escapy/data/
    └── di/DataModule.ios.kt       (idem)
```

### Accès à Firebase

- **Un Repository ne touche jamais `Firebase.auth` ou `Firebase.firestore` directement.** Il reçoit
  un `FirebaseProvider`, dont `services()` rend les services à appeler (`auth`, `firestore`,
  `functions`), ou `null` quand Firebase n'a pas démarré.
- Le `FirebaseProvider` de chaque plateforme choisit, une fois, à la première demande :
  - l'**app des émulateurs** quand la propriété Koin `escapy.firebaseEmulator` vaut `true` (un build
    debug qui l'a demandée, voir [Backend/README.md](../Backend/README.md)) ;
  - sinon l'**app par défaut**, démarrée depuis le fichier Firebase du sous-module privé ;
  - sinon **rien** : un build sans le sous-module (fork, CI) démarre déconnecté.
- Sans Firebase, une observation rend un état vide (`SessionRepositoryImpl` rend `SignedOut`), une
  action lève `FirebaseUnavailableException`.

### Erreurs

- Un appel à Firebase Auth passe par `authCall { }`, un appel à une callable par
  `functionCall { }` : ils traduisent les erreurs de Firebase en exceptions de `data`
  (`AuthFailureException` et son `AuthFailure`, `FunctionFailureException` et sa raison,
  `NetworkUnavailableException`). Les codes de Firebase Auth sont les mêmes sur Android et iOS
  (vérifié en provoquant chaque erreur sur les deux).
- Mauvais mot de passe, e-mail inconnu et identifiants invalides sont **une seule** erreur,
  `INVALID_CREDENTIALS` : avec la protection contre l'énumération des e-mails, le serveur ne les
  distingue pas, et l'app ne doit pas laisser deviner si une adresse a un compte. Pour la même raison,
  `sendPasswordReset` réussit pour une adresse inconnue.
- La traduction des codes (`authFailureOf`, `functionReasonOf`) est une fonction pure, testée seule.

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
- **La session suit `idTokenChanged`**, pas `authStateChanged` : le jeton change aussi quand un
  invité lie un e-mail, alors que l'utilisateur reste le même.

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
    private val firebaseProvider: FirebaseProvider,
) : SessionRepository {

    override fun watchSession(): Flow<StoredSession> {
        val auth = firebaseProvider.services()?.auth ?: return flowOf(StoredSession.SignedOut)
        // auth.idTokenChanged, mapped to StoredSession, then distinctUntilChanged()
    }
}
```

`di/DataModule.kt` (extrait)

```kotlin
val dataModule = module {
    includes(platformDataModule)

    singleOf(::SessionRepositoryImpl) bind SessionRepository::class
    singleOf(::AuthRepositoryImpl) bind AuthRepository::class
    singleOf(::ProfileRepositoryImpl) bind ProfileRepository::class
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
  [Backend/README.md](../Backend/README.md)). Elle se fait en deux temps : `data` traduit les erreurs
  de Firebase en ses exceptions (`exception/`, par `authCall` et `functionCall`), puis `domain` les
  traduit en exceptions métier (`withDomainExceptions`).
- Un premier appel après un temps de calme peut prendre 1 à 2 s (démarrage à froid) : l'écran montre
  l'attente. Les fonctions de partie gardent une instance chaude (`minInstances`, lot 6).

### Storage

- Un indice image ou audio s'envoie dans Storage, sous un chemin contrôlé par les règles ; la
  callable `sendHint` ne reçoit que ce chemin. L'envoi expose sa progression (`putDataResumable` rend un flux de progression)
  pour l'écran du maître du jeu.
- Les joueurs reçoivent l'indice par Firestore, puis téléchargent le fichier.

### Émulateur Firebase

`scripts/emulators.sh` lance les émulateurs sur le projet `demo-escapy` (voir
[Backend/README.md](../Backend/README.md)) ; un build debug s'y branche à la demande. En le visant depuis Android, l'hôte est `localhost`,
avec `adb reverse` ; `10.0.2.2` est refusé.
