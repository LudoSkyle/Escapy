# CLAUDE.md — backend Firebase

Périmètre : le backend (`firebase/`) — règles Firestore et Storage, index, Cloud Functions
(`functions/`, TypeScript), configuration de l'émulateur. Ne couvre pas l'app, qui appelle ce
backend depuis le module `data` (voir [../docs/KMP/01_MODULE_DATA.md](../docs/KMP/01_MODULE_DATA.md)).
Les principes et le contrat des fonctions sont dans [../docs/Backend/README.md](../docs/Backend/README.md).

## Règles

- **Le serveur est la source de vérité** ([ADR-0004](../docs/adr/0004-firestore-et-le-serveur-source-de-verite.md)).
  Toute écriture passe par une callable (`onCall`), avec le SDK Admin, que les règles n'arrêtent pas.
  Une callable commence par `requireUid(request)`, lit ses données par `requestData(request)` et
  vérifie tout ce qu'elle reçoit : un client peut envoyer n'importe quoi.
- **Erreurs** : une callable refuse avec `failure(code, reason, message)` (`src/core/errors.ts`), un
  code HTTPS et une raison stable dans `details.reason`, que l'app traduit en exception typée. Toute
  nouvelle raison s'ajoute au type `FailureReason` et au tableau de `docs/Backend/README.md`. Le
  message est en anglais, pour les logs ; il n'est jamais montré à l'utilisateur. Voir
  [ADR-0005](../docs/adr/0005-le-contrat-des-callables.md).
- **Idempotence** : une callable peut être rappelée (réseau coupé, nouvel essai). Une création passe
  par `create()` (qui échoue si le document existe) ou une transaction, jamais par une lecture suivie
  d'une écriture. Une suppression se relance sans erreur.
- **Compte supprimé** : le jeton d'un compte supprimé reste valable jusqu'à une heure. Une callable
  qui crée des données pour l'utilisateur appelle d'abord `requireExistingAccount(uid)`
  (`src/core/auth.ts`), sinon elle les créerait pour personne.
- **Horodatage serveur** (`FieldValue.serverTimestamp()`) pour toute heure stockée.
- **Aucune donnée personnelle superflue** : `users/{uid}` ne garde ni e-mail ni nom réel.
- **Règles** : tout est refusé par défaut (`match /{document=**}`), et chaque chemin ouvert l'est par
  un lot, avec ses tests qui couvrent l'accès permis et l'accès refusé (un autre utilisateur
  connecté, un client déconnecté). Aucune écriture client, sauf l'envoi d'un message de chat
  (`games/{gameId}/messages`), dont les règles vérifient chaque champ.
- **Région** `europe-west1` pour toutes les fonctions (`setGlobalOptions` dans `src/index.ts`), la
  région de la base.
- **Une fonction par fichier**, rangée par domaine (`src/profile/`, `src/account/`, `src/game/`),
  exportée par `src/index.ts`. Ce qui sert à plusieurs domaines vit dans `src/core/` ; ce qui touche
  aux données d'un domaine reste dans ce domaine, et les autres l'importent (`game/gameData.ts`, appelé
  par la suppression du compte et par `updateProfile`).
- **Tests** : chaque fonction et chaque règle se testent sur l'émulateur, dans `functions/test/`
  (vitest, un fichier par domaine). Une fonction s'appelle comme l'app l'appelle, par le SDK client
  (`newClient()` de `test/support/emulators.ts`), et son effet se vérifie par le SDK Admin. Une
  logique pure (une validation, un calcul d'heure) se teste aussi seule, sans émulateur.
- **L'émulateur tourne sur le projet `demo-escapy`** : un projet de démonstration, qui n'a besoin
  d'aucun identifiant et ne peut pas atteindre le vrai projet. Ni la CI ni les tests ne voient jamais
  le vrai projet.
- **Déploiement** : jamais depuis la CI, jamais sans la demande de l'utilisateur.
  `.firebaserc` n'a pas d'alias `default` : un `firebase deploy` sans `--project prod` échoue. Une
  fonction se supprime par `firebase functions:delete <nom> --project prod`, explicitement, jamais
  par `--force`.
- **Index** : une requête composée (égalité et plage, `array-contains` et tri) demande un index dans
  `firestore.indexes.json`, que l'émulateur n'exige pas ; il se déploie avec les règles
  (`--only firestore`). Une fonction planifiée (`onSchedule`) demande Cloud Scheduler, que le premier
  déploiement active sur le projet.
- `firebase-tools` est épinglé (14.26.0) dans les dépendances de développement : l'émulateur
  Firestore de la version suivante peut exiger un Java plus récent que le Java 17 du projet.

## Commandes

```bash
./scripts/emulators.sh                        # depuis la racine : émulateurs + ports Android (adb reverse)
cd firebase/functions && npm ci               # installer
cd firebase/functions && npm run typecheck    # types, tests compris
cd firebase/functions && npm test             # build, émulateurs, tests, arrêt
cd firebase/functions && npx firebase --config ../firebase.json deploy --only firestore,storage,functions --project prod   # sur demande seulement : règles et index Firestore compris
```

L'interface de l'émulateur s'ouvre sur http://localhost:4000.
