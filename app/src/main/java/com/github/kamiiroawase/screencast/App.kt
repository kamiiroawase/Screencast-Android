package com.github.kamiiroawase.screencast

import android.app.Activity
import android.app.Application
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import com.github.kamiiroawase.screencast.preference.AppPreference
import com.github.kamiiroawase.screencast.service.FloaterWindowService
import java.util.concurrent.atomic.AtomicBoolean

class App : Application() {
    companion object {
        private lateinit var INSTANCE: App

        const val RECORDING_CHANNEL_ID = "f47ac10b-58cc-4372-a567-0e02b2c3d479"

        val atomicFloaterBool = AtomicBoolean(false)

        fun getInstance(): App {
            return INSTANCE
        }
    }

    override fun onCreate() {
        super.onCreate()

        INSTANCE = this

        AppPreference.init(this)

        setupProcessLifecycle()

        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        val notificationManager = getSystemService(NotificationManager::class.java)

        if (notificationManager.getNotificationChannel(RECORDING_CHANNEL_ID) == null) {
            notificationManager.createNotificationChannel(
                NotificationChannel(
                    RECORDING_CHANNEL_ID,
                    getString(R.string.pingmuluzhizhong),
                    NotificationManager.IMPORTANCE_LOW
                ).apply {
                    setShowBadge(false)
                    lockscreenVisibility = Notification.VISIBILITY_PUBLIC
                }
            )
        }
    }

    private fun setupProcessLifecycle() {
        when (AppPreference.getInstance().getSettingsXuanfuqiuSwitch()) {
            "1", "2" -> {
                FloaterWindowService.isShouldShowing = true
            }
        }

        registerActivityLifecycleCallbacks(object : ActivityLifecycleCallbacks {
            private var activityCount = 0

            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
                activityCount++

                if (activityCount > 0 && !FloaterWindowService.isShowing && FloaterWindowService.isShouldShowing) {
                    if (Settings.canDrawOverlays(this@App)) {
                        if (!atomicFloaterBool.getAndSet(true)) {
                            startService(Intent(this@App, FloaterWindowService::class.java).apply {
                                action = FloaterWindowService.ACTION_START_FLOATING
                            })
                        }
                    }
                }
            }

            override fun onActivityStarted(activity: Activity) {
                //
            }

            override fun onActivityResumed(activity: Activity) {
                //
            }

            override fun onActivityPaused(activity: Activity) {
                //
            }

            override fun onActivityStopped(activity: Activity) {
                //
            }

            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
                //
            }

            override fun onActivityDestroyed(activity: Activity) {
                activityCount--

                if (activityCount <= 0 && FloaterWindowService.isShowing) {
                    if (!atomicFloaterBool.getAndSet(true)) {
                        startService(Intent(this@App, FloaterWindowService::class.java).apply {
                            action = FloaterWindowService.ACTION_STOP_FLOATING
                        })
                    }
                }
            }
        })
    }
}
