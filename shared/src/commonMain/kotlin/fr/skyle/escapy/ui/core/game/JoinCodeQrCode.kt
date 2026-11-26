package fr.skyle.escapy.ui.core.game

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import fr.skyle.escapy.designsystem.theme.ESTheme
import fr.skyle.escapy.domain.validation.JoinCodeQr
import fr.skyle.escapy.resources.Res
import fr.skyle.escapy.resources.join_code_qr_description
import io.github.alexzhirkevich.qrose.options.QrBrush
import io.github.alexzhirkevich.qrose.options.QrColors
import io.github.alexzhirkevich.qrose.options.solid
import io.github.alexzhirkevich.qrose.rememberQrCodePainter
import org.jetbrains.compose.resources.stringResource

/**
 * The QR code of the game of [joinCode], which a player scans from their home screen: black on white
 * whatever the theme, with the margin around it a camera needs to find it.
 */
@Composable
fun JoinCodeQrCode(
    joinCode: String,
    size: Dp,
    modifier: Modifier = Modifier,
) {
    val painter = rememberQrCodePainter(
        data = JoinCodeQr.contentOf(joinCode = joinCode),
        colors = QrColors(dark = QrBrush.solid(Color.Black)),
    )
    Image(
        modifier = modifier
            .size(size)
            .clip(ESTheme.shapes.medium)
            .background(Color.White)
            .padding(size / 12),
        painter = painter,
        contentDescription = stringResource(Res.string.join_code_qr_description, joinCode.toList().joinToString(" ")),
    )
}

@Preview
@Composable
private fun PreviewJoinCodeQrCode() {
    ESTheme {
        JoinCodeQrCode(joinCode = "ABC234", size = 200.dp)
    }
}
