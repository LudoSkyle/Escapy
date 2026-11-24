package fr.skyle.escapy.designsystem.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.ui.graphics.Color

// The palette of the first version, kept until the design system is redone (see docs/ROADMAP.md)
private val Amber = Color(0xFFF5B856)
private val Charcoal = Color(0xFF23272A)
private val Graphite = Color(0xFF2B3033)
private val Snow = Color(0xFFFAFAFA)
private val Red = Color(0xFFEA3A24)

// Escapy is dark only: the amber accent is its single bright color
internal val ESColorScheme: ColorScheme = darkColorScheme(
    primary = Amber,
    onPrimary = Charcoal,
    background = Charcoal,
    onBackground = Snow,
    surface = Graphite,
    onSurface = Snow,
    error = Red,
    onError = Snow,
)
