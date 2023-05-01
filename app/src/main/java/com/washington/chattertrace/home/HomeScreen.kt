package com.washington.chattertrace.home

import android.widget.RadioGroup
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.washington.chattertrace.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen() {
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
            modifier = Modifier.fillMaxWidth().padding(start = 47.dp, end = 47.dp),
            lineHeight = 24.sp,
            textAlign = TextAlign.Center
        )

        Text(
            text = "Session Timer (minutes)",
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            color = colorResource(id = R.color.light_text),
            modifier = Modifier.fillMaxWidth().padding(top = 40.dp),
            lineHeight = 24.sp,
            textAlign = TextAlign.Center
        )

        Text(
            text = "Optionally select a recording timer. Recordings will automatically stop at 180 minutes.",
            fontWeight = FontWeight.Normal,
            fontSize = 16.sp,
            color = colorResource(id = R.color.light_text),
            modifier = Modifier.fillMaxWidth().padding(top = 11.dp, start = 24.dp, end = 24.dp),
            lineHeight = 24.sp,
            textAlign = TextAlign.Center
        )

        // time select radio buttons
        val radioOptions = listOf(15, 30, 60, 90, 120, 180)
        var selectedOptionIndex by remember { mutableStateOf(0) }


        Text(
            text = "Recording Time",
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            color = colorResource(id = R.color.light_text),
            modifier = Modifier.fillMaxWidth().padding(top = 40.dp),
            lineHeight = 24.sp,
            textAlign = TextAlign.Center
        )
    }
}
