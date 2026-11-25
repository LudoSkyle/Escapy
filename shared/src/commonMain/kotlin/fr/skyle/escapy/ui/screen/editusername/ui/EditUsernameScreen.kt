package fr.skyle.escapy.ui.screen.editusername.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import fr.skyle.escapy.designsystem.core.button.ESButton
import fr.skyle.escapy.designsystem.core.button.ESButtonDefaults
import fr.skyle.escapy.designsystem.core.screenstructure.ESScreenStructure
import fr.skyle.escapy.designsystem.core.screenstructure.ESScreenStructureDefaults
import fr.skyle.escapy.designsystem.core.snackbar.ESSnackbarState
import fr.skyle.escapy.designsystem.core.snackbar.rememberESSnackbarState
import fr.skyle.escapy.designsystem.core.textfield.ESTextField
import fr.skyle.escapy.designsystem.core.textfield.ESTextFieldDefaults
import fr.skyle.escapy.designsystem.core.topappbar.ESTopAppBar
import fr.skyle.escapy.designsystem.theme.ESTheme
import fr.skyle.escapy.resources.Res
import fr.skyle.escapy.resources.action_save
import fr.skyle.escapy.resources.edit_username_rules
import fr.skyle.escapy.resources.edit_username_title
import fr.skyle.escapy.resources.field_username
import fr.skyle.escapy.ui.core.error.message
import fr.skyle.escapy.ui.screen.editusername.ui.EditUsernameViewModel.EditUsernameUIState
import org.jetbrains.compose.resources.stringResource

@Composable
fun EditUsernameScreen(
    username: String,
    uiState: EditUsernameUIState,
    snackbarState: ESSnackbarState,
    onBackClicked: () -> Unit,
    onUsernameChanged: (String) -> Unit,
    onSaveClicked: () -> Unit,
) {
    ESScreenStructure(
        style = ESScreenStructureDefaults.Style.STANDARD,
        topBar = {
            ESTopAppBar(
                title = stringResource(Res.string.edit_username_title),
                onBackClick = onBackClicked,
            )
        },
        snackbarState = snackbarState,
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(ESTheme.spacing.l),
        ) {
            ESTextField(
                modifier = Modifier.fillMaxWidth(),
                value = username,
                onValueChange = onUsernameChanged,
                label = stringResource(Res.string.field_username),
                kind = ESTextFieldDefaults.Kind.USERNAME,
                style = ESTextFieldDefaults.Style.STANDARD,
                isEnabled = !uiState.isSaving,
                supportingText = stringResource(Res.string.edit_username_rules),
                errorMessage = uiState.usernameError?.message(),
                imeAction = ImeAction.Done,
                onImeAction = onSaveClicked,
            )

            Spacer(modifier = Modifier.height(ESTheme.spacing.l))

            ESButton(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(Res.string.action_save),
                onClick = onSaveClicked,
                style = ESButtonDefaults.Style.PRIMARY,
                size = ESButtonDefaults.Size.LARGE,
                isEnabled = username.isNotBlank() || uiState.isSaving,
                isLoading = uiState.isSaving,
            )
        }
    }
}

@Preview
@Composable
private fun PreviewEditUsernameScreen() {
    ESTheme {
        EditUsernameScreen(
            username = "Player_ABC234",
            uiState = EditUsernameUIState(),
            snackbarState = rememberESSnackbarState(),
            onBackClicked = {},
            onUsernameChanged = {},
            onSaveClicked = {},
        )
    }
}
