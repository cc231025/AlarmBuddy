package com.example.alarmbuddy.ui

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavController
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine


@Composable
fun CameraSetup(navController: NavController, context: Context) {

    var showAlert by remember {mutableStateOf(false)}


    // Permission request launcher
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            Toast.makeText(context, "Camera permission Granted", Toast.LENGTH_SHORT).show()
            navController.navigate(Screens.Camera.name)

//            Is granted start camera here aswell
        } else {

            showAlert = true


        }
    }

    // Check permissions when the composable is first loaded
    LaunchedEffect(Unit) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
//            Is granted start trhe camera here
            Toast.makeText(context, "Camera permission Granted first", Toast.LENGTH_SHORT).show()
            navController.navigate(Screens.Camera.name)


        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }




    if (showAlert){
        AlertDialog(
            onDismissRequest = {},
            title = { Text("Permission Required") },
            text = { Text("Camera access is required. Please enable it in settings.") },
            confirmButton = {
                Button (onClick = {
                    showAlert = false
                    navController.navigate(Screens.Home.name)
                    Toast.makeText(context, "Navigate to the Settings of the App to change the permission", Toast.LENGTH_SHORT).show()


                }) {
                    Text("Open Settings")
                }
            },
            dismissButton = {
                Button(onClick = {
                    showAlert = false
                    navController.navigate(Screens.Home.name)
                    Toast.makeText(context, "Camera permission denied", Toast.LENGTH_SHORT)
                        .show() }) {
                    Text("Cancel")
                }
            }
        )
    }

}


@Composable
fun Camera(context: Context) {


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
        BarcodeAnalyzer(context)
    )


    LaunchedEffect(lensFacing) {
        val cameraProvider = context.getCameraProvider()
        cameraProvider.unbindAll()
        cameraProvider.bindToLifecycle(lifecycleOwner, cameraxSelector, preview, imageAnalysis)
        preview.setSurfaceProvider(previewView.surfaceProvider)
    }
    AndroidView(factory = { previewView }, modifier = Modifier.fillMaxSize())


}

private suspend fun Context.getCameraProvider(): ProcessCameraProvider =
    suspendCoroutine { continuation ->
        ProcessCameraProvider.getInstance(this).also { cameraProvider ->
            cameraProvider.addListener({
                continuation.resume(cameraProvider.get())
            }, ContextCompat.getMainExecutor(this))
        }
    }


class BarcodeAnalyzer(private val context: Context): ImageAnalysis.Analyzer{

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
                    ?.joinToString("," )
                    ?.let{Toast.makeText(context, it, Toast.LENGTH_SHORT).show()}

            }.addOnCompleteListener {
                imageProxy.close()
            }

        }
    }

}