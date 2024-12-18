package com.example.alarmbuddy

import android.app.AlarmManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.ContextCompat
import com.example.alarmbuddy.ui.AlarmBuddyApp
import com.example.alarmbuddy.ui.theme.AlarmBuddyTheme
import android.Manifest

class MainActivity : ComponentActivity() {


//    private val cameraPermissionRequest =
//        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
//            if (isGranted) {
//                // Implement camera related  code
//            } else {
//                // Camera permission denied
//            }
//
//        }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

//        when (PackageManager.PERMISSION_GRANTED) {
//            ContextCompat.checkSelfPermission(
//                this,
//                Manifest.permission.CAMERA
//            ) -> {
//                // Camera permission already granted
//                // Implement camera related code


        val navigateTo = intent?.getStringExtra("navigateTo") ?: ""
        val alarmId =
            intent.getIntExtra("id", -1)  // -1 is the default value if "id" is not found

        setContent {
            AlarmBuddyTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    AlarmBuddyApp(
                        Modifier.padding(innerPadding),
                        navigateTo = navigateTo,
                        alarmId = alarmId// Pass the navigation info to the app
                    )
                }
            }
        }
//            }
//
//            else -> {
//                cameraPermissionRequest.launch(Manifest.permission.CAMERA)
//            }


    }
}
