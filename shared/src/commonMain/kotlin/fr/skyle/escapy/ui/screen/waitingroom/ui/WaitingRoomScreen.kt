package fr.skyle.escapy.ui.screen.waitingroom.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import fr.skyle.escapy.designsystem.core.button.ESButton
import fr.skyle.escapy.designsystem.core.button.ESButtonDefaults
import fr.skyle.escapy.designsystem.core.card.ESCard
import fr.skyle.escapy.designsystem.core.card.ESCardDefaults
import fr.skyle.escapy.designsystem.core.dialog.ESDialog
import fr.skyle.escapy.designsystem.core.dialog.ESDialogDefaults
import fr.skyle.escapy.designsystem.core.divider.ESDivider
import fr.skyle.escapy.designsystem.core.progress.ESProgressIndicator
import fr.skyle.escapy.designsystem.core.progress.ESProgressIndicatorDefaults
import fr.skyle.escapy.designsystem.core.screenstructure.ESScreenStructure
import fr.skyle.escapy.designsystem.core.screenstructure.ESScreenStructureDefaults
import fr.skyle.escapy.designsystem.core.snackbar.ESSnackbarState
import fr.skyle.escapy.designsystem.core.snackbar.rememberESSnackbarState
import fr.skyle.escapy.designsystem.core.text.ESText
import fr.skyle.escapy.designsystem.core.topappbar.ESTopAppBar
import fr.skyle.escapy.designsystem.theme.ESTheme
import fr.skyle.escapy.domain.model.Game
import fr.skyle.escapy.domain.model.GameMember
import fr.skyle.escapy.domain.model.GameState
import fr.skyle.escapy.resources.Res
import fr.skyle.escapy.resources.action_back_to_home
import fr.skyle.escapy.resources.action_cancel
import fr.skyle.escapy.resources.action_cancel_game
import fr.skyle.escapy.resources.action_keep_game
import fr.skyle.escapy.resources.action_kick
import fr.skyle.escapy.resources.action_leave
import fr.skyle.escapy.resources.action_leave_game
import fr.skyle.escapy.resources.cancel_game_dialog_message
import fr.skyle.escapy.resources.cancel_game_dialog_title
import fr.skyle.escapy.resources.field_join_code
import fr.skyle.escapy.resources.kick_player_dialog_message
import fr.skyle.escapy.resources.kick_player_dialog_title
import fr.skyle.escapy.resources.leave_game_dialog_message
import fr.skyle.escapy.resources.leave_game_dialog_title
import fr.skyle.escapy.resources.waiting_room_code_help
import fr.skyle.escapy.resources.waiting_room_deleted
import fr.skyle.escapy.resources.waiting_room_excluded
import fr.skyle.escapy.resources.waiting_room_master
import fr.skyle.escapy.resources.waiting_room_no_players
import fr.skyle.escapy.resources.waiting_room_players
import fr.skyle.escapy.resources.waiting_room_start_by_master
import fr.skyle.escapy.ui.core.game.GameMemberRow
import fr.skyle.escapy.ui.core.game.durationText
import fr.skyle.escapy.ui.core.game.playersText
import fr.skyle.escapy.ui.screen.waitingroom.ui.WaitingRoomViewModel.WaitingRoomUIState
import org.jetbrains.compose.resources.stringResource

@Composable
fun WaitingRoomScreen(
    uiState: WaitingRoomUIState,
    snackbarState: ESSnackbarState,
    onBackClicked: () -> Unit,
    onKickClicked: (GameMember) -> Unit,
    onKickDismissed: () -> Unit,
    onKickConfirmed: () -> Unit,
    onLeaveClicked: () -> Unit,
    onLeaveDismissed: () -> Unit,
    onLeaveConfirmed: () -> Unit,
    onCancelClicked: () -> Unit,
    onCancelDismissed: () -> Unit,
    onCancelConfirmed: () -> Unit,
) {
    val state = uiState.state

    ESScreenStructure(
        style = ESScreenStructureDefaults.Style.STANDARD,
        topBar = {
            ESTopAppBar(
                title = (state as? GameState.Available)?.game?.title.orEmpty(),
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
            when (state) {
                null -> Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center,
                ) {
                    ESProgressIndicator(
                        style = ESProgressIndicatorDefaults.Style.PRIMARY,
                        size = ESProgressIndicatorDefaults.Size.LARGE,
                    )
                }

                is GameState.Available -> WaitingRoomContent(
                    state = state,
                    uiState = uiState,
                    onKickClicked = onKickClicked,
                    onLeaveClicked = onLeaveClicked,
                    onCancelClicked = onCancelClicked,
                )

                GameState.Deleted, GameState.Excluded -> {
                    ESText(
                        modifier = Modifier.fillMaxWidth(),
                        text = stringResource(
                            if (state == GameState.Deleted) Res.string.waiting_room_deleted else Res.string.waiting_room_excluded,
                        ),
                        style = ESTheme.typography.body,
                    )

                    Spacer(modifier = Modifier.height(ESTheme.spacing.l))

                    ESButton(
                        modifier = Modifier.fillMaxWidth(),
                        text = stringResource(Res.string.action_back_to_home),
                        onClick = onBackClicked,
                        style = ESButtonDefaults.Style.PRIMARY,
                        size = ESButtonDefaults.Size.LARGE,
                    )
                }
            }
        }
    }

    WaitingRoomDialogs(
        uiState = uiState,
        onKickDismissed = onKickDismissed,
        onKickConfirmed = onKickConfirmed,
        onLeaveDismissed = onLeaveDismissed,
        onLeaveConfirmed = onLeaveConfirmed,
        onCancelDismissed = onCancelDismissed,
        onCancelConfirmed = onCancelConfirmed,
    )
}

@Composable
private fun WaitingRoomContent(
    state: GameState.Available,
    uiState: WaitingRoomUIState,
    onKickClicked: (GameMember) -> Unit,
    onLeaveClicked: () -> Unit,
    onCancelClicked: () -> Unit,
) {
    val game = state.game

    JoinCodeCard(game = game)

    Spacer(modifier = Modifier.height(ESTheme.spacing.l))

    ESText(
        text = stringResource(Res.string.waiting_room_master),
        style = ESTheme.typography.subtitle,
    )

    Spacer(modifier = Modifier.height(ESTheme.spacing.s))

    ESCard(
        modifier = Modifier.fillMaxWidth(),
        style = ESCardDefaults.Style.STANDARD,
    ) {
        GameMemberRow(member = game.master)
    }

    Spacer(modifier = Modifier.height(ESTheme.spacing.l))

    ESText(
        text = stringResource(Res.string.waiting_room_players),
        style = ESTheme.typography.subtitle,
    )

    Spacer(modifier = Modifier.height(ESTheme.spacing.s))

    if (state.players.isEmpty()) {
        ESText(
            text = stringResource(Res.string.waiting_room_no_players),
            style = ESTheme.typography.body,
            color = ESTheme.colors.textSecondary,
        )
    } else {
        ESCard(
            modifier = Modifier.fillMaxWidth(),
            style = ESCardDefaults.Style.STANDARD,
        ) {
            state.players.forEachIndexed { index, player ->
                if (index > 0) {
                    ESDivider()
                }

                GameMemberRow(
                    member = player,
                    onKickClicked = if (game.isMaster) {
                        { onKickClicked(player) }
                    } else {
                        null
                    },
                )
            }
        }
    }

    Spacer(modifier = Modifier.height(ESTheme.spacing.xl))

    if (game.isMaster) {
        ESButton(
            modifier = Modifier.fillMaxWidth(),
            text = stringResource(Res.string.action_cancel_game),
            onClick = onCancelClicked,
            style = ESButtonDefaults.Style.DESTRUCTIVE,
            size = ESButtonDefaults.Size.LARGE,
            isLoading = uiState.isCancelling,
        )
    } else {
        ESText(
            modifier = Modifier.fillMaxWidth(),
            text = stringResource(Res.string.waiting_room_start_by_master),
            style = ESTheme.typography.bodySmall,
            color = ESTheme.colors.textSecondary,
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(ESTheme.spacing.m))

        ESButton(
            modifier = Modifier.fillMaxWidth(),
            text = stringResource(Res.string.action_leave_game),
            onClick = onLeaveClicked,
            style = ESButtonDefaults.Style.SECONDARY,
            size = ESButtonDefaults.Size.LARGE,
            isLoading = uiState.isLeaving,
        )
    }
}

/** The code to give to the players, large enough to be read from across the room. */
@Composable
private fun JoinCodeCard(game: Game) {
    ESCard(
        modifier = Modifier.fillMaxWidth(),
        style = ESCardDefaults.Style.STANDARD,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(ESTheme.spacing.m),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            ESText(
                text = stringResource(Res.string.field_join_code),
                style = ESTheme.typography.caption,
                color = ESTheme.colors.textSecondary,
            )

            Spacer(modifier = Modifier.height(ESTheme.spacing.xs))

            ESText(
                // Read character by character, as it is typed
                modifier = Modifier.semantics { contentDescription = game.joinCode.toList().joinToString(" ") },
                text = "${game.joinCode.take(3)} ${game.joinCode.drop(3)}",
                style = ESTheme.typography.headline,
                color = ESTheme.colors.primary,
            )

            Spacer(modifier = Modifier.height(ESTheme.spacing.xs))

            ESText(
                text = "${durationText(minutes = game.durationMinutes)} · " +
                    playersText(playerCount = game.playerCount, maxPlayers = game.maxPlayers),
                style = ESTheme.typography.bodySmall,
                color = ESTheme.colors.textSecondary,
            )

            Spacer(modifier = Modifier.height(ESTheme.spacing.s))

            ESText(
                text = stringResource(Res.string.waiting_room_code_help),
                style = ESTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun WaitingRoomDialogs(
    uiState: WaitingRoomUIState,
    onKickDismissed: () -> Unit,
    onKickConfirmed: () -> Unit,
    onLeaveDismissed: () -> Unit,
    onLeaveConfirmed: () -> Unit,
    onCancelDismissed: () -> Unit,
    onCancelConfirmed: () -> Unit,
) {
    val playerToKick = uiState.playerToKick
    if (playerToKick != null) {
        ESDialog(
            title = stringResource(Res.string.kick_player_dialog_title, playerToKick.username),
            message = stringResource(Res.string.kick_player_dialog_message, playerToKick.username),
            confirmText = stringResource(Res.string.action_kick),
            onConfirm = onKickConfirmed,
            dismissText = stringResource(Res.string.action_cancel),
            onDismiss = onKickDismissed,
            style = ESDialogDefaults.Style.DESTRUCTIVE,
            isConfirmLoading = uiState.isKicking,
        )
    }

    if (uiState.isLeaveDialogShown) {
        ESDialog(
            title = stringResource(Res.string.leave_game_dialog_title),
            message = stringResource(Res.string.leave_game_dialog_message),
            confirmText = stringResource(Res.string.action_leave),
            onConfirm = onLeaveConfirmed,
            dismissText = stringResource(Res.string.action_cancel),
            onDismiss = onLeaveDismissed,
            style = ESDialogDefaults.Style.STANDARD,
            isConfirmLoading = uiState.isLeaving,
        )
    }

    if (uiState.isCancelDialogShown) {
        ESDialog(
            title = stringResource(Res.string.cancel_game_dialog_title),
            message = stringResource(Res.string.cancel_game_dialog_message),
            confirmText = stringResource(Res.string.action_cancel_game),
            onConfirm = onCancelConfirmed,
            dismissText = stringResource(Res.string.action_keep_game),
            onDismiss = onCancelDismissed,
            style = ESDialogDefaults.Style.DESTRUCTIVE,
            isConfirmLoading = uiState.isCancelling,
        )
    }
}

private val previewMaster = GameMember(userId = "master", username = "Maître", avatarId = 3, isCurrentUser = true)

private val previewGame = Game(
    id = "manor",
    title = "Le manoir",
    durationMinutes = 90,
    maxPlayers = 6,
    master = previewMaster,
    playerCount = 2,
    joinCode = "ABC234",
)

@Preview
@Composable
private fun PreviewWaitingRoomScreenForTheMaster() {
    ESTheme {
        WaitingRoomScreen(
            uiState = WaitingRoomUIState(
                state = GameState.Available(
                    game = previewGame,
                    players = listOf(
                        GameMember(userId = "a", username = "Joueuse", avatarId = 8, isCurrentUser = false),
                        GameMember(userId = "b", username = "Énigme", avatarId = 12, isCurrentUser = false),
                    ),
                ),
            ),
            snackbarState = rememberESSnackbarState(),
            onBackClicked = {},
            onKickClicked = {},
            onKickDismissed = {},
            onKickConfirmed = {},
            onLeaveClicked = {},
            onLeaveDismissed = {},
            onLeaveConfirmed = {},
            onCancelClicked = {},
            onCancelDismissed = {},
            onCancelConfirmed = {},
        )
    }
}

@Preview
@Composable
private fun PreviewWaitingRoomScreenForAPlayer() {
    ESTheme {
        WaitingRoomScreen(
            uiState = WaitingRoomUIState(
                state = GameState.Available(
                    game = previewGame.copy(master = previewMaster.copy(isCurrentUser = false)),
                    players = listOf(GameMember(userId = "a", username = "Joueuse", avatarId = 8, isCurrentUser = true)),
                ),
            ),
            snackbarState = rememberESSnackbarState(),
            onBackClicked = {},
            onKickClicked = {},
            onKickDismissed = {},
            onKickConfirmed = {},
            onLeaveClicked = {},
            onLeaveDismissed = {},
            onLeaveConfirmed = {},
            onCancelClicked = {},
            onCancelDismissed = {},
            onCancelConfirmed = {},
        )
    }
}

@Preview
@Composable
private fun PreviewWaitingRoomScreenDeleted() {
    ESTheme {
        WaitingRoomScreen(
            uiState = WaitingRoomUIState(state = GameState.Deleted),
            snackbarState = rememberESSnackbarState(),
            onBackClicked = {},
            onKickClicked = {},
            onKickDismissed = {},
            onKickConfirmed = {},
            onLeaveClicked = {},
            onLeaveDismissed = {},
            onLeaveConfirmed = {},
            onCancelClicked = {},
            onCancelDismissed = {},
            onCancelConfirmed = {},
        )
    }
}
