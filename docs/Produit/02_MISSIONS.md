## Missions

Une mission est une épreuve que les joueurs résolvent dans l'app. Ce document fixe ses règles. Tout
est codé depuis le lot 8a, sauf le déclencheur par mot-clé du chat et le journal des tentatives,
qui arrivent au lot 8b (voir [../ROADMAP.md](../ROADMAP.md)). Le contrat des fonctions est dans
[../Backend/README.md](../Backend/README.md).

**Toute la logique d'une mission vit sur le serveur** (voir
[ADR-0004](../adr/0004-firestore-et-le-serveur-source-de-verite.md)) : la comparaison d'une réponse,
le déblocage, la détection des mots-clés du chat, la victoire. Le client envoie une tentative et
affiche ce que le serveur écrit. Il ne reçoit jamais une réponse attendue, et une mission verrouillée
n'existe pas pour lui : son document n'est créé, côté partie, qu'à son déblocage. Même un client
modifié ne peut donc ni lire une réponse ni voir une mission avant l'heure.

### Types

| Type | Saisie | Comparaison |
|---|---|---|
| `CODE` | De 3 à 8 chiffres, de longueur fixe, sur des molettes façon cadenas | Exacte, comme une chaîne : les zéros de tête comptent |
| `TEXT` | Un mot ou une phrase | Normalisée : sans casse, sans accents, sans ponctuation autour des mots, espaces réduits, apostrophes typographiques et ligatures ramenées |

- Une mission accepte **plusieurs réponses** (synonymes, orthographes), dix au plus ; les codes d'une
  mission ont tous la même longueur.
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

- Une mission n'en suit jamais une autre qui la suit déjà, même par d'autres : un cercle ne se
  débloquerait jamais. L'éditeur ne propose pas ces missions, le serveur les refuse.
- Le MJ crée et modifie les missions en salle d'attente comme pendant la partie (une mission
  improvisée). Une mission ajoutée ou modifiée en jeu se débloque aussitôt si son déclencheur est
  déjà passé (au lancement, ou après une mission déjà réussie).
- Supprimer une mission fait passer celles qui la suivaient en déblocage manuel.
- 30 missions par partie au plus.

### États

Une mission est **verrouillée** (elle n'existe pas encore côté partie), **active** (visible, à
résoudre) ou **réussie** (avec son heure, serveur, et qui l'a résolue).

### Mission finale

Une ou plusieurs missions sont marquées « finale ». Les réussir toutes déclenche la victoire, que
les joueurs les aient résolues ou que le MJ les ait validées. Seule une mission réussie fait gagner :
ni une modification ni une suppression. Une partie sans mission finale ne se gagne pas, elle finit
sur le temps ou à la main.

### Anti-force brute

10 réponses par minute et par mission au plus, tous joueurs confondus, côté serveur : une réponse
fausse compte. Une partie en pause refuse les réponses. Les tentatives apparaîtront dans le journal
du MJ (lot 8b).

### Latence

Une réponse validée apparaît chez tous les joueurs en une centaine de millisecondes quand la
fonction est chaude (voir [ADR-0003](../adr/0003-firebase-en-kotlin-commun-avec-gitlive.md)).
Pendant l'appel, l'écran montre que la réponse est en cours de vérification, sans préjuger du
résultat.

### Idées

- Un indice préparé rattaché à une mission : le MJ voit « mission bloquée depuis 10 min, envoyer
  l'indice ? ». Plus tard, un envoi automatique.
