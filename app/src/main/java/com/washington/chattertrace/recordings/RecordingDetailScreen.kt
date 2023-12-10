package com.washington.chattertrace.recordings

import android.content.SharedPreferences
import android.net.Uri
import android.os.Environment
import android.util.Log
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.preference.PreferenceManager
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.upstream.DataSpec
import com.google.android.exoplayer2.upstream.FileDataSource
import com.google.android.exoplayer2.util.MimeTypes
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.washington.chattertrace.R
import com.washington.chattertrace.data.Recording
import com.washington.chattertrace.data.recordingMap
import com.washington.chattertrace.utils.HttpPostTask
import com.washington.chattertrace.utils.Utils
import kotlinx.coroutines.delay
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.time.Duration.Companion.seconds

var DIR_PATH = Environment.getExternalStorageDirectory().absolutePath +
        "/Android/data/com.washington.chattertrace/files/Documents/Bid4Connection/"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordingDetailScreen(dateString: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        CenterAlignedTopAppBar(
            // header text
            title = {
                Text(
                    text = "${formatDateString(dateString)} Recordings",
                    fontWeight = FontWeight.Normal,
                    fontSize = 24.sp,
                    color = Color.Black,
                    textAlign = TextAlign.Center
                )
            },
            navigationIcon = {
                IconButton(onClick = { /*navController.popBackStack()*/ }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back button")
                }
            },
            actions = {
                IconButton(onClick = {
                    val formatter = DateTimeFormatter.ofPattern("yyyyMMdd")
                    val localDate = LocalDate.parse(dateString, formatter)

                    recordingMap[localDate]?.forEach { recording ->
                        Log.i("NETWORK", "DETAIL UPLOAD ALL")
                        HttpPostTask.upload("http://is-bids.ischool.uw.edu:3000/upload_files", recording.audio)
                        recording.isUploaded = true
                    }
                }) {
                    Text(
                        text = "Upload All",
                        fontWeight = FontWeight.Light,
                        fontSize = 16.sp,
                        color = colorResource(id = R.color.primary),
                    )
                }
            },
            modifier = Modifier.padding(top = 16.dp),
            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(Color.White)
        )

        RecordingList(dateString)

    }
}

/*
 * Recording Rows
 */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordingList(dateString: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Column {
            val formatter = DateTimeFormatter.ofPattern("yyyyMMdd")
            val localDate = LocalDate.parse(dateString, formatter)


            recordingMap[localDate]?.forEach { recording ->
                var filePath = DIR_PATH + recording.audio.name
                RecordingRow(recording)
            }
            Questionnaire()
        }
    }
}


@ExperimentalMaterial3Api
@Composable
fun RecordingRow(recording: Recording) {
    var audio = recording.audio
    val isUploaded = remember { mutableStateOf(recording.isUploaded) }
    // State to track the current position of the audio recording
    var currentPosition by rememberSaveable { mutableStateOf(0L) }

    // State to track whether the recording is currently playing
    var isPlaying by rememberSaveable { mutableStateOf(false) }

    val context = LocalContext.current

    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            val fileDataSourceFactory = FileDataSource.Factory()
            val dataSpec = DataSpec(Uri.fromFile(audio))
            val mediaItem = MediaItem.Builder()
                .setUri(dataSpec.uri)
                .setMediaId(audio.name)
                .setMimeType(MimeTypes.AUDIO_MPEG)
                .build()

            setMediaItem(mediaItem)

            prepare()
        }
    }

    val duration = exoPlayer.duration.coerceAtLeast(0L)

    exoPlayer.addListener(object : Player.Listener {
        override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
            try{
                when (playbackState) {
                    ExoPlayer.STATE_BUFFERING -> {}
                    ExoPlayer.STATE_ENDED -> {
                        /// reset player
                        exoPlayer.pause()
                        exoPlayer.seekTo(0)
                        isPlaying = false
                        currentPosition = 0L
                    }
                    ExoPlayer.STATE_IDLE -> {}
                    ExoPlayer.STATE_READY -> {}
                    else -> {}
                }
            }catch (error: ExoPlaybackException){
            }
        }

        fun onPlayWhenReadyCommitted() {}
        fun onPlayerError(error: ExoPlaybackException?) {}
    })

    if (isPlaying) {
        LaunchedEffect(Unit) {
            while(true) {
                currentPosition = exoPlayer.currentPosition
                delay(1.seconds / 30)
            }
        }
    }

    ListItem(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 26.dp, end = 26.dp, top = 15.dp)
            .border(
                width = 1.dp,
                color = colorResource(id = R.color.primary),
                shape = RoundedCornerShape(6.dp)
            ),
        headlineText = {
            Text(
                text = "Bid Name",
                fontSize = 16.sp,
            )
        },
        supportingText = {
            Slider(
                value = currentPosition.toFloat(),
                onValueChange = { newTime: Float ->
                    exoPlayer.seekTo(newTime.toLong())
                    currentPosition = newTime.toLong() },
                valueRange = 0f..duration.toFloat(),
                colors = SliderDefaults.colors(
                    thumbColor = colorResource(id = R.color.primary), // Customize thumb color
                    activeTrackColor = colorResource(id = R.color.primary), // Customize active track color
                    inactiveTrackColor = Color.LightGray // Customize inactive track color
                ),
                modifier = Modifier.height(24.dp)
            )
        },
        leadingContent = {
            IconButton(
                onClick = {
                    isPlaying = if (exoPlayer.isPlaying) {
                        // pause the video
                        exoPlayer.pause()
                        false
                    } else {
                        // play the video
                        // it's already paused
                        exoPlayer.play()
                        true
                    }
                },
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    painter = if (!isPlaying) painterResource(id = R.drawable.ic_play) else painterResource(id = R.drawable.ic_pause),
                    contentDescription = "play/pause",
                    tint = Color.Unspecified
                )
            }
        },
        trailingContent = {
                IconButton(
                    onClick = {
                        if(!isUploaded.value){
                            Log.i("NETWORK", "CLICKED")
                            HttpPostTask.upload("http://is-bids.ischool.uw.edu:3000/upload_files?PID=" + Utils.getUniqueID(context), audio)

                            recording.isUploaded = true
                            isUploaded.value = true

                            val preferences = PreferenceManager.getDefaultSharedPreferences(context)
                            val editor: SharedPreferences.Editor = preferences.edit()

                            val current_buffer: String? = preferences.getString("uploadedList", null)
                            var uploadedList = listOf(recording.id)
                            val gson = Gson()
                            if(current_buffer != null){
                                val type = object : TypeToken<List<String?>?>() {}.type
                                uploadedList = gson.fromJson<List<String?>?>(current_buffer, type) as List<String>
                                uploadedList = uploadedList.plus(recording.id)
                            }

                            val buffer_json = gson.toJson(uploadedList)

                            Log.d("SCREENWAKE", "Saved uploaded recordingMap: " + buffer_json)
                            editor.putString("uploadedList", buffer_json)
                            editor.apply()
                        }
                    }
                ) {
                    Icon(
                        painter = painterResource(id = if (isUploaded.value) R.drawable.ic_uploaded_checkmark else R.drawable.ic_upload),
                        contentDescription = "Localized description",
                        tint = Color.Unspecified,
                        modifier = Modifier.padding(start = 16.dp),
                    )
                }

            Icon(
                painter = painterResource(id = R.drawable.ic_arrow_drop_down_24),
                contentDescription = "Localized description",
                modifier = Modifier.padding(start = 54.dp)
            )
        }
    )
}

// TODO: turn this function into an activity instead
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Questionnaire() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(14.dp, 6.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(colorResource(id = R.color.light_surface))

        ) {
            var isChecked by rememberSaveable { mutableStateOf(false) }
            var sliderValue by rememberSaveable { mutableStateOf(3f) }
            var textValue by rememberSaveable { mutableStateOf("") }

            Text(
                text = "After playing the recording, if you are comfortable sharing, tell us what happened and how you feel about it.",
                fontWeight = FontWeight.Normal,
                fontSize = 14.sp,
                color = colorResource(id = R.color.light_text),
                modifier = Modifier.padding(16.dp, 16.dp, 16.dp, 8.dp)
            )

            Divider(color = Color.Gray, thickness = 1.dp, modifier = Modifier.padding(horizontal = 16.dp))

            // first question
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp, 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "1. Did you respond to someone?",
                    fontWeight = FontWeight.Normal,
                    fontSize = 14.sp,
                    color = Color.Black,
                    modifier = Modifier.align(Alignment.CenterVertically)
                )

                Checkbox(
                    checked = isChecked,
                    onCheckedChange = { isChecked = it }
                )
            }

            Divider(color = Color.Gray, thickness = 1.dp, modifier = Modifier.padding(horizontal = 16.dp))

            // second question
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp, 8.dp),
            ) {
                Text(
                    text = "2. How important was it that you respond?",
                    fontWeight = FontWeight.Normal,
                    fontSize = 14.sp,
                    color = Color.Black
                )

                Slider(
                    value = sliderValue,
                    onValueChange = { sliderValue = it },
                    valueRange = 1f..5f,
                    modifier = Modifier
                        .height(48.dp)
                        .padding(horizontal = 28.dp)
                )
            }

            Divider(color = Color.Gray, thickness = 1.dp, modifier = Modifier.padding(horizontal = 16.dp))

            // third question
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp, 8.dp),
            ) {
                Text(
                    text = "3. How do you feel about your response to others?",
                    fontWeight = FontWeight.Normal,
                    fontSize = 14.sp,
                    color = Color.Black
                )
            }

            Divider(color = Color.Gray, thickness = 1.dp, modifier = Modifier.padding(horizontal = 16.dp))

            // fourth question
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp, 8.dp),
            ) {
                Text(
                    text = "4. What did you notice and feel about the way you responded to other people?",
                    fontWeight = FontWeight.Normal,
                    fontSize = 14.sp,
                    color = Color.Black
                )

                OutlinedTextField(
                    value = textValue,
                    onValueChange = { textValue = it },
                    placeholder = { Text("Input Here")},
                    modifier = Modifier
                        .padding(top = 16.dp)
                        .fillMaxSize(),
                    trailingIcon = {
                        Icon(
                            painterResource(id = R.drawable.ic_cancel_24),
                            contentDescription = "Localized description",
                            modifier = Modifier.clickable { textValue = ""}
                        )
                    }
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(0.dp, 8.dp)
                ) {
                    OutlinedButton(
                        onClick = { /* Handle button click */ },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = (colorResource(id = R.color.light_surface)),
                            contentColor = colorResource(id = R.color.primary)
                        ),
                        border = BorderStroke(1.dp, colorResource(id = R.color.outline)),
                        shape = RoundedCornerShape(100.dp),
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Text("Delete")
                    }

                    OutlinedButton(
                        onClick = { /* Handle button click */ },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = (colorResource(id = R.color.light_surface)),
                            contentColor = colorResource(id = R.color.primary)
                        ),
                        border = BorderStroke(1.dp, colorResource(id = R.color.outline)),
                        shape = RoundedCornerShape(100.dp),
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Text("Save for later")
                    }

                    Button(
                        onClick = { /* Handle button click */ },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colorResource(id = R.color.primary),
                            contentColor = Color.White,

                            ),
                        shape = RoundedCornerShape(100.dp),
                    ) {
                        Text("Save")
                    }
                }

            }
        }
    }

}

fun formatDateString(dateString: String): String {
    var formatter = DateTimeFormatter.ofPattern("yyyyMMdd")
    val date = LocalDate.parse(dateString, formatter)

    formatter = DateTimeFormatter.ofPattern("MM/dd")
    val formattedDate = date.format(formatter)

    val parts = formattedDate.split("/")
    val month = parts[0].removePrefix("0")
    val day = parts[1].removePrefix("0")

    return "$month/$day"
}