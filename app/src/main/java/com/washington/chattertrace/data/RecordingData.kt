package com.washington.chattertrace.data

import android.os.Environment
import android.util.Log
import java.io.File
import java.time.LocalDate
import java.time.format.DateTimeFormatter

var DIR_PATH = Environment.getExternalStorageDirectory().absolutePath +
        "/Android/data/com.washington.chattertrace/files/Documents/Bid4Connection/"

/*
 * Data classes and data structures
 */
data class RecordingFolder(val recordings: List<Recording>, val date: LocalDate, val isUploaded: Boolean)
data class Recording(val id: String, val audio: File, var isUploaded: Boolean)
val recordingMap: HashMap<LocalDate, List<Recording>> = hashMapOf()

fun recordingDataSetup() {
    recordingMap.clear()
    val fileList = File(DIR_PATH).listFiles()

    // for each audio, make a recording object and add it to the map
    for (file in fileList) {
        // extract date from the audio file
        var dateString = file.name.substringBeforeLast('_')
        if(dateString.startsWith("preceding")){
            continue;
        }
        //Log.d("SCREENWAKE", "recording file: " + file)
        val formatter = DateTimeFormatter.ofPattern("yyyyMMdd")
        val localDate = LocalDate.parse(dateString, formatter)

        if (!recordingMap.containsKey(localDate)) {
            recordingMap[localDate] = mutableListOf<Recording>()
        }

        // make recording object and add it to map
        // TODO: change isUploaded to not be hardcoded
        val recording = Recording(file.name.substringBeforeLast('.'), file, false)
        recordingMap[localDate] = recordingMap[localDate]!! + recording;
    }
}