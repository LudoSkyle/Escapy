package fr.skyle.escapy.ui.screen.signin

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.runComposeUiTest
import fr.skyle.escapy.designsystem.core.snackbar.rememberESSnackbarState
import fr.skyle.escapy.designsystem.theme.ESTheme
import fr.skyle.escapy.resources.Res
import fr.skyle.escapy.resources.sign_in_action
import fr.skyle.escapy.test.ComposeTest
import fr.skyle.escapy.ui.screen.signin.ui.SignInScreen
import org.jetbrains.compose.resources.stringResource
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
internal class SignInScreenTest : ComposeTest() {

    private fun signInButton(label: String) = hasText(label) and hasClickAction()

    @Test
    fun `the button waits for an email and a password`() = runComposeUiTest {
        // Given
        var label = ""
        setContent {
            label = stringResource(Res.string.sign_in_action)
            ESTheme {
                SignInScreen(
                    email = "player@escapy.app",
                    password = "",
                    isSigningIn = false,
                    snackbarState = rememberESSnackbarState(),
                    onBackClicked = {},
                    onEmailChanged = {},
                    onPasswordChanged = {},
                    onSignInClicked = {},
                    onForgotPasswordClicked = {},
                )
            }
        }

        // Then
        onNode(signInButton(label)).assertIsNotEnabled()
    }

    @Test
    fun `a filled form can be sent`() = runComposeUiTest {
        // Given
        var label = ""
        setContent {
            label = stringResource(Res.string.sign_in_action)
            ESTheme {
                SignInScreen(
                    email = "player@escapy.app",
                    password = "Escapy-2026",
                    isSigningIn = false,
                    snackbarState = rememberESSnackbarState(),
                    onBackClicked = {},
                    onEmailChanged = {},
                    onPasswordChanged = {},
                    onSignInClicked = {},
                    onForgotPasswordClicked = {},
                )
            }
        }

        // Then
        onNode(signInButton(label)).assertIsEnabled()
    }

    @Test
    fun `the button reads as disabled while signing in`() = runComposeUiTest {
        // Given
        var label = ""
        setContent {
            label = stringResource(Res.string.sign_in_action)
            ESTheme {
                SignInScreen(
                    email = "player@escapy.app",
                    password = "Escapy-2026",
                    isSigningIn = true,
                    snackbarState = rememberESSnackbarState(),
                    onBackClicked = {},
                    onEmailChanged = {},
                    onPasswordChanged = {},
                    onSignInClicked = {},
                    onForgotPasswordClicked = {},
                )
            }
        }

        // Then
        onNode(signInButton(label), useUnmergedTree = false).assertIsNotEnabled()
    }
}
