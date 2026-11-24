# ADR-0005 — Le contrat des callables : erreurs, profil, suppression du compte

Status: Accepted — 2026-11-24

> ADR **de projet**. Vaut pour les Cloud Functions (`firebase/functions`) et pour le module `data`
> de l'app, qui les appelle.

## Contexte

Toute mutation passe par une callable ([ADR-0004](./0004-firestore-et-le-serveur-source-de-verite.md)).
L'app doit donc savoir pourquoi un appel a été refusé, pour afficher le bon message ou proposer la
bonne action, sans dépendre d'un texte anglais qui changerait. Côté client, le SDK Kotlin lève une
`FirebaseFunctionsException` qui porte un `code` (les codes canoniques de Google) et les `details`
envoyés par la fonction.

Le profil d'un utilisateur doit exister dès sa connexion. Créé par l'app juste après l'inscription,
il pouvait manquer (écriture échouée ou expirée) et laisser un compte connecté sans profil. Un
déclencheur Auth (`onUserCreated`, que Cloud Functions v2 propose désormais sans Identity Platform)
s'exécute après coup, de façon asynchrone : l'app peut lire avant qu'il ait écrit. Les déclencheurs
*bloquants* (`beforeUserCreated`) exigeraient de passer à Identity Platform.

Enfin, supprimer un compte avec le SDK Admin ne demande pas de connexion récente, alors que le SDK
client l'exige d'un membre. Un invité, lui, ne peut pas se reconnecter : il n'a pas de mot de passe.

## Décision

- **Une callable refuse avec un code HTTPS et une raison stable** dans `details.reason`, en
  majuscules (`USERNAME_INVALID`). Le module `data` traduit chaque raison en une exception typée
  `<Problème>Exception` ; une raison inconnue, ou une erreur sans raison, devient une erreur
  générique. Le message reste en anglais, pour les logs. Les raisons sont listées dans
  [Backend/README.md](../Backend/README.md).
- **Le profil est créé par la callable `ensureProfile`**, que l'app appelle après chaque connexion
  et attend avant d'ouvrir l'accueil. Elle crée le document par `create()`, qui échoue s'il existe :
  elle est idempotente, et deux appels simultanés gardent le même profil. Aucun déclencheur Auth.
- **Le profil ne garde aucune donnée personnelle superflue** : ni e-mail (Firebase Auth l'a déjà),
  ni nom réel. Il n'y a donc pas d'e-mail à synchroniser quand il change.
- **Pseudo par défaut** : `Player_` suivi de six caractères tirés au hasard, pour un invité comme
  pour un membre ; il se change par `updateProfile`. Rien ne trahit dans le pseudo qu'un joueur est
  invité, et lier un compte n'a pas à le renommer.
- **La suppression du compte passe par la callable `deleteAccount`** : elle supprime les données de
  l'utilisateur, puis son compte Auth. Elle se relance sans erreur si elle a échoué entre les deux.
  Elle exige d'un membre une connexion de moins de 5 minutes (`auth_time` du jeton), comme le SDK
  client : sinon `RECENT_SIGN_IN_REQUIRED`, et l'app le fait se reconnecter. Un invité en est
  dispensé.
- Chaque nouvelle donnée d'un utilisateur (parties dont il est maître du jeu ou joueur, scénarios,
  fichiers) ajoute sa suppression à `deleteUserData`, dans le lot qui la crée.

## Conséquences

- L'app ne compare jamais un message d'erreur : elle lit une raison, et une raison ajoutée côté
  serveur est une modification des deux côtés, dans la même pull request.
- La première connexion coûte un appel à `ensureProfile` avant l'accueil, démarrage à froid compris.
- Un compte supprimé hors de l'app (console Firebase) laisse son profil : un déclencheur
  `onUserDeleted` pourra le nettoyer si le cas se présente.
- Un invité perd tout en se déconnectant ou en supprimant son compte : l'app l'en avertit et lui
  propose de lier un compte (lot 5).
