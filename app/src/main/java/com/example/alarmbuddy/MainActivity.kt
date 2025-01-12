package com.example.alarmbuddy

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.content.res.AppCompatResources
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.alarmbuddy.ui.AlarmBuddyApp
import com.example.alarmbuddy.ui.theme.AlarmBuddyTheme
import com.google.accompanist.drawablepainter.rememberDrawablePainter

class MainActivity : ComponentActivity() {


    //  Render my actual App composables when all permissions have been given
    private fun startAlarmBuddy() {

//      Safety final permission check if something went wrong in the permission process
        if (!(Settings.System.canWrite(this) && Settings.canDrawOverlays(this) && allPermisionsGranted())) {

            setContent {
                AlarmBuddyTheme {
                    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                        ManualPermissionUi(
                            Modifier.padding(innerPadding)
                        )
                    }
                }
            }

        } else {


            // Get intent from AlarmReceiver when it restarts the activity on alarm activation
            // Only works for inital Launch when Receiver is triggered
            var navigateTo = intent?.getStringExtra("navigateTo") ?: ""
            var alarmId = intent.getIntExtra("id", -1)

//          If the App is terminated during Alarm process or Opened with Notification/App Icon Intent is not sufficient
//          Therefore we observe SharedPreferences additionally to ensure correct execution of the Alarm
            if (navigateTo == "") {
                val sharedPref = getSharedPreferences("AlarmState", Context.MODE_PRIVATE)
                navigateTo = sharedPref.getString("navigateTo", "") ?: ""
                alarmId = sharedPref.getInt("id", -1)
            }


            setContent {
                AlarmBuddyTheme {
                    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                        AlarmBuddyApp(
                            Modifier.padding(innerPadding),
                            navigateTo = navigateTo,
                            alarmId = alarmId
                        )
                    }
                }
            }
        }

    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()


//      First check if all permissions have been given

        if (Settings.System.canWrite(this) && Settings.canDrawOverlays(this) && allPermisionsGranted()) {
            startAlarmBuddy()
        } else {


            setContent {
                AlarmBuddyTheme {
                    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                        PermissionUi(
                            Modifier.padding(innerPadding)
                        )
                    }
                }
            }
        }


    }

    private fun requestPermissions() {
        activityResultLauncher.launch(REQUIRED_PERMISSIONS)
    }

//    Just check If all permissions have been granted except permissions that require Settings Menu
    private fun allPermisionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

//  Ask for Camera and Notification Permissions
//  If denied navigate to ManualPermissionUI Composable
    private val activityResultLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            //HandlePermission Granted or rejected
            var permissionGranted = true
            permissions.entries.forEach {
                if (it.key in REQUIRED_PERMISSIONS && it.value == false)
                    permissionGranted = false
            }
            if (!permissionGranted) {
                Toast.makeText(
                    baseContext,
                    "Permission request denied",
                    Toast.LENGTH_SHORT
                ).show()
                setContent {
                    AlarmBuddyTheme {
                        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                            ManualPermissionUi(
                                Modifier.padding(innerPadding)
                            )
                        }
                    }
                }
            } else {
                startAlarmBuddy()

            }

        }


//    Group Camera and Notification Permission for further checking and requests
    companion object {

        private val REQUIRED_PERMISSIONS =
            mutableListOf(
                Manifest.permission.CAMERA,
                Manifest.permission.POST_NOTIFICATIONS,
            ).apply {
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                    add(Manifest.permission.WRITE_EXTERNAL_STORAGE)

                }
            }.toTypedArray()

    }

    fun permissionManager() {

//      Start with Settings that require opening the Settings Menu

        if (!Settings.System.canWrite(this)) {
            val intent = Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS).apply {
                data = Uri.parse("package:$packageName")
            }
            this.startActivity(intent)
        }

        if (!Settings.canDrawOverlays(this)) {
            val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION).apply {
                data = Uri.parse("package:$packageName")
            }
            this.startActivity(intent)

        }


//      Go on with Pop Up permissions
        if (allPermisionsGranted()) {
            startAlarmBuddy()
        } else {
            requestPermissions()
        }


    }


//  Show the User the Necessary Permissions Initially
//  On Button Click start the permissionProcess with permissionManager()
    @Composable
    fun PermissionUi(
        modifier: Modifier = Modifier,
    ) {



        Column(
            Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {

            Image(
                modifier = Modifier.padding(horizontal = 30.dp),
                painter = rememberDrawablePainter(
                    drawable = AppCompatResources.getDrawable(
                        LocalContext.current,
                        R.drawable.permission
                    )
                ),
                contentDescription = "Permission",
                contentScale = ContentScale.FillWidth,
            )
            Spacer(Modifier.height(12.dp))

            Text(text = "Required Permissions", fontSize = 24.sp)
            Spacer(Modifier.height(20.dp))
            OutlinedCard(
                Modifier
                    .fillMaxWidth()
            ) {
                Column(
                    Modifier.padding(8.dp)
                ) {
                    Text(
                        text = "Modify System Settings",
                        color = colorResource(id = R.color.secondary)
                    )
                    Spacer(Modifier.height(10.dp))
                    Text(
                        text = "Needed to override the Alarm Volume. This ensures you are woken up and can't overhear your Alarm on accident.",
                        fontSize = 14.sp,
                        color = Color.LightGray
                    )
                }
            }

            Spacer(Modifier.height(12.dp))


            OutlinedCard(
                Modifier
                    .fillMaxWidth()
            ) {
                Column(
                    Modifier.padding(8.dp)
                ) {
                    Text(
                        text = "Display over other Apps",
                        color = colorResource(id = R.color.secondary)
                    )
                    Spacer(Modifier.height(10.dp))
                    Text(
                        text = "Let AlarmBuddy come first, and ensure it can always be displayed when an Alarm is ringing.",
                        fontSize = 14.sp,
                        color = Color.LightGray
                    )
                }
            }

            Spacer(Modifier.height(12.dp))


            OutlinedCard(
                Modifier
                    .fillMaxWidth()
            ) {
                Column(
                    Modifier.padding(8.dp)
                ) {
                    Text(
                        text = "Camera Access",
                        color = colorResource(id = R.color.secondary)
                    )
                    Spacer(Modifier.height(10.dp))
                    Text(
                        text = "Set personal Barcodes and Scan them to stop the Alarm.",
                        fontSize = 14.sp,
                        color = Color.LightGray
                    )
                }
            }

            Spacer(Modifier.height(12.dp))


            OutlinedCard(
                Modifier
                    .fillMaxWidth()
            ) {
                Column(
                    Modifier.padding(8.dp)
                ) {
                    Text(
                        text = "Allow Notifications",
                        color = colorResource(id = R.color.secondary)
                    )
                    Spacer(Modifier.height(10.dp))
                    Text(
                        text = "Ensure you will always get notified about the Alarm.",
                        fontSize = 14.sp,
                        color = Color.LightGray
                    )
                }
            }

            Spacer(Modifier.height(12.dp))



            Button(modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 30.dp), onClick = {
                permissionManager()
            }) {
                Text(text = "Give Permissions")
            }
        }

    }


//    If some permissions have been denied show this screen to prompt the user to open settings manually
    @Composable
    fun ManualPermissionUi(
        modifier: Modifier = Modifier,
    ) {

        Column(
            Modifier
                .fillMaxWidth()
                .padding(20.dp)
                .verticalScroll(rememberScrollState())
        ) {

            Image(
                modifier = Modifier.padding(horizontal = 30.dp),   //crops the image to circle shape
                painter = rememberDrawablePainter(
                    drawable = AppCompatResources.getDrawable(
                        LocalContext.current,
                        R.drawable.permission
                    )
                ),
                contentDescription = "Permission",
                contentScale = ContentScale.FillWidth,
            )
            Spacer(Modifier.height(12.dp))

            Text(text = "Permissions Denied!", fontSize = 24.sp, color = Color.Red)
            Spacer(Modifier.height(8.dp))

            Text(
                text = "Please go to the App Settings, and make sure to enable all listed permissions. Then restart AlarmBuddy! ",
                fontSize = 14.sp
            )

            Spacer(Modifier.height(20.dp))
            OutlinedCard(
                Modifier
                    .fillMaxWidth()
            ) {
                Column( // Wrap the content in a Column or Box if needed
                    Modifier.padding(8.dp) // Padding inside the card
                ) {
                    Text(
                        text = "Modify System Settings",
                        color = colorResource(id = R.color.secondary)
                    )
                    Spacer(Modifier.height(10.dp))
                    Text(
                        text = "Needed to override the Alarm Volume. This ensures you are woken up and can't overhear your Alarm on accident.",
                        fontSize = 14.sp,
                        color = Color.LightGray
                    )
                }
            }

            Spacer(Modifier.height(12.dp))


            OutlinedCard(
                Modifier
                    .fillMaxWidth()
            ) {
                Column( // Wrap the content in a Column or Box if needed
                    Modifier.padding(8.dp) // Padding inside the card
                ) {
                    Text(
                        text = "Display over other Apps",
                        color = colorResource(id = R.color.secondary)
                    )
                    Spacer(Modifier.height(10.dp))
                    Text(
                        text = "Let AlarmBuddy come first, and ensure it can always be displayed when an Alarm is ringing.",
                        fontSize = 14.sp,
                        color = Color.LightGray
                    )
                }
            }

            Spacer(Modifier.height(12.dp))


            OutlinedCard(
                Modifier
                    .fillMaxWidth()
            ) {
                Column( // Wrap the content in a Column or Box if needed
                    Modifier.padding(8.dp) // Padding inside the card
                ) {
                    Text(
                        text = "Camera Access",
                        color = colorResource(id = R.color.secondary)
                    )
                    Spacer(Modifier.height(10.dp))
                    Text(
                        text = "Set personal Barcodes and Scan them to stop the Alarm.",
                        fontSize = 14.sp,
                        color = Color.LightGray
                    )
                }
            }

            Spacer(Modifier.height(12.dp))


            OutlinedCard(
                Modifier
                    .fillMaxWidth()
            ) {
                Column( // Wrap the content in a Column or Box if needed
                    Modifier.padding(8.dp) // Padding inside the card
                ) {
                    Text(
                        text = "Allow Notifications",
                        color = colorResource(id = R.color.secondary)
                    )
                    Spacer(Modifier.height(10.dp))
                    Text(
                        text = "Ensure you will always get notified about the Alarm.",
                        fontSize = 14.sp,
                        color = Color.LightGray
                    )
                }
            }

            Spacer(Modifier.height(12.dp))



            Button(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 30.dp), onClick = {
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color.LightGray)
            ) {
                Text(text = "Give Permissions")
            }
        }


    }
}
