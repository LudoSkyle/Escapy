# ADR-0004 — Firestore, et le serveur comme source de vérité

Status: Accepted — 2026-11-24

> ADR **de projet**. Vaut pour l'app et pour le backend (`firebase/`, au lot 3).

## Contexte

Dans un escape game, les réponses doivent rester secrètes jusqu'à ce que les joueurs les trouvent,
et le temps doit être le même pour tous. Or un client mobile ne se contrôle pas : une app modifiée,
ou un appel direct à la base avec le jeton d'un joueur, peut lire tout ce que les règles lui
laissent lire et écrire tout ce qu'elles lui laissent écrire. Une vérification faite dans l'app ne
protège rien. Et l'horloge d'un téléphone n'est pas fiable : un émulateur Android a été mesuré avec
19 s de retard sur le serveur.

Pour la base, deux options Firebase :

| Critère | Firestore | Realtime Database |
|---|---|---|
| Expiration des scénarios | TTL natif sur un champ d'expiration (le document seul, avec un délai) | Une fonction planifiée qui nettoie tout |
| Règles de sécurité | Plus expressives, testables sur l'émulateur | Plus limitées sur les structures imbriquées |
| Requêtes (mes parties, historique) | Index composés, pagination | Dénormalisation à la main |
| Chat en temps réel | Écoute d'une sous-collection, ~100 ms d'accusé mesuré | Un peu plus vive, imperceptible dans un chat |
| Présence en ligne | Pas native | Native (`onDisconnect`) |
| SDK Kotlin de GitLive | Oui | Oui |

Le spike de l'[ADR-0003](./0003-firebase-en-kotlin-commun-avec-gitlive.md) a montré que la base
n'est pas le goulot : une mission validée est la chaîne « callable → écriture → listener », dont le
coût est le démarrage à froid de la fonction, quelle que soit la base.

## Décision

- **Firestore**, base `(default)` en mode natif, à **`europe-west1`**, la région des Cloud Functions.
  Ce choix de région est définitif à la création de la base. La Realtime Database ne reviendrait que
  pour la présence en ligne, si on la veut.
- **Toute mutation de l'état d'une partie, d'un profil ou d'un scénario passe par une callable**
  (`onCall`), qui exige `request.auth` et vérifie le rôle. Le client ne valide rien : il n'a jamais
  une réponse attendue, il ne débloque jamais une mission, il ne décide jamais d'une victoire ni du
  temps restant.
- **Les règles refusent toute écriture client, sauf l'envoi d'un message de chat** : auteur = soi,
  partie en attente ou en cours, longueur bornée. La lecture d'une partie et de ses sous-collections
  est réservée à ses membres ; les secrets (réponses, mots-clés), au maître du jeu et aux fonctions.
  Une mission verrouillée n'a pas de document côté partie : il n'est créé qu'à son déblocage.
- **Le code de partie vit dans un index d'unicité**, illisible par les clients, et se résout par la
  fonction qui fait rejoindre : un client ne peut ni lister les codes ni lire une partie dont il
  n'est pas membre.
- **Horodatage serveur** pour toute heure qui compte : messages, déblocages, minuteur (début, pause,
  temps ajouté). Le minuteur se calcule à partir de ces horodatages, corrigés de l'écart estimé entre
  l'appareil et le serveur.
- **Storage** : le bucket par défaut existe, en mode production (règles fermées). Les fichiers d'un
  indice seront lisibles par les membres de la partie et écrits par son maître du jeu. **Son
  emplacement exact reste à consigner ici** (lot 3).
- Les règles de Firestore et de Storage sont versionnées dans le dépôt et testées sur l'émulateur
  (`@firebase/rules-unit-testing`), dans la CI, à partir du lot 3. D'ici là, elles refusent tout
  accès client.

## Conséquences

- Un client modifié ne peut ni lire une réponse, ni voir une mission avant l'heure, ni se donner du
  temps, ni écrire autre chose qu'un message à son nom.
- Chaque action de jeu coûte un aller-retour à une fonction : `minInstances` sur les fonctions de
  partie (ADR-0003), et un écran qui montre l'attente sans préjuger du résultat.
- Le cache hors ligne de Firestore tient lieu de base locale : pas de Room.
- L'expiration par TTL ne supprime que le document, ni ses sous-collections ni ses fichiers, avec
  un délai qui peut aller jusqu'à environ 24 h : un scénario reste un seul document, et une fonction
  déclenchée par sa suppression nettoie ses fichiers (à confirmer au lot 3).
- Les principes sont repris dans [Backend/README.md](../Backend/README.md), et leur application côté
  app dans [KMP/01_MODULE_DATA.md](../KMP/01_MODULE_DATA.md).
