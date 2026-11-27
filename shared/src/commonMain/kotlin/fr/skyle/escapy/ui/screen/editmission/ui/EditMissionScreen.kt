package fr.skyle.escapy.ui.screen.editmission.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import fr.skyle.escapy.designsystem.core.button.ESButton
import fr.skyle.escapy.designsystem.core.button.ESButtonDefaults
import fr.skyle.escapy.designsystem.core.card.ESCard
import fr.skyle.escapy.designsystem.core.card.ESCardDefaults
import fr.skyle.escapy.designsystem.core.choice.ESChoiceRow
import fr.skyle.escapy.designsystem.core.choice.ESChoiceRowDefaults
import fr.skyle.escapy.designsystem.core.dialog.ESDialog
import fr.skyle.escapy.designsystem.core.dialog.ESDialogDefaults
import fr.skyle.escapy.designsystem.core.divider.ESDivider
import fr.skyle.escapy.designsystem.core.iconbutton.ESIconButton
import fr.skyle.escapy.designsystem.core.iconbutton.ESIconButtonDefaults
import fr.skyle.escapy.designsystem.core.progress.ESProgressIndicator
import fr.skyle.escapy.designsystem.core.progress.ESProgressIndicatorDefaults
import fr.skyle.escapy.designsystem.core.screenstructure.ESScreenStructure
import fr.skyle.escapy.designsystem.core.screenstructure.ESScreenStructureDefaults
import fr.skyle.escapy.designsystem.core.snackbar.ESSnackbarState
import fr.skyle.escapy.designsystem.core.snackbar.rememberESSnackbarState
import fr.skyle.escapy.designsystem.core.text.ESText
import fr.skyle.escapy.designsystem.core.textfield.ESTextField
import fr.skyle.escapy.designsystem.core.textfield.ESTextFieldDefaults
import fr.skyle.escapy.designsystem.core.topappbar.ESTopAppBar
import fr.skyle.escapy.designsystem.theme.ESTheme
import fr.skyle.escapy.domain.model.MissionTrigger
import fr.skyle.escapy.domain.model.MissionType
import fr.skyle.escapy.domain.validation.MissionRules
import fr.skyle.escapy.resources.Res
import fr.skyle.escapy.resources.action_add_answer
import fr.skyle.escapy.resources.action_delete_mission
import fr.skyle.escapy.resources.action_keep_mission
import fr.skyle.escapy.resources.action_remove_answer
import fr.skyle.escapy.resources.action_save_mission
import fr.skyle.escapy.resources.delete_mission_dialog_message
import fr.skyle.escapy.resources.delete_mission_dialog_title
import fr.skyle.escapy.resources.edit_mission_answer
import fr.skyle.escapy.resources.edit_mission_answers
import fr.skyle.escapy.resources.edit_mission_code_help
import fr.skyle.escapy.resources.edit_mission_final
import fr.skyle.escapy.resources.edit_mission_final_help
import fr.skyle.escapy.resources.edit_mission_new_title
import fr.skyle.escapy.resources.edit_mission_statement
import fr.skyle.escapy.resources.edit_mission_text_help
import fr.skyle.escapy.resources.edit_mission_title
import fr.skyle.escapy.resources.edit_mission_title_field
import fr.skyle.escapy.resources.edit_mission_trigger
import fr.skyle.escapy.resources.edit_mission_trigger_after
import fr.skyle.escapy.resources.edit_mission_trigger_after_none
import fr.skyle.escapy.resources.edit_mission_trigger_manual
import fr.skyle.escapy.resources.edit_mission_trigger_start
import fr.skyle.escapy.resources.edit_mission_type
import fr.skyle.escapy.resources.edit_mission_type_code
import fr.skyle.escapy.resources.edit_mission_type_text
import fr.skyle.escapy.resources.error_mission_not_found
import fr.skyle.escapy.resources.ic_close
import fr.skyle.escapy.ui.screen.editmission.ui.EditMissionViewModel.EditMissionUIState
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun EditMissionScreen(
    uiState: EditMissionUIState,
    isDeleteDialogShown: Boolean,
    snackbarState: ESSnackbarState,
    onBackClicked: () -> Unit,
    onTitleChanged: (String) -> Unit,
    onStatementChanged: (String) -> Unit,
    onTypeChanged: (MissionType) -> Unit,
    onAnswerChanged: (index: Int, answer: String) -> Unit,
    onAnswerAdded: () -> Unit,
    onAnswerRemoved: (index: Int) -> Unit,
    onTriggerChanged: (MissionTrigger) -> Unit,
    onFinalChanged: (Boolean) -> Unit,
    onSaveClicked: () -> Unit,
    onDeleteClicked: () -> Unit,
    onDeleteDismissed: () -> Unit,
    onDeleteConfirmed: () -> Unit,
) {
    ESScreenStructure(
        style = ESScreenStructureDefaults.Style.STANDARD,
        topBar = {
            ESTopAppBar(
                title = stringResource(if (uiState.isNew) Res.string.edit_mission_new_title else Res.string.edit_mission_title),
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
            when {
                !uiState.isLoaded -> Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    ESProgressIndicator(
                        style = ESProgressIndicatorDefaults.Style.PRIMARY,
                        size = ESProgressIndicatorDefaults.Size.LARGE,
                    )
                }

                uiState.isMissing -> ESText(
                    text = stringResource(Res.string.error_mission_not_found),
                    style = ESTheme.typography.body,
                )

                else -> MissionForm(
                    uiState = uiState,
                    onTitleChanged = onTitleChanged,
                    onStatementChanged = onStatementChanged,
                    onTypeChanged = onTypeChanged,
                    onAnswerChanged = onAnswerChanged,
                    onAnswerAdded = onAnswerAdded,
                    onAnswerRemoved = onAnswerRemoved,
                    onTriggerChanged = onTriggerChanged,
                    onFinalChanged = onFinalChanged,
                    onSaveClicked = onSaveClicked,
                    onDeleteClicked = onDeleteClicked,
                )
            }
        }
    }

    if (isDeleteDialogShown) {
        ESDialog(
            title = stringResource(Res.string.delete_mission_dialog_title),
            message = stringResource(Res.string.delete_mission_dialog_message),
            confirmText = stringResource(Res.string.action_delete_mission),
            onConfirm = onDeleteConfirmed,
            dismissText = stringResource(Res.string.action_keep_mission),
            onDismiss = onDeleteDismissed,
            style = ESDialogDefaults.Style.DESTRUCTIVE,
            isConfirmLoading = uiState.isDeleting,
        )
    }
}

@Composable
private fun MissionForm(
    uiState: EditMissionUIState,
    onTitleChanged: (String) -> Unit,
    onStatementChanged: (String) -> Unit,
    onTypeChanged: (MissionType) -> Unit,
    onAnswerChanged: (index: Int, answer: String) -> Unit,
    onAnswerAdded: () -> Unit,
    onAnswerRemoved: (index: Int) -> Unit,
    onTriggerChanged: (MissionTrigger) -> Unit,
    onFinalChanged: (Boolean) -> Unit,
    onSaveClicked: () -> Unit,
    onDeleteClicked: () -> Unit,
) {
    ESTextField(
        modifier = Modifier.fillMaxWidth(),
        value = uiState.title,
        onValueChange = onTitleChanged,
        label = stringResource(Res.string.edit_mission_title_field),
        kind = ESTextFieldDefaults.Kind.TEXT,
        style = ESTextFieldDefaults.Style.STANDARD,
        imeAction = ImeAction.Next,
    )

    Spacer(modifier = Modifier.height(ESTheme.spacing.s))

    ESTextField(
        modifier = Modifier.fillMaxWidth(),
        value = uiState.statement,
        onValueChange = onStatementChanged,
        label = stringResource(Res.string.edit_mission_statement),
        kind = ESTextFieldDefaults.Kind.MESSAGE,
        style = ESTextFieldDefaults.Style.STANDARD,
    )

    Spacer(modifier = Modifier.height(ESTheme.spacing.l))

    SectionTitle(text = stringResource(Res.string.edit_mission_type))

    ESCard(modifier = Modifier.fillMaxWidth(), style = ESCardDefaults.Style.STANDARD) {
        Column(modifier = Modifier.selectableGroup()) {
            ESChoiceRow(
                label = stringResource(Res.string.edit_mission_type_code),
                supportingText = stringResource(Res.string.edit_mission_code_help),
                isSelected = uiState.type == MissionType.CODE,
                onClick = { onTypeChanged(MissionType.CODE) },
                kind = ESChoiceRowDefaults.Kind.SINGLE,
            )
            ESDivider()
            ESChoiceRow(
                label = stringResource(Res.string.edit_mission_type_text),
                supportingText = stringResource(Res.string.edit_mission_text_help),
                isSelected = uiState.type == MissionType.TEXT,
                onClick = { onTypeChanged(MissionType.TEXT) },
                kind = ESChoiceRowDefaults.Kind.SINGLE,
            )
        }
    }

    Spacer(modifier = Modifier.height(ESTheme.spacing.l))

    SectionTitle(text = stringResource(Res.string.edit_mission_answers))

    uiState.answers.forEachIndexed { index, answer ->
        val position = (index + 1).toString()
        Row(verticalAlignment = Alignment.CenterVertically) {
            ESTextField(
                modifier = Modifier.weight(1f),
                value = answer,
                onValueChange = { text -> onAnswerChanged(index, text) },
                label = stringResource(Res.string.edit_mission_answer, position),
                kind = if (uiState.type == MissionType.CODE) ESTextFieldDefaults.Kind.DIGITS else ESTextFieldDefaults.Kind.TEXT,
                style = ESTextFieldDefaults.Style.STANDARD,
                imeAction = ImeAction.Next,
            )
            if (uiState.answers.size > 1) {
                Spacer(modifier = Modifier.width(ESTheme.spacing.xs))

                ESIconButton(
                    icon = painterResource(Res.drawable.ic_close),
                    contentDescription = stringResource(Res.string.action_remove_answer, position),
                    onClick = { onAnswerRemoved(index) },
                    style = ESIconButtonDefaults.Style.STANDARD,
                    size = ESIconButtonDefaults.Size.MEDIUM,
                )
            }
        }

        Spacer(modifier = Modifier.height(ESTheme.spacing.xs))
    }

    if (uiState.answers.size < MissionRules.ANSWERS_MAX) {
        ESButton(
            text = stringResource(Res.string.action_add_answer),
            onClick = onAnswerAdded,
            style = ESButtonDefaults.Style.TEXT,
            size = ESButtonDefaults.Size.MEDIUM,
        )
    }

    Spacer(modifier = Modifier.height(ESTheme.spacing.l))

    SectionTitle(text = stringResource(Res.string.edit_mission_trigger))

    ESCard(modifier = Modifier.fillMaxWidth(), style = ESCardDefaults.Style.STANDARD) {
        Column(modifier = Modifier.selectableGroup()) {
            ESChoiceRow(
                label = stringResource(Res.string.edit_mission_trigger_start),
                isSelected = uiState.trigger == MissionTrigger.OnStart,
                onClick = { onTriggerChanged(MissionTrigger.OnStart) },
                kind = ESChoiceRowDefaults.Kind.SINGLE,
            )
            ESDivider()
            ESChoiceRow(
                label = stringResource(Res.string.edit_mission_trigger_manual),
                isSelected = uiState.trigger == MissionTrigger.Manual,
                onClick = { onTriggerChanged(MissionTrigger.Manual) },
                kind = ESChoiceRowDefaults.Kind.SINGLE,
            )
            ESDivider()
            // One row per mission it may follow: the choice is the trigger and its mission at once
            if (uiState.followableMissions.isEmpty()) {
                ESChoiceRow(
                    label = stringResource(Res.string.edit_mission_trigger_after),
                    supportingText = stringResource(Res.string.edit_mission_trigger_after_none),
                    isSelected = false,
                    onClick = {},
                    kind = ESChoiceRowDefaults.Kind.SINGLE,
                    isEnabled = false,
                )
            } else {
                uiState.followableMissions.forEachIndexed { index, other ->
                    if (index > 0) ESDivider()
                    ESChoiceRow(
                        label = stringResource(Res.string.edit_mission_trigger_after),
                        supportingText = other.title,
                        isSelected = uiState.trigger == MissionTrigger.AfterMission(missionId = other.id),
                        onClick = { onTriggerChanged(MissionTrigger.AfterMission(missionId = other.id)) },
                        kind = ESChoiceRowDefaults.Kind.SINGLE,
                    )
                }
            }
        }
    }

    Spacer(modifier = Modifier.height(ESTheme.spacing.l))

    ESCard(modifier = Modifier.fillMaxWidth(), style = ESCardDefaults.Style.STANDARD) {
        ESChoiceRow(
            label = stringResource(Res.string.edit_mission_final),
            supportingText = stringResource(Res.string.edit_mission_final_help),
            isSelected = uiState.isFinal,
            onClick = { onFinalChanged(!uiState.isFinal) },
            kind = ESChoiceRowDefaults.Kind.MULTIPLE,
        )
    }

    Spacer(modifier = Modifier.height(ESTheme.spacing.xl))

    ESButton(
        modifier = Modifier.fillMaxWidth(),
        text = stringResource(Res.string.action_save_mission),
        onClick = onSaveClicked,
        style = ESButtonDefaults.Style.PRIMARY,
        size = ESButtonDefaults.Size.LARGE,
        isEnabled = uiState.canSave || uiState.isSaving,
        isLoading = uiState.isSaving,
    )

    if (!uiState.isNew) {
        Spacer(modifier = Modifier.height(ESTheme.spacing.m))

        ESButton(
            modifier = Modifier.fillMaxWidth(),
            text = stringResource(Res.string.action_delete_mission),
            onClick = onDeleteClicked,
            style = ESButtonDefaults.Style.DESTRUCTIVE,
            size = ESButtonDefaults.Size.LARGE,
        )
    }
}

@Composable
private fun SectionTitle(text: String) {
    ESText(
        modifier = Modifier.padding(bottom = ESTheme.spacing.s),
        text = text,
        style = ESTheme.typography.subtitle,
    )
}

@Preview
@Composable
private fun PreviewEditMissionScreen() {
    ESTheme {
        EditMissionScreen(
            uiState = EditMissionUIState(
                isNew = true,
                isLoaded = true,
                title = "Le coffre",
                answers = listOf("0427"),
            ),
            isDeleteDialogShown = false,
            snackbarState = rememberESSnackbarState(),
            onBackClicked = {},
            onTitleChanged = {},
            onStatementChanged = {},
            onTypeChanged = {},
            onAnswerChanged = { _, _ -> },
            onAnswerAdded = {},
            onAnswerRemoved = {},
            onTriggerChanged = {},
            onFinalChanged = {},
            onSaveClicked = {},
            onDeleteClicked = {},
            onDeleteDismissed = {},
            onDeleteConfirmed = {},
        )
    }
}
