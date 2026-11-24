# ADR-0003 — Firebase s'appelle en Kotlin commun, avec le SDK de GitLive

Status: Accepted — 2026-11-24

> ADR **de projet**. Vaut pour tout le dépôt.

## Contexte

Escapy a besoin, sur Android et iOS, de Firebase Auth (invité, e-mail, lien de compte), de Firestore
(écoutes en temps réel), des Cloud Functions (callables) et de Storage (indices image et audio).
Deux voies :

- **les SDK natifs**, derrière des interfaces communes : deux implémentations à écrire, à tester et
  à maintenir pour chaque appel ;
- **le firebase-kotlin-sdk de GitLive**, qui enveloppe les SDK natifs dans une API commune, proche
  de celle du SDK Android. Seule sa ligne 3.x est compilée avec Kotlin 2.4 et `firebase-ios-sdk` 13
  (par Swift Package Manager), et elle n'existe qu'en bêta : 3.0.0-beta1.

Un spike, dans un projet jetable, a vérifié GitLive 3.0.0-beta1 avec la pile d'Escapy (Kotlin
2.4.21, AGP 9.5.2, Compose Multiplatform 1.12.2, Xcode 26.6 avec XcodeGen), sur un émulateur
Android (API 37) et sur le simulateur iOS 26.2, contre l'émulateur Firebase puis le vrai projet :

- connexion anonyme ; appel d'une callable avec requête et réponse `@Serializable` ; écriture et
  écoute Firestore avec `includeMetadataChanges` ; la chaîne « callable → écriture → listener », qui
  est le chemin d'une mission validée ; horodatage serveur ; Storage (envoi, URL, lecture,
  suppression, fichiers relus octet par octet) ;
- un code commun **identique sur les deux plateformes** ;
- un build Android release minifié par R8 (`isMinifyEnabled`, `isShrinkResources`) qui fonctionne
  **sans aucune règle ProGuard ajoutée**, avec les mêmes latences ;
- un build iOS pour appareil (`iosArm64`, Release) qui compile et se lie avec `firebase-ios-sdk`
  13.2.0 (non signé, non lancé sur un appareil) ;
- dans les sources de GitLive (lues, pas exécutées), toutes les opérations d'Auth nécessaires à la
  parité : `linkWithCredential`, `reauthenticate`, `verifyBeforeUpdateEmail`,
  `sendPasswordResetEmail`, `updatePassword`, `sendEmailVerification`, `delete`, `reload`,
  `isAnonymous`, `EmailAuthProvider`, `GoogleAuthProvider.credential`.

Mesures sur le vrai projet (Firestore et fonctions à `europe-west1`) : médianes de 10 essais, sauf
les premiers appels (une seule mesure chacun) et Storage (3 allers-retours, l'app Android en build
release).
**Réserve : émulateur Android et simulateur iOS tournaient sur un Mac en wifi domestique**, pas sur
de vrais téléphones en 4G.

| | Android (émulateur) | iOS (simulateur) |
|---|---|---|
| Connexion anonyme | 958 ms | 823 ms |
| Callable vide, 1ᵉʳ appel | 1 309 ms (démarrage à froid) | 249 ms (instance encore chaude) |
| Callable vide, appels suivants | 38 ms | 35 ms |
| Chat : affichage local | 37 ms | 5 ms |
| Chat : accusé du serveur | 123 ms | 101 ms |
| Callable qui écrit → listener | 105 ms (1ᵉʳ : 2 242 ms, à froid) | 100 ms (1ᵉʳ : 1 340 ms) |
| Storage : envoi de 1 Mo | 1 175 ms | 412 ms |
| Storage : réception de 1 Mo (URL et octets) | 321 ms | 307 ms |

Lecture :

- le chat est instantané à l'écran : Firestore affiche le message avant l'accusé du serveur, qui
  arrive en 100 à 120 ms ;
- une mission validée apparaît chez tous les joueurs en ~100 ms quand la fonction est chaude ;
- **le démarrage à froid coûte 1,3 s (fonction vide) à 2,3 s (fonction qui écrit dans Firestore)** :
  un joueur qui tape un code après vingt minutes de réflexion le subirait ;
- le premier message d'une session est plus lent (ouverture du flux Firestore) : 0,3 à 0,5 s.

## Décision

- **GitLive 3.0.0-beta1 est retenu**, version **épinglée** dans `gradle/libs.versions.toml`, avec ses
  modules `firebase-app`, `-auth`, `-firestore`, `-functions` et `-storage`. On passe à la 3.0.0
  stable dès sa sortie.
- **Tout Firebase est isolé dans `data`**, derrière les interfaces de Repository. Seule exception :
  `shared/iosMain` dépend de `firebase-app` pour démarrer Firebase au lancement de l'app iOS.
- **Plan B** : si GitLive bloque (bug sans contournement, abandon, retard sur une version de Kotlin
  ou du SDK iOS), `data` passe aux SDK natifs derrière les mêmes interfaces. Rien d'autre ne bouge.
- **`minInstances` sur les fonctions de partie** (validation d'une réponse, envoi d'un indice, ajout
  de temps, démarrage), pour éviter le démarrage à froid ; le coût se chiffre avec ces fonctions, au lot 6. En attendant
  la réponse, l'écran montre que la demande est en cours.
- **R8 sans règle** : `proguard-rules.pro` reste vide tant qu'une release minifiée fonctionne ;
  aucun code ne s'appuie sur un nom de classe, que R8 obfusque.
- **iOS : le paquet de liaison SwiftPM.** GitLive déclare sa dépendance à `firebase-ios-sdk` en
  métadonnées ; le plugin Kotlin en génère un paquet local, `iosApp/KotlinMultiplatformLinkedPackage`
  (`:shared:integrateLinkagePackage`). Il est déclaré dans `project.yml`, commité, régénéré après tout
  changement de module GitLive, et vérifié par la CI. Firebase n'est jamais déclaré une seconde fois
  dans le projet Xcode (symboles en double), et le framework `Shared` reste statique. Le détail est
  dans [iosApp/CLAUDE.md](../../iosApp/CLAUDE.md).

## Conséquences

- Un appel Firebase s'écrit et se teste une fois, en code commun.
- La bêta est un risque accepté : la version épinglée ne bouge que par une pull request qui
  revérifie les deux plateformes (connexion, callable, écoute, Storage, release minifiée).
- Une API que GitLive n'expose pas se cherche dans l'objet natif qu'il enveloppe, dans le source set
  de la plateforme de `data`, derrière la même interface.
- Restent à vérifier : un vrai iPhone, un vrai téléphone Android en 4G, Crashlytics.
- Le jeton de connexion Google reste natif (Credential Manager, GoogleSignIn), derrière une interface
  commune, si la connexion Google est retenue.
