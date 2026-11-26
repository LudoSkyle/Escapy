## Module `domain`

Le module `domain` porte les modèles métier, les conversions depuis `data` et les UseCases. Il est
en Kotlin pur : aucune API de plateforme, aucune UI, aucun Firebase.

Il cache la donnée au reste de l'app : `shared` ne voit que lui (voir
[ADR-0001](../adr/0001-le-domaine-depend-de-la-donnee.md)).

**Le domaine de l'app n'est pas l'arbitre du jeu.** Les règles qui décident d'un résultat (une
réponse juste, une mission débloquée, la victoire, le temps restant qui fait foi) vivent sur le
serveur ([ADR-0004](../adr/0004-firestore-et-le-serveur-source-de-verite.md)). Le domaine de l'app
présente l'état que le serveur a écrit, et prépare les actions qu'il envoie. Il peut vérifier une
saisie pour l'ergonomie (un code de la bonne longueur, un champ vide), jamais décider qu'elle est
juste.

```
domain/src/commonMain/kotlin/fr/skyle/escapy/domain/
├── di/
│   └── DomainModule.kt
├── exception/                         (exceptions métier : AccountExceptions.kt, GameExceptions.kt)
├── mapper/                            (StoredSession -> Session, StoredGame -> Game, exceptions de data -> métier)
├── model/                             (Session, Profile, Game, GameMember, GameState…)
├── validation/                        (PasswordPolicy, GameRules : les bornes que le serveur impose)
└── usecase/
    ├── account/  auth/  profile/
    ├── game/                          (WatchMyGames, WatchGame, CreateGame, JoinGame, LeaveGame, KickPlayer, CancelGame)
    └── session/
        ├── WatchSessionUseCase.kt
        └── WatchSessionUseCaseImpl.kt
```

- **`GameRules`** reprend les bornes du serveur (durée, maximum de joueurs, longueur du code) : un
  écran ne propose que ce que le serveur accepte, il ne décide de rien.
- **`WatchGameUseCase`** rend un `GameState` : la partie et ses joueurs, puis `Deleted` (annulée) ou
  `Excluded` (le joueur n'y a plus accès), qui termine le flux. Un membre d'une partie y est marqué
  `isCurrentUser`, et `Game.isMaster` dit si l'utilisateur la mène.

### Modèles

- Un modèle métier est immuable (`val`, collections en lecture seule) : `compose-stability.conf` le
  déclare stable pour les composables (voir
  [UI/04_CONVENTIONS_COMPOSE.md](../UI/04_CONVENTIONS_COMPOSE.md)).
- Il parle le vocabulaire du produit, pas celui de Firebase : `Session.SignedIn.isGuest`, là où
  `data` rend `isAnonymous`.

`model/Session.kt`

```kotlin
/** Who uses the app: nobody yet, a guest, or a member with an account. */
sealed interface Session {

    data object SignedOut : Session

    data class SignedIn(
        val userId: String,
        // A guest plays without an email, and may link an account later
        val isGuest: Boolean,
    ) : Session
}
```

### Mappers

- Un modèle de `data` devient un modèle métier par une fonction d'extension `internal`, dans
  `mapper/` (`StoredSession.toSession()`). Elle est pure, donc testée seule.

### UseCases

- Un UseCase est **une interface et son implémentation, chacune dans son fichier** :
  `WatchSessionUseCase.kt` (publique) et `WatchSessionUseCaseImpl.kt` (`internal`).
- Il expose une seule méthode, `operator fun invoke(...)`, pour s'appeler comme une fonction :
  `watchSessionUseCase()`. Elle est `suspend`, sauf quand elle rend un `Flow` : un `Flow` est déjà
  froid, il n'a rien à suspendre.
- Il reçoit et rend des modèles métier, jamais des modèles de `data` : il fait la conversion.
- Comme `shared` ne voit pas `data`, toute lecture passe par un UseCase, même quand il ne fait que
  lire et convertir. C'est le prix de la frontière, accepté par l'ADR-0001.
- Il ne contient pas de `try`/`catch` global : c'est l'appelant (le ViewModel) qui gère l'erreur. Un
  `try`/`catch` local est permis pour traiter un cas précis ou relancer une exception plus parlante.
- Une erreur métier est une exception dédiée, dans `exception/`, nommée `<Problème>Exception`
  (`GameNotFoundException`, `GameFullException`, `KickedFromGameException`).
- Il est déclaré dans `di/DomainModule.kt` avec `factoryOf(::XxxUseCaseImpl) bind XxxUseCase::class`.
- Une heure locale, s'il en faut une, vient d'un `Clock` reçu par constructeur ; une heure de partie
  vient toujours du serveur (voir [Common/02_CONVENTIONS.md](../Common/02_CONVENTIONS.md)).

`usecase/session/WatchSessionUseCase.kt`

```kotlin
interface WatchSessionUseCase {
    operator fun invoke(): Flow<Session>
}
```

`usecase/session/WatchSessionUseCaseImpl.kt`

```kotlin
internal class WatchSessionUseCaseImpl(
    private val sessionRepository: SessionRepository,
) : WatchSessionUseCase {

    override fun invoke(): Flow<Session> =
        sessionRepository.watchSession().map { storedSession -> storedSession.toSession() }
}
```

`di/DomainModule.kt`

```kotlin
val domainModule = module {
    includes(dataModule)

    factoryOf(::WatchSessionUseCaseImpl) bind WatchSessionUseCase::class
}
```
