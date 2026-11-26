## Backend

Le backend d'Escapy est Firebase : Auth, Firestore (natif, `europe-west1`), Cloud Functions (v2,
TypeScript, `europe-west1`) et Storage. Il vit dans `firebase/`, dans ce dépôt : app, règles et
fonctions évoluent dans la même pull request, sous la même CI. Les règles de travail sont dans
[firebase/CLAUDE.md](../../firebase/CLAUDE.md).

### Principes

Décidés par l'[ADR-0004](../adr/0004-firestore-et-le-serveur-source-de-verite.md) :

- **Le serveur est la source de vérité.** Un client modifié ne doit rien pouvoir tricher : ni lire
  une réponse, ni voir une mission verrouillée, ni se donner du temps, ni rejoindre une partie sans
  son code.
- **Toute mutation passe par une callable** (`onCall`), qui exige une authentification
  (`request.auth`) et vérifie le rôle (maître du jeu, joueur de la partie, propriétaire du
  scénario).
- **Le client ne valide rien.** La comparaison d'une réponse, le déblocage d'une mission, la
  détection d'un mot-clé du chat, la victoire, l'expiration d'un scénario : tout se décide dans une
  fonction (voir [Produit/02_MISSIONS.md](../Produit/02_MISSIONS.md)).
- **Les règles refusent les écritures client**, sauf, plus tard, l'envoi d'un message de chat
  (auteur = soi, partie en attente ou en cours, longueur bornée).
- **Toute heure est un horodatage serveur** : minuteur, messages, journal.
- Les fonctions de partie garderont une instance chaude (`minInstances`) : un démarrage à froid
  coûte de 1,3 à 2,3 s ([ADR-0003](../adr/0003-firebase-en-kotlin-commun-avec-gitlive.md)).

### Données

| Chemin | Contenu | Lu par | Écrit par |
|---|---|---|---|
| `users/{uid}` | `username`, `avatarId` (1 à 50), `createdAt`, `updatedAt` (horodatages serveur). Ni e-mail ni nom réel | son propriétaire | les fonctions |
| `games/{gameId}` | `title`, `durationMinutes`, `maxPlayers` (ou `null`), `status` (`WAITING`), `masterId`, `master` (`username`, `avatarId`), `memberIds` (le maître du jeu et les joueurs), `playerCount`, `joinCode`, `createdAt`, `updatedAt` | ses membres ; une partie qui n'existe pas se lit absente | les fonctions |
| `games/{gameId}/players/{uid}` | `username`, `avatarId`, `joinedAt` : un joueur, le maître du jeu à part | les membres de la partie | les fonctions |
| `games/{gameId}/private/access` | `excludedIds` : les joueurs exclus, que le code ne laisse plus entrer | personne | les fonctions |
| `joinCodes/{code}` | `gameId`, `createdAt` : l'index d'unicité des codes de partie | personne | les fonctions |
| `joinAttempts/{uid}` | `failures`, `windowStartedAt` : les codes inconnus essayés par un utilisateur | personne | les fonctions |

Tout autre chemin est refusé aux clients. Le modèle des scénarios s'ajoute ici avec le lot qui le
crée (voir [ROADMAP.md](../ROADMAP.md)).

- **Le nom et l'avatar** d'un membre sont copiés dans chaque partie (`master`, `players/{uid}`) : un
  membre ne lit pas le profil d'un autre. `updateProfile` les recopie dans les parties de
  l'utilisateur.
- **L'accueil** liste les parties de l'utilisateur par
  `where("memberIds", "array-contains", uid).orderBy("createdAt", "desc")`, que la règle accepte
  parce que la requête la garantit. Elle demande un index composé (`firestore.indexes.json`), comme la
  recherche des parties abandonnées : **l'émulateur n'en exige aucun**, un index oublié ne se voit
  donc qu'en production.
- **Un joueur exclu** perd la lecture de la partie : son écoute s'arrête sur un refus. Une partie
  supprimée, elle, se lit absente.
- **Le code** a 6 caractères tirés de `ABCDEFGHJKMNPQRSTUVWXYZ23456789` (ni 0, O, 1, I ni L). Il est
  libéré quand la partie est supprimée.

Storage refuse tout accès client : les fichiers des indices ouvriront leurs chemins au lot des
indices.

### Fonctions

Toutes exigent un utilisateur connecté (sinon `unauthenticated`, raison `UNAUTHENTICATED`).

| Fonction | Données | Réponse | Rôle |
|---|---|---|---|
| `ensureProfile` | aucune | `{ username, avatarId }` | Crée le profil s'il n'existe pas, et le rend. Appelée par l'app après chaque connexion ; sans risque si elle est rappelée, ou appelée deux fois à la fois. Le pseudo par défaut est `Player_` suivi de six caractères, l'avatar est tiré au hasard. Refuse un compte supprimé, dont le jeton reste valable jusqu'à une heure : l'app voit le profil disparaître pendant la suppression |
| `updateProfile` | `{ username?, avatarId? }` | `{ username, avatarId }` | Change le pseudo, l'avatar, ou les deux |
| `deleteAccount` | aucune | `{ deleted: true }` | Supprime les données de l'utilisateur, puis son compte : les parties qu'il mène sont supprimées, il quitte les autres. Un membre doit s'être connecté il y a moins de 5 minutes ; un invité, qui n'a pas de mot de passe, n'en a pas besoin |
| `createGame` | `{ title, durationMinutes, maxPlayers? }` | `{ gameId, joinCode }` | Crée une partie en attente, menée par l'utilisateur, avec un code libre. Titre de 3 à 40 caractères, durée de 15 à 180 minutes par pas de 5, maximum de 2 à 20 joueurs ou aucun (20 au plus). 5 parties menées à la fois au plus |
| `joinGame` | `{ code }` | `{ gameId }` | Fait entrer l'utilisateur dans la partie en attente d'un code, tapé sans souci de casse ni d'espaces. Rappelée par un membre, ou par le maître du jeu, elle rend la partie sans rien changer. 10 codes inconnus en 10 minutes bloquent l'utilisateur jusqu'à la fin de la fenêtre |
| `leaveGame` | `{ gameId }` | `{ left: true }` | Fait quitter une partie à un joueur. Une partie supprimée ou déjà quittée n'est pas une erreur |
| `kickPlayer` | `{ gameId, playerId }` | `{ kicked: true }` | Exclut un joueur, pour le maître du jeu : il quitte la partie et son code ne le laisse plus entrer |
| `cancelGame` | `{ gameId }` | `{ cancelled: true }` | Supprime la partie pour tous, pour le maître du jeu, et libère son code. Une partie déjà supprimée n'est pas une erreur |

Une fonction planifiée, `deleteStaleGames`, supprime toutes les heures les parties encore en attente
24 h après leur création, avec leurs codes.

Aucune fonction n'a encore d'instance gardée chaude (`minInstances`) : créer ou rejoindre une partie
se fait avant de jouer, où un démarrage à froid se tolère. La décision vient avec les fonctions de la
partie en cours (lot 7).

Un pseudo est nettoyé avant d'être gardé (espaces au bord retirés, espaces intérieurs réduits à un,
forme Unicode composée), puis doit faire de 3 à 20 caractères : lettres de toutes les langues,
chiffres, espaces, `_`, `-` et `.` entre eux.

### Erreurs

Une callable refuse avec un code HTTPS et une raison stable dans `details.reason`. L'app traduit la
raison en exception typée (voir [ADR-0005](../adr/0005-le-contrat-des-callables.md)).

| Raison | Code | Quand |
|---|---|---|
| `UNAUTHENTICATED` | `unauthenticated` | Aucun utilisateur connecté, ou son compte supprimé (`ensureProfile`) |
| `INVALID_REQUEST` | `invalid-argument` | Les données de l'appel ne sont pas un objet |
| `USERNAME_INVALID` | `invalid-argument` | Pseudo trop court, trop long, ou avec un caractère refusé |
| `AVATAR_INVALID` | `invalid-argument` | Avatar hors de 1 à 50, ou pas un entier |
| `NOTHING_TO_UPDATE` | `invalid-argument` | `updateProfile` sans pseudo ni avatar |
| `PROFILE_NOT_FOUND` | `not-found` | `updateProfile`, `createGame` ou `joinGame` avant `ensureProfile` |
| `RECENT_SIGN_IN_REQUIRED` | `failed-precondition` | `deleteAccount` d'un membre connecté depuis plus de 5 minutes : il se reconnecte, puis rappelle |
| `GAME_TITLE_INVALID` | `invalid-argument` | Titre trop court, trop long, ou avec un caractère de contrôle |
| `GAME_DURATION_INVALID` | `invalid-argument` | Durée hors de 15 à 180 minutes, ou pas un multiple de 5 |
| `MAX_PLAYERS_INVALID` | `invalid-argument` | Maximum de joueurs hors de 2 à 20 |
| `TOO_MANY_GAMES` | `resource-exhausted` | Une sixième partie menée à la fois |
| `JOIN_CODE_INVALID` | `invalid-argument` | Un code qui ne peut pas en être un (longueur, caractère hors de l'alphabet) |
| `TOO_MANY_ATTEMPTS` | `resource-exhausted` | Trop de codes inconnus essayés : l'utilisateur réessaie plus tard |
| `GAME_NOT_FOUND` | `not-found` | Aucune partie n'a ce code, ou la partie a été supprimée |
| `GAME_NOT_JOINABLE` | `failed-precondition` | La partie n'attend plus de joueurs |
| `GAME_FULL` | `failed-precondition` | La partie a atteint son maximum de joueurs |
| `PLAYER_EXCLUDED` | `permission-denied` | Le maître du jeu a exclu cet utilisateur de la partie |
| `NOT_GAME_MASTER` | `permission-denied` | Exclure un joueur ou annuler la partie, sans en être le maître du jeu |
| `GAME_MASTER_CANNOT_LEAVE` | `failed-precondition` | Le maître du jeu quitte sa partie : il l'annule à la place |

### Émulateur Firebase

`scripts/emulators.sh`, depuis la racine du dépôt, construit les fonctions et lance les émulateurs
(Auth 9099, Firestore 8080, Functions 5001, Storage 9199, interface 4000) sur le projet de
démonstration `demo-escapy`, qui n'a besoin d'aucun identifiant et ne peut pas atteindre le vrai
projet.

- **Depuis Android**, l'hôte de l'émulateur est `localhost` : le script fait
  `adb reverse tcp:<port> tcp:<port>` pour chaque port et chaque appareil connecté. L'adresse
  `10.0.2.2` est refusée avec `targetSdk` 37 (`FirebaseNetworkException`, alors que le port répond
  depuis `adb shell`).
- **En passant de l'émulateur au vrai projet**, se déconnecter d'abord : un utilisateur persisté par
  l'émulateur est renvoyé tel quel par `signInAnonymously`, puis son jeton est refusé par le vrai
  projet.
- **L'app sur les émulateurs**, en debug seulement et à la demande :
  `./gradlew :androidApp:installDebug -Pescapy.firebaseEmulator=true` sur Android, la variable
  d'environnement `ESCAPY_FIREBASE_EMULATOR=1` sur iOS
  (`SIMCTL_CHILD_ESCAPY_FIREBASE_EMULATOR=1 xcrun simctl launch booted fr.skyle.escapy`, ou le schéma
  Xcode). L'app crée alors une app Firebase à part, nommée `emulator`, sur `demo-escapy` : avec ou
  sans `private/`, sa session et son cache ne se mêlent jamais à ceux du vrai projet (voir
  `FirebaseServices.kt`). Un build release ne le peut pas.
- La clé API de cette app est fausse mais a le format d'une vraie : le SDK Functions d'Android
  refuse une clé qui ne commence pas par `AIza`.
- Un build debug Android autorise le texte en clair vers `localhost` seulement
  (`androidApp/src/debug/res/xml/network_security_config.xml`), iOS le réseau local
  (`NSAllowsLocalNetworking`).
- Les e-mails envoyés par l'émulateur Auth (réinitialisation, vérification) ne partent pas : leurs
  codes se lisent sur `http://localhost:9099/emulator/v1/projects/demo-escapy/oobCodes`.
- Les mesures prises sur l'émulateur prouvent qu'une chaîne fonctionne ; elles ne disent rien de la
  latence réelle.

### Tests et CI

`npm test`, dans `firebase/functions`, construit les fonctions, lance les émulateurs, exécute les
tests (vitest, `functions/test/`) et arrête les émulateurs. La CI le lance dans un job à part
(`backend`, sur Ubuntu, Node 24 et Java 17), à chaque pull request.

### Déploiement

Jamais depuis la CI, jamais sans la demande de l'utilisateur, et seulement avec `--project prod`
(voir [firebase/CLAUDE.md](../../firebase/CLAUDE.md)).
