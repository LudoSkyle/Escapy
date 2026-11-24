## Architecture d'un écran

Un écran se compose, au minimum, d'une `RouteEntry`, d'une `Route` et d'un `Screen`, et si besoin
d'un `ViewModel`. Tout est dans `shared/src/commonMain`.

```
fr/skyle/escapy/
└── ui/
    └── screen/
        └── <feature>/
            ├── entry/
            │   └── <Feature>RouteEntry.kt
            └── ui/
                ├── <Feature>Route.kt
                ├── <Feature>Screen.kt
                ├── <Feature>ViewModel.kt
                ├── component/
                ├── mapper/
                └── model/
```

- `ui/component/` : les composants propres à la feature.
- `ui/model/` : les modèles d'affichage de la feature, quand un modèle métier ne suffit pas tel quel.
- `ui/mapper/` : les conversions modèle métier → modèle d'affichage.
- Un composant « produit » utilisé par plusieurs features (le minuteur, la carte d'un indice, un
  avatar) vit dans `fr/skyle/escapy/ui/core/`. Un composant générique, sans notion d'Escapy, vit
  dans `designSystem`.

Aujourd'hui, `welcome` et `home` n'ont qu'une `RouteEntry`, une `Route` et un `Screen` : leur
ViewModel arrive avec leur lot (compte au lot 5, parties au lot 6). Un écran sans logique n'a pas de
ViewModel.

### `RouteEntry`

Déclare l'entrée de navigation et branche les callbacks de navigation (voir
[01_NAVIGATION.md](./01_NAVIGATION.md)). Elle ne contient aucune logique.

### `Route`

Une `...Route` :

- récupère son ViewModel (`koinViewModel()`) et collecte son état
  (`collectAsStateWithLifecycle()`) ;
- collecte les events one-shot et déclenche la navigation, les dialogs, les actions de Compose
  (`LocalUriHandler`, `LocalClipboard`, `LocalHapticFeedback`) ;
- branche les callbacks du `Screen` sur le ViewModel.

Elle n'a pas de `Preview` : son ViewModel ne s'y construit pas.

### `Screen`

Un `...Screen` :

- reçoit les valeurs nécessaires à son affichage, et des callbacks explicites ;
- ne contient aucun autre `@Composable` que lui et sa `Preview` : ses sous-composants vivent dans
  `ui/component/` ;
- n'expose pas de paramètre `modifier` : il occupe tout l'écran ;
- se construit dans un `ESScreenStructure`, qui gère le fond et les insets.

### `ViewModel`

Un `...ViewModel` :

- vit en `commonMain` et ne reçoit **jamais** d'objet de plateforme (`Context`, `LifecycleOwner`,
  `UIViewController`) : ce qui en a besoin est derrière une interface native (voir
  [KMP/03_CODE_PLATEFORME.md](../KMP/03_CODE_PLATEFORME.md)) ;
- reçoit ses dépendances par constructeur : des interfaces de UseCase et d'actions natives, jamais
  une implémentation ;
- expose un seul `StateFlow<XxxUIState>`, couple d'un `MutableStateFlow` privé et de sa vue en
  lecture seule ;
- collecte dans `init` les flows dont son état dépend (une partie, ses messages, ses missions), pour
  mettre à jour l'état privé, plutôt que d'exposer un `stateIn` ;
- ne fait pas de `viewModelScope.launch` pour une simple mise à jour d'état : `update` est
  synchrone et thread-safe ;
- ne manipule pas de modèle de `data` (il ne le voit pas) ; il convertit un modèle métier en modèle
  d'affichage quand l'écran n'a besoin que d'une partie des champs ou d'une forme préparée ;
- **attrape les erreurs** des UseCases (`try`/`catch`, `CancellationException` relancée, voir
  [Common/02_CONVENTIONS.md](../Common/02_CONVENTIONS.md)) et les traduit en état ou en event ;
- pendant un appel au serveur, met l'écran en attente (`isLoading`, bouton désactivé) et n'affiche
  que ce que le serveur rend : il ne suppose pas le résultat d'une callable ;
- est déclaré dans `di/SharedModule.kt` avec `viewModelOf(::XxxViewModel)`.

`MainViewModel`, qui suit la session pour toute l'app, suit déjà cette forme.

Exemple (l'écran n'existe pas encore, il illustre la forme attendue) :

```kotlin
class JoinGameViewModel(
    private val joinGameUseCase: JoinGameUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(JoinGameUIState())
    val uiState: StateFlow<JoinGameUIState> = _uiState.asStateFlow()

    private val _event = Channel<JoinGameEvent>(Channel.BUFFERED)
    val event = _event.receiveAsFlow()

    fun onJoinClicked(joinCode: String) {
        viewModelScope.launch {
            _uiState.update { uiState ->
                uiState.copy(isJoining = true)
            }

            try {
                val gameId = joinGameUseCase(joinCode = joinCode)

                _event.send(JoinGameEvent.Joined(gameId = gameId))
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: GameNotFoundException) {
                _event.send(JoinGameEvent.GameNotFound)
            } catch (exception: Exception) {
                Logger.e(throwable = exception) { "Failed to join a game" }

                _event.send(JoinGameEvent.JoinFailed)
            } finally {
                _uiState.update { uiState ->
                    uiState.copy(isJoining = false)
                }
            }
        }
    }

    data class JoinGameUIState(
        val isJoining: Boolean = false,
    )

    sealed interface JoinGameEvent {
        data class Joined(val gameId: String) : JoinGameEvent
        data object GameNotFound : JoinGameEvent
        data object JoinFailed : JoinGameEvent
    }
}
```

### Events one-shot

Les évènements à usage unique (navigation, dialog d'erreur, vibration à la réception d'un indice) ne
transitent pas par l'`UIState`, mais par un `Channel` exposé en `Flow` :

- le ViewModel déclare `private val _event = Channel<XxxEvent>(Channel.BUFFERED)` et
  `val event = _event.receiveAsFlow()` ;
- il émet avec `_event.send(...)`, depuis une coroutine ;
- la `Route` collecte dans un `LaunchedEffect(Unit)` englobé par
  `lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED)`, pour ne traiter les events que quand
  l'écran est visible.

### Règles importantes

- Une `Route` n'a pas de `Preview`. Tous les autres composables d'un écran en ont au moins une.
- Un état que l'utilisateur ouvre ou saisit (dialog, bottom sheet, texte en cours) se garde avec
  `rememberSaveable` ; chaque dialog a son propre booléen de visibilité.
- Une bottom sheet qui charge des données a son propre ViewModel et ne reçoit que des identifiants,
  jamais l'objet chargé par l'écran qui l'ouvre.
- Ce qui change en temps réel (messages, missions, joueurs, minuteur) vient d'une observation
  Firestore collectée par le ViewModel, jamais d'un rafraîchissement manuel.
