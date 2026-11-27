package fr.skyle.escapy.ui.core.mission

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import fr.skyle.escapy.designsystem.core.button.ESButton
import fr.skyle.escapy.designsystem.core.button.ESButtonDefaults
import fr.skyle.escapy.designsystem.core.card.ESCard
import fr.skyle.escapy.designsystem.core.card.ESCardDefaults
import fr.skyle.escapy.designsystem.core.progress.ESProgressIndicator
import fr.skyle.escapy.designsystem.core.progress.ESProgressIndicatorDefaults
import fr.skyle.escapy.designsystem.core.text.ESText
import fr.skyle.escapy.designsystem.core.textfield.ESTextField
import fr.skyle.escapy.designsystem.core.textfield.ESTextFieldDefaults
import fr.skyle.escapy.designsystem.theme.ESTheme
import fr.skyle.escapy.domain.model.Mission
import fr.skyle.escapy.domain.model.MissionState
import fr.skyle.escapy.domain.model.MissionType
import fr.skyle.escapy.resources.Res
import fr.skyle.escapy.resources.action_add_mission
import fr.skyle.escapy.resources.action_submit_answer
import fr.skyle.escapy.resources.action_unlock
import fr.skyle.escapy.resources.action_validate
import fr.skyle.escapy.resources.mission_answer_field
import fr.skyle.escapy.resources.mission_final
import fr.skyle.escapy.resources.mission_state_active
import fr.skyle.escapy.resources.mission_state_locked
import fr.skyle.escapy.resources.mission_state_solved_by
import fr.skyle.escapy.resources.missions_empty_master
import fr.skyle.escapy.resources.missions_empty_player
import fr.skyle.escapy.resources.missions_title
import fr.skyle.escapy.resources.waiting_room_you
import org.jetbrains.compose.resources.stringResource

/**
 * The missions of a game. A player answers each active one in its card, and sees who solved the
 * others; the game master sees every mission, opens one to change it, adds one, and, once the game
 * started, unlocks and validates them.
 */
@Composable
fun MissionsSection(
    missions: List<Mission>?,
    isMaster: Boolean,
    isStarted: Boolean,
    answerDrafts: Map<String, String>,
    answeringMissionId: String?,
    actionMissionId: String?,
    onAnswerChanged: (missionId: String, answer: String) -> Unit,
    // The answer as shown: a code untouched is its wheels at 0
    onAnswerSubmitted: (missionId: String, answer: String) -> Unit,
    onAddMissionClicked: () -> Unit,
    onMissionClicked: (missionId: String) -> Unit,
    onUnlockClicked: (missionId: String) -> Unit,
    onValidateClicked: (missionId: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        ESText(
            text = stringResource(Res.string.missions_title),
            style = ESTheme.typography.subtitle,
        )

        Spacer(modifier = Modifier.height(ESTheme.spacing.s))

        when {
            missions == null -> ESProgressIndicator(
                style = ESProgressIndicatorDefaults.Style.PRIMARY,
                size = ESProgressIndicatorDefaults.Size.SMALL,
            )

            missions.isEmpty() -> ESText(
                text = stringResource(if (isMaster) Res.string.missions_empty_master else Res.string.missions_empty_player),
                style = ESTheme.typography.body,
                color = ESTheme.colors.textSecondary,
            )

            else -> Column(verticalArrangement = Arrangement.spacedBy(ESTheme.spacing.s)) {
                missions.forEach { mission ->
                    if (isMaster) {
                        MasterMissionCard(
                            mission = mission,
                            isStarted = isStarted,
                            isActing = actionMissionId == mission.id,
                            onClick = { onMissionClicked(mission.id) },
                            onUnlockClicked = { onUnlockClicked(mission.id) },
                            onValidateClicked = { onValidateClicked(mission.id) },
                        )
                    } else {
                        PlayerMissionCard(
                            mission = mission,
                            answer = answerDrafts[mission.id].orEmpty(),
                            isAnswering = answeringMissionId == mission.id,
                            onAnswerChanged = { answer -> onAnswerChanged(mission.id, answer) },
                            onAnswerSubmitted = { answer -> onAnswerSubmitted(mission.id, answer) },
                        )
                    }
                }
            }
        }

        if (isMaster) {
            Spacer(modifier = Modifier.height(ESTheme.spacing.m))

            ESButton(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(Res.string.action_add_mission),
                onClick = onAddMissionClicked,
                style = ESButtonDefaults.Style.SECONDARY,
                size = ESButtonDefaults.Size.LARGE,
            )
        }
    }
}

/** Where a mission is, for the game master: locked, in progress, or solved by someone. */
@Composable
private fun missionStateText(mission: Mission): String {
    val state = when (mission.state) {
        MissionState.LOCKED -> stringResource(Res.string.mission_state_locked)
        MissionState.ACTIVE -> stringResource(Res.string.mission_state_active)
        MissionState.SOLVED -> stringResource(Res.string.mission_state_solved_by, solverName(mission))
    }
    return if (mission.isFinal) "$state · ${stringResource(Res.string.mission_final)}" else state
}

@Composable
private fun solverName(mission: Mission): String {
    val solver = mission.solver ?: return ""
    return if (solver.isCurrentUser) stringResource(Res.string.waiting_room_you, solver.username) else solver.username
}

@Composable
private fun MasterMissionCard(
    mission: Mission,
    isStarted: Boolean,
    isActing: Boolean,
    onClick: () -> Unit,
    onUnlockClicked: () -> Unit,
    onValidateClicked: () -> Unit,
) {
    ESCard(
        modifier = Modifier.fillMaxWidth(),
        style = ESCardDefaults.Style.STANDARD,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(role = Role.Button, onClick = onClick)
                .padding(ESTheme.spacing.m),
        ) {
            ESText(text = mission.title, style = ESTheme.typography.subtitle)
            ESText(
                text = missionStateText(mission),
                style = ESTheme.typography.bodySmall,
                color = if (mission.state == MissionState.SOLVED) ESTheme.colors.success else ESTheme.colors.textSecondary,
            )

            if (isStarted && mission.state != MissionState.SOLVED) {
                Spacer(modifier = Modifier.height(ESTheme.spacing.s))

                Row(horizontalArrangement = Arrangement.spacedBy(ESTheme.spacing.s)) {
                    if (mission.state == MissionState.LOCKED) {
                        ESButton(
                            text = stringResource(Res.string.action_unlock),
                            onClick = onUnlockClicked,
                            style = ESButtonDefaults.Style.SECONDARY,
                            size = ESButtonDefaults.Size.MEDIUM,
                            isLoading = isActing,
                        )
                    }
                    ESButton(
                        text = stringResource(Res.string.action_validate),
                        onClick = onValidateClicked,
                        style = ESButtonDefaults.Style.SECONDARY,
                        size = ESButtonDefaults.Size.MEDIUM,
                    )
                }
            }
        }
    }
}

@Composable
private fun PlayerMissionCard(
    mission: Mission,
    answer: String,
    isAnswering: Boolean,
    onAnswerChanged: (String) -> Unit,
    onAnswerSubmitted: (String) -> Unit,
) {
    // A code shows its wheels at 0 until turned: that is the answer then
    val shownAnswer = if (mission.type == MissionType.CODE) answer.padEnd(mission.codeLength ?: 0, '0') else answer
    ESCard(
        modifier = Modifier.fillMaxWidth(),
        style = ESCardDefaults.Style.STANDARD,
    ) {
        Column(modifier = Modifier.padding(ESTheme.spacing.m)) {
            ESText(text = mission.title, style = ESTheme.typography.subtitle)
            if (mission.isFinal) {
                ESText(
                    text = stringResource(Res.string.mission_final),
                    style = ESTheme.typography.caption,
                    color = ESTheme.colors.primary,
                )
            }

            if (mission.state == MissionState.SOLVED) {
                ESText(
                    text = stringResource(Res.string.mission_state_solved_by, solverName(mission)),
                    style = ESTheme.typography.bodySmall,
                    color = ESTheme.colors.success,
                )
                return@Column
            }

            if (mission.statement.isNotEmpty()) {
                Spacer(modifier = Modifier.height(ESTheme.spacing.xs))

                ESText(text = mission.statement, style = ESTheme.typography.body)
            }

            Spacer(modifier = Modifier.height(ESTheme.spacing.m))

            when (mission.type) {
                MissionType.CODE -> CombinationLock(
                    digits = shownAnswer,
                    length = mission.codeLength ?: shownAnswer.length.coerceAtLeast(1),
                    onDigitsChange = onAnswerChanged,
                    isEnabled = !isAnswering,
                )
                MissionType.TEXT -> ESTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = answer,
                    onValueChange = onAnswerChanged,
                    label = stringResource(Res.string.mission_answer_field),
                    kind = ESTextFieldDefaults.Kind.TEXT,
                    style = ESTextFieldDefaults.Style.STANDARD,
                    isEnabled = !isAnswering,
                    imeAction = ImeAction.Send,
                    onImeAction = { onAnswerSubmitted(shownAnswer) },
                )
            }

            Spacer(modifier = Modifier.height(ESTheme.spacing.s))

            ESButton(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(Res.string.action_submit_answer),
                onClick = { onAnswerSubmitted(shownAnswer) },
                style = ESButtonDefaults.Style.PRIMARY,
                size = ESButtonDefaults.Size.LARGE,
                isEnabled = mission.type == MissionType.CODE || answer.isNotBlank(),
                isLoading = isAnswering,
            )
        }
    }
}

@Preview
@Composable
private fun PreviewMissionsSectionForAPlayer() {
    ESTheme {
        MissionsSection(
            missions = listOf(
                Mission(
                    id = "safe",
                    title = "Le coffre",
                    statement = "Le code est caché derrière le tableau.",
                    type = MissionType.CODE,
                    codeLength = 4,
                    isFinal = true,
                    state = MissionState.ACTIVE,
                    solver = null,
                    answers = emptyList(),
                    trigger = null,
                ),
            ),
            isMaster = false,
            isStarted = true,
            answerDrafts = mapOf("safe" to "0420"),
            answeringMissionId = null,
            actionMissionId = null,
            onAnswerChanged = { _, _ -> },
            onAnswerSubmitted = { _, _ -> },
            onAddMissionClicked = {},
            onMissionClicked = {},
            onUnlockClicked = {},
            onValidateClicked = {},
        )
    }
}
