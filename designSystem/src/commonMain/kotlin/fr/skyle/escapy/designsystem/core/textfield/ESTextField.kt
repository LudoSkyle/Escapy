package fr.skyle.escapy.designsystem.core.textfield

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentType
import androidx.compose.ui.semantics.error
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import fr.skyle.escapy.designsystem.core.text.ESText
import fr.skyle.escapy.designsystem.resources.Res
import fr.skyle.escapy.designsystem.resources.hide_password
import fr.skyle.escapy.designsystem.resources.ic_visibility
import fr.skyle.escapy.designsystem.resources.ic_visibility_off
import fr.skyle.escapy.designsystem.resources.show_password
import fr.skyle.escapy.designsystem.theme.ESTheme
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

/**
 * A text the user writes, named by its [label]. Its [kind] opens the keyboard that suits it and lets
 * the password manager fill it; a password is hidden until its eye is touched. An [errorMessage]
 * replaces the [supportingText] and is read by a screen reader as the error of the field. The key
 * of the keyboard does [imeAction], then calls [onImeAction].
 */
@Composable
fun ESTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    kind: ESTextFieldDefaults.Kind,
    style: ESTextFieldDefaults.Style,
    modifier: Modifier = Modifier,
    isEnabled: Boolean = true,
    supportingText: String? = null,
    errorMessage: String? = null,
    imeAction: ImeAction = ImeAction.Default,
    onImeAction: () -> Unit = {},
) {
    var isPasswordVisible by rememberSaveable { mutableStateOf(false) }
    // Material moves and shrinks the label: its size stays Material's, its font is Exo 2
    val fontFamily = ESTheme.typography.body.fontFamily

    OutlinedTextField(
        modifier = modifier.semantics {
            kind.contentType?.let { type -> contentType = type }
            errorMessage?.let { message -> error(message) }
        },
        value = value,
        onValueChange = onValueChange,
        enabled = isEnabled,
        label = {
            ESText(
                text = label,
                style = LocalTextStyle.current.copy(fontFamily = fontFamily),
            )
        },
        supportingText = (errorMessage ?: supportingText)?.let { text ->
            {
                ESText(
                    text = text,
                    style = ESTheme.typography.caption,
                )
            }
        },
        isError = errorMessage != null,
        trailingIcon = if (kind.isPassword) {
            {
                IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                    Icon(
                        painter = painterResource(if (isPasswordVisible) Res.drawable.ic_visibility_off else Res.drawable.ic_visibility),
                        contentDescription = stringResource(if (isPasswordVisible) Res.string.hide_password else Res.string.show_password),
                    )
                }
            }
        } else {
            null
        },
        visualTransformation = when {
            kind.isPassword && !isPasswordVisible -> PasswordVisualTransformation()
            kind == ESTextFieldDefaults.Kind.CODE -> CapitalsVisualTransformation
            else -> VisualTransformation.None
        },
        textStyle = ESTheme.typography.body,
        singleLine = true,
        keyboardOptions = KeyboardOptions(
            keyboardType = when (kind) {
                ESTextFieldDefaults.Kind.EMAIL -> KeyboardType.Email
                ESTextFieldDefaults.Kind.PASSWORD, ESTextFieldDefaults.Kind.NEW_PASSWORD -> KeyboardType.Password
                ESTextFieldDefaults.Kind.TEXT, ESTextFieldDefaults.Kind.USERNAME -> KeyboardType.Text
                ESTextFieldDefaults.Kind.CODE -> KeyboardType.Ascii
            },
            capitalization = when (kind) {
                ESTextFieldDefaults.Kind.CODE -> KeyboardCapitalization.Characters
                ESTextFieldDefaults.Kind.TEXT -> KeyboardCapitalization.Sentences
                else -> KeyboardCapitalization.Unspecified
            },
            autoCorrectEnabled = kind == ESTextFieldDefaults.Kind.TEXT,
            imeAction = imeAction,
        ),
        keyboardActions = KeyboardActions(onAny = { onImeAction() }),
        shape = ESTheme.shapes.medium,
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = ESTheme.colors.textPrimary,
            unfocusedTextColor = ESTheme.colors.textPrimary,
            disabledTextColor = ESTheme.colors.disabledContent,
            errorTextColor = ESTheme.colors.textPrimary,
            focusedContainerColor = style.containerColor,
            unfocusedContainerColor = style.containerColor,
            disabledContainerColor = style.containerColor,
            errorContainerColor = style.containerColor,
            cursorColor = ESTheme.colors.primary,
            errorCursorColor = ESTheme.colors.error,
            focusedBorderColor = ESTheme.colors.primary,
            unfocusedBorderColor = ESTheme.colors.outline,
            disabledBorderColor = ESTheme.colors.outlineVariant,
            errorBorderColor = ESTheme.colors.error,
            focusedLabelColor = ESTheme.colors.primary,
            unfocusedLabelColor = ESTheme.colors.textSecondary,
            disabledLabelColor = ESTheme.colors.disabledContent,
            errorLabelColor = ESTheme.colors.error,
            focusedTrailingIconColor = ESTheme.colors.textSecondary,
            unfocusedTrailingIconColor = ESTheme.colors.textSecondary,
            errorTrailingIconColor = ESTheme.colors.textSecondary,
            focusedSupportingTextColor = ESTheme.colors.textSecondary,
            unfocusedSupportingTextColor = ESTheme.colors.textSecondary,
            errorSupportingTextColor = ESTheme.colors.error,
            selectionColors = LocalTextSelectionColors.current,
        ),
    )
}

@Preview
@Composable
private fun PreviewESTextField() {
    ESTheme {
        Column(modifier = Modifier.padding(ESTheme.spacing.m)) {
            ESTextField(
                modifier = Modifier.fillMaxWidth(),
                value = "player@escapy.app",
                onValueChange = {},
                label = "Email",
                kind = ESTextFieldDefaults.Kind.EMAIL,
                style = ESTextFieldDefaults.Style.STANDARD,
            )
            ESTextField(
                modifier = Modifier.fillMaxWidth(),
                value = "secret",
                onValueChange = {},
                label = "Password",
                kind = ESTextFieldDefaults.Kind.PASSWORD,
                style = ESTextFieldDefaults.Style.STANDARD,
                errorMessage = "At least 8 characters",
            )
        }
    }
}

/**
 * Shows a code in capitals, whatever the keyboard typed, without changing the text: rewriting the
 * value while the keyboard composes it would lose keys. One character for one, so offsets map as is.
 */
private object CapitalsVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText =
        TransformedText(
            text = AnnotatedString(text.text.map { character -> character.uppercaseChar() }.joinToString(separator = "")),
            offsetMapping = OffsetMapping.Identity,
        )
}
