package fr.skyle.escapy.ui.screen.scancode.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import fr.skyle.escapy.designsystem.core.snackbar.rememberESSnackbarState
import fr.skyle.escapy.platform.camera.CameraPermissionStatus
import fr.skyle.escapy.platform.camera.QrCodeCamera
import fr.skyle.escapy.platform.camera.rememberCameraPermissionState
import fr.skyle.escapy.ui.core.error.gameErrorMessages
import fr.skyle.escapy.ui.core.event.ObserveEvents
import fr.skyle.escapy.ui.screen.scancode.ui.ScanCodeViewModel.ScanCodeEvent
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun ScanCodeRoute(
    navigateBack: () -> Unit,
    navigateToGame: (gameId: String) -> Unit,
    viewModel: ScanCodeViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val cameraPermission = rememberCameraPermissionState()
    val snackbarState = rememberESSnackbarState()
    val errorMessages = gameErrorMessages()

    // The user opened the scan to use the camera: asked at once, the first time
    LaunchedEffect(Unit) {
        if (cameraPermission.status == CameraPermissionStatus.UNKNOWN) {
            cameraPermission.request()
        }
    }

    ObserveEvents(viewModel.event) { event ->
        when (event) {
            is ScanCodeEvent.OpenGame -> navigateToGame(event.gameId)
            is ScanCodeEvent.ShowError -> snackbarState.show(message = errorMessages.getValue(event.error))
        }
    }

    ScanCodeScreen(
        uiState = uiState,
        cameraPermissionStatus = cameraPermission.status,
        snackbarState = snackbarState,
        onBackClicked = navigateBack,
        onAllowCameraClicked = cameraPermission::request,
        onOpenSettingsClicked = cameraPermission::openAppSettings,
        camera = { modifier ->
            QrCodeCamera(
                modifier = modifier,
                isReading = uiState.isReading,
                onQrCodeRead = viewModel::onQrCodeRead,
                onCameraUnavailable = viewModel::onCameraUnavailable,
            )
        },
    )
}
