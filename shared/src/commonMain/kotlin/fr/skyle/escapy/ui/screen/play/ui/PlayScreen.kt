package fr.skyle.escapy.ui.screen.play.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
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
import fr.skyle.escapy.designsystem.core.row.ESRow
import fr.skyle.escapy.designsystem.core.row.ESRowDefaults
import fr.skyle.escapy.designsystem.core.screenstructure.ESScreenStructure
import fr.skyle.escapy.designsystem.core.screenstructure.ESScreenStructureDefaults
import fr.skyle.escapy.designsystem.core.snackbar.ESSnackbarState
import fr.skyle.escapy.designsystem.core.snackbar.rememberESSnackbarState
import fr.skyle.escapy.designsystem.core.stepper.ESStepper
import fr.skyle.escapy.designsystem.core.stepper.ESStepperDefaults
import fr.skyle.escapy.designsystem.core.text.ESText
import fr.skyle.escapy.designsystem.core.topappbar.ESTopAppBar
import fr.skyle.escapy.designsystem.theme.ESTheme
import fr.skyle.escapy.domain.model.Game
import fr.skyle.escapy.domain.model.GameEnd
import fr.skyle.escapy.domain.model.GameMember
import fr.skyle.escapy.domain.model.GameResult
import fr.skyle.escapy.domain.model.GameState
import fr.skyle.escapy.domain.model.GameStatus
import fr.skyle.escapy.domain.model.GameTimer
import fr.skyle.escapy.domain.model.Mission
import fr.skyle.escapy.resources.Res
import fr.skyle.escapy.resources.action_add
import fr.skyle.escapy.resources.action_add_time
import fr.skyle.escapy.resources.action_back_to_home
import fr.skyle.escapy.resources.action_cancel
import fr.skyle.escapy.resources.action_delete_game
import fr.skyle.escapy.resources.action_end_game
import fr.skyle.escapy.resources.action_keep_game
import fr.skyle.escapy.resources.action_keep_playing
import fr.skyle.escapy.resources.action_kick
import fr.skyle.escapy.resources.action_leave
import fr.skyle.escapy.resources.action_leave_game
import fr.skyle.escapy.resources.action_pause
import fr.skyle.escapy.resources.action_resume
import fr.skyle.escapy.resources.action_validate
import fr.skyle.escapy.resources.add_time_dialog_message
import fr.skyle.escapy.resources.add_time_fewer
import fr.skyle.escapy.resources.add_time_minutes
import fr.skyle.escapy.resources.add_time_more
import fr.skyle.escapy.resources.chat_preview_empty
import fr.skyle.escapy.resources.chat_preview_message
import fr.skyle.escapy.resources.chat_title
import fr.skyle.escapy.resources.delete_game_dialog_message
import fr.skyle.escapy.resources.delete_game_dialog_title
import fr.skyle.escapy.resources.end_game_dialog_message
import fr.skyle.escapy.resources.end_game_dialog_title
import fr.skyle.escapy.resources.game_status_paused
import fr.skyle.escapy.resources.kick_player_dialog_message
import fr.skyle.escapy.resources.kick_player_dialog_title
import fr.skyle.escapy.resources.leave_game_dialog_message
import fr.skyle.escapy.resources.leave_game_dialog_title
import fr.skyle.escapy.resources.play_ended_by_master
import fr.skyle.escapy.resources.play_ended_title
import fr.skyle.escapy.resources.play_paused
import fr.skyle.escapy.resources.play_recap_added
import fr.skyle.escapy.resources.play_recap_played
import fr.skyle.escapy.resources.play_time_left
import fr.skyle.escapy.resources.play_time_up_message
import fr.skyle.escapy.resources.play_time_up_title
import fr.skyle.escapy.resources.play_won_message
import fr.skyle.escapy.resources.play_won_title
import fr.skyle.escapy.resources.validate_mission_dialog_message
import fr.skyle.escapy.resources.validate_mission_dialog_title
import fr.skyle.escapy.resources.waiting_room_deleted
import fr.skyle.escapy.resources.waiting_room_excluded
import fr.skyle.escapy.resources.waiting_room_master
import fr.skyle.escapy.resources.waiting_room_players
import fr.skyle.escapy.ui.core.game.GameMemberRow
import fr.skyle.escapy.ui.core.game.durationText
import fr.skyle.escapy.ui.core.game.timerText
import fr.skyle.escapy.ui.core.mission.MissionsSection
import fr.skyle.escapy.ui.screen.play.ui.PlayViewModel.PlayAction
import fr.skyle.escapy.ui.screen.play.ui.PlayViewModel.PlayUIState
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant
import org.jetbrains.compose.resources.stringResource

/** The minutes the game master may add at once, one step of the stepper each. */
internal val ADDED_MINUTES_CHOICES = listOf(1, 2, 3, 5, 10, 15, 20, 30, 45, 60)

/** The most minutes added to a game in all, as the server allows. */
internal const val ADDED_MINUTES_TOTAL_MAX = 180

@Composable
fun PlayScreen(
    uiState: PlayUIState,
    playerToKick: GameMember?,
    // The minutes the add-time dialog shows, or null when it is closed
    minutesToAdd: Int?,
    isEndDialogShown: Boolean,
    isLeaveDialogShown: Boolean,
    isDeleteDialogShown: Boolean,
    // The mission the validation dialog is about, or null when it is closed
    missionToValidate: Mission?,
    answerDrafts: Map<String, String>,
    snackbarState: ESSnackbarState,
    onBackClicked: () -> Unit,
    onChatClicked: () -> Unit,
    onAnswerChanged: (missionId: String, answer: String) -> Unit,
    onAnswerSubmitted: (missionId: String, answer: String) -> Unit,
    onAddMissionClicked: () -> Unit,
    onMissionClicked: (missionId: String) -> Unit,
    onUnlockClicked: (missionId: String) -> Unit,
    onValidateClicked: (missionId: String) -> Unit,
    onValidateDismissed: () -> Unit,
    onValidateConfirmed: () -> Unit,
    onPauseClicked: () -> Unit,
    onResumeClicked: () -> Unit,
    onAddTimeClicked: () -> Unit,
    onMinutesToAddChanged: (Int) -> Unit,
    onAddTimeDismissed: () -> Unit,
    onAddTimeConfirmed: () -> Unit,
    onEndClicked: () -> Unit,
    onEndDismissed: () -> Unit,
    onEndConfirmed: () -> Unit,
    onKickClicked: (GameMember) -> Unit,
    onKickDismissed: () -> Unit,
    onKickConfirmed: () -> Unit,
    onLeaveClicked: () -> Unit,
    onLeaveDismissed: () -> Unit,
    onLeaveConfirmed: () -> Unit,
    onDeleteClicked: () -> Unit,
    onDeleteDismissed: () -> Unit,
    onDeleteConfirmed: () -> Unit,
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
        val game = (state as? GameState.Available)?.game
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            // Always in sight while the game is played: the content scrolls under it
            if (game != null && game.status != GameStatus.FINISHED) {
                TimerCard(
                    modifier = Modifier.padding(start = ESTheme.spacing.l, end = ESTheme.spacing.l, top = ESTheme.spacing.l),
                    game = game,
                    uiState = uiState,
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(ESTheme.spacing.l),
            ) {
                when (state) {
                    null -> Loading()

                    is GameState.Available -> if (state.game.status == GameStatus.FINISHED) {
                        EndContent(
                            state = state,
                            uiState = uiState,
                            onBackClicked = onBackClicked,
                            onChatClicked = onChatClicked,
                            onLeaveClicked = onLeaveClicked,
                            onDeleteClicked = onDeleteClicked,
                        )
                    } else {
                        PlayContent(
                            state = state,
                            uiState = uiState,
                            answerDrafts = answerDrafts,
                            onChatClicked = onChatClicked,
                            onAnswerChanged = onAnswerChanged,
                            onAnswerSubmitted = onAnswerSubmitted,
                            onAddMissionClicked = onAddMissionClicked,
                            onMissionClicked = onMissionClicked,
                            onUnlockClicked = onUnlockClicked,
                            onValidateClicked = onValidateClicked,
                            onPauseClicked = onPauseClicked,
                            onResumeClicked = onResumeClicked,
                            onAddTimeClicked = onAddTimeClicked,
                            onEndClicked = onEndClicked,
                            onKickClicked = onKickClicked,
                            onLeaveClicked = onLeaveClicked,
                        )
                    }

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
    }

    if (missionToValidate != null) {
        ESDialog(
            title = stringResource(Res.string.validate_mission_dialog_title),
            message = stringResource(Res.string.validate_mission_dialog_message),
            confirmText = stringResource(Res.string.action_validate),
            onConfirm = onValidateConfirmed,
            dismissText = stringResource(Res.string.action_cancel),
            onDismiss = onValidateDismissed,
            style = ESDialogDefaults.Style.STANDARD,
            isConfirmLoading = uiState.action == PlayAction.VALIDATE,
        )
    }

    PlayDialogs(
        uiState = uiState,
        playerToKick = playerToKick,
        minutesToAdd = minutesToAdd,
        isEndDialogShown = isEndDialogShown,
        isLeaveDialogShown = isLeaveDialogShown,
        isDeleteDialogShown = isDeleteDialogShown,
        onMinutesToAddChanged = onMinutesToAddChanged,
        onAddTimeDismissed = onAddTimeDismissed,
        onAddTimeConfirmed = onAddTimeConfirmed,
        onEndDismissed = onEndDismissed,
        onEndConfirmed = onEndConfirmed,
        onKickDismissed = onKickDismissed,
        onKickConfirmed = onKickConfirmed,
        onLeaveDismissed = onLeaveDismissed,
        onLeaveConfirmed = onLeaveConfirmed,
        onDeleteDismissed = onDeleteDismissed,
        onDeleteConfirmed = onDeleteConfirmed,
    )
}

@Composable
private fun Loading() {
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center,
    ) {
        ESProgressIndicator(
            style = ESProgressIndicatorDefaults.Style.PRIMARY,
            size = ESProgressIndicatorDefaults.Size.LARGE,
        )
    }
}

@Composable
private fun PlayContent(
    state: GameState.Available,
    uiState: PlayUIState,
    answerDrafts: Map<String, String>,
    onChatClicked: () -> Unit,
    onAnswerChanged: (missionId: String, answer: String) -> Unit,
    onAnswerSubmitted: (missionId: String, answer: String) -> Unit,
    onAddMissionClicked: () -> Unit,
    onMissionClicked: (missionId: String) -> Unit,
    onUnlockClicked: (missionId: String) -> Unit,
    onValidateClicked: (missionId: String) -> Unit,
    onPauseClicked: () -> Unit,
    onResumeClicked: () -> Unit,
    onAddTimeClicked: () -> Unit,
    onEndClicked: () -> Unit,
    onKickClicked: (GameMember) -> Unit,
    onLeaveClicked: () -> Unit,
) {
    val game = state.game

    ChatCard(uiState = uiState, onClick = onChatClicked)

    Spacer(modifier = Modifier.height(ESTheme.spacing.l))

    if (game.isMaster) {
        val isPaused = game.status == GameStatus.PAUSED
        ESButton(
            modifier = Modifier.fillMaxWidth(),
            text = stringResource(if (isPaused) Res.string.action_resume else Res.string.action_pause),
            onClick = if (isPaused) onResumeClicked else onPauseClicked,
            style = ESButtonDefaults.Style.PRIMARY,
            size = ESButtonDefaults.Size.LARGE,
            isLoading = uiState.action == PlayAction.PAUSE || uiState.action == PlayAction.RESUME,
        )

        // Hidden once the server would refuse any more
        if (game.timer.addedMinutes + ADDED_MINUTES_CHOICES.first() <= ADDED_MINUTES_TOTAL_MAX) {
            Spacer(modifier = Modifier.height(ESTheme.spacing.m))

            ESButton(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(Res.string.action_add_time),
                onClick = onAddTimeClicked,
                style = ESButtonDefaults.Style.SECONDARY,
                size = ESButtonDefaults.Size.LARGE,
            )
        }

        Spacer(modifier = Modifier.height(ESTheme.spacing.m))

        ESButton(
            modifier = Modifier.fillMaxWidth(),
            text = stringResource(Res.string.action_end_game),
            onClick = onEndClicked,
            style = ESButtonDefaults.Style.DESTRUCTIVE,
            size = ESButtonDefaults.Size.LARGE,
        )

        Spacer(modifier = Modifier.height(ESTheme.spacing.l))
    }

    MissionsSection(
        missions = uiState.missions,
        isMaster = game.isMaster,
        isStarted = true,
        answerDrafts = answerDrafts,
        answeringMissionId = uiState.answeringMissionId,
        actionMissionId = uiState.actionMissionId,
        onAnswerChanged = onAnswerChanged,
        onAnswerSubmitted = onAnswerSubmitted,
        onAddMissionClicked = onAddMissionClicked,
        onMissionClicked = onMissionClicked,
        onUnlockClicked = onUnlockClicked,
        onValidateClicked = onValidateClicked,
    )

    Spacer(modifier = Modifier.height(ESTheme.spacing.l))

    Members(state = state, onKickClicked = if (game.isMaster) onKickClicked else null)

    if (!game.isMaster) {
        Spacer(modifier = Modifier.height(ESTheme.spacing.xl))

        ESButton(
            modifier = Modifier.fillMaxWidth(),
            text = stringResource(Res.string.action_leave_game),
            onClick = onLeaveClicked,
            style = ESButtonDefaults.Style.SECONDARY,
            size = ESButtonDefaults.Size.LARGE,
            isLoading = uiState.action == PlayAction.LEAVE,
        )
    }
}

/** The time left, large enough to be read from across the room. */
@Composable
private fun TimerCard(game: Game, uiState: PlayUIState, modifier: Modifier = Modifier) {
    ESCard(
        modifier = modifier.fillMaxWidth(),
        style = ESCardDefaults.Style.STANDARD,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(ESTheme.spacing.m),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            ESText(
                text = stringResource(Res.string.play_time_left),
                style = ESTheme.typography.caption,
                color = ESTheme.colors.textSecondary,
            )

            Spacer(modifier = Modifier.height(ESTheme.spacing.xs))

            val remaining = uiState.remaining
            if (remaining == null) {
                ESProgressIndicator(
                    style = ESProgressIndicatorDefaults.Style.PRIMARY,
                    size = ESProgressIndicatorDefaults.Size.LARGE,
                )
            } else {
                ESText(
                    text = timerText(duration = remaining),
                    style = ESTheme.typography.display,
                    color = if (game.status == GameStatus.PAUSED) ESTheme.colors.textSecondary else ESTheme.colors.primary,
                )
            }

            if (game.status == GameStatus.PAUSED) {
                Spacer(modifier = Modifier.height(ESTheme.spacing.xs))

                ESText(
                    // Told by a screen reader as soon as the game master pauses
                    modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite },
                    text = stringResource(if (game.isMaster) Res.string.game_status_paused else Res.string.play_paused),
                    style = ESTheme.typography.label,
                    color = ESTheme.colors.textPrimary,
                )
            }
        }
    }
}

@Composable
private fun EndContent(
    state: GameState.Available,
    uiState: PlayUIState,
    onBackClicked: () -> Unit,
    onChatClicked: () -> Unit,
    onLeaveClicked: () -> Unit,
    onDeleteClicked: () -> Unit,
) {
    val game = state.game
    val end = game.end

    ESText(
        modifier = Modifier
            .fillMaxWidth()
            .semantics { liveRegion = LiveRegionMode.Polite },
        text = stringResource(
            when (end?.result) {
                GameResult.WON -> Res.string.play_won_title
                GameResult.TIME_UP -> Res.string.play_time_up_title
                GameResult.ENDED_BY_MASTER, null -> Res.string.play_ended_title
            },
        ),
        style = ESTheme.typography.headline,
        textAlign = TextAlign.Center,
    )

    Spacer(modifier = Modifier.height(ESTheme.spacing.xs))

    ESText(
        modifier = Modifier.fillMaxWidth(),
        text = stringResource(
            when (end?.result) {
                GameResult.WON -> Res.string.play_won_message
                GameResult.TIME_UP -> Res.string.play_time_up_message
                GameResult.ENDED_BY_MASTER, null -> Res.string.play_ended_by_master
            },
        ),
        style = ESTheme.typography.body,
        color = ESTheme.colors.textSecondary,
        textAlign = TextAlign.Center,
    )

    Spacer(modifier = Modifier.height(ESTheme.spacing.l))

    if (end != null) {
        ESCard(
            modifier = Modifier.fillMaxWidth(),
            style = ESCardDefaults.Style.STANDARD,
        ) {
            RecapRow(label = stringResource(Res.string.play_recap_played), value = timerText(duration = end.played))
            if (game.timer.addedMinutes > 0) {
                ESDivider()
                RecapRow(label = stringResource(Res.string.play_recap_added), value = durationText(minutes = game.timer.addedMinutes))
            }
        }

        Spacer(modifier = Modifier.height(ESTheme.spacing.l))
    }

    ChatCard(uiState = uiState, onClick = onChatClicked)

    Spacer(modifier = Modifier.height(ESTheme.spacing.l))

    Members(state = state, onKickClicked = null)

    Spacer(modifier = Modifier.height(ESTheme.spacing.xl))

    ESButton(
        modifier = Modifier.fillMaxWidth(),
        text = stringResource(Res.string.action_back_to_home),
        onClick = onBackClicked,
        style = ESButtonDefaults.Style.PRIMARY,
        size = ESButtonDefaults.Size.LARGE,
    )

    Spacer(modifier = Modifier.height(ESTheme.spacing.m))

    if (game.isMaster) {
        ESButton(
            modifier = Modifier.fillMaxWidth(),
            text = stringResource(Res.string.action_delete_game),
            onClick = onDeleteClicked,
            style = ESButtonDefaults.Style.DESTRUCTIVE,
            size = ESButtonDefaults.Size.LARGE,
            isLoading = uiState.action == PlayAction.DELETE,
        )
    } else {
        ESButton(
            modifier = Modifier.fillMaxWidth(),
            text = stringResource(Res.string.action_leave_game),
            onClick = onLeaveClicked,
            style = ESButtonDefaults.Style.SECONDARY,
            size = ESButtonDefaults.Size.LARGE,
            isLoading = uiState.action == PlayAction.LEAVE,
        )
    }
}

/** The latest message of the chat, which opens it. */
@Composable
private fun ChatCard(uiState: PlayUIState, onClick: () -> Unit) {
    val lastMessage = uiState.lastMessage
    ESCard(
        modifier = Modifier.fillMaxWidth(),
        style = ESCardDefaults.Style.STANDARD,
    ) {
        ESRow(
            label = stringResource(Res.string.chat_title),
            supportingText = when {
                lastMessage != null -> stringResource(Res.string.chat_preview_message, lastMessage.authorUsername, lastMessage.text)
                uiState.hasReadChat -> stringResource(Res.string.chat_preview_empty)
                else -> null
            },
            onClick = onClick,
            style = ESRowDefaults.Style.STANDARD,
        )
    }
}

@Composable
private fun RecapRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(ESTheme.spacing.m),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ESText(
            modifier = Modifier.weight(1f),
            text = label,
            style = ESTheme.typography.body,
        )
        ESText(
            text = value,
            style = ESTheme.typography.subtitle,
            color = ESTheme.colors.primary,
        )
    }
}

@Composable
private fun Members(state: GameState.Available, onKickClicked: ((GameMember) -> Unit)?) {
    ESText(
        text = stringResource(Res.string.waiting_room_master),
        style = ESTheme.typography.subtitle,
    )

    Spacer(modifier = Modifier.height(ESTheme.spacing.s))

    ESCard(
        modifier = Modifier.fillMaxWidth(),
        style = ESCardDefaults.Style.STANDARD,
    ) {
        GameMemberRow(member = state.game.master)
    }

    if (state.players.isNotEmpty()) {
        Spacer(modifier = Modifier.height(ESTheme.spacing.l))

        ESText(
            text = stringResource(Res.string.waiting_room_players),
            style = ESTheme.typography.subtitle,
        )

        Spacer(modifier = Modifier.height(ESTheme.spacing.s))

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
                    onKickClicked = onKickClicked?.let { kick -> { kick(player) } },
                )
            }
        }
    }
}

@Composable
private fun PlayDialogs(
    uiState: PlayUIState,
    playerToKick: GameMember?,
    minutesToAdd: Int?,
    isEndDialogShown: Boolean,
    isLeaveDialogShown: Boolean,
    isDeleteDialogShown: Boolean,
    onMinutesToAddChanged: (Int) -> Unit,
    onAddTimeDismissed: () -> Unit,
    onAddTimeConfirmed: () -> Unit,
    onEndDismissed: () -> Unit,
    onEndConfirmed: () -> Unit,
    onKickDismissed: () -> Unit,
    onKickConfirmed: () -> Unit,
    onLeaveDismissed: () -> Unit,
    onLeaveConfirmed: () -> Unit,
    onDeleteDismissed: () -> Unit,
    onDeleteConfirmed: () -> Unit,
) {
    if (minutesToAdd != null) {
        val addedMinutes = (uiState.state as? GameState.Available)?.game?.timer?.addedMinutes ?: 0
        val choices = ADDED_MINUTES_CHOICES.filter { minutes -> addedMinutes + minutes <= ADDED_MINUTES_TOTAL_MAX }
        val index = choices.indexOf(minutesToAdd)
        ESDialog(
            title = stringResource(Res.string.action_add_time),
            message = stringResource(Res.string.add_time_dialog_message),
            confirmText = stringResource(Res.string.action_add),
            onConfirm = onAddTimeConfirmed,
            dismissText = stringResource(Res.string.action_cancel),
            onDismiss = onAddTimeDismissed,
            style = ESDialogDefaults.Style.STANDARD,
            isConfirmLoading = uiState.action == PlayAction.ADD_TIME,
        ) {
            ESStepper(
                label = stringResource(Res.string.add_time_minutes),
                value = durationText(minutes = minutesToAdd),
                onDecrease = { choices.getOrNull(index - 1)?.let(onMinutesToAddChanged) },
                onIncrease = { choices.getOrNull(index + 1)?.let(onMinutesToAddChanged) },
                decreaseContentDescription = stringResource(Res.string.add_time_fewer),
                increaseContentDescription = stringResource(Res.string.add_time_more),
                style = ESStepperDefaults.Style.STANDARD,
                size = ESStepperDefaults.Size.MEDIUM,
                isDecreaseEnabled = index > 0,
                isIncreaseEnabled = index in 0 until choices.lastIndex,
            )
        }
    }

    if (isEndDialogShown) {
        ESDialog(
            title = stringResource(Res.string.end_game_dialog_title),
            message = stringResource(Res.string.end_game_dialog_message),
            confirmText = stringResource(Res.string.action_end_game),
            onConfirm = onEndConfirmed,
            dismissText = stringResource(Res.string.action_keep_playing),
            onDismiss = onEndDismissed,
            style = ESDialogDefaults.Style.DESTRUCTIVE,
            isConfirmLoading = uiState.action == PlayAction.END,
        )
    }

    if (playerToKick != null) {
        ESDialog(
            title = stringResource(Res.string.kick_player_dialog_title, playerToKick.username),
            message = stringResource(Res.string.kick_player_dialog_message, playerToKick.username),
            confirmText = stringResource(Res.string.action_kick),
            onConfirm = onKickConfirmed,
            dismissText = stringResource(Res.string.action_cancel),
            onDismiss = onKickDismissed,
            style = ESDialogDefaults.Style.DESTRUCTIVE,
            isConfirmLoading = uiState.action == PlayAction.KICK,
        )
    }

    if (isLeaveDialogShown) {
        ESDialog(
            title = stringResource(Res.string.leave_game_dialog_title),
            message = stringResource(Res.string.leave_game_dialog_message),
            confirmText = stringResource(Res.string.action_leave),
            onConfirm = onLeaveConfirmed,
            dismissText = stringResource(Res.string.action_cancel),
            onDismiss = onLeaveDismissed,
            style = ESDialogDefaults.Style.STANDARD,
            isConfirmLoading = uiState.action == PlayAction.LEAVE,
        )
    }

    if (isDeleteDialogShown) {
        ESDialog(
            title = stringResource(Res.string.delete_game_dialog_title),
            message = stringResource(Res.string.delete_game_dialog_message),
            confirmText = stringResource(Res.string.action_delete_game),
            onConfirm = onDeleteConfirmed,
            dismissText = stringResource(Res.string.action_keep_game),
            onDismiss = onDeleteDismissed,
            style = ESDialogDefaults.Style.DESTRUCTIVE,
            isConfirmLoading = uiState.action == PlayAction.DELETE,
        )
    }
}

private val previewMaster = GameMember(userId = "master", username = "Maître", avatarId = 3, isCurrentUser = true)

private val previewPlayers = listOf(
    GameMember(userId = "a", username = "Joueuse", avatarId = 8, isCurrentUser = false),
    GameMember(userId = "b", username = "Énigme", avatarId = 12, isCurrentUser = false),
)

private val previewGame = Game(
    id = "manor",
    title = "Le manoir",
    durationMinutes = 60,
    maxPlayers = null,
    master = previewMaster,
    playerCount = 2,
    joinCode = "ABC234",
    status = GameStatus.RUNNING,
    timer = GameTimer(endsAt = Instant.fromEpochMilliseconds(0), remaining = null, addedMinutes = 0),
    end = null,
)

@Composable
private fun PreviewPlayScreen(uiState: PlayUIState) {
    ESTheme {
        PlayScreen(
            uiState = uiState,
            playerToKick = null,
            minutesToAdd = null,
            isEndDialogShown = false,
            isLeaveDialogShown = false,
            isDeleteDialogShown = false,
            missionToValidate = null,
            answerDrafts = emptyMap(),
            snackbarState = rememberESSnackbarState(),
            onBackClicked = {},
            onChatClicked = {},
            onAnswerChanged = { _, _ -> },
            onAnswerSubmitted = { _, _ -> },
            onAddMissionClicked = {},
            onMissionClicked = {},
            onUnlockClicked = {},
            onValidateClicked = {},
            onValidateDismissed = {},
            onValidateConfirmed = {},
            onPauseClicked = {},
            onResumeClicked = {},
            onAddTimeClicked = {},
            onMinutesToAddChanged = {},
            onAddTimeDismissed = {},
            onAddTimeConfirmed = {},
            onEndClicked = {},
            onEndDismissed = {},
            onEndConfirmed = {},
            onKickClicked = {},
            onKickDismissed = {},
            onKickConfirmed = {},
            onLeaveClicked = {},
            onLeaveDismissed = {},
            onLeaveConfirmed = {},
            onDeleteClicked = {},
            onDeleteDismissed = {},
            onDeleteConfirmed = {},
        )
    }
}

@Preview
@Composable
private fun PreviewPlayScreenForTheMaster() {
    PreviewPlayScreen(
        uiState = PlayUIState(
            state = GameState.Available(game = previewGame, players = previewPlayers),
            remaining = 42.minutes + 7.seconds,
        ),
    )
}

@Preview
@Composable
private fun PreviewPlayScreenPausedForAPlayer() {
    PreviewPlayScreen(
        uiState = PlayUIState(
            state = GameState.Available(
                game = previewGame.copy(
                    master = previewMaster.copy(isCurrentUser = false),
                    status = GameStatus.PAUSED,
                ),
                players = previewPlayers,
            ),
            remaining = 1.minutes + 30.seconds,
        ),
    )
}

@Preview
@Composable
private fun PreviewPlayScreenOver() {
    PreviewPlayScreen(
        uiState = PlayUIState(
            state = GameState.Available(
                game = previewGame.copy(
                    status = GameStatus.FINISHED,
                    timer = GameTimer(endsAt = null, remaining = null, addedMinutes = 10),
                    end = GameEnd(result = GameResult.TIME_UP, played = 70.minutes),
                ),
                players = previewPlayers,
            ),
        ),
    )
}
