package com.washington.chattertrace.service

import android.annotation.SuppressLint
import android.graphics.PixelFormat
import android.os.Build
import android.os.Handler
import android.util.DisplayMetrics
import android.view.*
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.ImageView
import androidx.lifecycle.LifecycleService
import com.washington.chattertrace.R
import com.washington.chattertrace.data.recordingMap
import com.washington.chattertrace.utils.HttpPostTask
import com.washington.chattertrace.utils.ItemViewTouchListener
import com.washington.chattertrace.utils.Utils
import com.washington.chattertrace.utils.ViewModleMain
import java.time.format.DateTimeFormatter


class SuspendwindowService : LifecycleService() {
    private lateinit var windowManager: WindowManager
    private var floatRootView: View? = null
    private var bubbleButton: ImageView? = null
    private var bubbleClose: ImageView? = null

    override fun onCreate() {
        super.onCreate()
        initObserve()
    }

    private fun initObserve() {
        ViewModleMain.apply {
            /**
             * Show and hide of bubble
             */
            isVisible.observe(this@SuspendwindowService, {
                floatRootView?.visibility = if (it) View.VISIBLE else View.GONE
            })
            /**
             * crete and remove of bubble
             */
            isShowSuspendWindow.observe(this@SuspendwindowService, {
                if (it) {
                    showWindow()
                } else {
                    if (!Utils.isNull(floatRootView)) {
                        if (!Utils.isNull(floatRootView?.windowToken)) {
                            if (!Utils.isNull(windowManager)) {
                                windowManager?.removeView(floatRootView)
                            }
                        }
                    }
                }
            })
        }
    }

    /**
     * create bubble
     */
    @SuppressLint("ClickableViewAccessibility")
    private fun showWindow() {
        //get WindowManager
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        val outMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(outMetrics)
        var layoutParam = WindowManager.LayoutParams().apply {
            type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            } else {
                WindowManager.LayoutParams.TYPE_PHONE
            }
            format = PixelFormat.RGBA_8888
            flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
            width = WRAP_CONTENT
            height = WRAP_CONTENT
            gravity = Gravity.LEFT or Gravity.TOP
            x = outMetrics.widthPixels / 2 - width / 2
            y = outMetrics.heightPixels / 2 - height / 2
        }

        floatRootView = LayoutInflater.from(this).inflate(R.layout.activity_float_item, null)
        floatRootView?.setOnTouchListener(ItemViewTouchListener(layoutParam, windowManager))
        windowManager.addView(floatRootView, layoutParam)

        bubbleButton = floatRootView?.findViewById(R.id.bubble_button)
        floatRootView?.setOnClickListener {
            val url = "http://is-bids.ischool.uw.edu:3000/upload_files"
            val requestBody = "{\"name\": \"Upendra\", \"job\": \"Programmer\"}";

            HttpPostTask.performHttpPost(url, requestBody, this)
            if(recordingMap.isNotEmpty()){
                val formatter = DateTimeFormatter.ofPattern("yyyyMMdd")
                val lastDateString = recordingMap.keys.last().toString().filter { it.isLetterOrDigit() }.format(formatter)
                //Toast.makeText(this, lastDateString, Toast.LENGTH_LONG).show()
                ViewModleMain.NavController.value?.navigate("recordingDetail/${lastDateString}")
            }
        }

        //Bubble fade after timeout
        Handler().postDelayed({
            bubbleButton?.alpha = 0.3f
        }, Utils.FADE_BUBBLE)

        //Bubble disappear after timeout
        Handler().postDelayed({
            ViewModleMain.isShowSuspendWindow.postValue(false)
            ViewModleMain.isShowWindow.postValue(false)
        }, Utils.DISAPPEAR_BUBBLE)

        bubbleClose = floatRootView?.findViewById(R.id.bubble_close)
        bubbleClose?.setOnClickListener(View.OnClickListener {
            ViewModleMain.isShowSuspendWindow.postValue(false)
            ViewModleMain.isShowWindow.postValue(false)
        })
    }
}