package com.example.alarmbuddy.ui

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import androidx.compose.ui.zIndex
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

//import com.example.alarmbuddy.data.Barcode


@Composable
fun CameraSetup(navController: NavController, context: Context, Intent: String) {


    var showAlert by remember { mutableStateOf(false) }


    // Permission request launcher
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            Toast.makeText(context, "Camera permission Granted", Toast.LENGTH_SHORT).show()
            navController.navigate("Camera/${Intent}")

//            Is granted start camera here aswell
        } else {

            showAlert = true


        }
    }

    // Check permissions when the composable is first loaded
    LaunchedEffect(Unit) {
        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        ) {
//            Is granted start trhe camera here
            Toast.makeText(context, "Camera permission Granted first", Toast.LENGTH_SHORT).show()
            navController.navigate("Camera/${Intent}")


        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    if (showAlert) {
        AlertDialog(
            onDismissRequest = {},
            title = { Text("Permission Required") },
            text = { Text("Camera access is required. Please enable it in settings.") },
            confirmButton = {
                Button(onClick = {
                    showAlert = false
                    navController.navigate(Screens.Home.name)
                    Toast.makeText(
                        context,
                        "Navigate to the Settings of the App to change the permission",
                        Toast.LENGTH_SHORT
                    ).show()


                }) {
                    Text("Open Settings")
                }
            },
            dismissButton = {
                Button(onClick = {
                    showAlert = false
                    navController.navigate(Screens.Home.name)
                    Toast.makeText(context, "Camera permission denied", Toast.LENGTH_SHORT)
                        .show()
                }) {
                    Text("Cancel")
                }
            }
        )
    }

}

@Composable
fun AddnewBarcode(
    showPopup: Boolean,
    viewModel: AlarmViewModel,
    navController: NavController,
    barcodeState: String
) {

    var name by remember { mutableStateOf("MyAlarm") }

    if (showPopup) {


        Column(
            Modifier
                .fillMaxWidth()
                .padding(vertical = 50.dp, horizontal = 10.dp),
        ) {
            OutlinedTextField(
                value = barcodeState,
                onValueChange = {},
                Modifier.fillMaxWidth(),
                enabled = false,
                label = { Text("Barcode Value") })
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = name,
                onValueChange = { newValue -> name = newValue },
                Modifier.fillMaxWidth(),
                label = { Text("Barcode Name (Bathroom Toothpaste ...)") })
            Spacer(Modifier.height(8.dp))

            Button(onClick = {

                viewModel.addBarcode(
                    com.example.alarmbuddy.data.Barcode(
                        name = name,
                        barcode = barcodeState
                    )
                )
                navController.popBackStack("CameraSetup/setBarcode", inclusive = true)

            }) {
                Text(text = "Save Barcode")


            }

        }


    }

}


@Composable
fun Camera(
    context: Context,
    Intent: String,
    navController: NavController,
    viewModel: AlarmViewModel,
    barcodeToConfirm: String = "None",
    barcodeConfirmed: () -> Unit = {},
) {

    var showPopup = remember { mutableStateOf(false) }
    var barcodeState = remember { mutableStateOf("0") }


    val lensFacing = CameraSelector.LENS_FACING_BACK

    val lifecycleOwner = LocalLifecycleOwner.current

    val preview = Preview.Builder().build()

    val previewView = remember {
        PreviewView(context)
    }

    val cameraxSelector = CameraSelector.Builder().requireLensFacing(lensFacing).build()

    val imageAnalysis = ImageAnalysis.Builder().build()
    imageAnalysis.setAnalyzer(
        ContextCompat.getMainExecutor(context),
        BarcodeAnalyzer(context, Intent, showPopup, barcodeState)
    )


    LaunchedEffect(lensFacing) {
        val cameraProvider = context.getCameraProvider()
        cameraProvider.unbindAll()
        cameraProvider.bindToLifecycle(lifecycleOwner, cameraxSelector, preview, imageAnalysis)
        preview.setSurfaceProvider(previewView.surfaceProvider)
    }
    if (!showPopup.value) {
        AndroidView(factory = { previewView }, modifier = Modifier.fillMaxSize())
    }

    if (Intent == "confirmBarcode" && barcodeState.value == barcodeToConfirm) {
        Toast.makeText(context, "Confirmed the correct barcode", Toast.LENGTH_SHORT).show()
        barcodeConfirmed()

    }

    AddnewBarcode(
        showPopup = showPopup.value,
        viewModel,
        navController,
        barcodeState.value
    )


}

private suspend fun Context.getCameraProvider(): ProcessCameraProvider =
    suspendCoroutine { continuation ->
        ProcessCameraProvider.getInstance(this).also { cameraProvider ->
            cameraProvider.addListener({
                continuation.resume(cameraProvider.get())
            }, ContextCompat.getMainExecutor(this))
        }
    }


class BarcodeAnalyzer(
    private val context: Context,
    private val Intent: String,
    private var showPopup: MutableState<Boolean>,
    private var barcodeState: MutableState<String>,
) : ImageAnalysis.Analyzer {


    private val options = BarcodeScannerOptions.Builder()
        .setBarcodeFormats(Barcode.FORMAT_ALL_FORMATS)
        .build()

    private val scanner = BarcodeScanning.getClient(options)

    @SuppressLint("UnsafeOptInUsageError")

    override fun analyze(imageProxy: ImageProxy) {
        imageProxy.image?.let { image ->
            scanner.process(
                InputImage.fromMediaImage(
                    image, imageProxy.imageInfo.rotationDegrees
                )
            ).addOnSuccessListener { barcode ->
                barcode?.takeIf { it.isNotEmpty() }
                    ?.mapNotNull { it.rawValue }
                    ?.joinToString(",")
                    ?.let {
                        if (Intent == "setBarcode") {
                            showPopup.value = true
                            barcodeState.value = it
                            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
                            imageProxy.close()


                        } else if (Intent == "confirmBarcode") {
                            barcodeState.value = it
                            imageProxy.close()



                        } else {
                            Toast.makeText(context, "Faulty Intent set", Toast.LENGTH_SHORT).show()

                        }

                        imageProxy.close()


                    }

            }.addOnCompleteListener {
                imageProxy.close()
            }

        }
    }

}