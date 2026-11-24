## Backend

Le backend d'Escapy est Firebase : Auth, Firestore (natif, `europe-west1`), Cloud Functions (v2,
TypeScript, `europe-west1`) et Storage. **Le dossier `firebase/` (règles, index, fonctions,
configuration de l'émulateur) n'est pas encore dans le dépôt : il arrive au lot 3**, avec un
`firebase/CLAUDE.md` et le détail de ce document (modèle de données, fonctions, règles). Voir
[../ROADMAP.md](../ROADMAP.md).

En attendant, Firestore et Storage refusent tout accès client.

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
  partie en attente ou en cours, longueur bornée). La lecture d'une partie est réservée à ses
  membres ; les secrets (réponses, mots-clés), au maître du jeu et aux fonctions.
- **Toute heure est un horodatage serveur** : minuteur, messages, journal.
- Les règles sont versionnées dans le dépôt et testées sur l'émulateur, dans la CI.
- Les fonctions de partie gardent une instance chaude (`minInstances`) : un démarrage à froid coûte
  1 à 2 s ([ADR-0003](../adr/0003-firebase-en-kotlin-commun-avec-gitlive.md)).

### Modèle visé

À confirmer au lot 3 ; les noms deviendront ceux du code.

| Chemin | Contenu |
|---|---|
| `users/{uid}` | Profil public (pseudo, avatar), écrit par les fonctions |
| `scenarios/{id}` | Un scénario en un seul document (missions et indices embarqués), avec son expiration |
| `scenarioSecrets/{id}` | Les réponses du scénario, pour son propriétaire seulement |
| `games/{id}` | La partie : titre, maître du jeu, statut, minuteur (horodatages serveur), joueurs |
| `games/{id}/players`, `/missions`, `/hints`, `/messages`, `/events` | Joueurs, missions débloquées, indices envoyés, chat, journal |
| `games/{id}/private/secrets` | Réponses et mots-clés, pour le maître du jeu et les fonctions |
| `joinCodes/{code}` | Index des codes de partie, illisible par les clients |

### Émulateur Firebase

Le script `scripts/emulators.sh`, qui lancera l'émulateur et préparera les appareils, arrive au
lot 3. Ce qu'il devra porter :

- **Depuis Android**, l'hôte de l'émulateur est `localhost`, avec
  `adb reverse tcp:<port> tcp:<port>` pour chaque port (Auth, Firestore, Functions, Storage).
  L'adresse `10.0.2.2` est refusée avec `targetSdk` 37 (`FirebaseNetworkException`, alors que le
  port répond depuis `adb shell`).
- **En passant de l'émulateur au vrai projet**, se déconnecter d'abord : un utilisateur persisté par
  l'émulateur est renvoyé tel quel par `signInAnonymously`, puis son jeton est refusé par le vrai
  projet. L'émulateur ne sert qu'en debug.
- Les mesures prises sur l'émulateur prouvent qu'une chaîne fonctionne ; elles ne disent rien de la
  latence réelle.
