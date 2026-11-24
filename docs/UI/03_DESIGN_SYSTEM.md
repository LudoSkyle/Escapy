## Design system

Le thème et les composants génériques vivent dans le module `designSystem`, à part des écrans. Le
module ne dépend que de Compose et de Material 3 : un composant ne connaît ni un écran, ni un modèle
métier. Un écran choisit un composant et l'une de ses variantes ; il ne décide ni d'une couleur, ni
d'une forme, ni d'une taille de texte.

**Le thème actuel est provisoire.** Il reprend l'identité de l'ancienne app, le temps de la
fondation ; le design system est refait au lot 4 (voir [../ROADMAP.md](../ROADMAP.md)).

### Ce qui existe

```
designSystem/src/commonMain/
├── kotlin/fr/skyle/escapy/designsystem/
│   ├── theme/
│   │   ├── ESColors.kt        (palette, ESColorScheme : un darkColorScheme de Material 3)
│   │   ├── ESTypography.kt    (Exo 2 sur tous les styles de Material 3, tailles par défaut)
│   │   └── ESTheme.kt         (un MaterialTheme avec ces couleurs et cette typographie)
│   └── core/
│       ├── screenstructure/   (ESScreenStructure : le fond sous les barres système, le contenu à l'intérieur)
│       └── text/              (ESText et ESTextStyle : Title, Body)
└── composeResources/
    └── font/                  (exo2_light, _regular, _medium, _semibold, _bold)
```

- **L'identité** : sombre seulement, un seul accent vif, l'ambre (`#F5B856`), sur un fond charbon ;
  la police Exo 2, à ses cinq poids (Light, Regular, Medium, SemiBold, Bold), chacun bien associé à
  son fichier.
- `ESTheme` enveloppe chaque écran. Il fournit aujourd'hui un `MaterialTheme` (`ESColorScheme`,
  `esTypography()`) : c'est provisoire.
- `ESText` affiche un texte dans un style du thème, `Title` ou `Body` (par défaut), dans la couleur
  du texte sur le fond.
- `ESScreenStructure` est le cadre d'un écran : il occupe tout l'écran, peint le fond sous les barres
  système et place son contenu à l'intérieur des insets (`safeDrawingPadding`).

### Ce qui vaut déjà

- Les composants génériques et le thème portent le préfixe `ES`.
- **Seul `designSystem` dépend de Material 3.** `shared` ne l'a pas sur son classpath : un écran
  n'appelle jamais `Text`, `Button` ou `MaterialTheme` de Material, il passe par un composant `ES…`.
  Quand un écran a besoin d'un élément qui n'existe pas encore, on crée le composant dans le design
  system, on n'ouvre pas la dépendance.
- **Aucune API Expressive** : `ExperimentalMaterial3ExpressiveApi` n'est jamais activée. Les autres
  API expérimentales de Material 3 sont permises, avec un `@OptIn(ExperimentalMaterial3Api::class)`
  local au composant.
- **Un package par composant**, nommé comme lui en minuscules (`core/screenstructure/`).
- Les insets système sont gérés par `ESScreenStructure`, pas écran par écran : Compose dessine sous
  les barres sur les deux plateformes.
- La classe `Res` générée (`fr.skyle.escapy.designsystem.resources`) est `internal` au module : une
  ressource du design system qu'un écran affiche aussi passe par un accesseur public dédié.
- Les previews d'un composant n'utilisent que des ressources du module, et l'annotation `@Preview`
  directement.
- Un composant n'est ajouté qu'avec le premier écran qui en a besoin.

### Ce qui arrive au lot 4

Ce qui suit est la cible ; aucun composant ne la suit encore.

- Des tokens propres à Escapy, plutôt que le `ColorScheme` de Material : surfaces sur quatre niveaux,
  contours, couleurs `on…`, espacements, états ; des contrastes WCAG AA. Ils se lisent par un objet
  `ESTheme` (`ESTheme.colors`, `ESTheme.typography`, `ESTheme.shapes`) et sont fournis par des
  `staticCompositionLocalOf`. Plus de `MaterialTheme` : chaque composant Material enveloppé reçoit
  les couleurs d'`ESTheme`.
- Une hiérarchie typographique propre (les tailles de Material par défaut aujourd'hui), des rayons
  généreux (20 à 28 dp), plus d'air.
- **Un objet `<Composant>Defaults` porte les variantes**, en `enum class Style` (les couleurs) et
  `enum class Size` (dimensions, formes, marges, typographie). Le composant prend `style` et `size`
  **sans valeur par défaut** : l'appelant choisit, et la relecture voit ce qu'il a choisi. Une
  variante n'est ajoutée que quand un écran en a besoin.
- Les previews d'un composant parcourent toutes ses combinaisons `Style` × `Size`, avec un
  `PreviewParameterProvider`.
- Un composant qui affiche une icône la reçoit en `Painter`. Un bouton qui n'affiche qu'une icône
  prend un `contentDescription` **obligatoire** et non nullable.
- Un bouton se désactive et montre son chargement pendant une action en cours.
- Les champs de texte gèrent l'erreur, le remplissage automatique, la visibilité d'un mot de passe
  et le focus.
- Les icônes sont des Material Symbols Rounded, en vectoriel XML dans Compose Resources.
- Pour les écrans de jeu : un minuteur en chiffres tabulaires, une saisie de code façon cadenas, la
  carte d'un indice reçu, les écrans de victoire et de défaite.
