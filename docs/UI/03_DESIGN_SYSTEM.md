## Design system

Le thème et les composants génériques vivent dans le module `designSystem`, à part des écrans. Le
module ne dépend que de Compose et de Material 3 : un composant ne connaît ni un écran, ni un modèle
métier. Un écran choisit un composant et l'une de ses variantes ; il ne décide ni d'une couleur, ni
d'une forme, ni d'une taille de texte.

**L'identité** : sombre seulement, un seul accent vif, l'ambre (`#F5B856`), sur des surfaces
charbon ; la police Exo 2, à ses cinq poids (Light, Regular, Medium, SemiBold, Bold) ; des rayons
généreux ; le trou de serrure de l'icône.

On y trouve :

- le thème : `ESColors`, `ESTypography`, `ESShapes`, `ESSpacing`, `ESTheme` ;
- les composants génériques : `ESScreenStructure` (fond, insets, barre du haut, barre fixée en bas,
  messages), `ESTopAppBar`, `ESIconButton`, `ESButton`, `ESText`, `ESTextField`, `ESDialog`,
  `ESCard`, `ESDivider`, `ESProgressIndicator`, `ESSnackbarState` ;
- le catalogue `ESCatalog`, tous les composants sur un écran ;
- les polices (Exo 2), les icônes des composants et leurs textes, dans Compose Resources.

On n'y trouve pas les composants « produit », propres à Escapy même s'ils servent dans plusieurs
écrans (la grille des avatars, la carte d'un indice, le minuteur) : ils vivent dans `shared`, sous
`ui/core/`, et arrivent avec leur lot.

```
designSystem/
├── ICONS.md                   (origine et licence des icônes)
└── src/commonMain/
    ├── kotlin/fr/skyle/escapy/designsystem/
    │   ├── theme/
    │   │   ├── ESColors.kt        (ESColors, LocalColors, valeurs des couleurs)
    │   │   ├── ESShapes.kt
    │   │   ├── ESSpacing.kt
    │   │   ├── ESTheme.kt         (composable ESTheme et objet ESTheme)
    │   │   └── ESTypography.kt    (ESTypography, LocalTypography, famille Exo 2)
    │   ├── catalog/               (ESCatalog : tous les composants, pour un build debug)
    │   └── core/
    │       ├── button/            (ESButton, ESButtonDefaults)
    │       ├── card/              (ESCard : un bloc détaché du fond, par sa couleur et son bord)
    │       ├── dialog/            (ESDialog : une question avant une action)
    │       ├── divider/           (ESDivider : une ligne décorative entre deux rangées)
    │       ├── iconbutton/        (ESIconButton : une action montrée par une icône seule)
    │       ├── progress/          (ESProgressIndicator : une attente de durée inconnue)
    │       ├── screenstructure/   (ESScreenStructure : le cadre d'un écran)
    │       ├── snackbar/          (ESSnackbarState, et son hôte interne)
    │       ├── text/              (ESText : un texte dans un style du thème)
    │       ├── textfield/         (ESTextField, ESTextFieldDefaults)
    │       └── topappbar/         (ESTopAppBar : retour, titre, actions)
    └── composeResources/
        ├── drawable/              (ic_*.xml, Material Symbols Rounded)
        ├── font/                  (exo2_*.ttf)
        ├── values/                (textes des composants, en anglais)
        └── values-fr/
```

### Thème

Les composants génériques et le thème portent le préfixe `ES`. Le composable `ESTheme` fournit les
valeurs ; l'objet `ESTheme` les lit (`ESTheme.colors`, `ESTheme.typography`, `ESTheme.shapes`,
`ESTheme.spacing`).

- **Il n'y a pas de `MaterialTheme`.** Chaque composant Material est enveloppé dans le design
  system, qui lui passe les couleurs, les formes et les styles d'`ESTheme`. Sans cela, il prendrait
  les valeurs par défaut de Material (le violet, la police du système). `ESTheme` fournit aussi ce
  que Material lit quand on ne lui donne rien : la couleur du contenu (`textPrimary`), le style de
  texte (`body`), les couleurs de sélection (l'ambre) et le ripple.
- Les valeurs sont des data classes `@Immutable`, fournies par des `staticCompositionLocalOf`.
- **Couleurs** (`ESColors`) :

  | Groupe | Couleurs | Usage |
  |---|---|---|
  | Surfaces | `background` → `surface` → `surfaceHigh` → `surfaceHighest` | l'écran, une carte sur l'écran, un contrôle sur une carte, un contrôle pressé ou choisi |
  | Contours | `outline`, `outlineVariant` | le bord d'un contrôle (champ, bouton secondaire) ; une ligne décorative (séparateur, bord d'une carte) |
  | Textes | `textPrimary`, `textSecondary`, `textTertiary` | partout ; ce qui explique ; une aide ou un exemple, jamais sur `surfaceHighest` |
  | Accents | `primary` / `onPrimary`, `error` / `onError`, `success` | l'action principale et le focus ; une erreur, une action destructive ; une réussite |
  | États | `disabledContainer`, `disabledContent`, `scrim` | un contrôle inutilisable ; le voile sous un dialogue |

- **Contrastes WCAG AA**, vérifiés par `ESColorsContrastTest` : 4,5:1 pour tout texte sur toute
  surface où il se pose, 3:1 pour le bord d'un contrôle et une icône. Une couleur qui change ou un
  couple qui s'ajoute passe par ce test. Une carte se distingue du fond par son bord
  (`outlineVariant`) autant que par sa couleur : la différence de couleur seule est trop faible.
- **Typographie** (`ESTypography`, Exo 2) : `headline` (32, Bold), `title` (24, SemiBold),
  `subtitle` (18, SemiBold), `body` (16), `bodySmall` (14), `label` (16, SemiBold, le texte d'un
  bouton), `caption` (13, Medium). Chaque interligne dépasse sa taille. Les polices se lisent dans
  Compose Resources, ce qui ne se fait que dans un composable : `escapyTypography()` est
  `@Composable`.
- **Formes** (`ESShapes`) : `small` (12 dp, un message), `medium` (20 dp, un bouton, un champ),
  `large` (28 dp, une carte, un dialogue), `full` (un bouton icône).
- **Espacements** (`ESSpacing`) : `xxs` 4, `xs` 8, `s` 12, `m` 16, `l` 24, `xl` 32, `xxl` 48 dp.
  Un écran n'écrit pas de `dp` pour un espace : il prend celui du thème.
- Le fond de démarrage des deux hôtes suit `background` (`escapy_background` sur Android,
  `LaunchBackground` sur iOS) : une valeur qui change change aussi là.
- Tant que l'app n'a qu'un thème, on n'en prévoit qu'un.

### Material 3

- La version est l'alpha alignée sur notre Compose Multiplatform (voir le catalogue et la
  [feuille de route](../ROADMAP.md)).
- **Seul `designSystem` dépend de Material 3.** `shared` ne l'a pas sur son classpath : un écran
  n'appelle jamais `Text`, `Button` ou `MaterialTheme` de Material, il passe par un composant `ES…`.
  Quand un écran a besoin d'un élément qui n'existe pas encore, on crée le composant dans le design
  system, on n'ouvre pas la dépendance.
- **Aucune API Expressive** : `ExperimentalMaterial3ExpressiveApi` n'est jamais activée. Les autres
  API expérimentales (`TopAppBar`) sont permises, avec un `@OptIn(ExperimentalMaterial3Api::class)`
  local au composant.

### Composants

- **Un package par composant**, nommé comme lui en minuscules (`core/iconbutton/`).
- **Un objet `<Composant>Defaults` porte les variantes**, en `enum class Style` (les couleurs) et
  `enum class Size` (dimensions, marges). Les valeurs sont des propriétés de l'enum, `@Composable`
  quand elles lisent le thème. Le composant prend `style` et `size` **sans valeur par défaut** :
  l'appelant choisit, et la relecture voit ce qu'il a choisi. Une enum à une seule entrée est
  acceptée : c'est le point d'extension.
- `ESText` prend un `style` du thème (`ESTheme.typography.body`), sans valeur par défaut, et la
  couleur du contenu autour de lui, sauf une couleur du thème donnée.
- **Une hauteur est un minimum** (`heightIn(min = …)`), jamais une hauteur fixe : un composant
  grandit avec une police agrandie plutôt que de couper son texte. Une cible de toucher fait au moins
  48 dp.
- Un composant qui affiche une icône la reçoit en `Painter`. **Un bouton qui n'affiche qu'une icône
  prend un `contentDescription` obligatoire** et non nullable ; le bouton retour d'`ESTopAppBar` a le
  sien, traduit, dans le module.
- **`ESButton`** : `PRIMARY` (l'action principale, une par écran), `SECONDARY` (cerné), `TEXT`
  (léger, dans un dialogue ou sous un formulaire), `DESTRUCTIVE` (ce qui supprime ou fait perdre) ;
  `MEDIUM` et `LARGE`. **Pendant `isLoading`, il garde sa taille et ses couleurs, montre sa
  progression à la place du texte, ignore le toucher et se lit comme désactivé** : une action n'est
  jamais envoyée deux fois. Un écran désactive aussi la touche du clavier qui enverrait le formulaire.
- **`ESTextField`** : son `kind` (`TEXT`, `USERNAME`, `EMAIL`, `PASSWORD`, `NEW_PASSWORD`) ouvre le
  bon clavier et dit au gestionnaire de mots de passe quoi remplir. Un mot de passe est masqué
  jusqu'au toucher de l'œil, dont la description dit l'action. Un `errorMessage` remplace l'aide
  (`supportingText`), colore le champ et se lit comme l'erreur du champ par le lecteur d'écran. Le
  focus se voit (bord ambre, plus épais). La touche du clavier fait `imeAction`, puis appelle
  `onImeAction`. Le libellé garde la taille que Material lui anime, dans la police Exo 2.
- **`ESDialog`** : `STANDARD` ou `DESTRUCTIVE` (l'action en couleur d'erreur). Pendant
  `isConfirmLoading`, l'action montre sa progression et le dialogue ne se ferme pas.
- **`ESScreenStructure`** : le cadre de chaque écran, qui garde son contenu hors des barres
  système et du clavier (`safeDrawing`) et lui passe ces marges (`PaddingValues`). Un écran affiche
  un message court par un `ESSnackbarState` (`rememberESSnackbarState()`) donné à
  `ESScreenStructure` ; `show` dit si son action a été touchée. Il ne voit jamais `Scaffold` ni
  `SnackbarHostState` de Material.
- Les previews d'un composant parcourent ses combinaisons `Style` × `Size`, avec un
  `PreviewParameterProvider`. Elles n'utilisent que des ressources du module, et `@Preview`
  directement.
- La classe `Res` générée (`fr.skyle.escapy.designsystem.resources`) est `internal` au module : une
  ressource du design system qu'un écran affiche aussi passe par un accesseur public dédié.
- **Un composant ou une variante n'est ajouté qu'avec le premier écran qui en a besoin.** Le lot 4
  a fait une exception assumée : il a construit d'avance les composants des écrans du compte
  (lot 5), pour les poser avant eux. Les composants des écrans de jeu (minuteur en chiffres
  tabulaires, saisie de code façon cadenas, carte d'un indice, victoire et défaite) arrivent avec
  leur lot, comme un sélecteur de durée ou une feuille du bas.

### Icônes

- Des **Material Symbols Rounded** (poids 400, non remplis, 24 dp), en vectoriel XML dans
  `composeResources/drawable/`, nommées `ic_<nom>.xml`. Le module n'a que celles que ses composants
  dessinent ; les icônes d'un écran vivent dans `shared` et arrivent avec lui.
- Une icône se prend dans le dépôt de Google
  (`symbols/android/<nom>/materialsymbolsrounded/<nom>_24px.xml`), puis s'adapte à Compose
  Resources, qui ne lit ni attribut de thème ni couleur du système : on retire `android:tint` (en
  refermant la balise `vector` s'il en était le dernier attribut) et on remplace
  `@android:color/white` par `#FFFFFFFF`. La couleur vient du composant. Une icône mal adaptée fait
  planter l'app à son premier affichage. Origine et licence (Apache 2.0) : `designSystem/ICONS.md`.

### Catalogue

`ESCatalog` montre tous les composants sur un écran, pour les regarder sur un appareil. Il ne fait
pas partie de la navigation : seul un build debug l'ouvre, à la demande.

```bash
adb shell am start -n fr.skyle.escapy.debug/fr.skyle.escapy.ui.MainActivity --ez catalog true   # Android
SIMCTL_CHILD_ESCAPY_CATALOG=1 xcrun simctl launch booted fr.skyle.escapy                        # iOS
```

Un composant qui s'ajoute s'ajoute au catalogue, et se regarde sur les deux plateformes, à une
police agrandie aussi.
