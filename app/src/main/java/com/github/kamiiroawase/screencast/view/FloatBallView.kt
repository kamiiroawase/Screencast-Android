package com.github.kamiiroawase.screencast.view

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import kotlin.math.abs
import androidx.core.graphics.toColorInt
import com.github.kamiiroawase.screencast.App
import com.github.kamiiroawase.screencast.activity.MainActivity
import com.github.kamiiroawase.screencast.fragment.LuzhiFragment
import com.github.kamiiroawase.screencast.preference.AppPreference
import com.github.kamiiroawase.screencast.service.ScreenRecorderService

class FloatBallView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    private val paint1 = Paint(Paint.ANTI_ALIAS_FLAG)
    private val paint2 = Paint(Paint.ANTI_ALIAS_FLAG)
    private val paint3 = Paint(Paint.ANTI_ALIAS_FLAG)
    private val paint4 = Paint(Paint.ANTI_ALIAS_FLAG)

    private var centerX = 0f
    private var centerY = 0f
    private var radius = 0f

    private var lastX = 0f
    private var lastY = 0f
    private var isDragging = false

    private var ballColor = "#00FFFFFF".toColorInt()
    private var ringColor = "#00FF0000".toColorInt()
    private var buttonStopColor = "#00FF0000".toColorInt()
    private var buttonStartColor = "#00FF0000".toColorInt()

    private val tempRect = RectF()

    var onBallClickListener: (() -> Unit)? = null
    var onBallMovedListener: ((x: Float, y: Float) -> Unit)? = null

    init {
        paint1.style = Paint.Style.FILL
        paint2.style = Paint.Style.FILL
        paint3.style = Paint.Style.FILL
        paint4.style = Paint.Style.FILL
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        centerX = w / 2f
        centerY = h / 2f
        radius = (w.coerceAtMost(h) / 2f) * 0.9f
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        paint1.color = ballColor
        canvas.drawCircle(centerX, centerY, radius, paint1)

        paint2.color = ringColor
        paint2.strokeWidth = radius * 0.1f
        paint2.style = Paint.Style.STROKE

        canvas.drawCircle(centerX, centerY, radius * 1f, paint2)

        paint3.color = buttonStartColor

        canvas.drawCircle(centerX, centerY, radius * 0.8f, paint3)

        paint4.color = buttonStopColor

        val stopRectSize = radius * 0.5f

        tempRect.set(
            centerX - stopRectSize,
            centerY - stopRectSize,
            centerX + stopRectSize,
            centerY + stopRectSize
        )

        val cornerRadius = radius * 0.1f

        canvas.drawRoundRect(tempRect, cornerRadius, cornerRadius, paint4)

        if (ScreenRecorderService.isRecording) {
            setButtonStart()
        } else {
            setButtonStop()
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        val x = event.rawX
        val y = event.rawY

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                lastX = x
                lastY = y
                isDragging = false
                return true
            }

            MotionEvent.ACTION_MOVE -> {
                val dx = x - lastX
                val dy = y - lastY

                if (!isDragging && (abs(dx) > 10 || abs(dy) > 10)) {
                    isDragging = true
                }

                if (isDragging) {
                    onBallMovedListener?.invoke(x - centerX, y - centerY)
                }
            }

            MotionEvent.ACTION_UP -> {
                if (!isDragging) {
                    onBallClickListener?.invoke()
                    if (ScreenRecorderService.isRecording) {
                        if (ScreenRecorderService.isPaused) {
                            try {
                                LuzhiFragment.getInstance().stopScreenRecording()
                                setButtonStart()
                            } catch (_: Exception) {
                                //
                            }
                        } else {
                            try {
                                LuzhiFragment.getInstance().stopScreenRecording()
                                setButtonStop()
                            } catch (_: Exception) {
                                //
                            }
                        }
                    } else {
                        try {
                            context.startActivity(Intent(App.getInstance(), MainActivity::class.java).apply {
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                                        Intent.FLAG_ACTIVITY_CLEAR_TOP
                            })

                            LuzhiFragment.getInstance().screenCapturePreStart({
                                setButtonStart()
                            })
                        } catch (_: Exception) {
                            //
                        }
                    }
                } else {
                    onDragEnd()
                }
            }

            MotionEvent.ACTION_CANCEL -> {
                onDragEnd()
            }
        }
        return super.onTouchEvent(event)
    }

    fun setButtonStart() {
        buttonStartColor = "#FFFFFFFF".toColorInt()
        buttonStopColor = "#FFFF0000".toColorInt()
        ringColor = "#FFFF0000".toColorInt()
        ballColor = "#FFFFFFFF".toColorInt()
        invalidate()
    }

    fun setButtonStop() {
        buttonStartColor = "#FFFF0000".toColorInt()
        buttonStopColor = "#FFFF0000".toColorInt()
        ringColor = "#FFFF0000".toColorInt()
        ballColor = "#FFFFFFFF".toColorInt()
        invalidate()
    }

    fun setButtonPause() {
        buttonStartColor = "#FFFF0000".toColorInt()
        buttonStopColor = "#FFFF0000".toColorInt()
        ringColor = "#FFFF0000".toColorInt()
        ballColor = "#FFFFFFFF".toColorInt()
        invalidate()
    }

    private fun onDragEnd() {
        isDragging = false
        AppPreference.getInstance().setSettingsXuanfuqiuLocation(lastX, lastY)
    }
}