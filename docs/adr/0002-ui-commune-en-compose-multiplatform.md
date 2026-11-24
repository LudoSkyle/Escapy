# ADR-0002 — L'UI est commune, en Compose Multiplatform

Status: Accepted — 2026-11-24

> ADR **de projet**. Vaut pour tout le dépôt.

## Contexte

L'ancienne app n'existait que sur Android. Escapy se joue à plusieurs, chacun sur son téléphone :
le maître du jeu et ses joueurs n'ont pas tous la même plateforme, et l'app doit exister sur iOS
avec les mêmes écrans et les mêmes fonctionnalités. Deux UI natives (Compose d'un côté, SwiftUI de
l'autre) doubleraient chaque écran, chaque correction et chaque test d'UI, pour une app qui a sa
propre identité visuelle (sombre, un accent ambre, des écrans de jeu immersifs) et n'a pas besoin du
rendu natif d'iOS.

Une partie du travail reste propre à chaque plateforme : scanner le QR code d'une partie, choisir
une image, enregistrer un son, les permissions, le partage, les notifications.

## Décision

- Tous les écrans, la navigation, les ViewModels et le design system sont écrits en **Compose
  Multiplatform**, dans `commonMain`.
- L'app iOS n'est qu'un **hôte SwiftUI** qui démarre Firebase et Koin, et affiche le
  `ComposeUIViewController` de `shared`. Aucun écran n'est écrit en SwiftUI.
- Le natif n'apparaît que derrière une frontière commune, selon les règles de
  [KMP/03_CODE_PLATEFORME.md](../KMP/03_CODE_PLATEFORME.md).
- Le code natif iOS s'écrit de préférence en Kotlin, dans `iosMain`, par l'interop Kotlin/Native.
  Du Swift n'est écrit que pour une API inaccessible depuis Kotlin ; il est alors fourni à `shared`
  au démarrage, derrière une interface commune.

## Conséquences

- Une fonctionnalité s'écrit et se teste une fois.
- Le rendu iOS n'est pas celui d'UIKit : c'est accepté, Escapy suit son propre design system.
- Tout le code vit dans le build Gradle, à l'exception de l'hôte SwiftUI et de la configuration du
  projet Xcode.
- Ce qui ne peut être que natif (l'aperçu de la caméra, la présentation d'un contrôleur iOS) reste
  dans un `actual` ou une implémentation de plateforme, jamais dans un écran.
