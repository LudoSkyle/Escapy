# Escapy — instructions pour l'agent IA

Escapy est une app d'escape game à la maison, pour Android et iOS, en Kotlin Multiplatform et
Compose Multiplatform. Un maître du jeu crée une partie, y invite des joueurs, leur donne des
missions à résoudre et des indices, et discute avec eux pendant que le minuteur tourne. Le backend
est Firebase (Auth, Firestore, Cloud Functions, Storage), appelé en Kotlin commun par le SDK de
GitLive.

L'app est découpée en modules : `androidApp` et `iosApp` (les hôtes de chaque plateforme), `shared`
(écrans, navigation, ViewModels), `designSystem` (thème et composants génériques), `domain`
(modèles métier, UseCases) et `data` (Firebase, Repositories).

## Avant de commencer

**Lis la documentation avant toute modification de code** : elle contient les règles que ce projet
applique, et elles priment sur les habitudes générales.

Point d'entrée : [docs/SOMMAIRE.md](./docs/SOMMAIRE.md). Ne charge que le(s) document(s) utile(s) à
la tâche en cours, chacun est autonome.

| Tâche | Document à lire |
|---|---|
| Savoir où en est la migration, quoi faire ensuite | [docs/ROADMAP.md](./docs/ROADMAP.md) |
| Comprendre le produit, son vocabulaire (partie, mission, indice…) | [docs/Produit/01_VISION_ET_VOCABULAIRE.md](./docs/Produit/01_VISION_ET_VOCABULAIRE.md) |
| Toucher aux missions, à leurs déclencheurs, à leur validation | [docs/Produit/02_MISSIONS.md](./docs/Produit/02_MISSIONS.md) |
| Créer un fichier, un module, choisir une lib | [docs/Common/01_MODULES_ET_LIBRAIRIES.md](./docs/Common/01_MODULES_ET_LIBRAIRIES.md) |
| Toute contribution (nommage, style, erreurs, heure, textes) | [docs/Common/02_CONVENTIONS.md](./docs/Common/02_CONVENTIONS.md) |
| Écrire ou modifier un test | [docs/Common/03_TESTS.md](./docs/Common/03_TESTS.md) |
| Ajouter une dépendance, toucher au build, à la signature, à la CI | [docs/Common/04_BUILD.md](./docs/Common/04_BUILD.md) |
| Ajouter un écran ou une route | [docs/UI/01_NAVIGATION.md](./docs/UI/01_NAVIGATION.md), [docs/UI/02_ARCHITECTURE_ECRAN.md](./docs/UI/02_ARCHITECTURE_ECRAN.md) |
| Toucher au thème ou à un composant générique | [docs/UI/03_DESIGN_SYSTEM.md](./docs/UI/03_DESIGN_SYSTEM.md) |
| Toute contribution UI Compose | [docs/UI/04_CONVENTIONS_COMPOSE.md](./docs/UI/04_CONVENTIONS_COMPOSE.md) |
| Firebase côté app, Repositories | [docs/KMP/01_MODULE_DATA.md](./docs/KMP/01_MODULE_DATA.md) |
| Logique métier, UseCase, exception typée | [docs/KMP/02_MODULE_DOMAIN.md](./docs/KMP/02_MODULE_DOMAIN.md) |
| Tout ce qui diffère par plateforme | [docs/KMP/03_CODE_PLATEFORME.md](./docs/KMP/03_CODE_PLATEFORME.md) |
| Règles Firestore, Cloud Functions, émulateur | [firebase/CLAUDE.md](./firebase/CLAUDE.md), [docs/Backend/README.md](./docs/Backend/README.md) |
| Toucher au projet Xcode, à l'`Info.plist`, au paquet de liaison SwiftPM | [iosApp/CLAUDE.md](./iosApp/CLAUDE.md) |

Cinq règles à ne jamais perdre de vue :

- **Le serveur est la source de vérité.** Toute écriture qui change l'état d'une partie passe par
  une Cloud Function appelable, qui vérifie l'identité et le rôle. Le client ne valide rien : il ne
  connaît jamais une réponse, ne débloque jamais une mission, ne calcule jamais une victoire. Voir
  [ADR-0004](./docs/adr/0004-firestore-et-le-serveur-source-de-verite.md).
- **Jamais l'horloge de l'appareil pour une partie** : ni pour trier des messages, ni pour le
  minuteur. Les horodatages viennent du serveur. Voir
  [docs/Common/02_CONVENTIONS.md](./docs/Common/02_CONVENTIONS.md).
- **Toute l'UI est en Compose Multiplatform**, dans `commonMain`. Le natif n'existe que derrière une
  frontière commune. Voir [ADR-0002](./docs/adr/0002-ui-commune-en-compose-multiplatform.md).
- **Le domaine dépend de la donnée, jamais l'inverse** : `shared` → `domain` → `data`, et `shared`
  ne voit ni `data` ni Firebase. Voir [ADR-0001](./docs/adr/0001-le-domaine-depend-de-la-donnee.md).
- **`internal` par défaut** : seul ce qui traverse une frontière de module est public.

## État du projet

La réécriture en KMP/CMP avance lot par lot, par pull request. Le spike Firebase (lot 1) a validé
le SDK de GitLive et Firestore. La fondation (lot 2) est en place : modules, build, Koin, navigation
pilotée par la session, hôtes Android et iOS, thème provisoire, CI. Le backend socle (lot 3) est
dans `firebase/` : règles testées, création, modification et suppression du profil, émulateur. Le
design system v2 (lot 4) est en place : tokens, composants des écrans du compte, icônes, catalogue.
Le compte (lot 5) est en place : connexion par e-mail ou Google, inscription, invité, mot de passe
oublié, profil (pseudo, avatar), liaison d'un invité à un e-mail ou à Google, changement d'e-mail et
de mot de passe, suppression du compte, à propos. Les parties (lot 6a) se créent, se rejoignent par
leur code ou leur QR code (6b) et attendent leurs joueurs ; les liens (6c) suivent. La partie en
cours (lot 7a) se lance, son minuteur est tenu par le serveur (pause, reprise, temps ajouté), et elle
finit à la main ou sur le temps, avec son récapitulatif ; ses membres discutent dans son chat (7b).
Les missions (lot 8a), code ou texte, se créent en salle d'attente comme en jeu, se débloquent au
lancement, après une autre ou à la main, se résolvent sur le serveur et font la victoire ; le
mot-clé du chat et le journal des tentatives (8b) suivent. La suite arrive dans l'ordre de la
[feuille de route](./docs/ROADMAP.md).

## Commandes

```bash
./gradlew :data:allTests :domain:allTests :designSystem:allTests :shared:allTests   # tests (JVM et simulateur iOS)
./gradlew :androidApp:assembleDebug                          # app Android
./gradlew :shared:linkDebugFrameworkIosSimulatorArm64        # framework iOS
(cd firebase/functions && npm ci && npm test)                # backend sur les émulateurs Firebase
./scripts/emulators.sh                                       # émulateurs Firebase pour l'app
xcodebuild build -project iosApp/iosApp.xcodeproj -scheme iosApp \
  -destination 'platform=iOS Simulator,name=iPhone 17' CODE_SIGNING_ALLOWED=NO   # app iOS, comme la CI
```

L'app iOS se lance depuis Xcode (`iosApp/iosApp.xcodeproj`, schéma `iosApp`) : une phase de build
y construit le framework `Shared`. Voir [iosApp/CLAUDE.md](./iosApp/CLAUDE.md).

Lance les tests du module touché avant de considérer une modification comme terminée.

## Tests

Les ADR communs 0001 (tests FIRST) et 0002 (le code livré vient avec ses tests) du référentiel
partagé s'appliquent. Avant d'écrire un test, lis [docs/Common/03_TESTS.md](./docs/Common/03_TESTS.md) :
il porte la forme KMP (source sets, outillage). Le skill `android-testing-first` donne la structure
d'un test de UseCase, de Repository et de ViewModel (Given/When/Then, fakes) ; quand il diverge du
document du projet (JUnit 4 et Robolectric partout), c'est le document du projet qui fait foi.

## Git

- **Ne jamais ajouter de `Co-Authored-By`, de `Generated with`, ni aucune autre mention d'un outil ou
  d'un modèle d'IA** dans les messages de commit et les descriptions de pull request. L'auteur du
  commit est la personne qui l'a demandé.
- Messages de commit en anglais, préfixés par leur type : `feat:`, `fix:`, `refactor:`, `chore:`,
  `docs:`, `test:`.
- Le corps du message d'un correctif décrit le **symptôme observable**, pas l'implémentation.
- Des commits courts et mono-périmètre : une correction, un renommage ou une fonctionnalité par
  commit.
- Le dépôt est **public**. Le sous-module privé `private/` (clés de signature, fichiers Firebase)
  n'est jamais affiché, ni son contenu décrit. Il n'est modifié qu'à la demande de l'utilisateur,
  pour y ranger un fichier qu'il fournit : commit poussé sur sa branche `main`, puis le pointeur du
  sous-module mis à jour par une pull request du dépôt principal.
- Le fichier `AUDIT.md`, tenu hors du dépôt, ne se commite jamais, ni en entier ni en extrait.
- **Chaque lot est livré par une pull request vers `develop`**, jamais poussé directement : une
  branche `<type>/<description-courte>` partie de `develop` à jour, poussée, puis la PR, fusionnée
  **en rebase** (historique linéaire) une fois les vérifications du lot passées et la CI GitHub
  Actions verte (`gh pr checks --watch`), et la branche supprimée. Une PR qui suit une autre est
  rebasée sur `develop` à jour avant d'être poussée. Le titre de la PR est le sujet du commit
  principal ; sa description, en anglais, dit ce qui change et ce qui a été vérifié.
- Une version passe de `develop` à `main` par une PR fusionnée par un **commit de fusion** : un rebase
  réécrirait sur `main` les commits de `develop`, et les deux branches divergeraient.
- Rien ne se pousse en dehors de ce circuit : pas de push direct sur `develop` ni sur `main`.

## Mise à jour de cette documentation

- Toute règle d'architecture, de nommage ou de test décidée en cours de route est ajoutée au
  document correspondant dans `docs/`, pas seulement appliquée au code. Une décision structurante
  devient un ADR dans `docs/adr/`.
- La [feuille de route](./docs/ROADMAP.md) est tenue à jour dans le commit qui termine un élément.
- La documentation interne vit dans `docs/`, à côté du site public (GitHub Pages, servi depuis
  `main:/docs`) : tout nouveau dossier ou fichier interne de `docs/` s'ajoute à la liste `exclude:`
  de `docs/_config.yml`, pour ne pas être publié. `docs/index.md` et `docs/privacy-policy.html` sont
  le site public : l'URL de la politique de confidentialité ne change pas.
- Le sommaire s'appelle `SOMMAIRE.md` et non `INDEX.md` : sur un disque macOS insensible à la casse,
  `INDEX.md` serait le même fichier que la page d'accueil du site, `index.md`.
- Le dépôt étant public, la documentation ne décrit ni le contenu de `private/`, ni une empreinte, un
  mot de passe ou un identifiant d'app Firebase.
- La documentation ne cite aucun autre projet qu'Escapy.
