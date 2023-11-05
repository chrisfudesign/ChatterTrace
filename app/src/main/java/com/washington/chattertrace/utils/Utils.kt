package com.washington.chattertrace.utils

import android.app.Activity
import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.provider.Settings
import android.text.TextUtils
import android.util.Log
import android.widget.Toast
import java.util.UUID


object Utils {
    const val REQUEST_FLOAT_CODE=1001

    //All parameters below are in Long type miliseconds
    const val SHOW_BUBBLE = 30000L //Timeout for show the bubble
    const val FADE_BUBBLE = 300000L //Timeout for bubble fade to 50% alpha
    const val DISAPPEAR_BUBBLE = 1500000L //Timeout for bubble disappear

    fun getUniqueID(context: Context): String? {
        val sharedPreferences =
            context.getSharedPreferences("MyAppPreferences", Context.MODE_PRIVATE)
        var uniqueId = sharedPreferences.getString("uniqueId", null)
        if (uniqueId == null) {
            // If not already created, then create new ID
            uniqueId = UUID.randomUUID().toString()
            val editor = sharedPreferences.edit()
            editor.putString("uniqueId", uniqueId)
            editor.apply()
        }
        return uniqueId
    }


    fun isServiceRunning(context: Context, ServiceName: String): Boolean {
        if (TextUtils.isEmpty(ServiceName)) {
            return false
        }
        val myManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val runningService =
            myManager.getRunningServices(1000) as ArrayList<ActivityManager.RunningServiceInfo>
        for (i in runningService.indices) {
            if (runningService[i].service.className == ServiceName) {
                return true
            }
        }
        return false
    }

    @JvmStatic
    fun showBubblewithTimeout(context: Context?){
        //Show the bubble after timeout
        Handler().postDelayed({
            ViewModleMain.isShowSuspendWindow.postValue(true)
        }, SHOW_BUBBLE)
    }

   private fun commonROMPermissionCheck(context: Context?): Boolean {
        var result = true
        if (Build.VERSION.SDK_INT >= 23) {
            try {
                val clazz: Class<*> = Settings::class.java
                val canDrawOverlays =
                    clazz.getDeclaredMethod("canDrawOverlays", Context::class.java)
                result = canDrawOverlays.invoke(null, context) as Boolean
            } catch (e: Exception) {
                Log.e("ServiceUtils", Log.getStackTraceString(e))
            }
        }
        return result
    }

    fun checkSuspendedWindowPermission(context: Activity, block: () -> Unit) {
        if (commonROMPermissionCheck(context)) {
            block()
        } else {
            Toast.makeText(context, "Please allow overlay window", Toast.LENGTH_SHORT).show()
            context.startActivityForResult(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION).apply {
                data = Uri.parse("package:${context.packageName}")
            }, REQUEST_FLOAT_CODE)
        }
    }

    fun isNull(any: Any?): Boolean = any == null

}