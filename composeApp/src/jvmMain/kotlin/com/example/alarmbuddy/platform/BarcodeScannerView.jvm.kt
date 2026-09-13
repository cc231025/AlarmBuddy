package com.example.alarmbuddy.platform

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

// Desktop stand-in only (never shipped): no camera plumbed in for desktop, so
// this renders a placeholder with a button that fakes a scan -- handy for
// clicking through the Add-alarm/Ringing flow locally without a camera.
@Composable
actual fun BarcodeScannerView(modifier: Modifier, onBarcodeDetected: (String) -> Unit) {
    Column(
        modifier.fillMaxSize().background(Color.DarkGray).padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text("Camera preview isn't available in the desktop dev build.", color = Color.White)
        Button(onClick = { onBarcodeDetected("desktop-fake-barcode") }) {
            Text("Simulate a scan")
        }
    }
}
