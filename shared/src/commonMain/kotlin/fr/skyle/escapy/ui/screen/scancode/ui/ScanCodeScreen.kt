package fr.skyle.escapy.ui.screen.scancode.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import fr.skyle.escapy.designsystem.core.button.ESButton
import fr.skyle.escapy.designsystem.core.button.ESButtonDefaults
import fr.skyle.escapy.designsystem.core.progress.ESProgressIndicator
import fr.skyle.escapy.designsystem.core.progress.ESProgressIndicatorDefaults
import fr.skyle.escapy.designsystem.core.screenstructure.ESScreenStructure
import fr.skyle.escapy.designsystem.core.screenstructure.ESScreenStructureDefaults
import fr.skyle.escapy.designsystem.core.snackbar.ESSnackbarState
import fr.skyle.escapy.designsystem.core.snackbar.rememberESSnackbarState
import fr.skyle.escapy.designsystem.core.text.ESText
import fr.skyle.escapy.designsystem.core.topappbar.ESTopAppBar
import fr.skyle.escapy.designsystem.theme.ESTheme
import fr.skyle.escapy.platform.camera.CameraPermissionStatus
import fr.skyle.escapy.resources.Res
import fr.skyle.escapy.resources.action_allow_camera
import fr.skyle.escapy.resources.action_open_settings
import fr.skyle.escapy.resources.scan_qr_code_camera_denied
import fr.skyle.escapy.resources.scan_qr_code_camera_needed
import fr.skyle.escapy.resources.scan_qr_code_camera_unavailable
import fr.skyle.escapy.resources.scan_qr_code_help
import fr.skyle.escapy.resources.scan_qr_code_title
import fr.skyle.escapy.ui.screen.scancode.ui.ScanCodeViewModel.ScanCodeUIState
import org.jetbrains.compose.resources.stringResource

/**
 * The camera, once allowed, under a frame to aim with; else why it is not there and the way to it.
 * The [camera] is given by the route: the preview of the device, which a preview cannot show.
 */
@Composable
fun ScanCodeScreen(
    uiState: ScanCodeUIState,
    cameraPermissionStatus: CameraPermissionStatus,
    snackbarState: ESSnackbarState,
    onBackClicked: () -> Unit,
    onAllowCameraClicked: () -> Unit,
    onOpenSettingsClicked: () -> Unit,
    camera: @Composable (Modifier) -> Unit,
) {
    ESScreenStructure(
        style = ESScreenStructureDefaults.Style.STANDARD,
        topBar = {
            ESTopAppBar(
                title = stringResource(Res.string.scan_qr_code_title),
                onBackClick = onBackClicked,
            )
        },
        snackbarState = snackbarState,
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            when {
                uiState.isCameraUnavailable -> ScanCodeMessage(
                    message = stringResource(Res.string.scan_qr_code_camera_unavailable),
                )

                cameraPermissionStatus == CameraPermissionStatus.GRANTED -> ScanCodeCamera(
                    isJoining = uiState.isJoining,
                    camera = camera,
                )

                cameraPermissionStatus == CameraPermissionStatus.DENIED_FOREVER -> ScanCodeMessage(
                    message = stringResource(Res.string.scan_qr_code_camera_denied),
                    actionText = stringResource(Res.string.action_open_settings),
                    onActionClicked = onOpenSettingsClicked,
                )

                else -> ScanCodeMessage(
                    message = stringResource(Res.string.scan_qr_code_camera_needed),
                    actionText = stringResource(Res.string.action_allow_camera),
                    onActionClicked = onAllowCameraClicked,
                )
            }
        }
    }
}

@Composable
private fun ScanCodeCamera(
    isJoining: Boolean,
    camera: @Composable (Modifier) -> Unit,
) {
    Box(modifier = Modifier.fillMaxSize()) {
        camera(Modifier.fillMaxSize())

        // Where to hold the code: the camera reads it anywhere in the image all the same
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .size(240.dp)
                .border(width = 3.dp, color = ESTheme.colors.primary, shape = ESTheme.shapes.large),
        )

        ESText(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(Color.Black.copy(alpha = 0.6f))
                .padding(ESTheme.spacing.l),
            text = stringResource(Res.string.scan_qr_code_help),
            style = ESTheme.typography.body,
            color = Color.White,
            textAlign = TextAlign.Center,
        )

        if (isJoining) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.6f)),
                contentAlignment = Alignment.Center,
            ) {
                ESProgressIndicator(
                    style = ESProgressIndicatorDefaults.Style.PRIMARY,
                    size = ESProgressIndicatorDefaults.Size.LARGE,
                )
            }
        }
    }
}

@Composable
private fun ScanCodeMessage(
    message: String,
    actionText: String? = null,
    onActionClicked: () -> Unit = {},
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(ESTheme.spacing.l),
    ) {
        ESText(
            modifier = Modifier.fillMaxWidth(),
            text = message,
            style = ESTheme.typography.body,
        )

        if (actionText != null) {
            Spacer(modifier = Modifier.height(ESTheme.spacing.l))

            ESButton(
                modifier = Modifier.fillMaxWidth(),
                text = actionText,
                onClick = onActionClicked,
                style = ESButtonDefaults.Style.PRIMARY,
                size = ESButtonDefaults.Size.LARGE,
            )
        }
    }
}

@Preview
@Composable
private fun PreviewScanCodeScreen() {
    ESTheme {
        ScanCodeScreen(
            uiState = ScanCodeUIState(),
            cameraPermissionStatus = CameraPermissionStatus.GRANTED,
            snackbarState = rememberESSnackbarState(),
            onBackClicked = {},
            onAllowCameraClicked = {},
            onOpenSettingsClicked = {},
            camera = { modifier -> Box(modifier = modifier.background(Color.DarkGray)) },
        )
    }
}

@Preview
@Composable
private fun PreviewScanCodeScreenDenied() {
    ESTheme {
        ScanCodeScreen(
            uiState = ScanCodeUIState(),
            cameraPermissionStatus = CameraPermissionStatus.DENIED_FOREVER,
            snackbarState = rememberESSnackbarState(),
            onBackClicked = {},
            onAllowCameraClicked = {},
            onOpenSettingsClicked = {},
            camera = {},
        )
    }
}
