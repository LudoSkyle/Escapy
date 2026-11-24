# Documentation Escapy — Sommaire

Ce document est le point d'entrée de la référence que l'agent IA suit pour ajouter, modifier ou
corriger du code dans Escapy, app Kotlin Multiplatform / Compose Multiplatform pour Android et iOS,
sur un backend Firebase.

Ne charge que le(s) document(s) pertinent(s) pour la tâche en cours plutôt que l'ensemble : chaque
fichier ci-dessous est autonome.

Les documents sont répartis par périmètre :

- `Produit/` : ce que fait l'app, son vocabulaire, les règles du jeu.
- `Common/` : ce qui vaut pour tout le projet (modules, conventions, tests, build).
- `UI/` : l'UI Compose Multiplatform, commune à Android et iOS (modules `shared` et `designSystem`).
- `KMP/` : les modules Kotlin sans UI (`data`, `domain`) et le code propre à chaque plateforme.
- `Backend/` : Firestore, Cloud Functions, règles, émulateur.
- `adr/` : les décisions d'architecture du projet. Elles priment sur le référentiel partagé en cas
  de conflit, dans la limite de leur périmètre.

Ce dossier `docs/` sert aussi le site public (GitHub Pages) : `index.md` et `privacy-policy.html`
en sont les pages. Tout le reste en est exclu par `_config.yml`.

| Document | Contenu | À consulter quand... |
|---|---|---|
| [ROADMAP.md](./ROADMAP.md) | Lots de la réécriture, parité avec l'ancienne app, nouveautés, décisions | Savoir quoi faire ensuite, cocher ce qui est fait |
| [Produit/01_VISION_ET_VOCABULAIRE.md](./Produit/01_VISION_ET_VOCABULAIRE.md) | Vision, glossaire, parcours du maître du jeu et du joueur, versions | Toute fonctionnalité, tout nommage métier |
| [Produit/02_MISSIONS.md](./Produit/02_MISSIONS.md) | Types de missions, déclencheurs, validation, mission finale, anti-force brute | Tout travail sur les missions |
| [Common/01_MODULES_ET_LIBRAIRIES.md](./Common/01_MODULES_ET_LIBRAIRIES.md) | Modules, graphe de dépendances, visibilité, Koin, bibliothèques | Vue d'ensemble, création d'un fichier ou d'un module, choix d'une lib |
| [Common/02_CONVENTIONS.md](./Common/02_CONVENTIONS.md) | Langue, nommage, style, gestion d'erreur, heure, textes et traductions | Toute contribution, en complément du document ciblé |
| [Common/03_TESTS.md](./Common/03_TESTS.md) | Source sets de test, outillage, FIRST, quoi tester et à quel niveau | Écriture ou modification d'un test |
| [Common/04_BUILD.md](./Common/04_BUILD.md) | Gradle, catalogue, plugins de convention, signature, build sans `private/`, CI | Ajout d'une dépendance, montée de version, configuration du build |
| [UI/01_NAVIGATION.md](./UI/01_NAVIGATION.md) | `Route.kt`, `RootNavDisplay.kt`, racine pilotée par la session | Ajout ou modification d'une route |
| [UI/02_ARCHITECTURE_ECRAN.md](./UI/02_ARCHITECTURE_ECRAN.md) | Structure d'un écran (`entry`, `Route`, `Screen`, `ViewModel`), events one-shot | Création ou modification d'un écran, d'un ViewModel |
| [UI/03_DESIGN_SYSTEM.md](./UI/03_DESIGN_SYSTEM.md) | Module `designSystem` : `ESTheme` provisoire, `ESText`, `ESScreenStructure` | Travail sur le thème ou un composant générique |
| [UI/04_CONVENTIONS_COMPOSE.md](./UI/04_CONVENTIONS_COMPOSE.md) | Signature d'un composable, état, performance, mise en page, ressources, previews | Toute contribution UI, en complément de [Common/02_CONVENTIONS.md](./Common/02_CONVENTIONS.md) |
| [KMP/01_MODULE_DATA.md](./KMP/01_MODULE_DATA.md) | Module `data` : Firebase par GitLive, Repositories, horodatages serveur | Tout accès à Firebase |
| [KMP/02_MODULE_DOMAIN.md](./KMP/02_MODULE_DOMAIN.md) | Module `domain` : modèles métier, mappers, UseCases, exceptions | Logique métier côté app |
| [KMP/03_CODE_PLATEFORME.md](./KMP/03_CODE_PLATEFORME.md) | `expect`/`actual`, interfaces natives, démarrage de Firebase, matrice Android/iOS | Tout ce qui diffère par plateforme |
| [Backend/README.md](./Backend/README.md) | Principes du backend, données, fonctions, raisons d'erreur, émulateur | Toute question sur ce que fait le serveur |
| [adr/0001-le-domaine-depend-de-la-donnee.md](./adr/0001-le-domaine-depend-de-la-donnee.md) | `shared` → `domain` → `data`, exceptions typées | Toute intervention sur `data` ou `domain` |
| [adr/0002-ui-commune-en-compose-multiplatform.md](./adr/0002-ui-commune-en-compose-multiplatform.md) | UI 100 % Compose Multiplatform, natif derrière une frontière | Toute question « commun ou natif ? » |
| [adr/0003-firebase-en-kotlin-commun-avec-gitlive.md](./adr/0003-firebase-en-kotlin-commun-avec-gitlive.md) | SDK Firebase de GitLive épinglé, isolé dans `data`, mesures, paquet de liaison | Toute dépendance Firebase, toute montée de version de GitLive |
| [adr/0004-firestore-et-le-serveur-source-de-verite.md](./adr/0004-firestore-et-le-serveur-source-de-verite.md) | Firestore à `europe-west1`, callables, règles, horodatage serveur | Toute écriture, toute lecture de données de partie |
| [adr/0005-le-contrat-des-callables.md](./adr/0005-le-contrat-des-callables.md) | Raison d'erreur stable, profil par `ensureProfile`, suppression du compte | Tout appel à une fonction, toute nouvelle fonction |

## Résumé rapide des modules

- `androidApp` : hôte Android (`EscapyApplication`, `MainActivity`, manifest, icône, signature).
- `iosApp` : hôte iOS (projet Xcode généré par XcodeGen, app SwiftUI qui affiche les écrans de
  `shared`, `Info.plist`, paquet de liaison SwiftPM qui apporte Firebase).
- `shared` : `EscapyApp`, navigation, écrans, ViewModels, module Koin racine. Produit le framework
  `Shared` pour iOS.
- `designSystem` : thème (`ESTheme`) et composants génériques, sans connaissance du métier.
- `domain` : modèles métier, mappers, UseCases. Kotlin pur.
- `data` : Firebase (Auth, Firestore, Functions, Storage) par GitLive, Repositories.

Voir [Common/01_MODULES_ET_LIBRAIRIES.md](./Common/01_MODULES_ET_LIBRAIRIES.md) pour le détail.
