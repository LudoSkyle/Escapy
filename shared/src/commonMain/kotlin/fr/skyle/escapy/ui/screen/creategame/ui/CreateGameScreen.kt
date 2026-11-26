package fr.skyle.escapy.ui.screen.creategame.ui

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
import fr.skyle.escapy.designsystem.core.card.ESCard
import fr.skyle.escapy.designsystem.core.card.ESCardDefaults
import fr.skyle.escapy.designsystem.core.divider.ESDivider
import fr.skyle.escapy.designsystem.core.screenstructure.ESScreenStructure
import fr.skyle.escapy.designsystem.core.screenstructure.ESScreenStructureDefaults
import fr.skyle.escapy.designsystem.core.snackbar.ESSnackbarState
import fr.skyle.escapy.designsystem.core.snackbar.rememberESSnackbarState
import fr.skyle.escapy.designsystem.core.stepper.ESStepper
import fr.skyle.escapy.designsystem.core.stepper.ESStepperDefaults
import fr.skyle.escapy.designsystem.core.textfield.ESTextField
import fr.skyle.escapy.designsystem.core.textfield.ESTextFieldDefaults
import fr.skyle.escapy.designsystem.core.topappbar.ESTopAppBar
import fr.skyle.escapy.designsystem.theme.ESTheme
import fr.skyle.escapy.domain.validation.GameRules
import fr.skyle.escapy.resources.Res
import fr.skyle.escapy.resources.action_create
import fr.skyle.escapy.resources.action_create_game
import fr.skyle.escapy.resources.create_game_duration
import fr.skyle.escapy.resources.create_game_fewer_players
import fr.skyle.escapy.resources.create_game_longer
import fr.skyle.escapy.resources.create_game_max_players
import fr.skyle.escapy.resources.create_game_max_players_help
import fr.skyle.escapy.resources.create_game_more_players
import fr.skyle.escapy.resources.create_game_no_limit
import fr.skyle.escapy.resources.create_game_shorter
import fr.skyle.escapy.resources.create_game_title_help
import fr.skyle.escapy.resources.field_game_title
import fr.skyle.escapy.ui.core.error.message
import fr.skyle.escapy.ui.core.game.durationText
import fr.skyle.escapy.ui.screen.creategame.ui.CreateGameViewModel.CreateGameUIState
import org.jetbrains.compose.resources.stringResource

@Composable
fun CreateGameScreen(
    title: String,
    uiState: CreateGameUIState,
    snackbarState: ESSnackbarState,
    onBackClicked: () -> Unit,
    onTitleChanged: (String) -> Unit,
    onDurationDecreased: () -> Unit,
    onDurationIncreased: () -> Unit,
    onMaxPlayersDecreased: () -> Unit,
    onMaxPlayersIncreased: () -> Unit,
    onCreateClicked: () -> Unit,
) {
    ESScreenStructure(
        style = ESScreenStructureDefaults.Style.STANDARD,
        topBar = {
            ESTopAppBar(
                title = stringResource(Res.string.action_create_game),
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
                value = title,
                onValueChange = onTitleChanged,
                label = stringResource(Res.string.field_game_title),
                kind = ESTextFieldDefaults.Kind.TEXT,
                style = ESTextFieldDefaults.Style.STANDARD,
                isEnabled = !uiState.isCreating,
                supportingText = stringResource(Res.string.create_game_title_help),
                errorMessage = uiState.titleError?.message(),
                imeAction = ImeAction.Done,
            )

            Spacer(modifier = Modifier.height(ESTheme.spacing.m))

            ESCard(
                modifier = Modifier.fillMaxWidth(),
                style = ESCardDefaults.Style.STANDARD,
            ) {
                ESStepper(
                    label = stringResource(Res.string.create_game_duration),
                    value = durationText(minutes = uiState.durationMinutes),
                    onDecrease = onDurationDecreased,
                    onIncrease = onDurationIncreased,
                    decreaseContentDescription = stringResource(Res.string.create_game_shorter),
                    increaseContentDescription = stringResource(Res.string.create_game_longer),
                    style = ESStepperDefaults.Style.STANDARD,
                    size = ESStepperDefaults.Size.MEDIUM,
                    isDecreaseEnabled = uiState.canShorten && !uiState.isCreating,
                    isIncreaseEnabled = uiState.canLengthen && !uiState.isCreating,
                )

                ESDivider()

                ESStepper(
                    label = stringResource(Res.string.create_game_max_players),
                    supportingText = stringResource(Res.string.create_game_max_players_help),
                    value = uiState.maxPlayers?.toString() ?: stringResource(Res.string.create_game_no_limit),
                    onDecrease = onMaxPlayersDecreased,
                    onIncrease = onMaxPlayersIncreased,
                    decreaseContentDescription = stringResource(Res.string.create_game_fewer_players),
                    increaseContentDescription = stringResource(Res.string.create_game_more_players),
                    style = ESStepperDefaults.Style.STANDARD,
                    size = ESStepperDefaults.Size.MEDIUM,
                    isDecreaseEnabled = uiState.canLowerMaxPlayers && !uiState.isCreating,
                    isIncreaseEnabled = uiState.canRaiseMaxPlayers && !uiState.isCreating,
                )
            }

            Spacer(modifier = Modifier.height(ESTheme.spacing.l))

            ESButton(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(Res.string.action_create),
                onClick = onCreateClicked,
                style = ESButtonDefaults.Style.PRIMARY,
                size = ESButtonDefaults.Size.LARGE,
                isEnabled = title.trim().length >= GameRules.TITLE_MIN_LENGTH || uiState.isCreating,
                isLoading = uiState.isCreating,
            )
        }
    }
}

@Preview
@Composable
private fun PreviewCreateGameScreen() {
    ESTheme {
        CreateGameScreen(
            title = "Le manoir",
            uiState = CreateGameUIState(durationMinutes = 90, maxPlayers = 6),
            snackbarState = rememberESSnackbarState(),
            onBackClicked = {},
            onTitleChanged = {},
            onDurationDecreased = {},
            onDurationIncreased = {},
            onMaxPlayersDecreased = {},
            onMaxPlayersIncreased = {},
            onCreateClicked = {},
        )
    }
}
