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
- **Les règles refusent les écritures client**, sauf l'envoi d'un message de chat (auteur = soi,
  avec le pseudo et l'avatar de son profil, partie en cours ou en pause avant son heure de fin,
  longueur bornée ; voir le chat plus bas).
- **Toute heure est un horodatage serveur** : minuteur, messages, journal.
- Une instance chaude (`minInstances`) pour les fonctions de partie se décide avec leur coût : un
  démarrage à froid coûte de 1,3 à 2,3 s ([ADR-0003](../adr/0003-firebase-en-kotlin-commun-avec-gitlive.md)).
  Aucune n'en a encore (voir la fin de la section des fonctions).

### Données

| Chemin | Contenu | Lu par | Écrit par |
|---|---|---|---|
| `users/{uid}` | `username`, `avatarId` (1 à 50), `createdAt`, `updatedAt` (horodatages serveur). Ni e-mail ni nom réel | son propriétaire | les fonctions |
| `users/{uid}/games/{gameId}` | `title`, `isMaster`, `master` (`username`, `avatarId`), `status`, `createdAt` : la liste des parties de l'utilisateur, pour son accueil. Une fiche écrite avant le minuteur n'a pas de `status` : elle est d'une partie en attente | son propriétaire | les fonctions |
| `games/{gameId}` | `title`, `durationMinutes`, `maxPlayers` (ou `null`), `status` (`WAITING`, `RUNNING`, `PAUSED`, `FINISHED`), `masterId`, `master` (`username`, `avatarId`), `memberIds` (le maître du jeu et les joueurs), `playerCount`, `joinCode`, `createdAt`, `updatedAt` ; une fois lancée : `startedAt`, `endsAt` (tant qu'elle tourne), `remainingMs` (en pause, et à la fin), `addedMinutes` ; à la fin : `result` (`TIME_UP`, `ENDED_BY_MASTER`), `playedMs` (le temps joué, pauses à part), `finishedAt` | ses membres ; une partie qui n'existe pas se lit absente | les fonctions |
| `games/{gameId}/messages/{messageId}` | `authorId`, `author` (`username`, `avatarId`, recopiés du profil), `text` (de 1 à 500 unités UTF-16, pas que des espaces), `sentAt` (horodatage serveur) : un message du chat | les membres de la partie | un membre, en son nom, partie en cours ou en pause avant son heure de fin ; jamais modifié ni supprimé |
| `games/{gameId}/players/{uid}` | `username`, `avatarId`, `joinedAt` : un joueur, le maître du jeu à part | les membres de la partie | les fonctions |
| `games/{gameId}/private/access` | `excludedIds` : les joueurs exclus, que le code ne laisse plus entrer | personne | les fonctions |
| `joinCodes/{code}` | `gameId`, `createdAt` : l'index d'unicité des codes de partie | personne | les fonctions |
| `joinAttempts/{uid}` | `failures`, `windowStartedAt` : les codes inconnus essayés par un utilisateur | personne | les fonctions |

Tout autre chemin est refusé aux clients. Le modèle des scénarios s'ajoute ici avec le lot qui le
crée (voir [ROADMAP.md](../ROADMAP.md)).

- **Le nom et l'avatar** d'un membre sont copiés dans chaque partie (`master`, `players/{uid}`) et
  dans les fiches de ses membres : un membre ne lit pas le profil d'un autre. `updateProfile` les
  recopie.
- **L'accueil** lit la liste de l'utilisateur, `users/{uid}/games`, que les fonctions tiennent à
  chaque création, entrée, sortie, exclusion et suppression : des documents qu'il lit toujours. Une
  requête sur les parties (`memberIds` contient l'utilisateur) garderait une partie dont il vient
  d'être exclu : le SDK Android la laisse dans son cache tant que la requête ne change pas.
- La recherche des parties abandonnées (`status` et `createdAt`, `status` et `updatedAt`) et le
  compte des parties menées non terminées (`masterId` et `status`) ont leurs index composés
  (`firestore.indexes.json`) : **l'émulateur n'en exige aucun**, un index oublié ne se voit donc
  qu'en production.
- **Un joueur exclu** perd la lecture de la partie : son écoute s'arrête sur un refus. Une partie
  supprimée, elle, se lit absente.
- **Le minuteur** : une partie qui tourne garde l'heure de sa fin (`endsAt`, heure du serveur), une
  partie en pause le temps qui lui reste (`remainingMs`). Une partie qui tourne dont l'heure de fin
  est passée est **terminée pour toutes les fonctions**, même avant qu'un appel ne l'écrive : chacune
  la termine d'abord (`TIME_UP`), dans sa transaction (`onGame`, `game/timer.ts`). Un toucher juste
  après zéro ne prolonge donc jamais une partie que les joueurs voient perdue.
- **Le temps écoulé** s'écrit par `expireGame`, que l'app de chaque membre appelle quand son
  minuteur atteint zéro ; le serveur décide par sa propre horloge. Sans aucune app ouverte, la
  partie reste écrite en cours, mais toute fonction la voit terminée.
- **Le chat** est la seule écriture des clients ([ADR-0004](../adr/0004-firestore-et-le-serveur-source-de-verite.md)).
  Un message porte le pseudo et l'avatar de son auteur tels qu'il les avait en l'écrivant : les
  règles les comparent au profil, `updateProfile` ne réécrit pas les anciens messages, et un profil
  en cache périmé fait refuser l'envoi. La longueur se compte en unités UTF-16, comme
  `String.length` en Kotlin (un emoji compte deux), vérifié par un test des règles. La partie doit
  être en cours ou en pause, et une partie dont l'heure de fin est passée est fermée, comme pour les
  fonctions. Les messages partent avec leur partie (`deleteGame`). La détection des mots-clés pour
  les missions viendra au lot 8, côté serveur.
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
| `cancelGame` | `{ gameId }` | `{ cancelled: true }` | Supprime la partie pour tous, pour le maître du jeu, et libère son code, quel que soit son état (une partie terminée aussi). Une partie déjà supprimée n'est pas une erreur |
| `startGame` | `{ gameId }` | `{ status, remainingMs }` | Lance une partie en attente, pour le maître du jeu, même sans joueur : le minuteur part de la durée, la partie ne prend plus personne. Rappelée, elle laisse une partie lancée telle quelle |
| `pauseGame` | `{ gameId }` | `{ status, remainingMs }` | Met en pause une partie qui tourne, pour le maître du jeu, en gardant le temps qui reste. Sans effet sur une partie en pause ou terminée |
| `resumeGame` | `{ gameId }` | `{ status, remainingMs }` | Relance une partie en pause, pour le maître du jeu, du temps qui lui restait. Sans effet sur une partie qui tourne ou terminée |
| `addTime` | `{ gameId, minutes, addedMinutes }` | `{ addedMinutes, remainingMs }` | Ajoute de 1 à 60 minutes à une partie lancée, 180 en tout, pour le maître du jeu. `addedMinutes` est ce que l'app voyait déjà ajouté : un appel renvoyé après une réponse perdue le trouve changé, et n'ajoute rien une seconde fois |
| `endGame` | `{ gameId }` | `{ status, remainingMs }` | Termine une partie lancée, pour le maître du jeu (`ENDED_BY_MASTER`) ; une partie dont le temps était écoulé reste terminée sur le temps. Sans effet sur une partie terminée |
| `expireGame` | `{ gameId }` | `{ status, remainingMs }` | Pour tout membre : termine une partie dont le temps est écoulé, à l'heure du serveur. Une partie qui n'en est pas là reste telle quelle, et la réponse dit le temps qui lui reste |
| `getServerTime` | aucune | `{ nowMs }` | L'heure du serveur, d'où l'app estime l'écart avec l'horloge de l'appareil |

Une fonction planifiée, `deleteStaleGames`, supprime toutes les heures les parties encore en attente
24 h après leur création, et les parties lancées sans changement depuis 24 h (une partie terminée,
24 h après sa fin), avec leurs codes. Une partie terminée ne compte plus dans les 5 parties menées à
la fois.

Aucune fonction n'a encore d'instance gardée chaude (`minInstances`). Créer ou rejoindre une partie
se fait avant de jouer ; les fonctions du minuteur, au lot 7a, sont appelées par le maître du jeu
(lancer, pause, temps ajouté, fin) ou en tâche de fond par l'app (`expireGame`, `getServerTime`,
dont l'estimation écarte une réponse lente). Le coût d'une instance chaude se décide avec
l'utilisateur, et au plus tard avec la validation des réponses (lot 8), qu'un joueur attend.

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
| `NOT_GAME_MASTER` | `permission-denied` | Exclure un joueur, annuler la partie ou toucher à son minuteur, sans en être le maître du jeu |
| `GAME_MASTER_CANNOT_LEAVE` | `failed-precondition` | Le maître du jeu quitte sa partie : il l'annule à la place |
| `NOT_GAME_MEMBER` | `permission-denied` | `expireGame` par un utilisateur qui n'est pas membre de la partie (un joueur exclu) |
| `GAME_NOT_STARTED` | `failed-precondition` | Pause, reprise, temps ajouté ou fin d'une partie pas encore lancée |
| `GAME_FINISHED` | `failed-precondition` | Temps ajouté à une partie terminée, ou dont le temps vient de s'écouler |
| `ADDED_TIME_INVALID` | `invalid-argument` | Moins de 1 ou plus de 60 minutes à la fois, ou plus de 180 en tout |

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
