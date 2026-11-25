## Tests

Les ADR communs 0001 (FIRST) et 0002 (le code livré vient avec ses tests) du référentiel partagé
s'appliquent : tout changement qui introduit ou déplace de la logique est livré avec ses tests, dans
le même commit. Ce document dit comment les écrire dans Escapy.

Tout est testé en **test unitaire**, sans appareil ni émulateur : `./gradlew :<module>:allTests`
exécute la suite sur la JVM et sur le simulateur iOS.

| Source set | Code testé | Exécuté sur |
|---|---|---|
| `src/commonTest` | `commonMain` | la JVM (tests hôtes Android) **et** le simulateur iOS |
| `src/androidHostTest` | `androidMain` | la JVM |
| `src/iosTest` | `iosMain` | le simulateur iOS (`iosSimulatorArm64Test`, Mac avec Xcode) |

Un test de code commun va dans `commonTest` : c'est ce qui vérifie que le code se comporte pareil sur
les deux plateformes. Les tests en place sont tous dans `commonTest` : `SessionRepositoryImplTest`
(`data`), `WatchSessionUseCaseImplTest` (`domain`), `MainViewModelTest` (`shared`), et ceux des
composants et des contrastes du design system (`designSystem`).

### Outillage

| Besoin | Outil |
|---|---|
| Framework | `kotlin.test` (`@Test`, `assertEquals`, `assertFailsWith`) |
| Coroutines | `kotlinx-coroutines-test` (`runTest`, `UnconfinedTestDispatcher`) |
| Doublures | Fakes écrits à la main, dans un package `fake/` du source set de test |
| Composants et écrans | `runComposeUiTest` (`org.jetbrains.compose.ui:ui-test`) dans `commonTest`, sur la JVM par Robolectric et sur le simulateur iOS tel quel |

Pas de framework de mock : les interfaces sont petites, un fake qui enregistre les appels est plus
lisible et ne casse pas au premier refactor.

### Que tester, et à quel niveau

| Niveau | Testé avec | Ce qu'on vérifie |
|---|---|---|
| Mapper (`data`, `domain`) | `kotlin.test` seul | chaque cas du modèle source, les champs absents, les valeurs inconnues |
| UseCase (`domain`) | fakes des Repositories de `data` | orchestration, conversion, exceptions typées levées |
| Repository (`data`) | voir « Firebase dans les tests » | ce qui ne dépend pas d'un appel réel : repli sans Firebase, conversion des documents et des erreurs |
| ViewModel (`shared`) | fakes des UseCases et des interfaces natives | état exposé, events émis, erreurs attrapées |
| Composant du design system | `runComposeUiTest` | ce qu'il promet : un bouton en chargement ignore le toucher, un champ dit son erreur, un bouton icône est nommé |
| Contrastes des couleurs | `kotlin.test` seul (`ESColorsContrastTest`) | chaque couple texte/surface à 4,5:1, chaque contour de contrôle à 3:1 |
| Règles Firestore et Storage, fonctions | émulateur Firebase, `firebase/functions/test/` (vitest) | qui peut lire et écrire quoi ; ce que fait chaque fonction, ses refus compris |

- **Tout UseCase, Repository et ViewModel est livré avec son fichier de test**, nommé comme son
  implémentation et suffixé `Test` (`WatchSessionUseCaseImplTest`), dans le même package du source
  set de test.
- Une `Preview` n'est pas un test.
- **Un test qui dessine** (`runComposeUiTest`) hérite de `ComposeTest`, une classe `expect` du
  source set de test : sur la JVM, elle lance Robolectric (`@RunWith(RobolectricTestRunner::class)`,
  graphismes natifs, `robolectric.properties` dans `androidHostTest/resources`, et
  `isIncludeAndroidResources` dans le build du module) ; sur iOS, elle ne fait rien. Le module
  ajoute `test-composeMultiplatform-ui` à `commonTest`, et `test-junit`, `test-robolectric`,
  `test-androidx-compose-ui-manifest` à `androidHostTest`.
- Un test qui dessine cherche un élément par ce qu'en lit un lecteur d'écran (texte, description,
  action), pas par une étiquette de test, et sans dépendre de la langue : un texte traduit se compare
  à sa ressource ou ne se compare pas.
- Un écran se teste ainsi à partir du lot 5, avec son premier écran qui a de la logique.

### Firebase dans les tests

- **Un test unitaire n'appelle jamais Firebase**, ni le vrai projet, ni l'émulateur : il serait lent,
  dépendant du réseau, et ne passerait pas en CI.
- Ce qu'un Repository fait autour de Firebase (conversion d'un document en modèle exposé, d'une
  erreur en exception typée) s'écrit dans des fonctions `internal` sans Firebase, testées seules.
- Le repli sans Firebase se teste avec un `FirebaseProvider` qui rend `null` : c'est le cas d'un
  fork et de la CI (`SessionRepositoryImplTest`).
- Ce qui ne se vérifie qu'avec un vrai serveur (règles, fonctions, latence) se teste dans
  `firebase/` contre l'émulateur, ou se vérifie à la main sur les deux plateformes, et la vérification
  se note dans la feuille de route.

### Règles FIRST

- **Fast** — en mémoire, sans réseau ni fichier.
- **Independent** — chaque test passe seul et dans n'importe quel ordre, sans état partagé.
- **Repeatable** — pas de dépendance à l'horloge ni à l'aléatoire : le code testé reçoit un
  `Clock` en paramètre, et les tests lui passent une heure fixe.
- **Self-validating** — le test conclut seul par des assertions, jamais par la lecture d'un log.
- **Timely** — le test s'écrit avec le code. Un correctif de bug **commence** par un test qui
  reproduit le bug et échoue.

### Nommage et structure

- Nom de test en backticks, descriptif et en anglais :
  ``fun `an anonymous user is a guest`()``. Sans virgule : Kotlin/Native la refuse dans un nom, et
  un test commun tourne aussi sur le simulateur iOS.
- Corps en trois temps, séparés par une ligne vide et marqués `// Given`, `// When`, `// Then`.
- Les arguments d'une assertion sont nommés, comme tout appel à plusieurs paramètres
  (`expected = …`, `actual = …`).
- Les constructeurs d'objets de test passent par un helper privé avec des valeurs par défaut, pour
  que chaque test ne mette en avant que ce qui lui importe.

Forme attendue, celle du test du dépôt :

```kotlin
class WatchSessionUseCaseImplTest {

    private val sessionRepository = FakeSessionRepository()

    private val watchSessionUseCase = WatchSessionUseCaseImpl(
        sessionRepository = sessionRepository,
    )

    @Test
    fun `an anonymous user is a guest`() = runTest {
        // Given
        sessionRepository.sessions = flowOf(
            StoredSession.SignedIn(
                userId = "guest",
                isAnonymous = true,
            ),
        )

        // When
        val session = watchSessionUseCase().first()

        // Then
        assertEquals(
            expected = Session.SignedIn(
                userId = "guest",
                isGuest = true,
            ),
            actual = session,
        )
    }
}
```

### Contraintes des tests communs

Kotlin/Native impose quelques règles au code de `commonTest` :

- Pas de virgule dans un nom de test.
- Une classe de base ne prépare rien dans un `@BeforeTest` : Kotlin/Native ne l'exécute pas
  forcément avant celui de la sous-classe. Elle prépare à la construction de l'instance de test.
- Ce qui dépend de la plateforme passe par un `expect` de test, implémenté dans `androidHostTest` et
  `iosTest`.
- Une exception attendue se vérifie avec `assertFailsWith<T> { }`.
- Un texte traduit se lit depuis sa ressource (`getString(Res.string.x)`), jamais en dur : le
  simulateur iOS parle la langue du Mac, la JVM parle anglais.

### `Flow`

- Pour vérifier ce qu'un `Flow` réémet après un changement, le test le collecte dans une liste,
  depuis le `backgroundScope` avec un `UnconfinedTestDispatcher(testScheduler)`, puis fait le
  changement et compare la liste.
- Un `Flow` fini (un `flowOf`) se lit avec `toList()` ou `first()`.

### ViewModels

- Un test de ViewModel hérite de `MainDispatcherTest` (`shared/src/commonTest/.../test/`), qui pose
  `Dispatchers.setMain(UnconfinedTestDispatcher())` à sa construction, avant que la sous-classe ne
  construise son ViewModel, et le retire dans un `@AfterTest`. Les coroutines du ViewModel tournent
  alors aussitôt, sur le fil du test.
- Les doublures des UseCases de `shared` vivent dans `fake/` ; celles des actions héritent de
  `FakeAction` : les appels reçus (`calls`), une erreur à lever (`failure`) et une barrière à
  attendre (`gate`).
- Ses events (`Channel`) se collectent dans une liste, comme un `Flow`.
- Pour vérifier ce qui se passe pendant un appel en cours (une deuxième tentative pendant qu'une
  réponse est vérifiée), la doublure attend un `CompletableDeferred` que le test complète quand il
  veut. Une doublure qui répond tout de suite ne fait jamais apparaître la course.
- Dans `shared`, `withHostTest { isReturnDefaultValues = true }` : les logs de Kermit passent par
  `android.util.Log`, absent de la JVM des tests hôtes.
- Un test qui croit vérifier un comportement le prouve en échouant quand on casse ce comportement :
  avant de livrer, on retire le code protégé, on voit le test échouer, on le remet.

### Ce que les tests ne couvrent pas

- Les appels réels à Firebase (connexion, callables, écoutes, Storage) : vérifiés à la main sur un
  émulateur Android et le simulateur iOS, contre l'émulateur Firebase ou le vrai projet.
- Les hôtes (`androidApp`, `iosApp`) : la CI construit les deux apps, sans les lancer.

### Accès aux `internal` depuis les tests

Les source sets de test voient les déclarations `internal` de leur module : c'est ce qui permet de
garder une API minimale entre modules **tout en** testant les implémentations directement.
