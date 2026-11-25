package fr.skyle.escapy.ui.core.form

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import fr.skyle.escapy.designsystem.core.button.ESButton
import fr.skyle.escapy.designsystem.core.button.ESButtonDefaults
import fr.skyle.escapy.designsystem.core.screenstructure.ESScreenStructure
import fr.skyle.escapy.designsystem.core.screenstructure.ESScreenStructureDefaults
import fr.skyle.escapy.designsystem.core.snackbar.ESSnackbarState
import fr.skyle.escapy.designsystem.core.text.ESText
import fr.skyle.escapy.designsystem.core.topappbar.ESTopAppBar
import fr.skyle.escapy.designsystem.theme.ESTheme

/**
 * A screen of the account that sends a form: a [title], its [content], then once done, a [result]
 * message with a way back instead of the form.
 */
@Composable
fun AccountFormLayout(
    title: String,
    snackbarState: ESSnackbarState,
    onBackClicked: () -> Unit,
    result: String?,
    resultActionText: String,
    content: @Composable ColumnScope.() -> Unit,
) {
    ESScreenStructure(
        style = ESScreenStructureDefaults.Style.STANDARD,
        topBar = {
            ESTopAppBar(
                title = title,
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
            if (result != null) {
                ESText(
                    modifier = Modifier.fillMaxWidth(),
                    text = result,
                    style = ESTheme.typography.body,
                )

                Spacer(modifier = Modifier.height(ESTheme.spacing.l))

                ESButton(
                    modifier = Modifier.fillMaxWidth(),
                    text = resultActionText,
                    onClick = onBackClicked,
                    style = ESButtonDefaults.Style.PRIMARY,
                    size = ESButtonDefaults.Size.LARGE,
                )
            } else {
                content()
            }
        }
    }
}
