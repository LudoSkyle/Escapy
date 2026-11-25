## Navigation

La navigation utilise Navigation 3, dans ses artefacts multiplateformes : elle est commune à Android
et iOS, dans `shared/src/commonMain`.

- Les routes sont centralisées dans `ui/main/navigation/Route.kt`.
- L'hôte de navigation est `ui/main/navigation/RootNavDisplay.kt`, affiché par `EscapyApp`.
- Ce que l'app décide une fois pour tous les écrans vit dans `ui/main/MainViewModel`, créé par
  `EscapyApp` : aujourd'hui, si une session est ouverte.

### La session décide de la racine

L'app a deux côtés : sans session (`Route.Welcome` : se connecter, s'inscrire, jouer en invité ;
puis `Route.SignIn`, `Route.SignUp`, `Route.ForgotPassword(email)`) et avec (`Route.Home` :
rejoindre ou créer une partie). La session, lue par `MainViewModel` depuis
Firebase Auth, décide de la racine du back stack :

- tant que la première session n'est pas lue, puis, connecté, tant que son profil n'est pas là (les
  deux viennent des caches locaux de Firebase ; le profil est créé par le serveur la première fois),
  `EscapyApp` montre un `ESScreenStructure` vide ;
- un profil qui ne se lit ni ne se crée (hors ligne à la première connexion) ouvre
  `ProfileUnavailableScreen`, hors navigation : réessayer, ou se déconnecter ;
- ensuite, `RootNavDisplay(isSignedIn = …)` part de `Route.Home` ou de `Route.Welcome`
  (`rootRoute(isSignedIn)`) ;
- une connexion ou une déconnexion **remplace tout le back stack** par la nouvelle racine : le
  retour ne ramène jamais à un écran de l'autre côté. Un écran de connexion ne navigue donc pas vers
  l'accueil : il connecte l'utilisateur, et la navigation suit la session ;
- une session qui ne se lit pas compte comme aucune session : l'utilisateur se reconnecte.

### Règles

- Une route implémente `NavKey` et est annotée `@Serializable` : le back stack est sauvegardé et
  restauré (recréation de l'activité, mort du process sur Android).
- Le back stack se crée avec `rememberSerializable` et un `NavBackStackSerializer` construit sur le
  sérialiseur de `Route` : c'est ce qui le rend sauvegardable sur iOS aussi, où aucune réflexion
  n'est disponible. Jamais `remember { mutableStateListOf(...) }`, qui perd la navigation.
- Une route ne transporte que des identifiants ou des valeurs simples (l'id d'une partie), jamais un
  objet chargé : l'écran ouvert recharge ce dont il a besoin, depuis Firestore.
- Chaque entrée de navigation a son propre `ViewModelStore` (`rememberViewModelStoreNavEntryDecorator`)
  et son propre état sauvegardé (`rememberSaveableStateHolderNavEntryDecorator`) : un ViewModel vit
  aussi longtemps que son entrée dans le back stack.
- Une `...Route` ne reçoit jamais le back stack : elle reçoit des callbacks de navigation
  (`navigateToGame`, `navigateBack`), branchés dans son `entry`. Une `entry` qui navigue reçoit le
  back stack en paramètre ; celles d'aujourd'hui (`welcomeRouteEntry()`, `homeRouteEntry()`) n'en ont
  pas besoin.
- Un joueur exclu d'une partie, ou une partie terminée, se traite comme la session : l'écran le lit
  dans Firestore et la navigation suit l'état, plutôt qu'un écran qui en pousse un autre.
- Aucune transition n'est déclarée pour l'instant : ce sont celles de Navigation 3 par défaut. Quand
  elles viendront, elles se déclareront une fois, sur `NavDisplay`, pas écran par écran.

`ui/main/navigation/Route.kt`

```kotlin
@Serializable
sealed interface Route : NavKey {

    /** The first screen without a session: sign in, sign up or play as a guest. */
    @Serializable
    data object Welcome : Route

    /** The first screen with a session: join or create a game. */
    @Serializable
    data object Home : Route
}
```

`ui/main/navigation/RootNavDisplay.kt` (extrait)

```kotlin
val root = rootRoute(isSignedIn = isSignedIn)
val backStack = rememberSerializable(serializer = NavBackStackSerializer(Route.serializer())) {
    NavBackStack(root)
}

LaunchedEffect(root) {
    if (backStack.firstOrNull() != root) {
        backStack.clear()
        backStack.add(root)
    }
}

NavDisplay(
    modifier = modifier,
    backStack = backStack,
    onBack = {
        backStack.removeLastOrNull()
    },
    entryDecorators = listOf(
        rememberSaveableStateHolderNavEntryDecorator(),
        rememberViewModelStoreNavEntryDecorator(),
    ),
    entryProvider = entryProvider {
        welcomeRouteEntry()

        homeRouteEntry()
    },
)
```

Chaque écran déclare son `entry` dans `ui/screen/<feature>/entry/<Feature>RouteEntry.kt` (voir
[02_ARCHITECTURE_ECRAN.md](./02_ARCHITECTURE_ECRAN.md)) :

```kotlin
fun EntryProviderScope<Route>.homeRouteEntry() {
    entry<Route.Home> {
        HomeRoute()
    }
}
```
