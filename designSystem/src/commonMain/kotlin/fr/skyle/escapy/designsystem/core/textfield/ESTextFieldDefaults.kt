package fr.skyle.escapy.designsystem.core.textfield

import androidx.compose.runtime.Composable
import androidx.compose.ui.autofill.ContentType
import androidx.compose.ui.graphics.Color
import fr.skyle.escapy.designsystem.theme.ESTheme

object ESTextFieldDefaults {

    enum class Style {
        STANDARD;

        val containerColor: Color
            @Composable
            get() = when (this) {
                STANDARD -> ESTheme.colors.surface
            }
    }

    /** What the field holds, for the keyboard and for the password manager to fill it. */
    enum class Kind {
        TEXT,
        USERNAME,
        EMAIL,
        PASSWORD,
        NEW_PASSWORD,
        // A code to type as it is read, such as the code of a game: in capitals, never corrected
        CODE,
        // A message to write, such as in a chat: on a few lines, corrected, sentences capitalized; the
        // key of the keyboard goes to the next line, a button of the screen sends
        MESSAGE;

        internal val isMultiLine: Boolean
            get() = this == MESSAGE

        internal val isPassword: Boolean
            get() = this == PASSWORD || this == NEW_PASSWORD

        internal val contentType: ContentType?
            get() = when (this) {
                TEXT -> null
                USERNAME -> ContentType.Username
                EMAIL -> ContentType.EmailAddress
                PASSWORD -> ContentType.Password
                NEW_PASSWORD -> ContentType.NewPassword
                CODE, MESSAGE -> null
            }
    }
}
