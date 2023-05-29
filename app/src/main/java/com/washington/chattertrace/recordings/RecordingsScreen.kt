package com.washington.chattertrace.recordings

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
import com.washington.chattertrace.R
import com.washington.chattertrace.data.RecordingFolder
import com.washington.chattertrace.data.folderList
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordingsScreen(navController: NavHostController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        CenterAlignedTopAppBar(
            // header text
            title = {
                Text(
                    text = "Recordings",
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
            val folders = folderList;
            folders.forEach { folder ->
                FolderRow(folder,  navController)
            }
        }
    }
}

@ExperimentalMaterial3Api
@Composable
fun FolderRow(folder: RecordingFolder, navController: NavHostController) {
    ListItem(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 26.dp, end = 26.dp, top = 15.dp)
            .border(
                width = 1.dp,
                color = colorResource(id = R.color.primary),
                shape = RoundedCornerShape(6.dp)
            )
            .clickable {
                // TODO: figure out how to pass in the folder item or find infrastructure to do so
                var date = "${folder.date.monthValue}-${folder.date.dayOfMonth}"
                navController.navigate("recordingDetail/${date}") {
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
        supportingText = {
            Text(text = "${String.format("%02d", folder.duration.minute)}:${String.format("%02d", folder.duration.second)}")
            Text(text = "0/${folder.numRecordings}") },
        leadingContent = {
            Icon(
                painter = painterResource(id = R.drawable.ic_outline_folder_24),
                contentDescription = "Localized description"
            )
        },
        trailingContent = {
            if (folder.isUploaded) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_uploaded_checkmark),
                    contentDescription = "Localized description",
                    tint = Color.Unspecified
                )
            } else {
                Icon(
                    painter = painterResource(id = R.drawable.ic_upload),
                    contentDescription = "Localized description",
                    tint = Color.Unspecified
                )
            }

            Icon(
                painter = painterResource(id = R.drawable.ic_baseline_arrow_forward_24),
                contentDescription = "Localized description",
                modifier = Modifier.padding(start = 54.dp)
            )
        }
    )
}