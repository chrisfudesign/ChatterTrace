package com.washington.chattertrace

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.washington.chattertrace.data.RecordingFolder
import com.washington.chattertrace.data.recordingMap
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReflectionsScreen(navController: NavHostController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        CenterAlignedTopAppBar(
            // header text
            title = {
                Text(
                    text = "Daily Reflections",
                    fontWeight = FontWeight.Normal,
                    fontSize = 24.sp,
                    color = Color.Black,
                    textAlign = TextAlign.Center
                )
            },
            modifier = Modifier.padding(top = 16.dp),
            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(Color.White)
        )

        FolderList(navController);
    }


}

/*
 * Recording Rows
 */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FolderList(navController: NavHostController) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Column {

            var folders = mutableListOf<RecordingFolder>()

            for (date in recordingMap.keys) {
                recordingMap[date]?.let { RecordingFolder(it, date, false) }?.let {
                    folders.add(
                        it
                    )
                }
            }

            if (folders.isEmpty()) {
                Text(
                    text = "No reflections available",
                    fontWeight = FontWeight.Normal,
                    fontSize = 18.sp,
                    color = Color.Gray,
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxSize()
                        .align(Alignment.CenterHorizontally),
                    textAlign = TextAlign.Center
                )
            } else {
                folders.forEach { folder ->
                    FolderRow(folder, false, navController)
                }
            }
        }
    }
}

@ExperimentalMaterial3Api
@Composable
fun FolderRow(folder: RecordingFolder, uploadStatus: Boolean, navController: NavHostController) {
    ListItem(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 26.dp, end = 26.dp, top = 15.dp)
            .border(
                width = 1.dp,
                color = colorResource(id = R.color.primary),
                shape = RoundedCornerShape(6.dp)
            )
            .height(75.dp) //TODO: fixed height or dynamic height??
            .clickable {
                // TODO: figure out how to pass in the folder item or find infrastructure to do so
                var date = "${folder.date.monthValue}-${folder.date.dayOfMonth}"
                navController.navigate("reflectionDetail/${date}") {
                    // Pop up to the start destination of the graph to
                    // avoid building up a large stack of destinations
                    // on the back stack as users select items
                    navController.graph.startDestinationRoute?.let { route ->
                        popUpTo(route) {
                            saveState = true
                        }
                    }
                    // Avoid multiple copies of the same destination when
                    // reselecting the same item
                    launchSingleTop = true
                }
            },
        headlineText = {
            // figure out to display Today or MM/DD
            var day = if (LocalDate.now() == folder.date) "Today" else "${folder.date.monthValue}/${folder.date.dayOfMonth}";
            Text(text = "Bid - ${day}")
        },
        leadingContent = {
            Icon(
                painter = painterResource(id = R.drawable.ic_reflection_black_24dp),
                contentDescription = "Localized description"
            )
        },
        trailingContent = {
            if (uploadStatus) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_uploaded_checkmark),
                    contentDescription = "Localized description",
                    tint = Color.Unspecified
                )
            }

            Icon(
                painter = painterResource(id = R.drawable.ic_pencil),
                contentDescription = "Localized description",
                modifier = Modifier.padding(start = 54.dp)
            )
        }
    )
}