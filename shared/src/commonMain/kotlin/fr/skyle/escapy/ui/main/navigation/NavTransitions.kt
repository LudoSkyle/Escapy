package fr.skyle.escapy.ui.main.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.navigation3.scene.Scene

/**
 * The transitions between screens, those of the first version of the app: a screen slides in from
 * the side it goes to, the one it covers slides out the other way, and the other way round going
 * back. Crossing the session (signing in, signing out) fades instead: the whole back stack changes.
 */

private const val SIGNED_OUT_KEY = "escapy.signedOut"

/** The metadata of a screen of the signed-out side: welcome, sign-in, sign-up, forgotten password. */
internal val SIGNED_OUT_SCREEN: Map<String, Any> = mapOf(SIGNED_OUT_KEY to true)

private val Scene<*>.isSignedOut: Boolean
    get() = entries.lastOrNull()?.metadata?.get(SIGNED_OUT_KEY) == true

private fun <T : Any> AnimatedContentTransitionScope<Scene<T>>.crossesSession(): Boolean =
    initialState.isSignedOut != targetState.isSignedOut

private fun sessionFade(): ContentTransform =
    fadeIn(animationSpec = tween()) togetherWith fadeOut(animationSpec = tween())

/** A screen opened: it comes from the end, the current one leaves by the start. */
internal fun <T : Any> AnimatedContentTransitionScope<Scene<T>>.forwardTransition(): ContentTransform =
    if (crossesSession()) {
        sessionFade()
    } else {
        slideInHorizontally(animationSpec = tween()) { width -> width } togetherWith
            slideOutHorizontally(animationSpec = tween()) { width -> -width }
    }

/** Back: the screen under comes from the start, the closed one leaves by the end. */
internal fun <T : Any> AnimatedContentTransitionScope<Scene<T>>.backTransition(): ContentTransform =
    if (crossesSession()) {
        sessionFade()
    } else {
        slideInHorizontally(animationSpec = tween()) { width -> -width } togetherWith
            slideOutHorizontally(animationSpec = tween()) { width -> width }
    }
