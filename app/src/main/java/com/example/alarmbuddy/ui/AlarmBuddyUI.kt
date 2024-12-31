package com.example.alarmbuddy.ui

import android.app.AlarmManager
import android.content.Context
import android.util.Log
import android.widget.Toast
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
import com.example.alarmbuddy.AlarmApplication
import com.example.alarmbuddy.R
import com.example.alarmbuddy.data.Alarm
import com.example.alarmbuddy.ui.theme.Typography
import java.time.LocalTime
import java.util.Calendar


enum class Screens {
    Home, Add, Edit, Ringing, BarcodeTask, ShakeTask, Camera, CameraSetup
}


@Composable
fun AlarmBuddyApp(
    modifier: Modifier = Modifier,
    navigateTo: String,
    alarmId: Int
) {

    //    Get Application context
    val context = LocalContext.current.applicationContext as AlarmApplication


    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    val nextAlarm = alarmManager.nextAlarmClock
    Log.d("AlarmManager", "Next alarm time: ${nextAlarm?.triggerTime}")


//  Get viewModel and Factory stuff - with this we dont need a extra file I think ...
    val viewModel: AlarmViewModel = viewModel(
        factory = context.alarmViewModelFactory
    )

    val navController = rememberNavController()


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

//            In Scaffold we have our screens, in ToDo Screen we put a Column with every todo that was created and a Add button
            composable(
                Screens.Home.name,
            ) {
                Box(modifier = Modifier.padding(innerPadding)) {
                    Home(
                        modifier,
//                    AlarmViewModel,
                        navController,
                        viewModel,
                        context
                    )
                }
            }

//            In add we just make a form to add text and create a new note
            composable(Screens.Add.name) {
                Box(modifier = Modifier.padding(innerPadding)) {
                    Add(
                        modifier,
//                    AlarmViewModel,
                        navController,
                        viewModel,
                        context
                    )

                }
            }

            composable("Edit/{id}") { navBackStackEntry ->

                val id = navBackStackEntry.arguments?.getString("id")?.toInt() ?: -1
                Box(modifier = Modifier.padding(innerPadding)) {
                    Edit(
                        id,
                        modifier,
//                    AlarmViewModel,
                        navController,
                        viewModel,
                        context
                    )

                }
            }


            composable(Screens.Ringing.name) {
                Box(modifier = Modifier.padding(innerPadding)) {
                    Ringing(
                        alarmId,
//                        modifier,
////                    AlarmViewModel,
//                        navController,
                        viewModel,
                        context
                    )

                }
            }

//            composable(Screens.Camera.name) {
//                Box(modifier = Modifier.padding(innerPadding)) {
//                    Camera(
//                    )
//
//                }
//            }

            composable("CameraSetup/{Intent}") { navBackStackEntry ->
                val Intent = navBackStackEntry.arguments?.getString("Intent") ?: "setBarcode"

                CameraSetup(
                    navController,
                    context,
                    Intent

                )
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

@Composable
fun Home(
    modifier: Modifier = Modifier,
    navController: NavController,
//  Viewmodel,
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

//                If Completed grey and cross out header text
                Row(horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(
                        style = Typography.headlineMedium,
                        text = "${alarm.time.hour}:${alarm.time.minute}",
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
//                    Some logic fuckery here, the alarmstate is changed before it reaches here even
//                    So since its allready changed we have to use !alarm - dirty work around but it works after all
                        if (!alarm.activated) {
//                        Nolt sure if needed
                            checkAndRequestExactAlarmPermission(context)

                            scheduleExactAlarm(context, alarm)
                            Toast.makeText(
                                context,
                                "Alarm with id: ${alarm.id} Set",
                                Toast.LENGTH_SHORT
                            ).show()

                            viewModel.updateAlarm(alarm.copy(activated = true))


                        } else {
                            cancelAlarm(context, alarm)
                            Toast.makeText(
                                context,
                                "Alarm with id: ${alarm.id} Canceled",
                                Toast.LENGTH_SHORT
                            ).show()
                            viewModel.updateAlarm(alarm.copy(activated = false))


                        }
                    }
                )

                Text(text = alarm.audioFile)
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Add(
    modifier: Modifier = Modifier,
    navController: NavController,
    viewModel: AlarmViewModel,
    context: Context
) {

    var dropDownExpanded by remember { mutableStateOf(false) }

    var volume by remember { mutableStateOf(0.5f) }

    var audiofile by remember { mutableStateOf(context.getString(R.string.classic_alarm)) }


    val currentTime = Calendar.getInstance()

    val timePickerState = rememberTimePickerState(
        initialHour = currentTime.get(Calendar.HOUR_OF_DAY),
        initialMinute = currentTime.get(Calendar.MINUTE),
        is24Hour = true,
    )


    Column(
        modifier
            .fillMaxWidth()
            .padding(horizontal = 2.dp, vertical = 2.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(horizontalArrangement = Arrangement.SpaceBetween) {
            Button(onClick = {
                navController.navigate(Screens.Home.name)
            }) { Text(text = "Cancel") }
            Button(onClick = {
                viewModel.addAlarm(
                    Alarm(
                        audioFile = audiofile,
                        barcode = "12345",
                        time = LocalTime.of(timePickerState.hour, timePickerState.minute, 0),
                        volume = volume,

                        )
                )
                navController.navigate(Screens.Home.name)
            }) { Text(text = "Save") }
        }
        TimePicker(
            state = timePickerState,
        )


        Box(
            modifier
                .border(border = BorderStroke(2.dp, Color.Magenta))
                .padding(4.dp)
                .fillMaxWidth()
                .clickable { dropDownExpanded = !dropDownExpanded }) {
            Text(text = audiofile)

            DropdownMenu(
                expanded = dropDownExpanded,
                onDismissRequest = { dropDownExpanded = false }
            ) {
                DropdownMenuItem(
                    text = { Text("Classic Alarm") },
                    onClick = {
                        audiofile = context.getString(R.string.classic_alarm)
                        dropDownExpanded = false
                    }
                )
                DropdownMenuItem(
                    text = { Text("Granular Alarm") },
                    onClick = {
                        audiofile = context.getString(R.string.granular_alarm)
                        dropDownExpanded = false
                    }
                )
                DropdownMenuItem(
                    text = { Text("Pain Alarm") },
                    onClick = {
                        audiofile = context.getString(R.string.pain_alarm)
                        dropDownExpanded = false
                    }
                )
            }
        }

        Slider(
            value = volume,
            onValueChange = { volume = it }
        )
    }
}


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
        // full screen background
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.6f))
                .zIndex(10F),

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
                        .background(Color.Black)
                        .clip(RoundedCornerShape(4.dp)),
                ) {

                    Row(horizontalArrangement = Arrangement.SpaceBetween) {
                        Button(onClick = { onClickOutside() }) { Text(text = "Cancel") }
                        Button(onClick = { navController.navigate("CameraSetup/setBarcode") }) {
                            Text(
                                text = "Set new Barcode"
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
                                   onAlarmChange(alarm.copy(barcode = barcode.barcode, barcodeName = barcode.name))
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
//                                    If we delete the Barcode then everything else linked with it might be broken
//                                    Except if we never use the Barcodedb to actually confirm a barcode scanned its fine
                                    Button(
                                        contentPadding = PaddingValues(4.dp),
                                        modifier = Modifier.defaultMinSize(minWidth = 6.dp, minHeight = 6.dp),
                                        onClick = {

                                            viewModel.deleteBarcode(barcode)

                                        }) {
                                        Icon(imageVector = Icons.Filled.Delete, contentDescription = "Delete")
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Edit(
    id: Int,
    modifier: Modifier = Modifier,
    navController: NavController,
    viewModel: AlarmViewModel,
    context: Context
) {

//    On edit old alarm has to be unset if it was activated before yes i still did not do that yet ;))=(9080

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


    Column(
        modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 2.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(horizontalArrangement = Arrangement.SpaceBetween) {
            Button(onClick = {
                navController.navigate(Screens.Home.name)
            }) { Text(text = "Cancel") }
            Button(onClick = {
                alarm.time = LocalTime.of(timePickerState.hour, timePickerState.minute, 0)

                viewModel.updateAlarm(
                    alarm

                )
                navController.navigate(Screens.Home.name)
            }) { Text(text = "Save") }
        }
        TimePicker(
            state = timePickerState,
        )
        Box(
            modifier
                .border(border = BorderStroke(2.dp, Color.Magenta))
                .padding(4.dp)
                .fillMaxWidth()
                .clickable { dropDownExpanded = !dropDownExpanded }) {
            Text(text = alarm.audioFile)

            DropdownMenu(
                expanded = dropDownExpanded,
                onDismissRequest = { dropDownExpanded = false }
            ) {
                DropdownMenuItem(
                    text = { Text("Classic Alarm") },
                    onClick = {
                        alarm.audioFile = context.getString(R.string.classic_alarm)
                        dropDownExpanded = false
                    }
                )
                DropdownMenuItem(
                    text = { Text("Granular Alarm") },
                    onClick = {
                        alarm.audioFile = context.getString(R.string.granular_alarm)
                        dropDownExpanded = false
                    }
                )
                DropdownMenuItem(
                    text = { Text("Pain Alarm") },
                    onClick = {
                        alarm.audioFile = context.getString(R.string.pain_alarm)
                        dropDownExpanded = false
                    }
                )
            }
        }

        Slider(
            value = alarm.volume,
            onValueChange = { volume ->
                alarm = alarm.copy(volume = volume)
            }
        )



        OutlinedCard(
            onClick = { showPopup = true },
            modifier = Modifier
                .fillMaxWidth()
//                .padding(8.dp)
//                label = { Text("BarcodeTask")}
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
                    Text(text = alarm.barcodeName, color= Color.Gray, fontSize = 14.sp)


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
        Spacer(Modifier.height(8.dp))

        OutlinedCard(
            Modifier
                .padding(8.dp)
        ) {

            Text(text = "Shake Task")
            Switch(
                checked = alarm.shakeTask,
                onCheckedChange = {
                    alarm = alarm.copy(shakeTask = !alarm.shakeTask)
                }
            )
        }




        OutlinedCard(
            modifier = Modifier
                .padding(8.dp)
        ) {

            Text(text = "Math Task")
            Switch(
                checked = alarm.mathTask,
                onCheckedChange = {
                    alarm = alarm.copy(mathTask = !alarm.mathTask)
                }
            )
        }

        Spacer(Modifier.height(8.dp))

        OutlinedCard(
            Modifier
                .padding(8.dp)
        ) {

            Text(text = "Memory Task")
            Switch(
                checked = alarm.memoryTask,
                onCheckedChange = {
                    alarm = alarm.copy(memoryTask = !alarm.memoryTask)
                }
            )
        }


    }

    AddBarcode(
        showPopup = showPopup,
        onClickOutside = { showPopup = false },
        onAlarmChange = {newAlarm -> alarm = newAlarm},
        viewModel,
        navController,
        alarm
    )


}