package com.washington.chattertrace.reflections

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.washington.chattertrace.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReflectionDetailScreen(date: String) {
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
                    text = "$displayDate Reflection",
                    fontWeight = FontWeight.Normal,
                    fontSize = 24.sp,
                    color = Color.Black,
                    textAlign = TextAlign.Center
                )
            },
            navigationIcon = {
                IconButton(onClick = { /* TODO: Handle back button click */ }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back button")
                }
            },
            modifier = Modifier.padding(top = 16.dp),
            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(Color.White)
        )

        // body
        Box(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(start = 30.dp, end = 30.dp),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White)
            ) {
                Text(
                    text = "What did you notice and feel about the way you responded to other people?",
                    fontWeight = FontWeight.Normal,
                    fontSize = 13.5.sp,
                    color = colorResource(id = R.color.surface),
                    modifier = Modifier.padding(top = 40.dp),
                    lineHeight = 24.sp,
                    textAlign = TextAlign.Left
                )
                // input text
                var textValue by remember { mutableStateOf("") }

                OutlinedTextField(
                    value = textValue,
                    onValueChange = { textValue = it },
                    placeholder = { Text("Input Here")},
                    modifier = Modifier
                        .padding(top = 24.dp)
                        .fillMaxSize(),
                    trailingIcon = {
                        Icon(
                            painterResource(id = R.drawable.ic_cancel_24),
                            contentDescription = "Localized description"
                        )
                    }
                )

                // buttons
                Row(
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier
                        .padding(top = 8.dp)
                        .fillMaxSize()
                ) {
                    OutlinedButton(
                        onClick = { /* Handle button click */ },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White,
                            contentColor = colorResource(id = R.color.primary)
                        ),
                        border = BorderStroke(1.dp, colorResource(id = R.color.outline)),
                        shape = RoundedCornerShape(100.dp),
                        modifier = Modifier.padding(end = 8.dp)
    //                    enabled = false
                    ) {
                        Text("Delete")
                    }

                    Button(
                        onClick = { /* Handle button click */ },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colorResource(id = R.color.primary),
                            contentColor = Color.White,

                        ),
                        shape = RoundedCornerShape(100.dp),
    //                    enabled = false
                    ) {
                        Text("Save")
                    }
                }
            }
        }
    }
}