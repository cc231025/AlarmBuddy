package com.example.alarmbuddy.platform

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

// Replaces CameraX + ML Kit's BarcodeScanning client. iOS actual uses
// AVCaptureSession + AVCaptureMetadataOutput (native barcode/QR detection,
// no ML Kit dependency needed) rendered through a UIKitView-wrapped
// AVCaptureVideoPreviewLayer.
@Composable
expect fun BarcodeScannerView(
    modifier: Modifier = Modifier,
    onBarcodeDetected: (String) -> Unit,
)
