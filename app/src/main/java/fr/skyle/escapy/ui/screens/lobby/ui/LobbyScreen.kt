package fr.skyle.escapy.ui.screens.lobby.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import fr.skyle.escapy.R
import fr.skyle.escapy.designsystem.core.iconButton.ProjectIconButtonDefaults
import fr.skyle.escapy.designsystem.core.topAppBar.ProjectTopAppBar
import fr.skyle.escapy.designsystem.core.topAppBar.component.ProjectTopAppBarItem
import fr.skyle.escapy.designsystem.theme.ProjectTheme
import fr.skyle.escapy.ext.formatDuration
import fr.skyle.escapy.ui.core.structure.ProjectScreenStructure
import fr.skyle.escapy.utils.ProjectComponentPreview
import fr.skyle.escapy.utils.ProjectScreenPreview
import kotlin.time.Duration.Companion.hours

@Composable
fun LobbyScreen(
    navigateBack: () -> Unit,
    state: LobbyViewModel.State,
) {
    ProjectScreenStructure(
        modifier = Modifier.fillMaxSize(),
        isPatternDisplayed = true,
        topContent = {
            ProjectTopAppBar(
                modifier = Modifier.fillMaxWidth(),
                title = stringResource(R.string.lobby_screen_title),
                leadingContent = {
                    ProjectTopAppBarItem(
                        icon = rememberVectorPainter(Icons.AutoMirrored.Filled.ArrowBack),
                        style = ProjectIconButtonDefaults.Style.FILLED,
                        onClick = navigateBack,
                    )
                },
            )
        }
    ) { innerPadding ->
        LobbyScreenContent(
            modifier = Modifier.fillMaxSize(),
            innerPadding = innerPadding,
            createdByName = state.createdByName,
            duration = state.duration
        )
    }
}

@Composable
private fun LobbyScreenContent(
    innerPadding: PaddingValues,
    createdByName: String,
    duration: Long,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .padding(top = innerPadding.calculateTopPadding())
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
    ) {
        Text(
            modifier = Modifier.fillMaxWidth(),
            text = stringResource(R.string.lobby_screen_manager),
            style = ProjectTheme.typography.h2,
            color = ProjectTheme.colors.onSurface,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            modifier = Modifier.fillMaxWidth(),
            text = createdByName,
            style = ProjectTheme.typography.b1,
            color = ProjectTheme.colors.onSurface,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            modifier = Modifier.fillMaxWidth(),
            text = stringResource(R.string.lobby_screen_game_duration),
            style = ProjectTheme.typography.h2,
            color = ProjectTheme.colors.onSurface,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            modifier = Modifier.fillMaxWidth(),
            text = formatDuration(duration),
            style = ProjectTheme.typography.b1,
            color = ProjectTheme.colors.onSurface,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            modifier = Modifier.fillMaxWidth(),
            text = stringResource(R.string.lobby_screen_participants),
            style = ProjectTheme.typography.b1,
            color = ProjectTheme.colors.onSurface,
        )

        Spacer(modifier = Modifier.height(12.dp))


    }
}

@ProjectScreenPreview
@Composable
private fun LobbyScreenPreview() {
    ProjectTheme {
        LobbyScreen(
            state = LobbyViewModel.State(),
            navigateBack = {},
        )
    }
}

@ProjectComponentPreview
@Composable
private fun LobbyScreenContentPreview() {
    ProjectTheme {
        LobbyScreenContent(
            modifier = Modifier.fillMaxSize(),
            innerPadding = PaddingValues(),
            createdByName = "John Doe",
            duration = 1.hours.inWholeMilliseconds,
        )
    }
}