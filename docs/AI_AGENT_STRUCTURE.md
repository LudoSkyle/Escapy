# Guide de structure pour un agent IA

Ce document sert de reference pour conserver la structure actuelle du projet Escapy quand un agent IA ajoute, modifie ou cree des fonctionnalites. Il decrit les conventions observes dans le code existant.

## Rappel important
- Les commentaires doivent etre en anglais si jamais il doit y en avoir.

## Modules principaux
- `app`: UI Compose, navigation, viewmodels, glue applicatif, DI cote app.
- `data`: couche data (repositories, data sources, use cases, models, mappers, db, rest).
- `design_system`: composants UI partages et styles.

## Arborescence UI (module app)
Structure type par ecran:
- `ui/screens/<feature>/ui`: composables et viewmodel de l ecran.
- `ui/screens/<feature>/ui/component`: composables de sous composants.
- `ui/screens/<feature>/ui/model`: modeles UI.
- `ui/screens/<feature>/ui/mapper`: conversion data -> UI.
- `ui/screens/<feature>/impl`: integration navigation (NavGraphBuilder, transitions).

Exemples:
- `app/src/main/java/fr/skyle/escapy/ui/screens/home/ui/HomeRoute.kt`
- `app/src/main/java/fr/skyle/escapy/ui/screens/home/impl/HomeRouteImpl.kt`
- `app/src/main/java/fr/skyle/escapy/ui/screens/home/ui/HomeViewModel.kt`

## Pattern d ecran
- Un `Route` Compose (ex: `HomeRoute`) qui gere les effets et la navigation.
- Un `Screen` Compose (ex: `HomeScreen`) qui affiche l UI et recoit l etat + callbacks.
- Un `ViewModel` par ecran avec `StateFlow` pour l etat et un event type sealed.
- Injection via Hilt (`@HiltViewModel`, `@Inject constructor`).

Exemple de ViewModel:
- `app/src/main/java/fr/skyle/escapy/ui/screens/home/ui/HomeViewModel.kt`

## Navigation
- Les routes sont definies dans `ui/main/navigation/Route.kt`.
- Chaque ecran declare sa fonction `...Route` dans `ui` et son `...RouteImpl` dans `impl`.

Exemple:
- `app/src/main/java/fr/skyle/escapy/ui/screens/home/impl/HomeRouteImpl.kt`

## Use cases (module data)
- Chaque use case a une interface et une implementation suffixee `Impl`.
- Les use cases sont regroupes par domaine: `data/usecase/<domain>`.
- L invocation se fait via `operator fun invoke()`.
- Les bindings Hilt sont centralises dans `data/di/UseCaseModule.kt`.

Exemple:
- `data/src/main/java/fr/skyle/escapy/data/usecase/lobby/FetchLobbiesForCurrentUserUseCase.kt`
- `data/src/main/java/fr/skyle/escapy/data/di/UseCaseModule.kt`

## Repositories et data sources (module data)
- Interfaces dans `data/repository/<feature>/api`.
- Implementations dans `data/repository/<feature>`.
- Data sources locales et distantes (local/remote) avec interfaces dans `api`.
- Modules Hilt par feature dans `data/repository/<feature>/di`.

Exemple:
- `data/src/main/java/fr/skyle/escapy/data/repository/lobby/api/LobbyRepository.kt`
- `data/src/main/java/fr/skyle/escapy/data/repository/lobby/LobbyRepositoryImpl.kt`

## Models, DTOs, mappers
- Modeles data dans `data/vo`.
- DTOs REST dans `data/rest/...`.
- Mappers dedies dans `data/vo/mapper`.
- Extensions utilitaires dans `data/ext` et `app/ext`.

Exemple:
- `data/src/main/java/fr/skyle/escapy/data/vo/Lobby.kt`
- `data/src/main/java/fr/skyle/escapy/data/rest/firebase/LobbyRequestDTO.kt`
- `data/src/main/java/fr/skyle/escapy/data/vo/mapper/LobbyMapper.kt`

## Base de donnees
- Room: `data/db` pour database, dao, converters.

Exemple:
- `data/src/main/java/fr/skyle/escapy/data/db/ProjectDatabase.kt`

## Conventions de nommage
- Suffixes explicites: `ViewModel`, `Screen`, `Route`, `RouteImpl`, `UseCase`, `UseCaseImpl`, `Repository`, `RepositoryImpl`.
- Paquets tries par feature ou domaine (UI par ecran, data par feature).

## Bonnes pratiques a respecter
- Garder la separation UI (app) / data (data) / design system.
- Eviter la logique metier dans les composables: passer par use cases.
- Garder les flux dans les ViewModels (`StateFlow`, `Flow`).
- Ajouter un mapper quand il y a un changement de representation entre data et UI.
- Si commentaires necessaires, ils doivent etre en anglais.
