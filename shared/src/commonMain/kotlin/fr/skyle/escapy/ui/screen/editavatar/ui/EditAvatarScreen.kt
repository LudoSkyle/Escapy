package fr.skyle.escapy.ui.screen.editavatar.ui

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import fr.skyle.escapy.designsystem.core.progress.ESProgressIndicator
import fr.skyle.escapy.designsystem.core.progress.ESProgressIndicatorDefaults
import fr.skyle.escapy.designsystem.core.screenstructure.ESScreenStructure
import fr.skyle.escapy.designsystem.core.screenstructure.ESScreenStructureDefaults
import fr.skyle.escapy.designsystem.core.snackbar.ESSnackbarState
import fr.skyle.escapy.designsystem.core.snackbar.rememberESSnackbarState
import fr.skyle.escapy.designsystem.core.topappbar.ESTopAppBar
import fr.skyle.escapy.designsystem.theme.ESTheme
import fr.skyle.escapy.resources.Res
import fr.skyle.escapy.resources.avatar_label
import fr.skyle.escapy.resources.edit_avatar_title
import fr.skyle.escapy.ui.core.avatar.AVATARS
import fr.skyle.escapy.ui.core.avatar.AvatarImage
import fr.skyle.escapy.ui.screen.editavatar.ui.EditAvatarViewModel.EditAvatarUIState
import org.jetbrains.compose.resources.stringResource

@Composable
fun EditAvatarScreen(
    uiState: EditAvatarUIState,
    snackbarState: ESSnackbarState,
    onBackClicked: () -> Unit,
    onAvatarSelected: (avatarId: Int) -> Unit,
) {
    ESScreenStructure(
        style = ESScreenStructureDefaults.Style.STANDARD,
        topBar = {
            ESTopAppBar(
                title = stringResource(Res.string.edit_avatar_title),
                onBackClick = onBackClicked,
            )
        },
        snackbarState = snackbarState,
    ) { padding ->
        val selectedAvatarId = uiState.savingAvatarId ?: uiState.currentAvatarId
        LazyVerticalGrid(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .selectableGroup(),
            columns = GridCells.Adaptive(minSize = 80.dp),
            contentPadding = PaddingValues(ESTheme.spacing.m),
        ) {
            itemsIndexed(AVATARS) { index, _ ->
                val avatarId = index + 1
                val isSelected = avatarId == selectedAvatarId
                val isSaving = avatarId == uiState.savingAvatarId
                Box(
                    modifier = Modifier
                        .padding(ESTheme.spacing.xs)
                        .aspectRatio(1f)
                        .selectable(
                            selected = isSelected,
                            enabled = uiState.savingAvatarId == null,
                            role = Role.RadioButton,
                            onClick = { onAvatarSelected(avatarId) },
                        )
                        .border(
                            width = 3.dp,
                            color = if (isSelected) ESTheme.colors.primary else Color.Transparent,
                            shape = CircleShape,
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    AvatarImage(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(ESTheme.spacing.xxs)
                            .alpha(if (uiState.savingAvatarId != null && !isSaving) 0.5f else 1f),
                        avatarId = avatarId,
                        size = 72.dp,
                        // Numbered, as they have no name
                        contentDescription = stringResource(Res.string.avatar_label, avatarId),
                    )

                    if (isSaving) {
                        ESProgressIndicator(
                            style = ESProgressIndicatorDefaults.Style.PRIMARY,
                            size = ESProgressIndicatorDefaults.Size.LARGE,
                        )
                    }
                }
            }
        }
    }
}

@Preview
@Composable
private fun PreviewEditAvatarScreen() {
    ESTheme {
        EditAvatarScreen(
            uiState = EditAvatarUIState(currentAvatarId = 7),
            snackbarState = rememberESSnackbarState(),
            onBackClicked = {},
            onAvatarSelected = {},
        )
    }
}
