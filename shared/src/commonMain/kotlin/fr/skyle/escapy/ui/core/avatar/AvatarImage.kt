package fr.skyle.escapy.ui.core.avatar

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.Dp
import fr.skyle.escapy.designsystem.theme.ESTheme
import org.jetbrains.compose.resources.painterResource

/**
 * The avatar [avatarId] in a circle. Its [contentDescription] is null when a name next to it already
 * tells who it is.
 */
@Composable
fun AvatarImage(
    avatarId: Int,
    size: Dp,
    contentDescription: String?,
    modifier: Modifier = Modifier,
) {
    Image(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(ESTheme.colors.surfaceHigh)
            .padding(size / 10),
        painter = painterResource(avatarResource(avatarId)),
        contentDescription = contentDescription,
    )
}
