## Vision et vocabulaire

Escapy permet de jouer un escape game à la maison, entre amis. Un maître du jeu prépare et pilote la
partie depuis son téléphone ; les joueurs, chacun sur le sien, résolvent des missions, reçoivent des
indices et lui écrivent, pendant que le minuteur tourne. L'énigme se joue « en vrai », dans la
pièce : l'app porte le temps, les réponses, les indices et la discussion.

### Vocabulaire

Ces termes nomment les mêmes choses dans le code (en anglais), dans les textes de l'app et dans la
documentation.

| Terme | Code | Définition |
|---|---|---|
| **Maître du jeu (MJ)** | `master` | Celui qui crée et pilote la partie. Il voit tout, réponses comprises. |
| **Joueur** | `player` | Participant invité dans une partie. |
| **Scénario** | `scenario` | La *préparation* d'un escape game, réutilisable : histoire d'intro, missions, indices préparés, durée. Gardé un temps limité. |
| **Partie** | `game` | Une *instance* jouée d'un scénario, ou improvisée sans scénario : des joueurs, un minuteur, un chat, l'état des missions. Remplace le terme « salon » (*lobby*) de l'ancienne app. |
| **Mission** | `mission` | Une épreuve que les joueurs résolvent dans l'app : saisir un code, un mot ou une phrase. Voir [02_MISSIONS.md](./02_MISSIONS.md). |
| **Déclencheur** | `trigger` | Ce qui rend une mission visible : manuel, au début de la partie, après une autre mission, mot-clé écrit dans le chat. |
| **Indice** | `hint` | Contenu envoyé par le MJ : texte, image, audio. Préparé dans le scénario ou improvisé. |
| **Code de partie** | `joinCode` | Les 6 caractères qui permettent de rejoindre une partie. Il est résolu par le serveur, dans un index que les clients ne lisent pas. |
| **Journal** | `events` | Le fil chronologique de la partie : début, missions débloquées ou réussies, indices envoyés, temps ajouté, fin. |
| **Invité** | `guest` (`isGuest`) | Un utilisateur sans e-mail, connecté anonymement. Il peut lier un compte plus tard. |

### Parcours du maître du jeu

1. **Préparer** (optionnel, en amont) : il crée un scénario, avec l'histoire d'intro (du texte, plus
   tard une image ou un audio d'ambiance), les missions avec leurs réponses et leurs déclencheurs, et
   les indices préparés. Le scénario expire au bout d'un temps réglé côté serveur.
2. **Créer la partie**, depuis un scénario ou de zéro : titre, durée, nombre maximum de joueurs
   (optionnel).
3. **Inviter** : code de partie, QR code, lien de partage.
4. **Salle d'attente** : il voit les joueurs arriver, peut en exclure, et lance la partie quand il
   veut.
5. **Pendant la partie**, un tableau de bord :
   - le minuteur, avec pause, reprise et ajout de temps ;
   - l'état de chaque mission (verrouillée, active, réussie) ; il peut en débloquer une à la main, ou
     la valider à la place des joueurs s'ils ont trouvé « en vrai » ;
   - l'envoi d'indices : préparés, en un geste, ou improvisés (texte, photo, galerie, audio) ;
   - le chat avec les joueurs, et la liste des joueurs, qu'il peut exclure ;
   - le journal de la partie.
6. **Fin** : victoire (les missions finales réussies), défaite (temps écoulé) ou fin manuelle.
   Un récapitulatif : temps, indices utilisés, journal.

### Parcours du joueur

1. Il rejoint par code, QR code ou lien, **sans compte obligatoire** (en invité).
2. Salle d'attente : il voit les autres joueurs et attend le lancement.
3. **Intro** : l'histoire s'affiche au lancement, en plein écran.
4. **Écran de jeu** : le minuteur toujours visible, les missions actives à résoudre (un champ de
   code ou de texte, un retour immédiat bon ou mauvais), les indices reçus (avec vibration), le chat.
5. **Fin** : un écran de succès ou de défaite, avec le récapitulatif.

### Versions

| Version | Contenu |
|---|---|
| **v1** | Parité avec l'ancienne app, sur Android et iOS. Parties : créer, rejoindre (code, QR, lien), salle d'attente, exclure, lancer. Minuteur serveur avec ajout de temps et pause. Chat. Intro en texte. Missions code et texte, avec les quatre déclencheurs. Indices texte et image. Fin, victoire ou défaite. Scénarios à durée de vie limitée. |
| **v1.1** | Indices audio (enregistrement et fichier). Notifications push. Journal complet côté MJ. Indices liés aux missions. |
| **v2** | Premium (scénarios sans expiration, médias plus lourds). Nouveaux types de mission. Mode écran partagé. Historique et statistiques. |

Idées non engagées : une pénalité de temps par indice et un score final ; un bouton « Demander un
indice » côté joueur ; une tablette posée dans la pièce qui affiche le minuteur en grand ; une
ambiance sonore diffusée par le MJ ; une bibliothèque de scénarios.
