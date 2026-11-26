package fr.skyle.escapy.ui.core.game

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import fr.skyle.escapy.designsystem.core.card.ESCard
import fr.skyle.escapy.designsystem.core.card.ESCardDefaults
import fr.skyle.escapy.designsystem.core.iconbutton.ESIconButton
import fr.skyle.escapy.designsystem.core.iconbutton.ESIconButtonDefaults
import fr.skyle.escapy.designsystem.core.text.ESText
import fr.skyle.escapy.designsystem.theme.ESTheme
import fr.skyle.escapy.domain.model.GameMember
import fr.skyle.escapy.resources.Res
import fr.skyle.escapy.resources.action_kick_player
import fr.skyle.escapy.resources.ic_person_remove
import fr.skyle.escapy.resources.waiting_room_you
import fr.skyle.escapy.ui.core.avatar.AvatarImage
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

/** A member of a game: their avatar and name, the user marked, and a way to exclude them if given. */
@Composable
fun GameMemberRow(
    member: GameMember,
    modifier: Modifier = Modifier,
    onKickClicked: (() -> Unit)? = null,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .padding(horizontal = ESTheme.spacing.m, vertical = ESTheme.spacing.xs),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // The name next to it tells who it is
        AvatarImage(
            avatarId = member.avatarId,
            size = 40.dp,
            contentDescription = null,
        )

        Spacer(modifier = Modifier.width(ESTheme.spacing.s))

        ESText(
            modifier = Modifier.weight(1f),
            text = if (member.isCurrentUser) {
                stringResource(Res.string.waiting_room_you, member.username)
            } else {
                member.username
            },
            style = ESTheme.typography.body,
        )

        if (onKickClicked != null) {
            ESIconButton(
                icon = painterResource(Res.drawable.ic_person_remove),
                contentDescription = stringResource(Res.string.action_kick_player, member.username),
                onClick = onKickClicked,
                style = ESIconButtonDefaults.Style.STANDARD,
                size = ESIconButtonDefaults.Size.MEDIUM,
            )
        }
    }
}

@Preview
@Composable
private fun PreviewGameMemberRow() {
    ESTheme {
        ESCard(
            modifier = Modifier.padding(ESTheme.spacing.m),
            style = ESCardDefaults.Style.STANDARD,
        ) {
            GameMemberRow(
                member = GameMember(userId = "master", username = "Maître", avatarId = 3, isCurrentUser = true),
            )
            GameMemberRow(
                member = GameMember(userId = "player", username = "Joueuse", avatarId = 8, isCurrentUser = false),
                onKickClicked = {},
            )
        }
    }
}
