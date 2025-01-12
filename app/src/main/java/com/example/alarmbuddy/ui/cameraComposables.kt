package com.example.alarmbuddy.ui

import android.annotation.SuppressLint
import android.content.Context
import android.widget.Toast
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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



//
@Composable
fun AddnewBarcode(
    showPopup: Boolean,
    viewModel: AlarmViewModel,
    navController: NavController,
    barcodeState: String
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
            Text(text = "Name your Barcode, in a way you will recognize even in your half awake zombie State!", textAlign = TextAlign.Center)

            Spacer(Modifier.height(32.dp))
            OutlinedTextField(
                value = name,
                onValueChange = { newValue -> name = newValue },
                Modifier.fillMaxWidth(),
                label = { Text("Barcode Name") })
            Spacer(Modifier.height(100.dp))



            Button(modifier = Modifier.fillMaxWidth(),
                colors = MainButtonColors(),
                contentPadding = PaddingValues(
                    vertical = 12.dp
                ), onClick = {

                viewModel.addBarcode(
                    com.example.alarmbuddy.data.Barcode(
                        name = name,
                        barcode = barcodeState
                    )
                )
                navController.popBackStack("Camera/setBarcode", inclusive = true)

            }) {
                Text(text = "Save Barcode", fontSize = 20.sp)


            }

        }


    }

}


// My camera Composable does two things at once depending on the Intents confirm- setBarcode
// It as said either sets a new Barcode by adding it to the barcode dao
// or confirms the correct barcode when called from the Ringing composable to stop the alarm
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

//    This initializes the BarcodeAnalyzer from a 3rd party library
    val imageAnalysis = ImageAnalysis.Builder().build()
    imageAnalysis.setAnalyzer(
        ContextCompat.getMainExecutor(context),
        BarcodeAnalyzer(context, Intent, showPopup, barcodeState)
    )

//  Start the cameraPreview with imageAnalysis, Selector ...
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


// Analyze barcodes, based on Intent either the AddnewBarcode composable will open or the barcode will be added to the database
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