package com.washington.chattertrace

import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.compose.rememberNavController
import com.washington.chattertrace.Data.DataManager
import com.washington.chattertrace.Data.dummyDataSetup
import com.washington.chattertrace.RecordingLogic.RecordingManager
import java.io.IOException


class MainActivity : ComponentActivity() {

    private var recordingManager: RecordingManager? = null
    private var dataManager: DataManager? = null
    var context: Context? = null
    private val REQUEST_ID_MULTIPLE_PERMISSIONS = 1
    private var serviceBound = false
    private val preceding_time = 10 // PAY ATTENTION TO THIS
    private val preceding_mode = true // PAY ATTENTION TO THIS

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        context = this;

        //The procedure to instantiate the datamanager
        //first call getInstance, then setfoldername, then call Initialize
        dataManager = DataManager.getInstance()
        try {
            dataManager?.Initialize(context)
            dataManager?.setFolderName("Bid4Connection")
        } catch (e: IOException) {
            e.printStackTrace()
        }

        //check for necessary permissions

        //check for necessary permissions
        if (checkAndRequestPermissions()) {
            StartService()
        }
        else {
            println("NO PERMISSION")
        }


//        setContent {
//            // create data maps
//            dummyDataSetup()
//            MainScreen(recordingManager = recordingManager, dataManager = dataManager)
//        }
    }

    private fun checkAndRequestPermissions(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val permissionRecording: Int =
                ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
            val permissionStorage: Int =
                ContextCompat.checkSelfPermission(this, Manifest.permission.MANAGE_EXTERNAL_STORAGE)
            val listPermissionsNeeded: MutableList<String> = ArrayList()
            if (permissionRecording != PackageManager.PERMISSION_GRANTED) {
                listPermissionsNeeded.add(Manifest.permission.RECORD_AUDIO)
            }
//            if (permissionStorage != PackageManager.PERMISSION_GRANTED) {
//                listPermissionsNeeded.add(Manifest.permission.MANAGE_EXTERNAL_STORAGE)
//            }
            println(listPermissionsNeeded.count())
            println(permissionRecording)
            println(permissionStorage)
            println(PackageManager.PERMISSION_GRANTED)
            return if (listPermissionsNeeded.isNotEmpty()) {
                ActivityCompat.requestPermissions(
                    this,
                    listPermissionsNeeded.toTypedArray(),
                    REQUEST_ID_MULTIPLE_PERMISSIONS
                )
                false
            } else {
                true
            }
        }
        return true
    }

    private fun StartService() {
        println("TRY TO START SERVICE")
        val recorderIntent = Intent(this, RecordingManager::class.java)
        startService(recorderIntent)
        bindService(recorderIntent, serviceConnection, BIND_IMPORTANT)
//        bindService(recorderIntent, serviceConnection, BIND_AUTO_CREATE)
        println("AAAAAAAAAAAAAAAAAAAAAAA")
    }

    private val serviceConnection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            val binder: RecordingManager.LocalBinder = service as RecordingManager.LocalBinder
            recordingManager = binder.getServiceInstance()
            serviceBound = true
            println("SERVICE CONNECTION INIT RECORDING MANAGER")
            println(recordingManager?.isRecording())
            //for auto-recording
//            updateRecordSettings()
            recordingManager?.setPrecedingTime(10)

            if (preceding_time > 0 && preceding_mode) {
                recordingManager?.StartRecordingSilently(
                    dataManager?.getRecordingNameOfTimeWithPrefix(
                        "preceding"
                    ), preceding_time
                )
            }
            setContent {
                // create data maps
                dummyDataSetup()
                MainScreen(recordingManager = recordingManager, dataManager = dataManager)
            }
        }

        override fun onServiceDisconnected(name: ComponentName) {
            serviceBound = false
        }
    }

}



@Composable
fun MainScreen(recordingManager: RecordingManager?, dataManager: DataManager?) {
    val navController = rememberNavController()
    Scaffold(
        bottomBar = { BottomNavigationBar(navController) },
        content = { padding -> // We have to pass the scaffold inner padding to our content. That's why we use Box.
            Box(modifier = Modifier.padding(padding)) {
                Navigation(navController = navController, recordingManager = recordingManager, dataManager = dataManager)
            }
        },
        backgroundColor = Color.White // Set background color to avoid the white flashing when you switch between screens
    )
}
