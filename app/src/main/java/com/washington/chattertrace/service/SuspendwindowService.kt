package com.washington.chattertrace.service

import android.annotation.SuppressLint
import android.graphics.PixelFormat
import android.os.Build
import android.util.DisplayMetrics
import android.view.*
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import androidx.lifecycle.LifecycleService
import com.washington.chattertrace.R
import com.washington.chattertrace.utils.Utils
import com.washington.chattertrace.utils.ViewModleMain
import com.washington.chattertrace.utils.ItemViewTouchListener

/**
 * @功能:应用外打开Service 有局限性 特殊界面无法显示
 * @User Lmy
 * @Creat 4/15/21 5:28 PM
 * @Compony 永远相信美好的事情即将发生
 */
class SuspendwindowService : LifecycleService() {
    private lateinit var windowManager: WindowManager
    private var floatRootView: View? = null//悬浮窗View

    override fun onCreate() {
        super.onCreate()
        initObserve()
    }

    /**
     * 初始化订阅
     */
    private fun initObserve() {
        ViewModleMain.apply {
            /**
             * 悬浮窗按钮的显示和隐藏
             */
            isVisible.observe(this@SuspendwindowService, {
                floatRootView?.visibility = if (it) View.VISIBLE else View.GONE
            })
            /**
             * 悬浮窗按钮的创建和移除
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
     * 创建悬浮窗
     */
    @SuppressLint("ClickableViewAccessibility")
    private fun showWindow() {
        //获取WindowManager
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        val outMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(outMetrics)
        var layoutParam = WindowManager.LayoutParams().apply {
            /**
             * 设置type 这里进行了兼容
             */
            type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            } else {
                WindowManager.LayoutParams.TYPE_PHONE
            }
            format = PixelFormat.RGBA_8888
            flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
            //位置大小设置
            width = WRAP_CONTENT
            height = WRAP_CONTENT
            gravity = Gravity.LEFT or Gravity.TOP
            //设置剧中屏幕显示
            x = outMetrics.widthPixels / 2 - width / 2
            y = outMetrics.heightPixels / 2 - height / 2
        }
        // 新建悬浮窗控件
        floatRootView = LayoutInflater.from(this).inflate(R.layout.activity_float_item, null)
        floatRootView?.setOnTouchListener(ItemViewTouchListener(layoutParam, windowManager))
        // 将悬浮窗控件添加到WindowManager
        windowManager.addView(floatRootView, layoutParam)
    }
}