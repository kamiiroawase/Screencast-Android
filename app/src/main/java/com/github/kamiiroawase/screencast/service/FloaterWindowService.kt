package com.github.kamiiroawase.screencast.service

import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.os.IBinder
import android.view.*
import android.widget.FrameLayout
import com.github.kamiiroawase.screencast.App
import com.github.kamiiroawase.screencast.preference.AppPreference
import com.github.kamiiroawase.screencast.view.FloatBallView

class FloaterWindowService : Service() {
    private lateinit var windowManager: WindowManager

    companion object {
        var isShowing = false
        var isShouldShowing = false
        lateinit var floatBallView: FloatBallView
        const val ACTION_STOP_FLOATING = "ACTION_STOP_FLOATING"
        const val ACTION_START_FLOATING = "ACTION_START_FLOATING"
    }

    private val params = WindowManager.LayoutParams().apply {
        type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        format = PixelFormat.RGBA_8888
        flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
        gravity = Gravity.START or Gravity.TOP

        val (xx, yy) = AppPreference.getInstance().getSettingsXuanfuqiuLocation()

        x = xx.toInt()
        y = yy.toInt()

        width = WindowManager.LayoutParams.WRAP_CONTENT
        height = WindowManager.LayoutParams.WRAP_CONTENT
    }

    override fun onCreate() {
        super.onCreate()

        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager

        initFloatBall()
    }

    private fun initFloatBall() {
        val container = FrameLayout(this)

        floatBallView = FloatBallView(this).apply {
            val density = resources.displayMetrics.density

            layoutParams = FrameLayout.LayoutParams(
                (60 * density).toInt(),
                (60 * density).toInt()
            )

            onBallClickListener = {

            }

            onBallMovedListener = { dx, dy ->
                params.x = dx.toInt()
                params.y = dy.toInt()
                windowManager.updateViewLayout(container, params)
            }
        }

        container.addView(floatBallView)

        try {
            windowManager.addView(container, params)
            isShowing = true
            App.atomicFloaterBool.set(false)
        } catch (_: Exception) {
            //
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START_FLOATING -> {
                if (!isShowing) {
                    initFloatBall()
                }
            }

            ACTION_STOP_FLOATING -> {
                try {
                    stopSelf()
                } catch (_: Exception) {
                    //
                }
            }
        }

        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()

        removeFloatWindow()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun removeFloatWindow() {
        try {
            if (isShowing) {
                val parent = floatBallView.parent as? ViewGroup

                parent?.let {
                    windowManager.removeView(it)
                }

                isShowing = false
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        if (isShowing) {
            isShowing = false
        }

        try {
            App.atomicFloaterBool.set(false)
        } catch (_: Exception) {
            //
        }
    }
}
