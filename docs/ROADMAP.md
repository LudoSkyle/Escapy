# Feuille de route — Escapy v2 (KMP/CMP)

Point de départ (2026-11-24) : l'ancienne app, Android seulement, sert de référence fonctionnelle. Son code, travail en cours compris, est gardé
sur la branche `legacy/android`. Chaque lot qui reprend une de ses fonctionnalités la **reconçoit**
selon les règles de ce dépôt (serveur source de vérité, exceptions typées, UI commune) : on ne porte
pas son code.

Chaque lot est livré par une pull request vers `develop` (voir la section Git de `CLAUDE.md`). Un
élément se coche dans le commit qui le termine.

## Parité avec l'ancienne app (minimum)

### Compte et profil (lot 5)

- [ ] Connexion par e-mail et mot de passe, inscription
- [ ] Jouer en invité, sans compte, puis lier un compte à cet invité
- [ ] Profil : pseudo, avatar (les 50 avatars, en Compose Resources)
- [ ] Changer d'e-mail, changer de mot de passe
- [ ] Supprimer son compte
- [ ] À propos

### Parties (lot 6)

- [ ] Créer une partie : titre et durée
- [ ] Rejoindre une partie par QR code
- [ ] La partie en cours, vue par ses joueurs (l'écran n'était pas terminé)

## Nouveautés

- [ ] L'app sur iOS, avec les mêmes écrans
- [ ] Rejoindre par un code de partie à 6 caractères ou par un lien, en plus du QR code
- [ ] Salle d'attente : les joueurs arrivent, le maître du jeu en exclut, lance la partie ; un
      joueur quitte
- [ ] Minuteur tenu par le serveur, le même chez tous : pause, reprise, ajout de temps
- [ ] Chat écrit entre le maître du jeu et les joueurs
- [ ] Écran d'histoire d'intro, au lancement
- [ ] Missions « code » et « mot ou phrase », avec leurs déclencheurs : manuel, dès le début, après
      une autre mission, mot ou phrase écrit dans le chat (voir
      [Produit/02_MISSIONS.md](./Produit/02_MISSIONS.md))
- [ ] Indices envoyés à tout moment : texte et image (v1), audio (v1.1)
- [ ] Fin de partie : victoire, défaite ou fin manuelle, et son récapitulatif
- [ ] Scénarios : une partie préparée et réutilisable, gardée un temps limité
- Plus tard (v1.1 et v2) : notifications push, journal complet côté maître du jeu, indices liés aux
  missions, nouveaux types de mission, mode écran partagé, historique, premium. Le détail est dans
  [Produit/01_VISION_ET_VOCABULAIRE.md](./Produit/01_VISION_ET_VOCABULAIRE.md).

## Lots techniques

Dans l'ordre. Chaque lot laisse l'app compilable sur les deux plateformes.

0. **Point de départ**
   - [x] Le travail en cours de l'ancienne app sauvé sur `legacy/android`
   - [x] `develop` créée depuis `main`
1. **Spike Firebase KMP**, dans un projet jetable hors du dépôt
   - [x] Connexion anonyme, appel d'une callable (requête et réponse `@Serializable`), écriture et
         écoute Firestore, chaîne « callable → écriture → listener », sur un émulateur Android et le
         simulateur iOS, contre l'émulateur Firebase puis le vrai projet
   - [x] Horodatage serveur, Storage (envoi, URL, lecture, suppression), build Android release
         minifié par R8 sans règle, build iOS pour appareil (`iosArm64`) qui compile
   - [x] Mesures de latence (voir [ADR-0003](./adr/0003-firebase-en-kotlin-commun-avec-gitlive.md))
   - [x] Base `(default)` en Firestore natif à `europe-west1`, projet au forfait Blaze ; Firestore et
         Storage refusent tout accès client en attendant les règles du lot 3
   - [x] Décisions : GitLive ([ADR-0003](./adr/0003-firebase-en-kotlin-commun-avec-gitlive.md)),
         Firestore et serveur source de vérité
         ([ADR-0004](./adr/0004-firestore-et-le-serveur-source-de-verite.md))
   - Non vérifié : un vrai iPhone, un vrai téléphone Android en 4G, Crashlytics.
2. **Fondation**
   - [x] Modules `androidApp`, `shared`, `designSystem`, `domain`, `data` ; plugins de convention
         `escapy-kmp-library` et `escapy-compose-library` dans `buildSrc` ; catalogue de versions
   - [x] Koin (un module par couche, `sharedModule` démarré par les hôtes), Kermit pour les logs
   - [x] Firebase par GitLive dans `data` ; `SessionRepository`, `WatchSessionUseCase` et
         `MainViewModel`, avec leurs tests, sur la JVM et le simulateur iOS
   - [x] Navigation 3 multiplateforme, racine du back stack pilotée par la session : `Route.Welcome`
         sans session, `Route.Home` avec ; les deux écrans ne sont que des emplacements
   - [x] Design system provisoire : `ESTheme` (palette sombre et ambre, Exo 2 à ses bons poids),
         `ESText`, `ESScreenStructure` ; `compose-stability.conf`
   - [x] Hôte Android : écran de démarrage, barres système claires sur fond sombre, portrait
   - [x] Hôte iOS : projet XcodeGen, phase de build du framework `Shared`, paquet de liaison SwiftPM
         qui apporte Firebase, démarrage de Firebase seulement avec son fichier
   - [x] Build sans `private/` (fork, CI) : l'app démarre déconnectée
   - [x] CI GitHub Actions : tests JVM et simulateur iOS, app Android, paquet de liaison à jour, app
         iOS, à chaque pull request
   - [x] `CLAUDE.md`, `iosApp/CLAUDE.md` et `docs/`, exclus du site public
3. **Backend socle**
   - [x] Dossier `firebase/` dans le dépôt : `firebase.json`, `firestore.rules`, `storage.rules`,
         `firestore.indexes.json`, `functions/` (TypeScript, Cloud Functions v2 à `europe-west1`)
   - [x] Règles versionnées et testées sur l'émulateur (`@firebase/rules-unit-testing`) : profil lu
         par son propriétaire seulement, tout le reste refusé, Storage fermé
   - [x] Émulateur Firebase sur le projet de démonstration `demo-escapy`, et `scripts/emulators.sh`
         (ports, `adb reverse` pour Android)
   - [x] `ensureProfile` (idempotente, sans risque en double appel), `updateProfile` (pseudo et
         avatar validés côté serveur), `deleteAccount` (données puis compte, connexion récente pour
         un membre) ; contrat d'erreur `details.reason`
         ([ADR-0005](./adr/0005-le-contrat-des-callables.md)). Pas d'e-mail dans le profil, donc rien
         à synchroniser
   - [x] Tests des fonctions et des règles sur l'émulateur, dans un job `backend` de la CI
   - [x] `firebase/CLAUDE.md`, `docs/Backend/README.md`, comportement du TTL vérifié dans la
         documentation Firebase
   - [x] Déployé sur le vrai projet le 2026-11-24 : règles Firestore et Storage, index, les trois
         fonctions ; l'ancienne fonction `finalizeEmailUpdate` supprimée
   - [ ] Emplacement du bucket Storage par défaut, consigné dans
         [ADR-0004](./adr/0004-firestore-et-le-serveur-source-de-verite.md)
4. **Design system v2**
   - [x] Tokens propres, sans `MaterialTheme` : `ESColors` (surfaces sur quatre niveaux, contours,
         textes sur trois niveaux, ambre, erreur, succès, états désactivés), `ESTypography` (Exo 2,
         sept styles), `ESShapes` (12, 20 et 28 dp), `ESSpacing` (échelle de 4 dp) ; contrastes WCAG
         AA vérifiés par un test
   - [x] Les composants dont les écrans du compte (lot 5) ont besoin : `ESButton` (chargement qui
         ignore le toucher et garde la largeur), `ESIconButton` (`contentDescription` obligatoire),
         `ESTextField` (erreur lue par le lecteur d'écran, remplissage automatique, mot de passe à
         afficher, focus visible), `ESTopAppBar`, `ESScreenStructure`, `ESSnackbarState`, `ESDialog`,
         `ESCard`, `ESDivider`, `ESProgressIndicator`, `ESText` ; variantes dans des
         `<Composant>Defaults`
   - [x] Icônes Material Symbols Rounded des composants (retour, afficher et masquer le mot de passe)
   - [x] Icône de l'app iOS et écran de démarrage iOS (le trou de serrure de l'icône Android) ; fond
         de démarrage Android aligné sur le nouveau fond
   - [x] Catalogue des composants, ouvert par un build debug seulement ; tests des composants en
         Compose (`runComposeUiTest`), sur la JVM et le simulateur iOS
   - Vérifié sur un émulateur Android (API 31, police à 2×) et le simulateur iOS 26.2. Non vérifié :
     le remplissage automatique par un gestionnaire de mots de passe, sur les deux plateformes.
5. **Compte et profil**, reconçus, en trois pull requests :
   - 5a, **entrer dans l'app** :
     - [x] Bienvenue, connexion, inscription (règles du mot de passe affichées une à une, lues par le
           lecteur d'écran), invité, mot de passe oublié (sans dire si l'adresse a un compte)
     - [x] Profil lu dans le cache de Firestore ; `ensureProfile` appelée seulement s'il manque, une
           fois ; écran « Profil indisponible » (réessayer, se déconnecter)
     - [x] Erreurs de Firebase traduites en exceptions typées, les mêmes sur Android et iOS (vérifié
           en provoquant chaque erreur sur les deux) ; mauvais mot de passe et e-mail inconnu
           confondus volontairement
     - [x] Déconnexion depuis l'accueil, provisoire jusqu'à l'écran du profil ; un invité confirme
     - [x] Build debug branché sur les émulateurs Firebase à la demande, dans une app Firebase à part
           (projet `demo-escapy`), avec ou sans `private/`
     - Vérifié de bout en bout contre les émulateurs sur le simulateur iOS et un émulateur Android
       (API 31) ; Firebase démarre sur Android avec le vrai projet. Non vérifié : un vrai appareil, le
       remplissage automatique par un gestionnaire de mots de passe.
   - 5b, **le profil** : écran du profil (pseudo, les 50 avatars en Compose Resources, avec un test
     qui les dessine tous), lier un compte invité à un e-mail (avec vérification de l'e-mail),
     changer d'e-mail et de mot de passe, supprimer son compte (reconnexion si demandée), à propos.
     **La déconnexion d'un invité supprime son compte** (`deleteAccount`) : sinon il resterait sur le
     serveur, sans personne pour s'y reconnecter.
   - 5c, **la connexion Google**, sur les deux plateformes : le jeton obtenu nativement (Credential
     Manager sur Android, GoogleSignIn sur iOS) derrière une interface commune, puis
     `GoogleAuthProvider.credential`. Demande d'activer le fournisseur Google dans la console Firebase,
     et sans doute de nouveaux fichiers de configuration dans `private/`.
6. **Parties** : créer (un vrai sélecteur de durée), rejoindre (code, QR code qui porte un lien
   universel, saisie manuelle), salle d'attente, exclure, quitter, liste en temps réel sur l'accueil.
   `minInstances` des fonctions de partie, avec son coût (une instance gardée chaude se facture
   aussi au repos).
7. **Partie en cours** : minuteur serveur (écart avec l'horloge du serveur estimé, voir
   [Common/02_CONVENTIONS.md](./Common/02_CONVENTIONS.md)), chat, intro, fin, victoire ou défaite.
8. **Missions** : types, déclencheurs, soumission avec anti-force brute, mots-clés du chat.
9. **Indices** : texte et image (Storage), avec la progression de l'envoi côté maître du jeu.
10. **Scénarios** : éditeur, expiration (TTL), créer une partie depuis un scénario.
11. **Finitions v1** : animations, accessibilité, langues, remontée des crashs, publication.
12. **v1.1 et au-delà** : indices audio, notifications push, journal, indices liés aux missions.

## Décisions prises

- **Kotlin Multiplatform et Compose Multiplatform** pour toute l'app, UI comprise
  ([ADR-0002](./adr/0002-ui-commune-en-compose-multiplatform.md)) ; `shared` → `domain` → `data`
  ([ADR-0001](./adr/0001-le-domaine-depend-de-la-donnee.md)).
- **Firebase en Kotlin commun par GitLive**, 3.0.0-beta1 épinglée, isolée dans `data`
  ([ADR-0003](./adr/0003-firebase-en-kotlin-commun-avec-gitlive.md)).
- **Firestore natif à `europe-west1`**, plutôt que la Realtime Database ; toute mutation par une
  callable ; le client ne valide rien
  ([ADR-0004](./adr/0004-firestore-et-le-serveur-source-de-verite.md)).
- **Pas de Room** : le cache hors ligne de Firestore suffit.
- **Connexion Google en v1** (décidé le 2026-11-24), au lot 5c.
- **`minInstances` sur les fonctions de partie** (validation d'une réponse, indice, ajout de temps,
  démarrage) : un démarrage à froid coûte de 1,3 à 2,3 s. Le coût se chiffre avec ces fonctions, au
  lot 6. Les fonctions du profil n'en ont pas.
- **Le dossier `firebase/` est dans ce dépôt** : app, règles et fonctions évoluent dans la même pull
  request, sous la même CI.
- **Profil créé par une callable idempotente** (`ensureProfile`), pas par un déclencheur Auth ;
  erreurs par une raison stable ; suppression du compte par une callable
  ([ADR-0005](./adr/0005-le-contrat-des-callables.md)).
- **Vocabulaire** : « partie » remplace « salon » (*lobby*) ; voir
  [Produit/01_VISION_ET_VOCABULAIRE.md](./Produit/01_VISION_ET_VOCABULAIRE.md).
- **Identité** : `fr.skyle.escapy` sur Android et iOS, version 2.0. iOS 18.5 minimum, iPhone en
  portrait. L'app est sombre seulement.
- **Builds debug Android** signés avec le keystore debug du sous-module privé : la clé API du projet
  Firebase n'accepte que les apps signées avec ses clés (voir
  [Common/04_BUILD.md](./Common/04_BUILD.md)).
- **Material 3** : 1.12.0-alpha03, l'alpha compilée sur Compose Multiplatform 1.12 (la dernière
  stable, 1.9.0, est compilée contre Compose 1.9). Seul `designSystem` en dépend.
- **Le site public** (`docs/index.md`, `docs/privacy-policy.html`) reste servi depuis `main:/docs`,
  à la même URL ; la documentation interne en est exclue.

## Décisions ouvertes

- **Présence en ligne** (« joueur connecté ») : Firestore ne la fournit pas. La Realtime Database
  pourrait servir à elle seule, si on la veut en v1.
- **Expiration des scénarios** : 24 ou 48 h (durée exacte à trancher), réglée côté serveur ; à vie pour les
  premiums, plus tard. Le TTL de Firestore supprime le document seul, sans ses sous-collections ni
  les fichiers de Storage, en général dans les 24 h (vérifié, voir
  [ADR-0004](./adr/0004-firestore-et-le-serveur-source-de-verite.md)) : suppression en cascade des
  médias par une fonction.
- **Remontée des crashs** : Crashlytics n'est pas encore branché. Sur iOS, il passera par le code
  Kotlin et le paquet de liaison, jamais par un paquet SwiftPM ajouté à la main (voir
  [iosApp/CLAUDE.md](../iosApp/CLAUDE.md)).
- **Mesure sur appareils réels** : un vrai téléphone en 4G avant la mise en production ; un vrai
  iPhone avec le premier build TestFlight.
- **Données de l'ancienne version** : Firebase Auth reste le même ; ce qui doit être repris d'autre
  reste à décider.
