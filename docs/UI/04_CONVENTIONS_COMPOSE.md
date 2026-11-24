## Conventions Compose

Ces conventions s'appliquent à `shared` et `designSystem`, en complément de
[Common/02_CONVENTIONS.md](../Common/02_CONVENTIONS.md).

### Signature et appel d'un composable

- Un composable qui dessine quelque chose prend un `modifier: Modifier = Modifier`, **premier
  paramètre optionnel**, placé après les paramètres obligatoires, et l'applique à son conteneur
  racine.
- Il ne force pas sa taille : un `fillMaxSize()` dans le corps d'un composant réutilisable retire à
  l'appelant le contrôle de sa mise en page. C'est le rôle du `modifier` reçu. (`ESScreenStructure`
  est l'exception : il est par nature le cadre de tout l'écran.)
- Les `Route` et les `Screen` n'exposent pas de `modifier` : ils occupent tout l'écran.
- À l'appel, tout composant reçoit un `modifier` qui dit sa stratégie de taille
  (`fillMaxWidth()`, `fillMaxSize()`, `wrapContentWidth()`, `size(...)`, le `modifier` reçu…).
    - Exemple à éviter : `ESText(text = title)`.
    - Exemple attendu : `ESText(modifier = Modifier.fillMaxWidth(), text = title)`, sur plusieurs
      lignes.
- `modifier = ...` est le **premier argument nommé** de l'appel :
  ```kotlin
  ESText(
      modifier = Modifier.padding(24.dp),
      text = stringResource(Res.string.home_coming_soon),
      textAlign = TextAlign.Center,
  )
  ```
- Dans un composable, une ligne vide sépare chaque appel frère d'un même bloc (deux `ESText`
  successifs, un `Spacer` et un bouton…).
- Une lambda composable (un slot comme `title`, `icon`, ou le contenu d'un conteneur) s'écrit sur
  plusieurs lignes, son contenu sur sa propre ligne. Un callback court
  (`onClick = { onGameClicked(game.id) }`) reste sur une ligne.

### Ressources

- Textes et polices passent par **Compose Resources** : `src/commonMain/composeResources/`
  (dossiers `values`, `values-fr`, `font`, et `drawable` quand des icônes arriveront), lus avec
  `stringResource(Res.string.x)`, `painterResource(Res.drawable.x)` et `Font(Res.font.x)`.
- Hors d'un composable, une chaîne se lit avec la fonction suspendue `getString(Res.string.x)`.
- Les icônes seront des **Material Symbols** (style Rounded), en vectoriel XML dans `drawable`,
  préfixées `ic_` (`ic_timer`).
- Une icône a toujours un `contentDescription` non nul, même à côté d'un texte qui dit la même
  chose.
- Voir [Common/02_CONVENTIONS.md](../Common/02_CONVENTIONS.md) pour les règles des `strings.xml`.

### Temps affiché

- Le temps restant d'une partie se calcule dans le ViewModel, à partir des horodatages serveur et de
  l'écart estimé avec le serveur (voir [Common/02_CONVENTIONS.md](../Common/02_CONVENTIONS.md)),
  jamais dans la composition à partir de l'horloge de l'appareil.
- Un tic-tac qui change chaque seconde se lit au plus près de ce qui l'affiche, pour ne recomposer
  que le minuteur.

### État et recomposition

- Un état que l'utilisateur ouvre ou saisit se garde avec `rememberSaveable`, pas `remember` : une
  recréation ne doit ni fermer une dialog ni perdre une saisie.
- **`derivedStateOf` ne sert qu'à dériver d'un autre `State`** (un `LazyListState`, un
  `mutableStateOf`). Dériver d'un paramètre se fait avec `remember(parametre) { … }`.
- **Un `remember` qui lit un paramètre prend ce paramètre en clé.** Sans clé, la valeur n'est
  jamais recalculée : c'est un bug d'affichage silencieux.
- Pas de `mutableStateOf` enveloppant une valeur déjà dérivable : `val isEmpty = games.isEmpty()`
  suffit.
- Le fichier `compose-stability.conf` à la racine déclare stables les modèles immuables venus d'un
  module sans compilateur Compose (`fr.skyle.escapy.domain.model.**`). Un composable qui les reçoit
  reste ainsi *skippable*. Un modèle de `domain` est donc immuable (`val`, collections en lecture
  seule).

### Performance

- Une `LazyColumn` (messages du chat, joueurs, parties) fournit une `key` **et** un `contentType`
  stables pour chaque élément : l'id du document Firestore.
- Aucune allocation coûteuse (formatage de date, `Regex`) dans le corps d'un composable appelé par
  élément de liste : ce qu'affiche une cellule se prépare dans le ViewModel, dans un modèle propre à
  la liste.
- Le tri, le regroupement et le filtrage d'une liste appartiennent au ViewModel (ou à la requête
  Firestore), pas à la composition.
- Une valeur animée (minuteur qui pulse, confettis) se lit de préférence dans
  `Modifier.graphicsLayer` ou `Modifier.layout` plutôt que dans le corps du composable : la lecture
  reste en phase de layout ou de dessin, sans recomposition à chaque frame.

### Mise en page

- Ne pas utiliser `Arrangement.spacedBy(X.dp)` dès lors que l'espacement entre les enfants n'obéit
  pas à une vraie règle unique et volontaire.
    - Acceptable dans un composant dont le design impose le même espacement quel que soit le contenu.
    - À éviter sur un écran composé de blocs distincts : un `Spacer` explicite entre chaque bloc dit
      mieux l'intention.
      ```kotlin
      Column(modifier = Modifier.fillMaxWidth()) {
          ESText(
              modifier = Modifier.fillMaxWidth(),
              text = stringResource(Res.string.app_name),
              style = ESTextStyle.Title,
          )

          Spacer(modifier = Modifier.height(12.dp))

          ESText(
              modifier = Modifier.fillMaxWidth(),
              text = stringResource(Res.string.welcome_coming_soon),
          )
      }
      ```
- Un modificateur conditionnel s'exprime par une concaténation, pas par un `let` :
    - Exemple à éviter : `.let { if (isSelected) it.border(...) else it }`
    - Exemple attendu : `.then(if (isSelected) Modifier.border(...) else Modifier)`
- Une surface cliquable ne porte aucun autre élément cliquable.
- Les insets système sont gérés par `ESScreenStructure`, pas écran par écran.

### Previews

- Chaque composable non trivial a au moins une preview, `private`, nommée `Preview` + nom du
  composable (`PreviewHomeScreen`, `PreviewHomeScreen_Empty`), et enveloppée dans `ESTheme`.
- Les previews couvrent les états limites : vide, chargement, contenu long, valeur absente.
- Une preview a plusieurs variantes : elle passe par un `PreviewParameterProvider`.
- Les previews utilisent `@Preview` (`androidx.compose.ui.tooling.preview`). Des annotations
  propres à `shared`, qui rendraient une preview en français et en anglais, viendront avec les
  premiers vrais écrans si le besoin se confirme.
