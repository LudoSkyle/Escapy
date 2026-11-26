package fr.skyle.escapy.ui.main.navigation

import androidx.navigation3.runtime.NavBackStack
import kotlin.test.Test
import kotlin.test.assertEquals

class BackStackNavigationTest {

    @Test
    fun `a second back from the same screen does nothing more`() {
        // Given
        val backStack = NavBackStack<Route>(Route.Home, Route.Profile, Route.About)

        // When
        backStack.navigateBackFrom(from = Route.About)
        backStack.navigateBackFrom(from = Route.About)

        // Then
        assertEquals(expected = listOf<Route>(Route.Home, Route.Profile), actual = backStack.toList())
    }

    @Test
    fun `the root is never closed`() {
        // Given
        val backStack = NavBackStack<Route>(Route.Home)

        // When
        backStack.navigateBackFrom(from = Route.Home)

        // Then
        assertEquals(expected = listOf<Route>(Route.Home), actual = backStack.toList())
    }

    @Test
    fun `a screen opened twice in a row is opened once`() {
        // Given
        val backStack = NavBackStack<Route>(Route.Home)

        // When
        backStack.navigateTo(route = Route.CreateGame)
        backStack.navigateTo(route = Route.CreateGame)

        // Then
        assertEquals(expected = listOf<Route>(Route.Home, Route.CreateGame), actual = backStack.toList())
    }

    @Test
    fun `a screen is replaced only while it is the current one`() {
        // Given
        val backStack = NavBackStack<Route>(Route.Home, Route.CreateGame)

        // When
        backStack.replace(from = Route.CreateGame, to = Route.WaitingRoom(gameId = "game"))
        backStack.replace(from = Route.CreateGame, to = Route.WaitingRoom(gameId = "other"))

        // Then
        assertEquals(expected = listOf(Route.Home, Route.WaitingRoom(gameId = "game")), actual = backStack.toList())
    }
}
