# ADR-0001 — Le domaine dépend de la donnée

Status: Accepted — 2026-11-24

> ADR **de projet**. Il prime sur l'ADR Kotlin 0001 du référentiel partagé (« les couches métier ne
> dépendent d'aucun framework de DI ») pour les points listés dans la décision, et seulement
> ceux-là.

## Contexte

L'ADR Kotlin 0001 du référentiel partagé place les interfaces de Repository dans le domaine, que la
donnée implémente (`data` → `domain`), et fait renvoyer aux UseCases un type `sealed` plutôt que de
laisser remonter une exception.

Dans Escapy, l'essentiel des règles du jeu vit sur le serveur : la validation d'une réponse, le
déblocage d'une mission, le minuteur, la victoire
([ADR-0004](./0004-firestore-et-le-serveur-source-de-verite.md)). Côté app, le domaine présente ce
que le serveur a écrit et prépare ce qu'on lui envoie. Sa donnée, c'est Firebase, par un SDK tiers
en bêta ([ADR-0003](./0003-firebase-en-kotlin-commun-avec-gitlive.md)). Le domaine existe pour
**cacher la donnée** au reste de l'app, et Firebase avec elle, pas pour qu'elle s'y conforme.

## Décision

- Les dépendances vont dans un seul sens : `shared` → `domain` → `data`. `data` ne connaît pas
  `domain`.
- Les interfaces de Repository **et** leurs implémentations vivent dans `data`, seul module qui
  dépend de Firebase.
- `domain` dépend de `data` en `implementation` : `shared` ne voit ni les Repositories, ni les
  modèles de `data`, ni Firebase. Le compilateur garantit que l'UI passe par les UseCases.
- `domain` porte les modèles métier, leurs conversions depuis `data` et les UseCases.
- Un UseCase qui échoue lève une **exception typée**, `<Problème>Exception`. C'est l'appelant, en
  pratique le ViewModel, qui l'attrape (`try`/`catch`, `CancellationException` relancée). Pas de
  type `Response` sealed, ni d'`Either`.

Ce que cet ADR ne remplace pas dans l'ADR Kotlin 0001 continue de s'appliquer : domaine en Kotlin
pur, sans API de plateforme ; UseCase = une interface + une implémentation ; l'appelant reçoit
toujours l'interface.

## Conséquences

- Changer de façon d'accéder à Firebase (le plan B de l'ADR-0003 : les SDK natifs) ne touche que
  `data`, et le mapping vers les modèles métier dans `domain`.
- Un test de UseCase double les Repositories de `data` par des fakes : leurs interfaces sont
  publiques.
- Aucune annotation `@Throws` : l'UI est en Kotlin sur les deux plateformes (ADR-0002), aucune
  exception ne traverse vers du Swift.
- Le détail de mise en œuvre est dans [KMP/01_MODULE_DATA.md](../KMP/01_MODULE_DATA.md) et
  [KMP/02_MODULE_DOMAIN.md](../KMP/02_MODULE_DOMAIN.md).
