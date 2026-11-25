package fr.skyle.escapy.ui.core

import androidx.compose.foundation.layout.Column
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.unit.dp
import fr.skyle.escapy.designsystem.theme.ESTheme
import fr.skyle.escapy.test.ComposeTest
import fr.skyle.escapy.ui.core.avatar.AVATARS
import fr.skyle.escapy.ui.core.avatar.AvatarImage
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
internal class AvatarsTest : ComposeTest() {

    @Test
    fun `the app has the 50 avatars the functions accept`() {
        assertEquals(
            expected = 50,
            actual = AVATARS.size,
        )
    }

    // A vector drawable Compose Resources cannot read stops the app the first time it is drawn
    @Test
    fun `every avatar is drawn`() = runComposeUiTest {
        // Given
        setContent {
            ESTheme {
                Column {
                    AVATARS.indices.forEach { index ->
                        AvatarImage(
                            avatarId = index + 1,
                            size = 8.dp,
                            contentDescription = "avatar ${index + 1}",
                        )
                    }
                }
            }
        }

        // Then
        AVATARS.indices.forEach { index ->
            onNodeWithContentDescription("avatar ${index + 1}").assertExists()
        }
    }
}
