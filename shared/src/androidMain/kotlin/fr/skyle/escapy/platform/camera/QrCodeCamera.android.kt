package fr.skyle.escapy.platform.camera

import androidx.camera.compose.CameraXViewfinder
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.core.SurfaceRequest
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.lifecycle.awaitInstance
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import co.touchlab.kermit.Logger
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import kotlinx.coroutines.awaitCancellation
import java.util.concurrent.Executors

// QR codes only: ML Kit looks for nothing else, and reads them sooner
private val qrCodeScannerOptions = BarcodeScannerOptions.Builder()
    .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
    .build()

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
    if (LocalInspectionMode.current) {
        Box(modifier = modifier.background(color = Color.Black))
        return
    }

    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val currentIsReading by rememberUpdatedState(isReading)
    val currentOnQrCodeRead by rememberUpdatedState(onQrCodeRead)
    val currentOnCameraUnavailable by rememberUpdatedState(onCameraUnavailable)
    var surfaceRequest by remember { mutableStateOf<SurfaceRequest?>(null) }

    LaunchedEffect(lifecycleOwner) {
        val cameraProvider = ProcessCameraProvider.awaitInstance(context)
        val barcodeScanner = BarcodeScanning.getClient(qrCodeScannerOptions)
        val analysisExecutor = Executors.newSingleThreadExecutor()
        val mainExecutor = ContextCompat.getMainExecutor(context)

        val preview = Preview.Builder().build().apply {
            setSurfaceProvider { request ->
                surfaceRequest = request
            }
        }
        val imageAnalysis = ImageAnalysis.Builder()
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()
        imageAnalysis.setAnalyzer(analysisExecutor) @androidx.annotation.OptIn(ExperimentalGetImage::class) { image ->
            val mediaImage = image.image
            if (mediaImage == null) {
                image.close()
                return@setAnalyzer
            }
            barcodeScanner.process(InputImage.fromMediaImage(mediaImage, image.imageInfo.rotationDegrees))
                .addOnSuccessListener(mainExecutor) { barcodes ->
                    if (currentIsReading) {
                        barcodes.mapNotNull { barcode -> barcode.rawValue }.forEach { content -> currentOnQrCodeRead(content) }
                    }
                }
                .addOnFailureListener { exception ->
                    Logger.w(throwable = exception) { "Failed to read the QR codes of an image" }
                }
                // An image left open would stop the camera from sending the next ones
                .addOnCompleteListener {
                    image.close()
                }
        }

        try {
            cameraProvider.bindToLifecycle(lifecycleOwner, CameraSelector.DEFAULT_BACK_CAMERA, preview, imageAnalysis)
            awaitCancellation()
        } catch (exception: IllegalArgumentException) {
            // The device has no back camera
            Logger.w(throwable = exception) { "No back camera to scan with" }
            currentOnCameraUnavailable()
        } finally {
            cameraProvider.unbind(preview, imageAnalysis)
            barcodeScanner.close()
            analysisExecutor.shutdown()
        }
    }

    Box(modifier = modifier.background(color = Color.Black)) {
        surfaceRequest?.let { request ->
            CameraXViewfinder(
                modifier = Modifier.fillMaxSize(),
                surfaceRequest = request,
            )
        }
    }
}
