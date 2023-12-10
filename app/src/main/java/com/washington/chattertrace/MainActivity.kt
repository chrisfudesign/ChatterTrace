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
import android.util.Log
import android.view.View
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
import androidx.preference.PreferenceManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.washington.chattertrace.DataLogic.DataManager
import com.washington.chattertrace.RecordingLogic.RecordingManager
import com.washington.chattertrace.data.Recording
import com.washington.chattertrace.data.recordingDataSetup
import com.washington.chattertrace.data.recordingMap
import com.washington.chattertrace.service.SuspendwindowService
import com.washington.chattertrace.utils.Utils
import com.washington.chattertrace.utils.ViewModleMain
import java.io.IOException
import java.time.LocalDate


public var recordingManager: RecordingManager? = null
class MainActivity : ComponentActivity() {
    private var floatRootView: View? = null//floating window View
    private var isReceptionShow = false
    private var dataManager: DataManager? = null
    var context: Context? = null
    private val REQUEST_ID_MULTIPLE_PERMISSIONS = 1
    private var serviceBound = false

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
        if (checkAndRequestPermissions()) {
            StartService()
        }
        else {
            println("NO PERMISSION")
        }

        if(!Utils.isServiceRunning(this, "SuspendwindowService")){
            startService(Intent(this, SuspendwindowService::class.java))
            Utils.checkSuspendedWindowPermission(this) {
                isReceptionShow = true
                Utils.showBubblewithTimeout(this)
            }
        }
        
//        setContent {
//            // create data maps
//            dummyDataSetup()
//            MainScreen(recordingManager = recordingManager, dataManager = dataManager)
//        }
    }

    override fun onResume() {
        super.onResume()
        if (!Utils.isNull(floatRootView)) {
            if (!Utils.isNull(floatRootView?.windowToken)) {
                if (Utils.isNull(windowManager)) {
                    windowManager?.removeView(floatRootView)
                }
            }
        }
//        if(ViewModleMain.isShowSuspendWindow.value == false){
//            Utils.showBubblewithTimeout(this)
//        }
    }

//    override fun onNewIntent(intent: Intent?) {
//        super.onNewIntent(intent)
//        val message = intent?.getStringExtra("message")
//        if (message != null) {
//            // Handle the received message here
//        }
//    }

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
        val recorderIntent = Intent(this, RecordingManager::class.java)
        startService(recorderIntent)
        bindService(recorderIntent, serviceConnection, BIND_IMPORTANT)
//        bindService(recorderIntent, serviceConnection, BIND_AUTO_CREATE)
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

//            if (preceding_time > 0 && preceding_mode) {
//                recordingManager?.StartRecordingSilently(
//                    dataManager?.getRecordingNameOfTimeWithPrefix(
//                        "preceding"
//                    ), preceding_time
//                )
//            }
            setContent {
                // create data maps
                recordingDataSetup(context as MainActivity)
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
    ViewModleMain.NavController.postValue(navController)

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
