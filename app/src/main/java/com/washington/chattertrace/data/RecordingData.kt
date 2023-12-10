package com.washington.chattertrace.data

import android.content.Context
import android.os.Environment
import android.util.Log
import androidx.preference.PreferenceManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
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
var recordingMap: HashMap<LocalDate, List<Recording>> = hashMapOf()

fun recordingDataSetup(context: Context) {
    //recordingMap.clear()
    val fileList = File(DIR_PATH).listFiles()
    //Log.d("SCREENWAKE", "recording map set up start: " + recordingMap)

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
            var isUploaded = false
            recordingMap[localDate] = mutableListOf<Recording>()
            // make recording object and add it to map
            val preferences = PreferenceManager.getDefaultSharedPreferences(context)
            val gson = Gson()
            val buffer_json: String? = preferences.getString("uploadedList", null)
            val type = object : TypeToken<List<String?>?>() {}.type
            Log.d("SCREENWAKE", "recordingDataSetUp uploadedList: " + buffer_json)
            var uploadedList = gson.fromJson<List<String?>?>(buffer_json, type)
            if(uploadedList != null && uploadedList.contains(file.name.substringBeforeLast('.'))){
                Log.d("SCREENWAKE", "recordingDataSetUp found uploaded: " + file.name.substringBeforeLast('.'))
                isUploaded = true
            }
            val recording = Recording(file.name.substringBeforeLast('.'), file, isUploaded)
            recordingMap[localDate] = recordingMap[localDate]!! + recording;
        }
    }
    //Log.d("SCREENWAKE", "recording map set up over: " + recordingMap)
}