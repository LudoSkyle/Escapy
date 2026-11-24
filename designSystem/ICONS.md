# Icons

The vector drawables of `src/commonMain/composeResources/drawable/ic_*.xml` are
[Material Symbols](https://github.com/google/material-design-icons) (Rounded, weight 400, grade 0,
optical size 24, unfilled), by Google, under the
[Apache License 2.0](https://github.com/google/material-design-icons/blob/master/LICENSE).

Taken from `symbols/android/<name>/materialsymbolsrounded/<name>_24px.xml`, with these changes so
Compose Resources reads them: the `android:tint` attribute removed (and the start tag closed again), and `@android:color/white`
replaced by `#FFFFFFFF`. The color comes from the component that draws the icon.
