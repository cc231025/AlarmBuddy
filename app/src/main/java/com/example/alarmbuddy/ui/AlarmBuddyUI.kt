package com.example.alarmbuddy.ui

import android.app.AlarmManager
import android.content.Context
import android.content.res.Resources.Theme
import android.hardware.SensorManager
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.EaseIn
import androidx.compose.animation.core.EaseOut
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerColors
import androidx.compose.material3.rememberTimePickerState
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import androidx.compose.ui.zIndex
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
//import chaintech.network.cmpshakedetection.ShakeDetector
import com.example.alarmbuddy.AlarmApplication
import com.example.alarmbuddy.R
import com.example.alarmbuddy.data.Alarm
import com.example.alarmbuddy.ui.theme.Typography
import java.time.LocalTime
import java.util.Calendar
import java.util.Locale


enum class Screens {
    Home, Add, Ringing,
}


@Composable
fun AlarmBuddyApp(
    modifier: Modifier = Modifier,
    navigateTo: String,
    alarmId: Int
) {

    //    Get needed Application context, alarmManager vieModel...
    val context = LocalContext.current.applicationContext as AlarmApplication

    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    val nextAlarm = alarmManager.nextAlarmClock
    Log.d("AlarmManager", "Next alarm time: ${nextAlarm?.triggerTime}")

    val viewModel: AlarmViewModel = viewModel(
        factory = context.alarmViewModelFactory
    )

    val navController = rememberNavController()


    val alarmSoundArr = listOf(
        R.string.classic_alarm,
        R.string.pain_alarm,
        R.string.granular_alarm,
        R.string.ambient_scifi,
        R.string.increasing_panic,
        R.string.level_up,
        R.string.relaxing_piano,
        R.string.school_clock,
        R.string.sunny_morning,
        R.string.synth_power
    )



//  If navigatTo has a value from before Set Intent or SharedPreferences navigate to Ringing
    LaunchedEffect(navigateTo) {
        if (navigateTo == "Ringing") {
            navController.navigate("Ringing")
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
    ) { innerPadding ->
        NavHost(navController, startDestination = Screens.Home.name,
            enterTransition = {
                fadeIn(
                    animationSpec = tween(
                        300, easing = LinearEasing
                    )
                ) + slideIntoContainer(
                    animationSpec = tween(300, easing = EaseIn),
                    towards = AnimatedContentTransitionScope.SlideDirection.Start
                )
            },
            exitTransition = {
                fadeOut(
                    animationSpec = tween(
                        300, easing = LinearEasing
                    )
                ) + slideOutOfContainer(
                    animationSpec = tween(300, easing = EaseOut),
                    towards = AnimatedContentTransitionScope.SlideDirection.End
                )
            }
        ) {

            composable(
                Screens.Home.name,
            ) {
                Box(modifier = Modifier.padding(innerPadding)) {
                    Home(
                        modifier,
                        navController,
                        viewModel,
                        context
                    )
                }
            }

            composable(Screens.Add.name) {
                Box(modifier = Modifier.padding(innerPadding)) {
                    Add(
                        modifier,
                        navController,
                        viewModel,
                        context,
                        alarmSoundArr
                    )

                }
            }

            composable("Edit/{id}") { navBackStackEntry ->

                val id = navBackStackEntry.arguments?.getString("id")?.toInt() ?: -1
                Box(modifier = Modifier.padding(innerPadding)) {
                    Edit(
                        id,
                        modifier,
                        navController,
                        viewModel,
                        context,
                        alarmSoundArr
                    )

                }
            }


            composable(Screens.Ringing.name) {
                BackHandler(true) {
                    //Do nothing to stop the user from navigating out of the Ringing Screen
                }
                Box(modifier = Modifier.padding(innerPadding)) {
                    Ringing(
                        alarmId,
                        viewModel,
                        context,
                        navController

                    )

                }
            }

            composable("Camera/{Intent}") { navBackStackEntry ->
                val Intent = navBackStackEntry.arguments?.getString("Intent") ?: "setBarcode"

                Camera(
                    context,
                    Intent,
                    navController,
                    viewModel

                )
            }
        }
    }
}


//Our Initial Homescreen, just Give the option to add a new alarm and Show all Alarms from the database
@Composable
fun Home(
    modifier: Modifier = Modifier,
    navController: NavController,
    viewModel: AlarmViewModel,
    context: Context
) {

    val state by viewModel.alarmUIState.collectAsStateWithLifecycle()


    Column(modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {

        Button(
            onClick = {
                navController.navigate(Screens.Add.name)
            }) {
            Icon(imageVector = Icons.Filled.Add, contentDescription = "Add Alarm")
        }

        LazyColumn {

            itemsIndexed(state) { _, alarm ->
                AlarmItem(alarm, viewModel, context, navController)
            }
        }


    }


}

//Single Alarm Instance displayed in the Home Screen
//Bases on the alarms values displays which tasks are active, lets the user delete or navigate to the edit screen
//And allows enabling/disabling the alarm
@Composable
fun AlarmItem(
    alarm: Alarm,
    viewModel: AlarmViewModel,
    context: Context,
    navController: NavController

) {


    OutlinedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
    ) {
        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {

            Column(Modifier.padding(16.dp)) {

                Row(horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(
                        style = Typography.headlineMedium,
                        text = String.format(
                            Locale.getDefault(),
                            "%02d:%02d",
                            alarm.time.hour,
                            alarm.time.minute
                        ),
                        color = Color.Gray

                    )
                }
                Row(horizontalArrangement = Arrangement.Start) {

                    Button(
                        contentPadding = PaddingValues(4.dp),
                        modifier = Modifier.defaultMinSize(minWidth = 6.dp, minHeight = 6.dp),

                        onClick = {
                            if (alarm.activated) {
                                cancelAlarm(context, alarm)
                            }

                            viewModel.deleteAlarm(alarm)
                        }) {
                        Icon(imageVector = Icons.Filled.Delete, contentDescription = "Delete")
                    }
                    Button(
                        contentPadding = PaddingValues(4.dp),
                        modifier = Modifier.defaultMinSize(minWidth = 6.dp, minHeight = 6.dp),
                        onClick = {
                            navController.navigate("Edit/${alarm.id}")

                        }) {
                        Icon(imageVector = Icons.Filled.Edit, contentDescription = "Edit")
                    }

                }


            }

            Column(Modifier.padding(16.dp), horizontalAlignment = Alignment.End) {


                Switch(
                    checked = alarm.activated,
                    onCheckedChange = {
//                    Some weird logic here, the alarmstate is changed before it reaches here even
//                    So since its already changed we have to use !alarm - dirty work around but it works after all
                        if (!alarm.activated) {

                            scheduleExactAlarm(context, alarm)
                            viewModel.updateAlarm(alarm.copy(activated = true))


                        } else {
                            cancelAlarm(context, alarm)
                            viewModel.updateAlarm(alarm.copy(activated = false))


                        }
                    }
                )

                Text(text = alarm.audioFile)
                Spacer(Modifier.height(6.dp))
                Row() {

                    if(alarm.barcodeTask){

                        Icon(
                            painter = painterResource(R.drawable.barcode),
                            contentDescription = "Edit",
                            modifier = Modifier
                                .clip(RoundedCornerShape(50.dp))
                                .background(colorResource(id = R.color.secondary))
                                .padding(4.dp),
                            tint = Color.Black
                        )
                        Spacer(Modifier.width(6.dp))
                    }

                    if(alarm.shakeTask) {


                        Icon(
                            painter = painterResource(R.drawable.shake),
                            contentDescription = "Edit",
                            modifier = Modifier
                                .clip(RoundedCornerShape(50.dp))
                                .background(colorResource(id = R.color.secondary))
                                .padding(4.dp),
                            tint = Color.Black
                        )

                        Spacer(Modifier.width(6.dp))
                    }

                    if(alarm.memoryTask) {


                        Icon(
                            painter = painterResource(R.drawable.memory),
                            contentDescription = "Edit",
                            modifier = Modifier
                                .clip(RoundedCornerShape(50.dp))
                                .background(colorResource(id = R.color.secondary))
                                .padding(4.dp),
                            tint = Color.Black
                        )
                        Spacer(Modifier.width(6.dp))
                    }

                    if(alarm.mathTask) {


                        Icon(
                            painter = painterResource(R.drawable.math),
                            contentDescription = "Edit",
                            modifier = Modifier
                                .clip(RoundedCornerShape(50.dp))
                                .background(colorResource(id = R.color.secondary))
                                .padding(4.dp),
                            tint = Color.Black
                        )
                    }
                }


            }
        }
    }
}

//TimePicker is for some reason a Experimental Feature, but since it is very convenient I decided to tolerate that
// Here we can Set our new Alarms with all settings
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Add(
    modifier: Modifier = Modifier,
    navController: NavController,
    viewModel: AlarmViewModel,
    context: Context,
    alarmSoundArr: List<Int>
) {


    var dropDownExpanded by remember { mutableStateOf(false) }


    val currentTime = Calendar.getInstance()

    val timePickerState = rememberTimePickerState(
        initialHour = currentTime.get(Calendar.HOUR_OF_DAY),
        initialMinute = currentTime.get(Calendar.MINUTE),
        is24Hour = true,
    )

    var showPopup by remember { mutableStateOf(false) }

    var alarmState = Alarm(
        audioFile = context.getString(R.string.classic_alarm),
        time = LocalTime.of(
            currentTime.get(Calendar.HOUR_OF_DAY),
            currentTime.get(Calendar.MINUTE),
            0
        )
    )

    var alarm by remember { mutableStateOf(alarmState.copy()) }



    Box(Modifier.fillMaxSize()) {


        Column(
            modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState())
                .padding(bottom = 66.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
                Button(colors = MainButtonColors(), onClick = {
                    navController.navigate(Screens.Home.name)
                }) {
                    Icon(imageVector = Icons.Filled.Clear, contentDescription = "Cancel")
                }
            }

            Spacer(Modifier.height(20.dp))
            TimePicker(
                state = timePickerState,

                )

            OutlinedCard(
                Modifier
                    .fillMaxWidth()
                    .clickable { dropDownExpanded = !dropDownExpanded }
                    .padding(0.dp)) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = alarm.audioFile)
                    Icon(
                        imageVector = Icons.Filled.ArrowDropDown,
                        contentDescription = "Dropdown",
                        tint = colorResource(id = R.color.highlight)
                    )


                }
                DropdownMenu(
                    modifier = Modifier.fillMaxWidth(),
                    expanded = dropDownExpanded,
                    onDismissRequest = { dropDownExpanded = false }
                ) {
                    alarmSoundArr.map { sound ->

                        DropdownMenuItem(
                            text = { Text(text = stringResource(id = sound)) },
                            onClick = {
                                alarm.audioFile = context.getString(sound)
                                dropDownExpanded = false
                            }
                        )


                    }
                }
            }
            Spacer(Modifier.height(16.dp))

            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(0.dp)
            ) {
                Text(
                    text = "Alarm Volume",
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 0.dp),
                    fontSize = 14.sp
                )
                Slider(
                    modifier = Modifier.padding(vertical = 0.dp),
                    value = alarm.volume,
                    onValueChange = { volume ->
                        alarm = alarm.copy(volume = volume)
                    }
                )
            }



            OutlinedCard(
                onClick = { showPopup = true },
                modifier = Modifier
                    .fillMaxWidth()
            ) {


                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically

                ) {

                    Column() {
                        Text(text = "BarcodeTask")
                        Spacer(Modifier.height(6.dp))
                        Text(text = alarm.barcodeName, color = Color.Gray, fontSize = 14.sp)


                    }
                    Switch(
                        checked = alarm.barcodeTask,
                        onCheckedChange = {
                            if (alarm.barcodeName == "No Barcode Selected") {
                                showPopup = true
                            } else {
                                alarm = alarm.copy(barcodeTask = !alarm.barcodeTask)
                            }


                        }
                    )

                }
            }
            Spacer(Modifier.height(16.dp))

            OutlinedCard(
                Modifier
                    .fillMaxWidth()
            ) {

                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically

                ) {

                    Column() {
                        Text(text = "Shake Task")

                    }
                    Switch(
                        checked = alarm.shakeTask,
                        onCheckedChange = {
                            alarm = alarm.copy(shakeTask = !alarm.shakeTask)
                        }
                    )

                }

            }

            Spacer(Modifier.height(16.dp))

            OutlinedCard(
                Modifier
                    .fillMaxWidth()
            ) {

                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically

                ) {

                    Column() {
                        Text(text = "Math Task")

                    }
                    Switch(
                        checked = alarm.mathTask,
                        onCheckedChange = {
                            alarm = alarm.copy(mathTask = !alarm.mathTask)
                        }
                    )

                }

            }

            Spacer(Modifier.height(16.dp))

            OutlinedCard(
                Modifier
                    .fillMaxWidth()
            ) {

                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically

                ) {

                    Column() {
                        Text(text = "Memory Task")

                    }
                    Switch(
                        checked = alarm.memoryTask,
                        onCheckedChange = {
                            alarm = alarm.copy(memoryTask = !alarm.memoryTask)
                        }
                    )

                }

            }


        }


// An extra Composable to show our AddBarcode Pop Up, from there we can navigate to the camera to set new barcodes to use for all other alarms
        AddBarcode(
            showPopup = showPopup,
            onClickOutside = { showPopup = false },
            onAlarmChange = { newAlarm -> alarm = newAlarm },
            viewModel,
            navController,
            alarm
        )

        Row(
            Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .background(color = colorResource(id = R.color.background))
                .padding(horizontal = 20.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            Button(modifier = Modifier.fillMaxWidth(),
                colors = MainButtonColors(),
                contentPadding = PaddingValues(
                    vertical = 12.dp
                ),
                onClick = {
                    alarm.time = LocalTime.of(timePickerState.hour, timePickerState.minute, 0)

                    viewModel.addAlarm(
                        alarm
                    )
                    navController.navigate(Screens.Home.name)
                }) { Text(text = "Add Alarm", fontSize = 14.sp) }

        }
    }
}


// Here we display all barcodes from the barcode database and allow the user to select 1 for this specific alarm
// Also the user can set new barcodes by navigating to the camera
// This is still a problem since when we navigate back from the camera the UI State is reset and all changes to the alarm have to be redone
// For Future progress this should be solved as its a bad feedback for the user
@Composable
fun AddBarcode(
    showPopup: Boolean,
    onClickOutside: () -> Unit,
    onAlarmChange: (Alarm) -> Unit,
    viewModel: AlarmViewModel,
    navController: NavController,
    alarm: Alarm
) {

    val state by viewModel.barcodeUIState.collectAsStateWithLifecycle()


    if (showPopup) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.6f))
                .zIndex(10F)
                .clip(RoundedCornerShape(50.dp)),

            contentAlignment = Alignment.Center
        ) {
            // popup
            Popup(
                alignment = Alignment.Center,
                properties = PopupProperties(
                    excludeFromSystemGesture = true,
                ),
                // to dismiss on click outside
                onDismissRequest = { onClickOutside() }
            ) {
                Column(
                    Modifier
                        .fillMaxWidth(0.9f)
                        .fillMaxHeight(0.8f)
                        .background(color = colorResource(id = R.color.background))
                        .clip(RoundedCornerShape(10.dp)),
                ) {

                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp, vertical = 8.dp)
                    ) {
                        Button(onClick = { onClickOutside() }) {
                            Icon(
                                imageVector = Icons.Filled.Clear,
                                contentDescription = "Cancel"
                            )
                        }
                        Button(onClick = { navController.navigate("Camera/setBarcode") }) {
                            Text(
                                text = "Add new Barcode"
                            )
                        }

                    }

                    Spacer(Modifier.height(20.dp))

                    LazyColumn(
                        Modifier
                            .fillMaxHeight()
                    ) {

                        itemsIndexed(state) { _, barcode ->

                            OutlinedCard(
                                onClick = {
                                    onAlarmChange(
                                        alarm.copy(
                                            barcode = barcode.barcode,
                                            barcodeName = barcode.name
                                        )
                                    )
                                    onClickOutside()

                                },

                                Modifier
                                    .fillMaxWidth()
                                    .padding(4.dp),


                                ) {
                                Row(
                                    Modifier
                                        .fillMaxWidth()
                                        .padding(8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Text(text = barcode.name, fontSize = 24.sp)
                                    Button(
                                        contentPadding = PaddingValues(4.dp),
                                        modifier = Modifier.defaultMinSize(
                                            minWidth = 6.dp,
                                            minHeight = 6.dp
                                        ),
                                        onClick = {

                                            viewModel.deleteBarcode(barcode)

                                        }) {
                                        Icon(
                                            imageVector = Icons.Filled.Delete,
                                            contentDescription = "Delete"
                                        )
                                    }

                                }

                            }

                        }
                    }
                }
            }
        }
    }
}

//Edit an Alarm - basically almost the same as adding a new Alarm
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Edit(
    id: Int,
    modifier: Modifier = Modifier,
    navController: NavController,
    viewModel: AlarmViewModel,
    context: Context,
    alarmSoundArr: List<Int>

) {

    val state by viewModel.alarmUIState.collectAsStateWithLifecycle()

    var showPopup by remember { mutableStateOf(false) }


    val alarmState: Alarm? = state.find { it.id == id }

    if (alarmState == null) {
        Toast.makeText(context, "Cant get alarm with this id Error!", Toast.LENGTH_SHORT).show()
        navController.navigate(Screens.Home.name)
        return
    }

    var alarm by remember { mutableStateOf(alarmState.copy()) }


    var dropDownExpanded by remember { mutableStateOf(false) }

    val timePickerState = rememberTimePickerState(
        initialHour = alarm.time.hour,
        initialMinute = alarm.time.minute,
        is24Hour = true,
    )

    Box(Modifier.fillMaxSize()) {


        Column(
            modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState())
                .padding(bottom = 66.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
                Button(colors = MainButtonColors(), onClick = {
                    navController.navigate(Screens.Home.name)
                }) {
                    Icon(imageVector = Icons.Filled.Clear, contentDescription = "Cancel")
                }
            }

            Spacer(Modifier.height(20.dp))
            TimePicker(
                state = timePickerState,

                )

            OutlinedCard(
                Modifier
                    .fillMaxWidth()
                    .clickable { dropDownExpanded = !dropDownExpanded }
                    .padding(0.dp)) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = alarm.audioFile)
                    Icon(
                        imageVector = Icons.Filled.ArrowDropDown,
                        contentDescription = "Dropdown",
                        tint = colorResource(id = R.color.highlight)
                    )


                }
                DropdownMenu(
                    modifier = Modifier.fillMaxWidth(),
                    expanded = dropDownExpanded,
                    onDismissRequest = { dropDownExpanded = false }
                ) {
                    alarmSoundArr.map { sound ->

                        DropdownMenuItem(
                            text = { Text(text = stringResource(id = sound)) },
                            onClick = {
                                alarm.audioFile = context.getString(sound)
                                dropDownExpanded = false
                            }
                        )


                    }
                }
            }
            Spacer(Modifier.height(16.dp))

            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(0.dp)
            ) {
                Text(
                    text = "Alarm Volume",
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 0.dp),
                    fontSize = 14.sp
                )
                Slider(
                    modifier = Modifier.padding(vertical = 0.dp),
                    value = alarm.volume,
                    onValueChange = { volume ->
                        alarm = alarm.copy(volume = volume)
                    }
                )
            }



            OutlinedCard(
                onClick = { showPopup = true },
                modifier = Modifier
                    .fillMaxWidth()
            ) {


                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically

                ) {

                    Column() {
                        Text(text = "BarcodeTask")
                        Spacer(Modifier.height(6.dp))
                        Text(text = alarm.barcodeName, color = Color.Gray, fontSize = 14.sp)


                    }
                    Switch(
                        checked = alarm.barcodeTask,
                        onCheckedChange = {
                            if (alarm.barcodeName == "No Barcode Selected") {
                                showPopup = true
                            } else {
                                alarm = alarm.copy(barcodeTask = !alarm.barcodeTask)
                            }


                        }
                    )

                }
            }
            Spacer(Modifier.height(16.dp))

            OutlinedCard(
                Modifier
                    .fillMaxWidth()
            ) {

                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically

                ) {

                    Column() {
                        Text(text = "Shake Task")

                    }
                    Switch(
                        checked = alarm.shakeTask,
                        onCheckedChange = {
                            alarm = alarm.copy(shakeTask = !alarm.shakeTask)
                        }
                    )

                }

            }

            Spacer(Modifier.height(16.dp))



            OutlinedCard(
                Modifier
                    .fillMaxWidth()
            ) {

                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically

                ) {

                    Column() {
                        Text(text = "Math Task")

                    }
                    Switch(
                        checked = alarm.mathTask,
                        onCheckedChange = {
                            alarm = alarm.copy(mathTask = !alarm.mathTask)
                        }
                    )

                }

            }

            Spacer(Modifier.height(16.dp))

            OutlinedCard(
                Modifier
                    .fillMaxWidth()
            ) {

                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically

                ) {

                    Column() {
                        Text(text = "Memory Task")

                    }
                    Switch(
                        checked = alarm.memoryTask,
                        onCheckedChange = {
                            alarm = alarm.copy(memoryTask = !alarm.memoryTask)
                        }
                    )

                }

            }


        }

        AddBarcode(
            showPopup = showPopup,
            onClickOutside = { showPopup = false },
            onAlarmChange = { newAlarm -> alarm = newAlarm },
            viewModel,
            navController,
            alarm
        )

        Row(
            Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .background(color = colorResource(id = R.color.background))
                .padding(horizontal = 20.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            Button(modifier = Modifier.fillMaxWidth(),
                colors = MainButtonColors(),
                contentPadding = PaddingValues(
                    vertical = 12.dp
                ), // Custom inner padding
                onClick = {
                    alarm.time = LocalTime.of(timePickerState.hour, timePickerState.minute, 0)

                    viewModel.updateAlarm(
                        alarm

                    )
//                    If the alarm was activated initially we have to cancel the old alarm and re-set the edited one to make sure
                    if(alarmState.activated){
                        cancelAlarm(context, alarmState)
                        scheduleExactAlarm(context, alarm)
                    }
                    navController.navigate(Screens.Home.name)
                }) { Text(text = "Save", fontSize = 14.sp) }

        }
    }


}