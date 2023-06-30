package com.washington.chattertrace.recordings

import android.net.Uri
import android.os.Environment
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.upstream.DataSpec
import com.google.android.exoplayer2.upstream.FileDataSource
import com.google.android.exoplayer2.util.MimeTypes
import com.washington.chattertrace.R
import com.washington.chattertrace.data.recordingMap
import kotlinx.coroutines.delay
import java.io.File
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
                IconButton(onClick = { /* Upload all action here */ }) {
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
                RecordingRow(recording.audio, recording.isUploaded)
            }
        }
    }
}


@ExperimentalMaterial3Api
@Composable
fun RecordingRow(audio: File, isUploaded: Boolean) {


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
                )
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
            if (isUploaded) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_uploaded_checkmark),
                    contentDescription = "Localized description",
                    tint = Color.Unspecified
                )
            } else {
                Icon(
                    painter = painterResource(id = R.drawable.ic_upload),
                    contentDescription = "Localized description",
                    tint = Color.Unspecified,
                    modifier = Modifier.padding(start = 16.dp)
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