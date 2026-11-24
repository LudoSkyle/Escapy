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
        NEW_PASSWORD;

        internal val isPassword: Boolean
            get() = this == PASSWORD || this == NEW_PASSWORD

        internal val contentType: ContentType?
            get() = when (this) {
                TEXT -> null
                USERNAME -> ContentType.Username
                EMAIL -> ContentType.EmailAddress
                PASSWORD -> ContentType.Password
                NEW_PASSWORD -> ContentType.NewPassword
            }
    }
}
