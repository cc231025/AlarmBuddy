package com.example.alarmbuddy.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign

import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.alarmbuddy.data.Barcode
import com.example.alarmbuddy.platform.BarcodeScannerView

// Replaces the CameraX + ML Kit `Camera` composable. Serves the same two
// purposes as before depending on `mode`: "setBarcode" registers a new
// barcode to use in future alarms, "confirmBarcode" is used from the Ringing
// screen to check a scan against the alarm's stored barcode.
@Composable
fun CameraScreen(
    mode: String,
    viewModel: AlarmViewModel,
    barcodeToConfirm: String = "None",
    onBarcodeSaved: () -> Unit = {},
    onBarcodeConfirmed: () -> Unit = {},
) {
    var showSavePopup by remember { mutableStateOf(false) }
    var scannedValue by remember { mutableStateOf("") }

    if (!showSavePopup) {
        BarcodeScannerView(
            modifier = Modifier.fillMaxSize(),
            onBarcodeDetected = { value ->
                scannedValue = value
                if (mode == "setBarcode") {
                    showSavePopup = true
                } else if (mode == "confirmBarcode" && value == barcodeToConfirm) {
                    onBarcodeConfirmed()
                }
            },
        )
    }

    AddNewBarcode(
        showPopup = showSavePopup,
        scannedValue = scannedValue,
        onSaved = {
            viewModel.addBarcode(Barcode(name = it, barcode = scannedValue))
            onBarcodeSaved()
        },
    )
}

@Composable
private fun AddNewBarcode(
    showPopup: Boolean,
    scannedValue: String,
    onSaved: (name: String) -> Unit,
) {
    var name by remember { mutableStateOf("Toothpaste Bathroom") }

    if (showPopup) {
        Column(
            Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .padding(vertical = 100.dp, horizontal = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "Name your Barcode, in a way you will recognize even in your half awake zombie State!",
                textAlign = TextAlign.Center,
            )

            Spacer(Modifier.height(32.dp))
            OutlinedTextField(
                value = name,
                onValueChange = { newValue -> name = newValue },
                Modifier.fillMaxWidth(),
                label = { Text("Barcode Name") },
            )
            Spacer(Modifier.height(100.dp))

            Button(
                modifier = Modifier.fillMaxWidth(),
                colors = MainButtonColors(),
                contentPadding = PaddingValues(vertical = 12.dp),
                onClick = { onSaved(name) },
            ) {
                Text(text = "Save Barcode", fontSize = 20.sp)
            }
        }
    }
}
