package com.example.alarmbuddy.ui

import androidx.camera.core.processing.Operation
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
//import chaintech.network.cmpshakedetection.rememberShakeDetector
import com.example.alarmbuddy.R
import kotlinx.coroutines.delay
import kotlin.random.Random


@Composable
fun ShakeTask(
    onShakeComplete: () -> Unit,


    ) {


//    val shakeDetector = rememberShakeDetector()
//    var count by remember { mutableStateOf(0f) }
//    var barColor by remember { mutableStateOf(Color.Red) }
//
//    LaunchedEffect(Unit) {
//        shakeDetector.start()
//    }
//
//    shakeDetector.onShake {
//        count++
//        if (count >= 15) barColor = Color.Green
//        if (count >= 5) {
//            shakeDetector.stop()
//            onShakeComplete()
//        }
//    }
//
//    Column(
//        Modifier
//            .fillMaxSize()
//            .padding(30.dp),
//        verticalArrangement = Arrangement.Center,
//        horizontalAlignment = Alignment.CenterHorizontally
//    ) {
//
//        Text(textAlign = TextAlign.Center, text = "Shake your Phone 20 Times to stop the Alarm!")
//        Spacer(Modifier.height(50.dp))
//
//        LinearProgressIndicator(
//            progress = { count / 20f },
//            modifier = Modifier
//                .fillMaxWidth()
//                .height(30.dp)
//                .clip(RoundedCornerShape(20.dp)),
//
//            color = barColor,
//            trackColor = Color.White
//
//        )
//
//        Spacer(Modifier.height(50.dp))
//
//        Text(textAlign = TextAlign.Center, text = "Shakes: ${count.toInt()}")
//    }

}


fun calc(a: Int, b: Int, operator: String?): Int {
    var result = 0

    when (operator) {
        "+" -> result = a + b;
        "-" -> result = a - b;
        "*" -> result = a * b
    }
    return result
}


val generateProblem: () -> Triple<String, Array<Int>, Int> = {

    val operators = HashMap<Int, String>()
    operators[0] = "+"
    operators[1] = "-"
    operators[2] = "*"

//        2 operators
//        1st +-* 2nd +-
//        lambda functions to genrate answers

    val variables = arrayOf(Random.nextInt(1, 10), Random.nextInt(1, 20), Random.nextInt(1, 100))
    val operator1 = Random.nextInt(3)
    val operator2 = Random.nextInt(2)

//        2 operators
//        1st +-* 2nd +-
//        lambda functions to genrate answers

    val problemString =
        "${variables[0]} ${operators[operator1]} ${variables[1]} ${operators[operator2]} ${variables[2]}"

    val result = calc(variables[0], variables[1], operators[operator1])
    val finalResult = calc(result, variables[2], operators[operator2])

    val resultList = arrayOf(
        finalResult,
        (finalResult + Random.nextInt(1, 25)),
        (finalResult - Random.nextInt(1, 25))
    )
    resultList.shuffle()

    Triple(problemString, resultList, finalResult)

}


@Composable
fun MathTask(
    onMathComplete: () -> Unit,


    ) {


    var completedTasks by remember { mutableIntStateOf(0) }


    if (completedTasks >= 5) onMathComplete()

    var key by remember { mutableIntStateOf(0) }
    val (problemString, problemList, result) = remember(key) { generateProblem() }

    Column(
        Modifier
            .fillMaxSize()
            .padding(30.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text(textAlign = TextAlign.Center, text = "Get 5 Calculations right to stop the alarm")
        Spacer(Modifier.height(30.dp))
        Text(
            textAlign = TextAlign.Center,
            text = "${5 - completedTasks} problems left",
            fontSize = 26.sp,
            color = Color.Red
        )

        Spacer(Modifier.height(50.dp))
        Box(
            Modifier
                .background(Color.Gray)
                .border(width = 4.dp, color = Color.White)
                .padding(16.dp)
                .clip(RoundedCornerShape(4.dp)),

            ) {
            Text(
                textAlign = TextAlign.Center,
                text = problemString,
                color = Color.Black,
                fontSize = 30.sp
            )


        }
        Spacer(Modifier.height(30.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            problemList.map { problem ->
                Button(onClick = {
                    if (problem == result) {
                        completedTasks++
                        key++
                    } else if (completedTasks > 0) {
                        completedTasks--
                        key++
                    } else key++


                }) { Text(text = problem.toString(), fontSize = 20.sp) }
            }
        }


    }


}

data class MemoryTile(var identifier: Int) {
    var enabled: Boolean by mutableStateOf(false)
    var finished: Boolean by mutableStateOf(false)
    var standardColor: Color = Color.Blue
}



@Composable
fun MemoryTask(

    onMemoryComplete: () -> Unit,

) {

    val cMap = HashMap<Int, Color>()
    cMap[0] = Color.Green
    cMap[1] = Color.Red
    cMap[2] = Color.Yellow
    cMap[3] = Color.White
    cMap[4] = Color.Blue
    cMap[5] = Color.Cyan
    cMap[6] = Color.Magenta
    cMap[7] = Color(0xFFFF5722)
    cMap[8] = Color(0xFF5A368A)
    cMap[9] = Color(0xFF006C52)


    val arr = (0..9).toList().toTypedArray()
    val memoryArr = arr + arr
    memoryArr.shuffle()

    var delayRender by remember { mutableStateOf(false) }

    var tileArray = remember {
        memoryArr.map { identifier -> MemoryTile(identifier) }.toMutableStateList()
    }
    var lastTileClicked by remember { mutableStateOf(-1) }
    var currentTile by remember { mutableStateOf(-1) }

    var finishedTiles by remember { mutableStateOf(0) }

    fun compareTiles(tile :Int){
        if (lastTileClicked == -1) lastTileClicked = tile
        else if (tileArray[lastTileClicked].identifier == tileArray[tile].identifier  ){

            tileArray[lastTileClicked].finished = true
            tileArray[tile].finished = true

            lastTileClicked = -1

            if(finishedTiles >= 9){
                onMemoryComplete()
            }else finishedTiles++

        }else{
            currentTile = tile
            delayRender = true
//            tileArray[lastTileClicked].enabled = false
//            tileArray[tile].enabled = false
//            lastTileClicked = -1
        }
    }

//    fun resetTiles(){
//        tileArray[lastTileClicked].enabled = false
//        tileArray[currentTile].enabled = false
//        lastTileClicked = -1
//    }

    LaunchedEffect(delayRender) {
        if(delayRender){
            delay(1000)

            tileArray[lastTileClicked].enabled = false
            tileArray[currentTile].enabled = false
            lastTileClicked = -1

            delayRender = false
        }
    }


    Column(
        Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .padding(30.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(4),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(tileArray.size) { index ->
                Box(
                    Modifier
                        .background(
                            if (tileArray[index].enabled) cMap[tileArray[index].identifier]!!
                            else {
                                Color.Gray
                            }
                        )
                        .height(74.dp)
                        .clip(RoundedCornerShape(100.dp))
                        .clickable {
                            if (!tileArray[index].finished && index != lastTileClicked && !delayRender) {
                                if (!tileArray[index].enabled) {
                                    tileArray[index].enabled = !tileArray[index].enabled
                                }
                                compareTiles(index)
                            }
                        },
                )
            }
        }
        Spacer(Modifier.height(30.dp))
        Text(text = "Finish the Memory to stop the Alarm, you got this!", textAlign = TextAlign.Center)

    }

}


