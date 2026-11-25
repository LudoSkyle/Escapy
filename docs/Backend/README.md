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

Tout autre chemin est refusé aux clients. Le modèle des parties, des scénarios et des codes de
partie s'ajoute ici avec le lot qui le crée (voir [ROADMAP.md](../ROADMAP.md)) : une partie et ses
sous-collections lisibles par ses membres, ses secrets par son maître du jeu, les codes dans un index
illisible par les clients.

Storage refuse tout accès client : les fichiers des indices ouvriront leurs chemins au lot des
indices.

### Fonctions

Toutes exigent un utilisateur connecté (sinon `unauthenticated`, raison `UNAUTHENTICATED`).

| Fonction | Données | Réponse | Rôle |
|---|---|---|---|
| `ensureProfile` | aucune | `{ username, avatarId }` | Crée le profil s'il n'existe pas, et le rend. Appelée par l'app après chaque connexion ; sans risque si elle est rappelée, ou appelée deux fois à la fois. Le pseudo par défaut est `Player_` suivi de six caractères, l'avatar est tiré au hasard |
| `updateProfile` | `{ username?, avatarId? }` | `{ username, avatarId }` | Change le pseudo, l'avatar, ou les deux |
| `deleteAccount` | aucune | `{ deleted: true }` | Supprime les données de l'utilisateur, puis son compte. Un membre doit s'être connecté il y a moins de 5 minutes ; un invité, qui n'a pas de mot de passe, n'en a pas besoin |

Un pseudo est nettoyé avant d'être gardé (espaces au bord retirés, espaces intérieurs réduits à un,
forme Unicode composée), puis doit faire de 3 à 20 caractères : lettres de toutes les langues,
chiffres, espaces, `_`, `-` et `.` entre eux.

### Erreurs

Une callable refuse avec un code HTTPS et une raison stable dans `details.reason`. L'app traduit la
raison en exception typée (voir [ADR-0005](../adr/0005-le-contrat-des-callables.md)).

| Raison | Code | Quand |
|---|---|---|
| `UNAUTHENTICATED` | `unauthenticated` | Aucun utilisateur connecté |
| `INVALID_REQUEST` | `invalid-argument` | Les données de l'appel ne sont pas un objet |
| `USERNAME_INVALID` | `invalid-argument` | Pseudo trop court, trop long, ou avec un caractère refusé |
| `AVATAR_INVALID` | `invalid-argument` | Avatar hors de 1 à 50, ou pas un entier |
| `NOTHING_TO_UPDATE` | `invalid-argument` | `updateProfile` sans pseudo ni avatar |
| `PROFILE_NOT_FOUND` | `not-found` | `updateProfile` avant `ensureProfile` |
| `RECENT_SIGN_IN_REQUIRED` | `failed-precondition` | `deleteAccount` d'un membre connecté depuis plus de 5 minutes : il se reconnecte, puis rappelle |

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
