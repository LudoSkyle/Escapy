package fr.skyle.escapy.platform.camera

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.UIKitInteropProperties
import androidx.compose.ui.viewinterop.UIKitView
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.readValue
import platform.AVFoundation.AVCaptureConnection
import platform.AVFoundation.AVCaptureDevice
import platform.AVFoundation.AVCaptureDeviceInput
import platform.AVFoundation.AVCaptureDevicePositionBack
import platform.AVFoundation.AVCaptureDeviceTypeBuiltInDualWideCamera
import platform.AVFoundation.AVCaptureDeviceTypeBuiltInTripleCamera
import platform.AVFoundation.AVCaptureDeviceTypeBuiltInWideAngleCamera
import platform.AVFoundation.AVCaptureFocusModeContinuousAutoFocus
import platform.AVFoundation.AVCaptureMetadataOutput
import platform.AVFoundation.AVCaptureMetadataOutputObjectsDelegateProtocol
import platform.AVFoundation.AVCaptureOutput
import platform.AVFoundation.AVCaptureSession
import platform.AVFoundation.AVCaptureVideoPreviewLayer
import platform.AVFoundation.AVLayerVideoGravityResizeAspectFill
import platform.AVFoundation.AVMediaTypeVideo
import platform.AVFoundation.AVMetadataMachineReadableCodeObject
import platform.AVFoundation.AVMetadataObjectTypeQRCode
import platform.AVFoundation.defaultDeviceWithDeviceType
import platform.AVFoundation.focusMode
import platform.AVFoundation.isFocusModeSupported
import platform.AVFoundation.virtualDeviceSwitchOverVideoZoomFactors
import platform.AVFoundation.videoZoomFactor
import platform.CoreGraphics.CGRectZero
import platform.Foundation.NSNumber
import platform.QuartzCore.CATransaction
import platform.UIKit.UIView
import platform.darwin.DISPATCH_QUEUE_PRIORITY_DEFAULT
import platform.darwin.NSObject
import platform.darwin.dispatch_async
import platform.darwin.dispatch_get_global_queue
import platform.darwin.dispatch_get_main_queue

@Composable
actual fun QrCodeCamera(
    isReading: Boolean,
    onQrCodeRead: (content: String) -> Unit,
    onCameraUnavailable: () -> Unit,
    modifier: Modifier,
) {
    val simulatedContent = SimulatedQrCode.content
    if (simulatedContent != null) {
        SimulatedQrCodeCamera(
            modifier = modifier,
            content = simulatedContent,
            isReading = isReading,
            onQrCodeRead = onQrCodeRead,
        )
        return
    }

    val currentIsReading by rememberUpdatedState(isReading)
    val currentOnQrCodeRead by rememberUpdatedState(onQrCodeRead)
    val camera = remember { IosQrCodeCamera() }

    DisposableEffect(camera) {
        val isStarted = camera.start { content ->
            if (currentIsReading) {
                currentOnQrCodeRead(content)
            }
        }
        if (!isStarted) {
            onCameraUnavailable()
        }

        onDispose {
            camera.stop()
        }
    }

    UIKitView(
        modifier = modifier,
        factory = {
            camera.previewView
        },
        properties = UIKitInteropProperties(interactionMode = null),
    )
}

@OptIn(ExperimentalForeignApi::class)
private class IosQrCodeCamera {

    private val session = AVCaptureSession()
    private val metadataOutput = AVCaptureMetadataOutput()

    // AVFoundation keeps a weak reference to its delegate
    private var metadataDelegate: MetadataDelegate? = null

    val previewView = CameraPreviewView(session = session)

    // False when the device has no back camera, as the simulator
    fun start(onQrCodeRead: (String) -> Unit): Boolean {
        val camera = backCamera() ?: return false
        val input = AVCaptureDeviceInput.deviceInputWithDevice(device = camera, error = null) ?: return false

        val delegate = MetadataDelegate(onQrCodeRead = onQrCodeRead)
        session.beginConfiguration()
        if (session.canAddInput(input)) {
            session.addInput(input)
        }
        if (session.canAddOutput(metadataOutput)) {
            session.addOutput(metadataOutput)
        }
        metadataOutput.setMetadataObjectsDelegate(objectsDelegate = delegate, queue = dispatch_get_main_queue())
        // QR codes only, once the output is in the session, which lists the types it can read
        if (AVMetadataObjectTypeQRCode in metadataOutput.availableMetadataObjectTypes) {
            metadataOutput.metadataObjectTypes = listOf(AVMetadataObjectTypeQRCode)
        }
        session.commitConfiguration()
        metadataDelegate = delegate

        if (camera.lockForConfiguration(null)) {
            // A virtual camera starts on its ultra wide angle camera, 0.5x: framed as the wide angle one
            (camera.virtualDeviceSwitchOverVideoZoomFactors.firstOrNull() as? NSNumber)?.doubleValue?.let { factor ->
                camera.videoZoomFactor = factor
            }
            if (camera.isFocusModeSupported(AVCaptureFocusModeContinuousAutoFocus)) {
                camera.focusMode = AVCaptureFocusModeContinuousAutoFocus
            }
            camera.unlockForConfiguration()
        }

        // Starting a session blocks until the camera runs: never on the main thread
        dispatch_async(dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT.toLong(), 0u)) {
            session.startRunning()
        }
        return true
    }

    fun stop() {
        dispatch_async(dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT.toLong(), 0u)) {
            session.stopRunning()
        }
    }

    /**
     * The virtual camera that switches by itself to the ultra wide angle one, which focuses the
     * closest, as a code comes near; else the wide angle camera.
     */
    private fun backCamera(): AVCaptureDevice? =
        listOf(
            AVCaptureDeviceTypeBuiltInTripleCamera,
            AVCaptureDeviceTypeBuiltInDualWideCamera,
            AVCaptureDeviceTypeBuiltInWideAngleCamera,
        ).firstNotNullOfOrNull { deviceType ->
            AVCaptureDevice.defaultDeviceWithDeviceType(
                deviceType = deviceType,
                mediaType = AVMediaTypeVideo,
                position = AVCaptureDevicePositionBack,
            )
        }
}

@OptIn(ExperimentalForeignApi::class)
private class CameraPreviewView(
    session: AVCaptureSession,
) : UIView(frame = CGRectZero.readValue()) {

    private val previewLayer = AVCaptureVideoPreviewLayer(session = session).apply {
        videoGravity = AVLayerVideoGravityResizeAspectFill
    }

    init {
        layer.addSublayer(previewLayer)
    }

    override fun layoutSubviews() {
        super.layoutSubviews()
        // Without the implicit animation of a layer, which would lag behind the resized view
        CATransaction.begin()
        CATransaction.setDisableActions(true)
        previewLayer.frame = bounds
        CATransaction.commit()
    }
}

private class MetadataDelegate(
    private val onQrCodeRead: (String) -> Unit,
) : NSObject(), AVCaptureMetadataOutputObjectsDelegateProtocol {

    override fun captureOutput(
        output: AVCaptureOutput,
        didOutputMetadataObjects: List<*>,
        fromConnection: AVCaptureConnection,
    ) {
        didOutputMetadataObjects
            .filterIsInstance<AVMetadataMachineReadableCodeObject>()
            .mapNotNull { code -> code.stringValue }
            .forEach(onQrCodeRead)
    }
}
