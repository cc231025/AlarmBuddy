package com.example.alarmbuddy.ui

import android.app.AlarmManager
import android.app.Application
import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.Crossfade
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
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
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.materialIcon
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.alarmbuddy.AlarmApplication
import com.example.alarmbuddy.data.Alarm
import com.example.alarmbuddy.ui.theme.PurpleGrey40
import com.example.alarmbuddy.ui.theme.Typography
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Date
import java.util.Locale


enum class Screens {
    Home, Add, Edit, Ringing, BarcodeTask, ShakeTask,
}

@Composable
fun AlarmBuddyApp(
    modifier: Modifier = Modifier,
    navigateTo: String,
    alarmId : Int
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
                        viewModel
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
                AlarmItem(alarm, viewModel, context)
            }
        }


    }


}


@Composable
fun AlarmItem(
    alarm: Alarm,
    viewModel: AlarmViewModel,
    context:Context

) {



    OutlinedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {

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
                            if(alarm.activated){
                            cancelAlarm(context, alarm)}

                            viewModel.deleteAlarm(alarm)
                        }) {
                        Icon(imageVector = Icons.Filled.Delete, contentDescription = "Delete")
                    }
                    Button(
                        contentPadding = PaddingValues(4.dp),
                        modifier = Modifier.defaultMinSize(minWidth = 6.dp, minHeight = 6.dp),
                        onClick = {
                        }) {
                        Icon(imageVector = Icons.Filled.Edit, contentDescription = "Edit")
                    }


                }


            }


            Switch(
                checked = alarm.activated,
                onCheckedChange = {
                    viewModel.updateAlarm(alarm.copy(activated = !alarm.activated))
                    if(alarm.activated) {
//                        Nolt sure if needed
                        checkAndRequestExactAlarmPermission(context)

                        scheduleExactAlarm(context, alarm)
                        Toast.makeText(context, "Alarm with id: ${alarm.id} Set", Toast.LENGTH_SHORT).show()

//                        viewModel.updateAlarm(alarm.copy(activated = true))


                    }else {
                        cancelAlarm(context, alarm)
                        Toast.makeText(context, "Alarm with id: ${alarm.id} Canceled", Toast.LENGTH_SHORT).show()
//                        viewModel.updateAlarm(alarm.copy(activated = false))


                    }
                }
            )
        }
    }
}




@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Add(
    modifier: Modifier = Modifier,
    navController: NavController,
    viewModel: AlarmViewModel
) {

//    val date = Date() // Current date and time
//    val timeFormater = SimpleDateFormat("HH:mm", Locale.getDefault())
//    val currentTime = timeFormater.format(date) // Format date to extract time
//
//    var time by remember { mutableStateOf(currentTime) }
    var dropDownExpanded by remember { mutableStateOf(false) }

    var volume by remember { mutableStateOf(0.5f) }


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
            }) { Text(text = "Save") }
            Button(onClick = {
                viewModel.addAlarm(
                    Alarm(
                        audioFile = "audio",
                        barcode = "12345",
                        time = LocalTime.of(timePickerState.hour, timePickerState.minute, 0)
                    )
                )
                navController.navigate(Screens.Home.name)
            }) { Text(text = "Save") }
        }
        TimePicker(
            state = timePickerState,
        )
        Text(text = "Wake-Up Sound")
        Box(
            modifier
                .border(border = BorderStroke(2.dp, Color.Magenta))
                .padding(4.dp)
                .fillMaxWidth()
                .clickable { dropDownExpanded = !dropDownExpanded }) {
            Text(text = "Audio")

            DropdownMenu(
                expanded = dropDownExpanded,
                onDismissRequest = { dropDownExpanded = false }
            ) {
                DropdownMenuItem(
                    text = { Text("Option 1") },
                    onClick = { /* Do something... */ }
                )
                DropdownMenuItem(
                    text = { Text("Option 2") },
                    onClick = { /* Do something... */ }
                )
            }
        }

        Slider(
            value = volume,
            onValueChange = { volume = it }
        )


    }
}