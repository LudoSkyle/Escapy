## Conventions de code

Conventions transverses à tous les modules. Pour l'UI, voir en complément
[UI/04_CONVENTIONS_COMPOSE.md](../UI/04_CONVENTIONS_COMPOSE.md).

### Langue et commentaires

- Le code, les noms et les commentaires sont **en anglais**. La documentation du projet (`docs/`,
  `CLAUDE.md`) est en français.
- **On commente peu.** Un commentaire explique **pourquoi**, jamais **quoi**.
    - Exemple à éviter : `// Watch the session` au-dessus de `sessionRepository.watchSession()`.
    - Exemple attendu : `// Started by the google-services plugin, which the build applies only
      with the Firebase file`.
- Sont du bruit, donc à ne pas écrire :
    - le KDoc qui ne fait que reformuler une signature ;
    - les étiquettes de section (`// Insert`, `// Read`, `// View`) ;
    - les commentaires accolés aux dépendances d'un `build.gradle.kts`, sauf pour justifier un choix
      qui ne se devine pas ;
    - tout commentaire qui paraphrase la ligne suivante.
- Un champ d'un modèle ou d'un document Firestore se commente quand son sens, son unité ou son
  format ne se lit pas dans son nom (`durationSec` en secondes, un horodatage serveur).

### Nommage

- `UI` s'écrit toujours en majuscules, jamais `Ui` : `HomeUIState`, `UIModel`.
- Les acronymes de 2 lettres restent en majuscules (`UI`, `ID`, `QR`), ceux de 3 lettres ou plus
  s'écrivent comme un mot (`Url`, `Json`, `Http`).
    - Exemple à éviter : `JSONMapper`, `parseURL`.
    - Exemple attendu : `JsonMapper`, `parseUrl`, `QRCodeScanner`.
- Un `val` ne se nomme jamais `getXxx` : c'est déjà un accesseur.
- Le nom dit **ce que fait** la fonction : une observation commence par `watch` (`watchSession`),
  une méthode qui exclut un joueur s'appelle `kickPlayer`, pas `updatePlayer`.
- Pas de redondance : dans un `GameRepository`, la méthode s'appelle `watchGame`, pas
  `watchGameDocument`.
- Pas de suffixe `Helper` / `Utils` / `Manager` sur une classe qui a un rôle précis : le nom dit le
  rôle.
- Une exception se nomme `<Problème>Exception` (`GameNotFoundException`, `GameFullException`).
- Les termes métier sont ceux du vocabulaire du produit (voir
  [Produit/01_VISION_ET_VOCABULAIRE.md](../Produit/01_VISION_ET_VOCABULAIRE.md)) : `game`, jamais
  `lobby` ni `room`.

### Style

- Ne jamais laisser de ligne vide en fin de fichier.
- Jamais deux lignes vides consécutives. Aérer son code pour gagner en lisibilité.
- Si un appel comporte 2 paramètres ou plus, **tous** les paramètres sont nommés (sauf là où Kotlin
  l'interdit : méthode Java, lambda reçue en paramètre, `vararg`), et chacun va sur sa propre ligne,
  même quand l'appel tiendrait sur une seule.
    - Exemple à éviter : `Session.SignedIn(userId = userId, isGuest = isAnonymous)` sur une ligne.
    - Exemple attendu :
      ```kotlin
      Session.SignedIn(
          userId = userId,
          isGuest = isAnonymous,
      )
      ```
- `combine` (comme `zip`) reçoit ses flows sans les nommer : c'est sa lambda, aux paramètres nommés,
  qui dit ce qu'est chaque valeur. La lambda construit l'objet avec des arguments nommés.
- Pour une fonction déclarée avec `=` (expression body), rien sur la ligne du `=` : le corps
  commence à la ligne suivante. Ne s'applique qu'aux fonctions, pas aux propriétés.
    - Exemple attendu :
      ```kotlin
      internal fun rootRoute(isSignedIn: Boolean): Route =
          if (isSignedIn) Route.Home else Route.Welcome
      ```
- Une fonction qui retourne `Unit` utilise un corps en bloc, pas une expression body.
- Virgule finale (trailing comma) sur les listes de paramètres et d'arguments multi-lignes.
- Les `enum class` ne portent pas de `;` final inutile ni de `companion object` vide.

### Gestion d'erreur

- Un `catch (exception: Exception)` relance toujours `CancellationException` :
  ```kotlin
  try {
      joinGameUseCase(joinCode = joinCode)
  } catch (exception: CancellationException) {
      throw exception
  } catch (exception: GameNotFoundException) {
      _event.send(JoinGameEvent.GameNotFound)
  } catch (exception: Exception) {
      Logger.e(throwable = exception) { "Failed to join the game" }
      _event.send(JoinGameEvent.JoinFailed)
  }
  ```
- Le `try`/`catch` s'écrit sur place, là où l'erreur se traite (le ViewModel), et n'est pas caché
  derrière une fonction utilitaire.
- `throw Exception("...")` est interdit : toujours un type dédié, `<Problème>Exception`.
- **Pas de `withTimeout` dans un `try`/`catch`.** Il lève une `TimeoutCancellationException`, qui
  est une `CancellationException` : le motif ci-dessus la relance, et le délai dépassé disparaît
  sans message ni erreur à l'écran. On écrit `withTimeoutOrNull`, et un `null` devient une exception
  typée :
  ```kotlin
  val result = withTimeoutOrNull(timeout = CALL_TIMEOUT) {
      joinGameFunction.invoke(request)
  } ?: throw GameCallTimeoutException()
  ```
- Une observation (`watch…`) qui échoue au point de ne plus rien pouvoir rendre est traitée là où
  elle est collectée, avec un état d'écran qui le dit ; elle n'est jamais avalée en silence.
- Une erreur de programmation (argument invalide, état impossible) se signale par `require` /
  `check`, pas par un silence.
- Un log d'erreur passe par Kermit (`Logger.e`), jamais par `println`.
- **Ne jamais s'appuyer sur `::class.simpleName`** (ni sur `qualifiedName`) pour une valeur qui
  compte : un nom de log, une clé, un type écrit en base. R8 obfusque les noms de classes en release
  (`Timestamp` y devient `ac2`). Une valeur stable s'écrit en dur, ou vient d'un `@SerialName`.

### Heure et horloges

**On ne trie et on ne chronomètre jamais sur l'horloge de l'appareil.** Les horloges diffèrent d'un
téléphone à l'autre : un émulateur Android a été mesuré avec 19 s de retard sur le serveur. Deux
joueurs verraient deux temps différents, et des messages dans le désordre.

- Toute heure qui compte pour une partie (envoi d'un message, déblocage, début, pause, fin) est un
  **horodatage serveur** (`serverTimestamp`), écrit par Firestore ou par une fonction.
- Un message en attente de l'accusé du serveur n'a pas encore son horodatage : on le lit avec
  `ServerTimestampBehavior.ESTIMATE`, pour qu'il s'affiche et se trie sans sauter (voir
  [KMP/01_MODULE_DATA.md](../KMP/01_MODULE_DATA.md)).
- Le minuteur se calcule à partir des horodatages serveur de la partie, corrigés de l'**écart estimé
  entre l'horloge de l'appareil et celle du serveur** : l'heure du serveur est l'heure de l'appareil
  plus cet écart. `EstimateServerClockOffsetUseCase` demande l'heure du serveur (`getServerTime`)
  trois fois ; le serveur a lu son horloge pendant l'aller-retour, à mi-chemin en moyenne, et
  l'aller-retour le plus court donne l'estimation la plus juste. Le premier appel, souvent lent
  (fonction à froid), donne déjà une estimation, que les suivants remplacent s'ils sont plus
  rapides. L'aller-retour se mesure sur une horloge monotone (`TimeSource`), que personne ne règle
  pendant la mesure.
- L'écran de jeu estime l'écart à son ouverture, puis à chaque retour au premier plan (l'appareil a
  pu dormir, ou son heure être changée). Sans réponse du serveur, il prend l'horloge de l'appareil.
  Le temps restant se recalcule depuis l'horloge à chaque tic, jamais en décomptant un compteur ; les
  tics ne tournent que tant que la partie tourne et que l'écran l'affiche.
- **Le serveur décide de la fin.** À zéro, l'app demande au serveur d'écrire le temps écoulé
  (`expireGame`), qu'il vérifie à sa propre horloge ; tant qu'il répond que la partie continue, l'app
  redemande toutes les 2 s.
- Quand du code a besoin de l'heure locale (une durée d'attente, l'affichage d'une date), il la
  reçoit par un `kotlin.time.Clock` passé au constructeur, jamais par un appel direct : les tests
  passent une heure fixe.

### Textes et traductions

- Tout texte affiché vient des ressources, même un sigle qui ne se traduit pas : il est alors
  déclaré dans `values/strings.xml` seulement, avec `translatable="false"` (`app_name`).
- Les textes vivent dans Compose Resources : `src/commonMain/composeResources/values/strings.xml`
  (anglais, langue par défaut) et `values-fr/strings.xml` (français), dans le module qui les
  affiche. Le dossier ne porte que la langue, sans région.
- Un texte ajouté ou changé l'est dans toutes les langues, dans le même commit. Une langue de plus
  se déclare aussi dans `CFBundleLocalizations` de `iosApp/iosApp/Info.plist`.
- Les fichiers `strings.xml` restent triés par ordre alphabétique des clés.
- Les clés sont en anglais, même si la valeur est en français.
- La clé décrit le contenu, pas l'usage à l'écran.
    - Exemple attendu : `join_game -> Join a game`.
    - Exemple à éviter : `home_button_title -> Join a game`.
- Dans un `strings.xml` de Compose Resources, l'apostrophe s'écrit telle quelle (`L'escape game`, et
  non `L\'escape game`, dont l'antislash risque de s'afficher). Les variables restent positionnelles
  (`%1$s`, `%1$d`), sans largeur ni autre option : Compose Resources ne remplace que ces deux formes,
  et affiche `%2$02d` tel quel. Un nombre à mettre en forme (deux chiffres) se prépare dans le code et
  se passe en `%1$s`.
- Un message d'erreur dit à l'utilisateur ce qui s'est passé et ce qu'il peut faire, sans détail
  technique (pas de code d'erreur Firebase à l'écran).
