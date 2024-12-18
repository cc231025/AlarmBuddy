package com.example.alarmbuddy

import android.content.ContentValues.TAG
import android.nfc.Tag
import android.os.Build
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.test.espresso.ViewFinder
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import android.Manifest
import androidx.activity.result.contract.ActivityResultContracts





//class CameraManager(
//    private val activity: MainActivity
//){
//
//    private val cameraPermissionRequest =
//        activity.registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
//            if (isGranted) {
//                // Implement camera related  code
//            } else {
//                // Camera permission denied
//            }
//
//        }
//
//
//}
//















//class CameraManager(
//    private val activity: AppCompatActivity,  // This will now definitely be an AppCompatActivity
//    private val lifecycleOwner: LifecycleOwner,
//    private val viewFinder: PreviewView
//) {
//
//    private val cameraExecutor: ExecutorService = Executors.newSingleThreadExecutor()
//
//    private var imageCapture: ImageCapture? = null
//
//    fun startCamera() {
//        val cameraProviderFuture = ProcessCameraProvider.getInstance(activity)
//
//        cameraProviderFuture.addListener({
//            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()
//
//            val preview = Preview.Builder()
//                .build()
//                .also {
//                    it.setSurfaceProvider(viewFinder.surfaceProvider)
//                }
//
//            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
//
//            try {
//                cameraProvider.unbindAll()
//                cameraProvider.bindToLifecycle(lifecycleOwner, cameraSelector, preview)
//            } catch (exc: Exception) {
//                Log.e(TAG, "Use case binding failed", exc)
//            }
//        }, ContextCompat.getMainExecutor(activity))
//    }
//
//    fun requestPermissions(onPermissionResult: (Boolean) -> Unit) {
//        val REQUIRED_PERMISSIONS = mutableListOf(
//            Manifest.permission.CAMERA,
//            Manifest.permission.RECORD_AUDIO
//        ).apply {
//            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
//                add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
//            }
//        }.toTypedArray()
//
//        val activityResultLauncher =
//            activity.registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
//                val allGranted = permissions.all { it.value }
//                onPermissionResult(allGranted)
//            }
//
//        activityResultLauncher.launch(REQUIRED_PERMISSIONS)
//    }
//
//    companion object {
//        private const val TAG = "CameraManager"
//    }
//}
