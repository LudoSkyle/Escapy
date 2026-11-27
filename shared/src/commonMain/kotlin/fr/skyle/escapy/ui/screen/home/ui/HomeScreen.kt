package fr.skyle.escapy.ui.screen.home.ui

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import fr.skyle.escapy.designsystem.core.button.ESButton
import fr.skyle.escapy.designsystem.core.button.ESButtonDefaults
import fr.skyle.escapy.designsystem.core.card.ESCard
import fr.skyle.escapy.designsystem.core.card.ESCardDefaults
import fr.skyle.escapy.designsystem.core.divider.ESDivider
import fr.skyle.escapy.designsystem.core.iconbutton.ESIconButton
import fr.skyle.escapy.designsystem.core.iconbutton.ESIconButtonDefaults
import fr.skyle.escapy.designsystem.core.progress.ESProgressIndicator
import fr.skyle.escapy.designsystem.core.progress.ESProgressIndicatorDefaults
import fr.skyle.escapy.designsystem.core.row.ESRow
import fr.skyle.escapy.designsystem.core.row.ESRowDefaults
import fr.skyle.escapy.designsystem.core.screenstructure.ESScreenStructure
import fr.skyle.escapy.designsystem.core.screenstructure.ESScreenStructureDefaults
import fr.skyle.escapy.designsystem.core.snackbar.ESSnackbarState
import fr.skyle.escapy.designsystem.core.snackbar.rememberESSnackbarState
import fr.skyle.escapy.designsystem.core.text.ESText
import fr.skyle.escapy.designsystem.core.textfield.ESTextField
import fr.skyle.escapy.designsystem.core.textfield.ESTextFieldDefaults
import fr.skyle.escapy.designsystem.core.topappbar.ESTopAppBar
import fr.skyle.escapy.designsystem.theme.ESTheme
import fr.skyle.escapy.domain.model.GameStatus
import fr.skyle.escapy.domain.model.GameSummary
import fr.skyle.escapy.domain.validation.GameRules
import fr.skyle.escapy.resources.Res
import fr.skyle.escapy.resources.action_create_game
import fr.skyle.escapy.resources.action_join
import fr.skyle.escapy.resources.action_scan_qr_code
import fr.skyle.escapy.resources.field_join_code
import fr.skyle.escapy.resources.game_summary_master
import fr.skyle.escapy.resources.game_summary_player
import fr.skyle.escapy.resources.home_background
import fr.skyle.escapy.resources.home_join_code_help
import fr.skyle.escapy.resources.home_join_title
import fr.skyle.escapy.resources.home_my_games
import fr.skyle.escapy.resources.home_no_games
import fr.skyle.escapy.resources.home_welcome
import fr.skyle.escapy.resources.home_welcome_anonymous
import fr.skyle.escapy.resources.ic_person
import fr.skyle.escapy.resources.ic_qr_code_scanner
import fr.skyle.escapy.resources.profile_title
import fr.skyle.escapy.ui.core.error.message
import fr.skyle.escapy.ui.core.game.gameStatusText
import fr.skyle.escapy.ui.screen.home.ui.HomeViewModel.HomeUIState
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun HomeScreen(
    joinCode: String,
    uiState: HomeUIState,
    snackbarState: ESSnackbarState,
    onProfileClicked: () -> Unit,
    onJoinCodeChanged: (String) -> Unit,
    onJoinClicked: () -> Unit,
    onScanClicked: () -> Unit,
    onCreateGameClicked: () -> Unit,
    onGameClicked: (gameId: String) -> Unit,
) {
    val isJoinCodeComplete = GameRules.isJoinCodeComplete(joinCode)

    ESScreenStructure(
        style = ESScreenStructureDefaults.Style.STANDARD,
        background = { HomeBackground() },
        topBar = {
            ESTopAppBar(
                // The welcome below says where the user is
                title = "",
                actions = {
                    ESIconButton(
                        icon = painterResource(Res.drawable.ic_person),
                        contentDescription = stringResource(Res.string.profile_title),
                        onClick = onProfileClicked,
                        style = ESIconButtonDefaults.Style.STANDARD,
                        size = ESIconButtonDefaults.Size.MEDIUM,
                    )
                },
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
            Welcome(username = uiState.username)

            Spacer(modifier = Modifier.height(ESTheme.spacing.l))

            ESCard(
                modifier = Modifier.fillMaxWidth(),
                style = ESCardDefaults.Style.STANDARD,
            ) {
                Column(modifier = Modifier.padding(ESTheme.spacing.m)) {
                    ESText(
                        text = stringResource(Res.string.home_join_title),
                        style = ESTheme.typography.subtitle,
                    )

                    Spacer(modifier = Modifier.height(ESTheme.spacing.s))

                    Row(verticalAlignment = Alignment.Top) {
                        ESTextField(
                            modifier = Modifier.weight(1f),
                            value = joinCode,
                            onValueChange = onJoinCodeChanged,
                            label = stringResource(Res.string.field_join_code),
                            kind = ESTextFieldDefaults.Kind.CODE,
                            style = ESTextFieldDefaults.Style.STANDARD,
                            isEnabled = !uiState.isJoining,
                            supportingText = stringResource(Res.string.home_join_code_help),
                            errorMessage = uiState.joinCodeError?.message(),
                            imeAction = ImeAction.Go,
                            onImeAction = onJoinClicked,
                        )

                        Spacer(modifier = Modifier.width(ESTheme.spacing.xs))

                        ESIconButton(
                            // Level with the box of the field, under the room of its label
                            modifier = Modifier.padding(top = 12.dp),
                            icon = painterResource(Res.drawable.ic_qr_code_scanner),
                            contentDescription = stringResource(Res.string.action_scan_qr_code),
                            onClick = onScanClicked,
                            style = ESIconButtonDefaults.Style.FILLED,
                            size = ESIconButtonDefaults.Size.MEDIUM,
                            isEnabled = !uiState.isJoining,
                        )
                    }

                    Spacer(modifier = Modifier.height(ESTheme.spacing.s))

                    ESButton(
                        modifier = Modifier.fillMaxWidth(),
                        text = stringResource(Res.string.action_join),
                        onClick = onJoinClicked,
                        style = ESButtonDefaults.Style.SECONDARY,
                        size = ESButtonDefaults.Size.MEDIUM,
                        isEnabled = isJoinCodeComplete || uiState.isJoining,
                        isLoading = uiState.isJoining,
                    )
                }
            }

            Spacer(modifier = Modifier.height(ESTheme.spacing.m))

            ESButton(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(Res.string.action_create_game),
                onClick = onCreateGameClicked,
                style = ESButtonDefaults.Style.PRIMARY,
                size = ESButtonDefaults.Size.LARGE,
            )

            Spacer(modifier = Modifier.height(ESTheme.spacing.xl))

            ESText(
                text = stringResource(Res.string.home_my_games),
                style = ESTheme.typography.subtitle,
            )

            Spacer(modifier = Modifier.height(ESTheme.spacing.s))

            HomeGames(
                games = uiState.games,
                onGameClicked = onGameClicked,
            )
        }
    }
}

/** "Welcome, Player_ABC234!", the name in the accent color, as the first version of the app said it. */
@Composable
private fun Welcome(username: String?) {
    val primary = ESTheme.colors.primary
    val text = if (username == null) {
        AnnotatedString(stringResource(Res.string.home_welcome_anonymous))
    } else {
        val sentence = stringResource(Res.string.home_welcome, username)
        val start = sentence.indexOf(username)
        buildAnnotatedString {
            append(sentence)
            if (start >= 0) {
                addStyle(style = SpanStyle(color = primary), start = start, end = start + username.length)
            }
        }
    }
    ESText(
        modifier = Modifier.fillMaxWidth(),
        text = text,
        style = ESTheme.typography.headline,
    )
}

/** The rings of the first version of the app, at the top left, faint behind the bar and the welcome. */
@Composable
private fun HomeBackground() {
    Image(
        modifier = Modifier.width(375.dp),
        painter = painterResource(Res.drawable.home_background),
        contentDescription = null,
        contentScale = ContentScale.FillWidth,
        // Drawn in black: tinted with a surface color, it shows on the dark background
        colorFilter = ColorFilter.tint(ESTheme.colors.surfaceHighest),
    )
}

@Composable
private fun HomeGames(
    games: List<GameSummary>?,
    onGameClicked: (gameId: String) -> Unit,
) {
    when {
        games == null -> Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(ESTheme.spacing.l),
            contentAlignment = Alignment.Center,
        ) {
            ESProgressIndicator(
                style = ESProgressIndicatorDefaults.Style.PRIMARY,
                size = ESProgressIndicatorDefaults.Size.LARGE,
            )
        }

        games.isEmpty() -> ESText(
            text = stringResource(Res.string.home_no_games),
            style = ESTheme.typography.body,
            color = ESTheme.colors.textSecondary,
        )

        else -> ESCard(
            modifier = Modifier.fillMaxWidth(),
            style = ESCardDefaults.Style.STANDARD,
        ) {
            games.forEachIndexed { index, game ->
                if (index > 0) {
                    ESDivider()
                }

                val role = if (game.isMaster) {
                    stringResource(Res.string.game_summary_master)
                } else {
                    stringResource(Res.string.game_summary_player, game.masterUsername)
                }
                val status = gameStatusText(status = game.status)
                ESRow(
                    label = game.title,
                    supportingText = if (status == null) role else "$status · $role",
                    onClick = { onGameClicked(game.gameId) },
                    style = ESRowDefaults.Style.STANDARD,
                )
            }
        }
    }
}

@Preview
@Composable
private fun PreviewHomeScreen() {
    ESTheme {
        HomeScreen(
            joinCode = "ABC2",
            uiState = HomeUIState(
                games = listOf(
                    previewGame(id = "manor", title = "Le manoir", isMaster = true),
                    previewGame(id = "cellar", title = "La cave", isMaster = false, status = GameStatus.RUNNING),
                ),
            ),
            snackbarState = rememberESSnackbarState(),
            onProfileClicked = {},
            onJoinCodeChanged = {},
            onJoinClicked = {},
            onScanClicked = {},
            onCreateGameClicked = {},
            onGameClicked = {},
        )
    }
}

@Preview
@Composable
private fun PreviewHomeScreenWithoutGames() {
    ESTheme {
        HomeScreen(
            joinCode = "",
            uiState = HomeUIState(games = emptyList()),
            snackbarState = rememberESSnackbarState(),
            onProfileClicked = {},
            onJoinCodeChanged = {},
            onJoinClicked = {},
            onScanClicked = {},
            onCreateGameClicked = {},
            onGameClicked = {},
        )
    }
}

private fun previewGame(id: String, title: String, isMaster: Boolean, status: GameStatus = GameStatus.WAITING): GameSummary =
    GameSummary(
        gameId = id,
        title = title,
        isMaster = isMaster,
        masterUsername = "Maître",
        masterAvatarId = 3,
        status = status,
    )
