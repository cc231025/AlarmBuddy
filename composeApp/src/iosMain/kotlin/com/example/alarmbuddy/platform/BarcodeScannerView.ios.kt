package com.example.alarmbuddy.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.interop.UIKitView
import kotlinx.cinterop.ExperimentalForeignApi
import platform.AVFoundation.AVCaptureConnection
import platform.AVFoundation.AVCaptureDevice
import platform.AVFoundation.AVCaptureDeviceInput
import platform.AVFoundation.AVCaptureMetadataOutput
import platform.AVFoundation.AVCaptureMetadataOutputObjectsDelegateProtocol
import platform.AVFoundation.AVCaptureOutput
import platform.AVFoundation.AVCaptureSession
import platform.AVFoundation.AVCaptureVideoPreviewLayer
import platform.AVFoundation.AVLayerVideoGravityResizeAspectFill
import platform.AVFoundation.AVMediaTypeVideo
import platform.AVFoundation.AVMetadataMachineReadableCodeObject
import platform.AVFoundation.AVMetadataObjectTypeCode128Code
import platform.AVFoundation.AVMetadataObjectTypeCode39Code
import platform.AVFoundation.AVMetadataObjectTypeEAN13Code
import platform.AVFoundation.AVMetadataObjectTypeEAN8Code
import platform.AVFoundation.AVMetadataObjectTypeQRCode
import platform.AVFoundation.AVMetadataObjectTypeUPCECode
import platform.Foundation.NSObject
import platform.UIKit.UIView
import platform.darwin.dispatch_get_main_queue

// Replaces CameraX + ML Kit's BarcodeScanning client. AVCaptureMetadataOutput
// detects barcodes/QR codes natively -- no extra dependency needed, unlike on
// Android where ML Kit is a separate library.
@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun BarcodeScannerView(modifier: Modifier, onBarcodeDetected: (String) -> Unit) {
    val session = remember { AVCaptureSession() }
    val previewLayer = remember { AVCaptureVideoPreviewLayer(session = session) }
    val delegate = remember { BarcodeMetadataDelegate(onBarcodeDetected) }

    DisposableEffect(Unit) {
        configureSession(session, previewLayer, delegate)
        session.startRunning()
        onDispose { session.stopRunning() }
    }

    UIKitView(
        factory = {
            val container = UIView()
            previewLayer.setFrame(container.bounds)
            container.layer.addSublayer(previewLayer)
            container
        },
        modifier = modifier,
        update = { view -> previewLayer.setFrame(view.bounds) },
    )
}

@OptIn(ExperimentalForeignApi::class)
private fun configureSession(
    session: AVCaptureSession,
    previewLayer: AVCaptureVideoPreviewLayer,
    delegate: BarcodeMetadataDelegate,
) {
    session.beginConfiguration()
    previewLayer.videoGravity = AVLayerVideoGravityResizeAspectFill

    val device = AVCaptureDevice.defaultDeviceWithMediaType(AVMediaTypeVideo)
    val input = device?.let { AVCaptureDeviceInput.deviceInputWithDevice(it, error = null) }
    if (input != null && session.canAddInput(input)) {
        session.addInput(input)
    }

    val output = AVCaptureMetadataOutput()
    if (session.canAddOutput(output)) {
        session.addOutput(output)
        output.setMetadataObjectsDelegate(delegate, queue = dispatch_get_main_queue())
        output.metadataObjectTypes = listOf(
            AVMetadataObjectTypeQRCode,
            AVMetadataObjectTypeEAN13Code,
            AVMetadataObjectTypeEAN8Code,
            AVMetadataObjectTypeCode128Code,
            AVMetadataObjectTypeCode39Code,
            AVMetadataObjectTypeUPCECode,
        )
    }
    session.commitConfiguration()
}

@OptIn(ExperimentalForeignApi::class)
private class BarcodeMetadataDelegate(
    private val onBarcodeDetected: (String) -> Unit,
) : NSObject(), AVCaptureMetadataOutputObjectsDelegateProtocol {

    override fun captureOutput(
        output: AVCaptureOutput,
        didOutputMetadataObjects: List<*>,
        fromConnection: AVCaptureConnection,
    ) {
        didOutputMetadataObjects
            .filterIsInstance<AVMetadataMachineReadableCodeObject>()
            .firstOrNull()
            ?.stringValue
            ?.let(onBarcodeDetected)
    }
}
