## Missions

Une mission est une épreuve que les joueurs résolvent dans l'app. Ce document fixe ses règles. Rien
de ce qui suit n'est encore codé : les missions arrivent au lot 8 (voir
[../ROADMAP.md](../ROADMAP.md)).

**Toute la logique d'une mission vit sur le serveur** (voir
[ADR-0004](../adr/0004-firestore-et-le-serveur-source-de-verite.md)) : la comparaison d'une réponse,
le déblocage, la détection des mots-clés du chat, la victoire. Le client envoie une tentative et
affiche ce que le serveur écrit. Il ne reçoit jamais une réponse attendue, et une mission verrouillée
n'existe pas pour lui : son document n'est créé, côté partie, qu'à son déblocage. Même un client
modifié ne peut donc ni lire une réponse ni voir une mission avant l'heure.

### Types

| Type | Saisie | Comparaison |
|---|---|---|
| `CODE` | Des chiffres, de longueur fixe, sur un pavé façon cadenas | Exacte |
| `TEXT` | Un mot ou une phrase | Normalisée : sans casse, sans accents, espaces réduits |

- Une mission accepte **plusieurs réponses** (synonymes, orthographes).
- Le client peut vérifier une saisie pour l'ergonomie (la longueur d'un code, un champ vide), jamais
  décider qu'elle est juste.
- Plus tard : choix multiple, scan d'un QR code physique caché dans la pièce, photo validée par le MJ.

### Déclencheurs

| Déclencheur | La mission devient visible… |
|---|---|
| `MANUAL` | quand le MJ la débloque |
| `ON_START` | au lancement de la partie |
| `AFTER_MISSION(missionId)` | quand la mission ciblée est réussie |
| `CHAT_KEYWORD(mots)` | quand un des mots ou une des phrases est écrit dans le chat (même normalisation que `TEXT`) |

Le MJ peut toujours débloquer une mission à la main, quel que soit son déclencheur, et la valider à
la place des joueurs s'ils ont trouvé « en vrai ».

### États

Une mission est **verrouillée** (elle n'existe pas encore côté partie), **active** (visible, à
résoudre) ou **réussie** (avec son heure, serveur, et qui l'a résolue).

### Mission finale

Une ou plusieurs missions sont marquées « finale ». Les réussir toutes déclenche la victoire.

### Anti-force brute

Le nombre de tentatives est limité par minute et par mission, côté serveur. Les tentatives
apparaissent dans le journal du MJ.

### Latence

Une réponse validée apparaît chez tous les joueurs en une centaine de millisecondes quand la
fonction est chaude (voir [ADR-0003](../adr/0003-firebase-en-kotlin-commun-avec-gitlive.md)).
Pendant l'appel, l'écran montre que la réponse est en cours de vérification, sans préjuger du
résultat.

### Idées

- Un indice préparé rattaché à une mission : le MJ voit « mission bloquée depuis 10 min, envoyer
  l'indice ? ». Plus tard, un envoi automatique.
