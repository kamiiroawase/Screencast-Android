package com.github.kamiiroawase.screencast.service

import android.Manifest
import android.app.Activity
import android.app.PendingIntent
import android.app.Service
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.MediaRecorder
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.*
import android.provider.DocumentsContract
import android.provider.MediaStore
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import com.github.kamiiroawase.screencast.App
import com.github.kamiiroawase.screencast.activity.MainActivity
import com.github.kamiiroawase.screencast.fragment.LuzhiFragment
import com.github.kamiiroawase.screencast.preference.AppPreference
import com.github.kamiiroawase.screencast.R
import java.io.File
import java.io.FileDescriptor
import java.text.SimpleDateFormat
import java.util.*

class ScreenRecorderService : Service() {
    companion object {
        var isPaused = false
        var isRecording = false
        var lastPauseTime: Long = 0
        var totalPausedTime: Long = 0
        var recordingStartTime: Long = 0
        private const val NOTIFICATION_ID = 9999
        const val EXTRA_RESULT_DATA = "EXTRA_RESULT_DATA"
        const val EXTRA_SCREEN_WIDTH = "EXTRA_SCREEN_WIDTH"
        const val EXTRA_SCREEN_HEIGHT = "EXTRA_SCREEN_HEIGHT"
        const val EXTRA_SCREEN_DENSITY = "EXTRA_SCREEN_DENSITY"
        const val ACTION_STOP_RECORDING = "ACTION_STOP_RECORDING"
        const val ACTION_START_RECORDING = "ACTION_START_RECORDING"
        const val ACTION_PAUSE_RECORDING = "ACTION_PAUSE_RECORDING"
        const val ACTION_RESUME_RECORDING = "ACTION_RESUME_RECORDING"
    }

    private var mediaProjection: MediaProjection? = null
    private var virtualDisplay: VirtualDisplay? = null
    private var mediaRecorder: MediaRecorder? = null
    private var mediaProjectionCallback: MediaProjection.Callback? = null
    private var outputFilePath: String = ""
    private var outputFileDescriptor: FileDescriptor? = null

    private var displayWidth = 0
    private var displayHeight = 0
    private var screenDensity = 0

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        if (!isRecording) {
            try {
                mediaRecorder?.release()
            } catch (_: Exception) {
                //
            }

            try {
                virtualDisplay?.release()
            } catch (_: Exception) {
                //
            }

            try {
                mediaProjectionCallback?.let { callback ->
                    mediaProjection?.unregisterCallback(callback)
                }
            } catch (_: Exception) {
                //
            }

            virtualDisplay = null
            mediaProjectionCallback = null
            mediaProjection = null
            mediaRecorder = null
        } else {
            stopScreenRecording()
        }

        super.onDestroy()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START_RECORDING -> {
                updateForegroundService(true)

                val data = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    intent.getParcelableExtra(EXTRA_RESULT_DATA, Intent::class.java)
                } else {
                    @Suppress("DEPRECATION")
                    intent.getParcelableExtra(EXTRA_RESULT_DATA)
                }

                if (data != null) {
                    getScreenMetrics(intent)
                    startScreenRecording(data)
                } else {
                    stopSelf()
                }
            }

            ACTION_STOP_RECORDING -> {
                stopScreenRecording()
            }

            ACTION_PAUSE_RECORDING -> {
                pauseScreenRecording()
            }

            ACTION_RESUME_RECORDING -> {
                resumeScreenRecording()
            }
        }

        return START_STICKY
    }

    private fun onRecordStart() {
        isPaused = false
        isRecording = true
        lastPauseTime = 0
        totalPausedTime = 0
        recordingStartTime = System.currentTimeMillis()

        try {
            LuzhiFragment.getInstance().updateUiButtonKaishiluzhi()
        } catch (_: Exception) {
            //
        }

        when (AppPreference.getInstance().getSettingsXuanfuqiuSwitch()) {
            "1" -> {
                if (FloaterWindowService.isShowing) {
                    try {
                        FloaterWindowService.floatBallView.setButtonStart()
                    } catch (_: Exception) {
                        //
                    }
                }
            }

            "2" -> {
                FloaterWindowService.isShouldShowing = false

                if (FloaterWindowService.isShowing) {
                    try {
                        val context = App.getInstance()

                        context.startService(
                            Intent(
                                context,
                                FloaterWindowService::class.java
                            ).apply {
                                action = FloaterWindowService.ACTION_STOP_FLOATING
                            }
                        )
                    } catch (_: Exception) {
                        //
                    }
                }
            }
        }
    }

    private fun onRecordStop() {
        isPaused = false
        isRecording = false

        try {
            LuzhiFragment.getInstance().updateUiButtonKaishiluzhi()
        } catch (_: Exception) {
            //
        }

        when (AppPreference.getInstance().getSettingsXuanfuqiuSwitch()) {
            "1" -> {
                if (FloaterWindowService.isShowing) {
                    try {
                        FloaterWindowService.floatBallView.setButtonStop()
                    } catch (_: Exception) {
                        //
                    }
                }
            }

            "2" -> {
                FloaterWindowService.isShouldShowing = true

                if (!FloaterWindowService.isShowing) {
                    try {
                        val context = App.getInstance()

                        context.startService(
                            Intent(
                                context,
                                FloaterWindowService::class.java
                            ).apply {
                                action = FloaterWindowService.ACTION_START_FLOATING
                            }
                        )
                    } catch (_: Exception) {
                        //
                    }
                }
            }
        }
    }

    private fun onRecordPause() {
        isPaused = true
        lastPauseTime = System.currentTimeMillis()

        when (AppPreference.getInstance().getSettingsXuanfuqiuSwitch()) {
            "1" -> {
                if (FloaterWindowService.isShowing) {
                    try {
                        FloaterWindowService.floatBallView.setButtonPause()
                    } catch (_: Exception) {
                        //
                    }
                }
            }
        }

        updateForegroundService(false)
    }

    private fun onRecordResume() {
        isPaused = false
        totalPausedTime += (System.currentTimeMillis() - lastPauseTime)

        when (AppPreference.getInstance().getSettingsXuanfuqiuSwitch()) {
            "1" -> {
                if (FloaterWindowService.isShowing) {
                    try {
                        FloaterWindowService.floatBallView.setButtonStart()
                    } catch (_: Exception) {
                        //
                    }
                }
            }
        }

        updateForegroundService(false)
    }

    private fun startScreenRecording(data: Intent) {
        try {
            val projectionManager = getSystemService(MEDIA_PROJECTION_SERVICE)
                    as MediaProjectionManager

            mediaProjection = projectionManager.getMediaProjection(Activity.RESULT_OK, data)

            if (mediaProjection == null) {
                stopSelf()
                return
            }

            mediaProjectionCallback = object : MediaProjection.Callback() {
                override fun onStop() {
                    Handler(Looper.getMainLooper()).post {
                        stopScreenRecording()
                    }
                }
            }

            mediaProjection?.registerCallback(mediaProjectionCallback!!, null)

            initMediaRecorder { success ->
                if (success) {
                    if (!createVirtualDisplay()) {
                        try {
                            mediaRecorder?.start()
                            onRecordStart()
                        } catch (_: Exception) {
                            stopSelf()
                        }
                    } else {
                        stopSelf()
                    }
                } else {
                    stopSelf()
                }
            }
        } catch (_: Exception) {
            stopSelf()
        }
    }

    private fun stopScreenRecording() {
        if (!isRecording) {
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
            return
        }

        try {
            mediaRecorder?.stop()
        } catch (_: Exception) {
            //
        }

        try {
            mediaRecorder?.release()
        } catch (_: Exception) {
            //
        }

        try {
            virtualDisplay?.release()
        } catch (_: Exception) {
            //
        }

        try {
            mediaProjectionCallback?.let { callback ->
                mediaProjection?.unregisterCallback(callback)
            }
        } catch (_: Exception) {
            //
        }

        try {
            mediaProjection?.stop()
        } catch (_: Exception) {
            //
        }

        try {
            stopForeground(STOP_FOREGROUND_REMOVE)
        } catch (_: Exception) {
            //
        }

        try {
            stopSelf()
        } catch (_: Exception) {
            //
        }

        onRecordStop()

        virtualDisplay = null
        mediaProjectionCallback = null
        mediaProjection = null
        mediaRecorder = null
    }

    private fun pauseScreenRecording() {
        if (!isRecording || isPaused) {
            return
        }

        try {
            mediaRecorder?.pause()
            onRecordPause()
        } catch (_: Exception) {
            //
        }
    }

    private fun resumeScreenRecording() {
        if (!isRecording || !isPaused) {
            return
        }

        try {
            mediaRecorder?.resume()
            onRecordResume()
        } catch (_: Exception) {
            //
        }
    }

    private fun updateForegroundService(isStart: Boolean) {
        val notificationIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, notificationIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val stopIntent = Intent(this, ScreenRecorderService::class.java).apply {
            action = ACTION_STOP_RECORDING
        }
        val stopPendingIntent = PendingIntent.getService(
            this, 1, stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notificationBuilder = NotificationCompat.Builder(this, App.RECORDING_CHANNEL_ID)
            .setContentTitle(getString(R.string.pingmuluzhizhong))
            .setContentText(getString(R.string.zhengzailuzhinindepingmu))
            .setSmallIcon(android.R.drawable.ic_menu_camera)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setOnlyAlertOnce(false)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setSound(null)
            .setVibrate(null)
            .addAction(
                android.R.drawable.ic_media_pause,
                getString(R.string.tingzhi),
                stopPendingIntent
            )

        val addPauseActionFn = fun() {
            val pauseIntent = Intent(this, ScreenRecorderService::class.java).apply {
                action = ACTION_PAUSE_RECORDING
            }
            val pausePendingIntent = PendingIntent.getService(
                this, 3, pauseIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            notificationBuilder.addAction(
                android.R.drawable.ic_media_pause,
                getString(R.string.zanting),
                pausePendingIntent
            )
        }

        if (isRecording) {
            if (isPaused) {
                val resumeIntent = Intent(this, ScreenRecorderService::class.java).apply {
                    action = ACTION_RESUME_RECORDING
                }
                val resumePendingIntent = PendingIntent.getService(
                    this, 2, resumeIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )

                notificationBuilder.addAction(
                    android.R.drawable.ic_media_play,
                    getString(R.string.jixu),
                    resumePendingIntent
                )
            } else {
                addPauseActionFn()
            }
        } else if (isStart) {
            addPauseActionFn()
        }

        val notification = notificationBuilder.build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    private fun initMediaRecorder(onComplete: (Boolean) -> Unit) {
        Thread {
            try {
                val timeStamp =
                    SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())

                val getDefaultFilePath = fun(): Boolean {
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                        val dirParent =
                            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)

                        val outputDir = File(dirParent, "Recordings").apply {
                            if (!exists()) mkdirs()
                        }

                        val fileName = "ScreenRecord_$timeStamp.mp4"
                        val outputFile = File(outputDir, fileName)

                        outputFilePath = outputFile.absolutePath

                        if (!outputDir.exists() && !outputDir.mkdirs()) {
                            Handler(Looper.getMainLooper()).post { onComplete(false) }
                            return false
                        }
                    } else {
                        val contentValues = ContentValues().apply {
                            put(MediaStore.Downloads.DISPLAY_NAME, "ScreenRecord_$timeStamp.mp4")
                            put(MediaStore.Downloads.MIME_TYPE, "video/mp4")
                            put(
                                MediaStore.Downloads.RELATIVE_PATH,
                                Environment.DIRECTORY_DOWNLOADS + "/Recordings"
                            )
                        }

                        val uri = contentResolver.insert(
                            MediaStore.Downloads.EXTERNAL_CONTENT_URI,
                            contentValues
                        )

                        val pfd = contentResolver.openFileDescriptor(uri!!, "w")

                        outputFileDescriptor = pfd!!.fileDescriptor
                    }

                    return true
                }

                val baocunmulu = AppPreference.getInstance().getSettingsBaocunmulu()

                if (baocunmulu != "") {
                    val contentResolver = App.getInstance().contentResolver

                    val fileName = "/ScreenRecord_$timeStamp.mp4"

                    val treeDocId = DocumentsContract.getTreeDocumentId(baocunmulu.toUri())

                    val parentDocumentUri = DocumentsContract.buildDocumentUriUsingTree(
                        baocunmulu.toUri(),
                        treeDocId
                    )

                    try {
                        val videoUri = DocumentsContract.createDocument(
                            contentResolver,
                            parentDocumentUri,
                            "video/mp4",
                            fileName
                        ) ?: throw IllegalStateException()

                        outputFileDescriptor = contentResolver
                            .openFileDescriptor(videoUri, "rw")!!
                            .fileDescriptor
                    } catch (_: Exception) {
                        AppPreference.getInstance().setSettingsBaocunmulu("")

                        try {
                            LuzhiFragment.getInstance().updateUiButtonBaocunmulu()
                        } catch (_: Exception) {
                            //
                        }

                        if (!getDefaultFilePath()) {
                            return@Thread
                        }
                    }
                } else {
                    if (!getDefaultFilePath()) {
                        return@Thread
                    }
                }

                val isNeedAudio =
                    if (AppPreference.getInstance().getSettingsAudioRecordSwitch() == "2"
                        && ContextCompat.checkSelfPermission(
                            this@ScreenRecorderService,
                            Manifest.permission.RECORD_AUDIO
                        ) == PackageManager.PERMISSION_GRANTED
                    ) {
                        2
                    } else if (AppPreference.getInstance().getSettingsAudioRecordSwitch() == "1") {
                        1
                    } else {
                        0
                    }


                mediaRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    MediaRecorder(this)
                } else {
                    @Suppress("DEPRECATION")
                    MediaRecorder()
                }.apply {
                    setVideoSource(MediaRecorder.VideoSource.SURFACE)

                    if (isNeedAudio == 2) {
                        setAudioSource(MediaRecorder.AudioSource.MIC)
                    }

                    setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)

                    setVideoEncoder(MediaRecorder.VideoEncoder.H264)

                    if (isNeedAudio == 2) {
                        setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                        setAudioEncodingBitRate(256000)
                        setAudioSamplingRate(44100)
                    }

                    setVideoEncodingBitRate(
                        (AppPreference.getInstance().getSettingsLupinghuazhi() + "000000").toInt()
                    )

                    setVideoFrameRate(
                        AppPreference.getInstance().getSettingsLupingzhenshu().toInt()
                    )

                    val (width, height) = adjustToEvenSize(displayWidth, displayHeight)

                    setVideoSize(width, height)

                    setOrientationHint(0)

                    if (outputFileDescriptor != null) {
                        setOutputFile(outputFileDescriptor)
                    } else {
                        setOutputFile(outputFilePath)
                    }

                    try {
                        prepare()
                    } catch (e: Exception) {
                        Handler(Looper.getMainLooper()).post { onComplete(false) }
                        throw e
                    }

                    Handler(Looper.getMainLooper()).post { onComplete(true) }
                }
            } catch (_: Exception) {
                Handler(Looper.getMainLooper()).post { onComplete(false) }
            }
        }.start()
    }

    private fun createVirtualDisplay(): Boolean {
        try {
            virtualDisplay = mediaProjection?.createVirtualDisplay(
                "ScreenRecorderDisplay",
                displayWidth,
                displayHeight,
                screenDensity,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR or
                        DisplayManager.VIRTUAL_DISPLAY_FLAG_PUBLIC,
                mediaRecorder?.surface,
                null,
                null
            )
        } catch (_: Exception) {
            //
        }

        return virtualDisplay == null
    }

    private fun getScreenMetrics(intent: Intent) {
        displayWidth = intent.getIntExtra(EXTRA_SCREEN_WIDTH, 0)
        displayHeight = intent.getIntExtra(EXTRA_SCREEN_HEIGHT, 0)
        screenDensity = intent.getIntExtra(EXTRA_SCREEN_DENSITY, 0)

        val fenbianlv = AppPreference.getInstance().getSettingsLupingfenbianlv().toInt()

        if (fenbianlv != 0) {
            if (displayWidth >= displayHeight) {
                val (value, _) = adjustToEvenSize(
                    (displayWidth / (displayHeight / fenbianlv).toDouble()).toInt(),
                    0
                )
                displayHeight = fenbianlv
                displayWidth = value
            } else {
                val (_, value) = adjustToEvenSize(
                    0,
                    (displayHeight / (displayWidth / fenbianlv).toDouble()).toInt(),
                )
                displayWidth = fenbianlv
                displayHeight = value
            }
        }

        val fangxiang = AppPreference.getInstance().getSettingsLupingfangxiang()

        if (fangxiang == "2") {
            val temp = displayHeight
            displayHeight = displayWidth
            displayWidth = temp
        }
    }

    private fun adjustToEvenSize(width: Int, height: Int): Pair<Int, Int> {
        val w = if (width % 2 == 0) width else width - 1
        val h = if (height % 2 == 0) height else height - 1
        return Pair(w, h)
    }
}