package com.washington.chattertrace.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.washington.chattertrace.DataLogic.DataManager
import com.washington.chattertrace.DataLogic.NotificationHelper
import com.washington.chattertrace.MainActivity
import com.washington.chattertrace.R
import com.washington.chattertrace.RecordingLogic.RecordingManager
import com.washington.chattertrace.utils.Utils
import kotlinx.coroutines.delay
import java.util.Timer
import java.util.TimerTask

var timer: Timer? = null

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(recordingManager: RecordingManager?, dataManager: DataManager?) {
    var isButtonClicked by rememberSaveable { mutableStateOf(false) }

    var elapsedTime by rememberSaveable { mutableStateOf(0L) }

    val scope = rememberCoroutineScope()

    val preceding_time = 30 // Record 30s in advance
    val preceding_mode = true
    val record_time_after_user_tap = 60 // After user tap, record 60s more so the total file would be 90s long

//    LaunchedEffect(isButtonClicked) {
//        if (isButtonClicked) {
//            while (true) {
//                delay(1000L) // Delay for 1 second
//                elapsedTime += 1L
//            }
//        }
//    }

    fun startTimer() {
        timer!!.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                // This code will run every 1000 milliseconds (1 second)
                elapsedTime += 1L
            }
        }, 0L, 1000L) // Initial delay of 0 milliseconds, repeat every 1000 milliseconds
    }

    fun stopTimer() {
        if (timer != null) {
            timer!!.cancel()
            timer!!.purge()
            timer = null
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .wrapContentWidth(Alignment.CenterHorizontally)
    ) {
        CenterAlignedTopAppBar(
            // header text
            title = {
                Text(
                    text = "Session Recording",
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp,
                    color = Color.Black,
                    textAlign = TextAlign.Center
                )
            },
            modifier = Modifier.padding(top = 16.dp),
            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(Color.White)
        )

        Text(
            text = "Select the START button to begin a new session recording.",
            fontWeight = FontWeight.Normal,
            fontSize = 16.sp,
            color = colorResource(id = R.color.light_text),
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 47.dp, end = 47.dp),
            lineHeight = 24.sp,
            textAlign = TextAlign.Center
        )

        Text(
            text = "Your PID is: " + Utils.PID,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            color = colorResource(id = R.color.light_text),
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 40.dp),
            lineHeight = 24.sp,
            textAlign = TextAlign.Center
        )

        Text(
            text = "Session Timer (minutes)",
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            color = colorResource(id = R.color.light_text),
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 40.dp),
            lineHeight = 24.sp,
            textAlign = TextAlign.Center
        )

        Text(
            text = "Optionally select a recording timer. Recordings will automatically stop at 180 minutes.",
            fontWeight = FontWeight.Normal,
            fontSize = 16.sp,
            color = colorResource(id = R.color.light_text),
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 11.dp, start = 24.dp, end = 24.dp),
            lineHeight = 24.sp,
            textAlign = TextAlign.Center
        )

        // time select radio buttons
        val radioOptions = listOf(15, 30, 60, 90, 120)
        var selectedOptionIndex by rememberSaveable { mutableStateOf(0) }

        Row(
            Modifier
                .fillMaxWidth()
                .padding(top = 30.dp, start = 9.dp, end = 9.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            radioOptions.forEachIndexed { index, text ->
                OutlinedButton(
                    onClick = { selectedOptionIndex = index },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (index == selectedOptionIndex) colorResource(id = R.color.light_surface) else Color.White,
                        contentColor = colorResource(id = R.color.primary),
                    ),
                    shape = RoundedCornerShape(100.dp),
                    modifier = Modifier.padding(start = 2.dp, end = 2.dp)
                ) {
                    Text(
                        text = "$text",
                        fontSize = 10.sp
                    )
                }

            }
        }

        Text(
            text = "Recording Time",
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            color = colorResource(id = R.color.light_text),
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 40.dp),
            lineHeight = 24.sp,
            textAlign = TextAlign.Center
        )

        Row(
            Modifier
                .fillMaxWidth()
                .padding(top = 30.dp, start = 9.dp, end = 9.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = formatTime(elapsedTime),
                fontSize = 70.sp,
                color = colorResource(id = R.color.primary),
                modifier = Modifier
                    .background(
                        color = colorResource(id = R.color.light_surface),
                        shape = RoundedCornerShape(8.dp),
                    )
                    .padding(16.dp, 0.dp)
            )
        }

        if (!isButtonClicked) {
            Button(
                onClick = {
                    recordingManager?.setPrecedingTime(preceding_time)
                    if (preceding_time > 0 && preceding_mode) {
                        recordingManager?.StartRecordingSilently(
                            dataManager?.getRecordingNameOfTimeWithPrefix(
                                "preceding"
                            ), preceding_time
                        )
                    }
                    if (timer == null) {
                        timer = Timer()
                        startTimer()
                    }
                    isButtonClicked = !isButtonClicked
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = colorResource(id = R.color.primary),
                    contentColor = Color.White,
                ),
                shape = RoundedCornerShape(100.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 30.dp, start = 30.dp, end = 30.dp),
            ) {
                Text(
                    text = "Start Study Session",
                    fontSize = 20.sp
                )
            }
        } else {
            Button(
                onClick = {
                    if (recordingManager?.isRecording() == true) {
                        recordingManager.StopRecordingSilently()
                        elapsedTime = 0L
                        stopTimer()
                    }
//                    if (recordingManager?.isRecording() == true) {
//                        recordingManager?.StopRecording();
//                        elapsedTime = 0L
//                    }
                    isButtonClicked = !isButtonClicked
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = colorResource(id = R.color.primary),

                ),
                border = BorderStroke(1.dp, colorResource(id = R.color.outline)),
                shape = RoundedCornerShape(100.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 30.dp, start = 30.dp, end = 30.dp),
            ) {
                Text(
                    text = "End Now",
                    fontSize = 20.sp
                )
            }
        }

//        Button(onClick = {
//            recordingManager?.StartRecording(dataManager?.getRecordingNameOfTimeWithPrefix(""), record_time_after_user_tap)
//        }) {
//            Text(
//                text = "Mimic tap on notification",
//                fontSize = 20.sp
//            )
//        }
    }
}

private fun formatTime(timeInSeconds: Long): String {
    val hours = (timeInSeconds / 3600).toString().padStart(2, '0')
    val minutes = (timeInSeconds / 60).toString().padStart(2, '0')
    val seconds = (timeInSeconds % 60).toString().padStart(2, '0')
    return "$hours:$minutes:$seconds"
}
