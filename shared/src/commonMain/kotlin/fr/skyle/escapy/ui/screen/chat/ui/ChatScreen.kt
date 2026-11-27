package fr.skyle.escapy.ui.screen.chat.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import fr.skyle.escapy.designsystem.core.button.ESButton
import fr.skyle.escapy.designsystem.core.button.ESButtonDefaults
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
import fr.skyle.escapy.domain.model.ChatMessage
import fr.skyle.escapy.domain.model.Game
import fr.skyle.escapy.domain.model.GameMember
import fr.skyle.escapy.domain.model.GameState
import fr.skyle.escapy.domain.model.GameStatus
import fr.skyle.escapy.domain.model.GameTimer
import fr.skyle.escapy.domain.validation.ChatRules
import fr.skyle.escapy.resources.Res
import fr.skyle.escapy.resources.action_back_to_home
import fr.skyle.escapy.resources.action_send
import fr.skyle.escapy.resources.chat_closed_over
import fr.skyle.escapy.resources.chat_empty
import fr.skyle.escapy.resources.chat_message_field
import fr.skyle.escapy.resources.chat_pending
import fr.skyle.escapy.resources.chat_title
import fr.skyle.escapy.resources.ic_send
import fr.skyle.escapy.resources.waiting_room_deleted
import fr.skyle.escapy.resources.waiting_room_excluded
import fr.skyle.escapy.ui.core.avatar.AvatarImage
import fr.skyle.escapy.ui.screen.chat.ui.ChatViewModel.ChatUIState
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun ChatScreen(
    uiState: ChatUIState,
    draft: String,
    snackbarState: ESSnackbarState,
    onBackClicked: () -> Unit,
    onDraftChanged: (String) -> Unit,
    onSendClicked: () -> Unit,
) {
    val state = uiState.state

    ESScreenStructure(
        style = ESScreenStructureDefaults.Style.STANDARD,
        topBar = {
            ESTopAppBar(
                title = stringResource(Res.string.chat_title),
                onBackClick = onBackClicked,
            )
        },
        snackbarState = snackbarState,
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            when (state) {
                GameState.Deleted, GameState.Excluded -> Column(modifier = Modifier.padding(ESTheme.spacing.l)) {
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

                else -> {
                    Messages(
                        modifier = Modifier.weight(1f),
                        messages = uiState.messages,
                    )

                    when {
                        state == null -> Unit
                        uiState.isOpen -> MessageField(
                            draft = draft,
                            onDraftChanged = onDraftChanged,
                            onSendClicked = onSendClicked,
                        )
                        (state as? GameState.Available)?.game?.status == GameStatus.FINISHED -> ESText(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(ESTheme.spacing.l),
                            text = stringResource(Res.string.chat_closed_over),
                            style = ESTheme.typography.bodySmall,
                            color = ESTheme.colors.textSecondary,
                            textAlign = TextAlign.Center,
                        )
                        else -> Unit
                    }
                }
            }
        }
    }
}

/** The messages, the latest at the bottom: the list starts there and follows each new one. */
@Composable
private fun Messages(messages: List<ChatMessage>?, modifier: Modifier = Modifier) {
    when {
        messages == null -> Box(
            modifier = modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center,
        ) {
            ESProgressIndicator(
                style = ESProgressIndicatorDefaults.Style.PRIMARY,
                size = ESProgressIndicatorDefaults.Size.LARGE,
            )
        }

        messages.isEmpty() -> Box(
            modifier = modifier
                .fillMaxWidth()
                .padding(ESTheme.spacing.l),
            contentAlignment = Alignment.Center,
        ) {
            ESText(
                text = stringResource(Res.string.chat_empty),
                style = ESTheme.typography.body,
                color = ESTheme.colors.textSecondary,
            )
        }

        else -> LazyColumn(
            modifier = modifier.fillMaxWidth(),
            // From the bottom: the latest message shows first, and a new one keeps the list at it
            reverseLayout = true,
            contentPadding = PaddingValues(ESTheme.spacing.m),
            // Spaced from the bottom: a short chat starts at the field, not at the top of the screen
            verticalArrangement = Arrangement.spacedBy(ESTheme.spacing.s, Alignment.Bottom),
        ) {
            items(items = messages.asReversed(), key = { message -> message.id }) { message ->
                MessageBubble(message = message)
            }
        }
    }
}

@Composable
private fun MessageBubble(message: ChatMessage) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (message.isMine) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Bottom,
    ) {
        if (!message.isMine) {
            // The name above the text tells who wrote it
            AvatarImage(
                avatarId = message.authorAvatarId,
                size = 32.dp,
                contentDescription = null,
            )

            Spacer(modifier = Modifier.width(ESTheme.spacing.xs))
        }

        Column(
            modifier = Modifier.widthIn(max = 280.dp),
            horizontalAlignment = if (message.isMine) Alignment.End else Alignment.Start,
        ) {
            if (!message.isMine) {
                ESText(
                    text = message.authorUsername,
                    style = ESTheme.typography.caption,
                    color = ESTheme.colors.textSecondary,
                )

                Spacer(modifier = Modifier.height(ESTheme.spacing.xxs))
            }

            ESText(
                modifier = Modifier
                    .clip(ESTheme.shapes.small)
                    .background(if (message.isMine) ESTheme.colors.primary else ESTheme.colors.surfaceHigh)
                    .padding(horizontal = ESTheme.spacing.s, vertical = ESTheme.spacing.xs),
                text = message.text,
                style = ESTheme.typography.body,
                color = if (message.isMine) ESTheme.colors.onPrimary else ESTheme.colors.textPrimary,
            )

            if (message.isPending) {
                Spacer(modifier = Modifier.height(ESTheme.spacing.xxs))

                ESText(
                    text = stringResource(Res.string.chat_pending),
                    style = ESTheme.typography.caption,
                    color = ESTheme.colors.textSecondary,
                )
            }
        }
    }
}

@Composable
private fun MessageField(
    draft: String,
    onDraftChanged: (String) -> Unit,
    onSendClicked: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = ESTheme.spacing.m, vertical = ESTheme.spacing.s),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ESTextField(
            modifier = Modifier.weight(1f),
            value = draft,
            onValueChange = onDraftChanged,
            label = stringResource(Res.string.chat_message_field),
            kind = ESTextFieldDefaults.Kind.MESSAGE,
            style = ESTextFieldDefaults.Style.STANDARD,
        )

        Spacer(modifier = Modifier.width(ESTheme.spacing.xs))

        ESIconButton(
            icon = painterResource(Res.drawable.ic_send),
            contentDescription = stringResource(Res.string.action_send),
            onClick = onSendClicked,
            style = ESIconButtonDefaults.Style.FILLED,
            size = ESIconButtonDefaults.Size.MEDIUM,
            isEnabled = ChatRules.isValidMessage(draft),
        )
    }
}

private val previewGame = Game(
    id = "manor",
    title = "Le manoir",
    durationMinutes = 60,
    maxPlayers = null,
    master = GameMember(userId = "master", username = "Maître", avatarId = 3, isCurrentUser = false),
    playerCount = 2,
    joinCode = "ABC234",
    status = GameStatus.RUNNING,
    timer = GameTimer(endsAt = null, remaining = null, addedMinutes = 0),
    end = null,
)

private val previewMessages = listOf(
    ChatMessage(
        id = "1",
        authorId = "master",
        authorUsername = "Maître",
        authorAvatarId = 3,
        text = "Avez-vous regardé derrière le tableau ?",
        sentAt = null,
        isMine = false,
        isPending = false,
    ),
    ChatMessage(
        id = "2",
        authorId = "me",
        authorUsername = "Joueuse",
        authorAvatarId = 8,
        text = "On y va !",
        sentAt = null,
        isMine = true,
        isPending = true,
    ),
)

@Preview
@Composable
private fun PreviewChatScreen() {
    ESTheme {
        ChatScreen(
            uiState = ChatUIState(state = GameState.Available(game = previewGame, players = emptyList()), messages = previewMessages),
            draft = "Merci",
            snackbarState = rememberESSnackbarState(),
            onBackClicked = {},
            onDraftChanged = {},
            onSendClicked = {},
        )
    }
}

@Preview
@Composable
private fun PreviewChatScreenOver() {
    ESTheme {
        ChatScreen(
            uiState = ChatUIState(
                state = GameState.Available(game = previewGame.copy(status = GameStatus.FINISHED), players = emptyList()),
                messages = emptyList(),
            ),
            draft = "",
            snackbarState = rememberESSnackbarState(),
            onBackClicked = {},
            onDraftChanged = {},
            onSendClicked = {},
        )
    }
}
