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
import java.io.File


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordingDetailScreen(date: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        CenterAlignedTopAppBar(
            // header text
            title = {
                var displayDate = date.replace("-", "/")
                Text(
                    text = "$displayDate Recordings",
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

        RecordingList()
    }
}

/*
 * Recording Rows
 */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordingList() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Column {
            var filePath = Environment.getExternalStorageDirectory().absolutePath +
                    "/Android/data/com.washington.chattertrace/files/Documents/Bid4Connection/1_0.mp3"
            var audio = File(filePath)
            RecordingRow(audio)
        }
    }
}


@ExperimentalMaterial3Api
@Composable
fun RecordingRow(audio: File) {


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

    exoPlayer.addListener(object : Player.Listener {
        override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
            when (playbackState) {
                ExoPlayer.STATE_BUFFERING -> {}
                ExoPlayer.STATE_ENDED -> {
                    exoPlayer.pause()
                    exoPlayer.seekTo(0)
                    isPlaying = false}
                ExoPlayer.STATE_IDLE -> {}
                ExoPlayer.STATE_READY -> {}
                else -> {}
            }
        }

        fun onPlayWhenReadyCommitted() {}
        fun onPlayerError(error: ExoPlaybackException?) {}
    })

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
            // figure out to display Today or MM/DD
            Text(text = "Bid Name")

            Slider(
                value = currentPosition.toFloat(),
                onValueChange = { /* Handle slider value change here */ },
                valueRange = 0f..100f,
                colors = SliderDefaults.colors(
                    thumbColor = colorResource(id = R.color.primary), // Customize thumb color
                    activeTrackColor = colorResource(id = R.color.primary), // Customize active track color
                    inactiveTrackColor = Color.LightGray // Customize inactive track color
                ),
//                modifier = Modifier
//                    .padding(horizontal = 16.dp)
            )
        },
        supportingText = {},
        leadingContent = {
            IconButton(
                onClick = {
                    if (exoPlayer.isPlaying) {
                        // pause the video
                        exoPlayer.pause()
                        isPlaying = false
                    } else {
                        // play the video
                        // it's already paused
                        exoPlayer.play()
                        isPlaying = true
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
//            if (folder.isUploaded) {
//                Icon(
//                    painter = painterResource(id = R.drawable.ic_uploaded_checkmark),
//                    contentDescription = "Localized description",
//                    tint = Color.Unspecified
//                )
//            } else {
                Icon(
                    painter = painterResource(id = R.drawable.ic_upload),
                    contentDescription = "Localized description",
                    tint = Color.Unspecified,
                    modifier = Modifier.padding(start = 16.dp)
                )
//            }

            Icon(
                painter = painterResource(id = R.drawable.ic_arrow_drop_down_24),
                contentDescription = "Localized description",
                modifier = Modifier.padding(start = 54.dp)
            )
        }
    )
}
