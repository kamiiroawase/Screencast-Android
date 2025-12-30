package com.github.kamiiroawase.screencast.activity

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Rect
import android.graphics.RectF
import android.os.Build
import android.os.Bundle
import android.util.AttributeSet
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.graphics.toColorInt
import com.github.kamiiroawase.screencast.R
import com.github.kamiiroawase.screencast.fragment.LuzhiFragment
import com.github.kamiiroawase.screencast.preference.AppPreference
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

class RegionSelectionActivity : AppCompatActivity() {
    private var selectedRect: Rect? = null

    private lateinit var overlayView: OverlayView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)

        WindowCompat.setDecorFitsSystemWindows(window, false)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.attributes.layoutInDisplayCutoutMode =
                WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_ALWAYS
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            window.attributes.layoutInDisplayCutoutMode =
                WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
        }

        WindowInsetsControllerCompat(window, window.decorView).let { controller ->
            controller.hide(WindowInsetsCompat.Type.systemBars())
            controller.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }

        val container = FrameLayout(this)

        overlayView = OverlayView(this)

        val confirmButton = createButton(getString(R.string.querenxuanzequyu)).apply {
            setBackgroundColor("#2196F3".toColorInt())
            setOnClickListener {
                selectedRect?.let { rect ->
                    if (rect.width() > overlayView.minSelectionSize &&
                        rect.height() > overlayView.minSelectionSize
                    ) {
                        AppPreference.getInstance().setSettingsLupingquyu(rect)

                        try {
                            LuzhiFragment.getInstance().updateUiButtonLupingquyu()
                        } catch (_: Exception) {
                            //
                        }

                        finish()
                    }
                }
            }
        }

        val confirmButtonParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.WRAP_CONTENT,
            FrameLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            gravity = Gravity.START or Gravity.BOTTOM
            leftMargin = (12 * resources.displayMetrics.density).toInt()
            bottomMargin = (24 * resources.displayMetrics.density).toInt()
        }

        val clearButton = createButton(getString(R.string.qingchuxuanzequyu)).apply {
            setBackgroundColor("#F44336".toColorInt())
            setOnClickListener {
                selectedRect?.let { rect ->
                    if (rect.width() > overlayView.minSelectionSize &&
                        rect.height() > overlayView.minSelectionSize
                    ) {
                        AppPreference.getInstance().setSettingsLupingquyu()

                        try {
                            LuzhiFragment.getInstance().updateUiButtonLupingquyu()
                        } catch (_: Exception) {
                            //
                        }

                        finish()
                    }
                }
            }
        }

        val clearButtonParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.WRAP_CONTENT,
            FrameLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            gravity = Gravity.END or Gravity.BOTTOM
            rightMargin = (12 * resources.displayMetrics.density).toInt()
            bottomMargin = (24 * resources.displayMetrics.density).toInt()
        }

        container.addView(overlayView)
        container.addView(clearButton, clearButtonParams)
        container.addView(confirmButton, confirmButtonParams)

        setContentView(container)

        overlayView.addOnLayoutChangeListener { _, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom ->
            overlayView.invalidate()
        }

        overlayView.onRegionSelected = { rect ->
            selectedRect = rect
        }
    }

    private fun createButton(text: String): AppCompatButton {
        return AppCompatButton(this).apply {
            this.text = text

            setTextColor(Color.WHITE)

            textSize = 14f

            val padding = (12 * resources.displayMetrics.density).toInt()

            setPadding(padding, padding, padding, padding)
        }
    }

    class OverlayView @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0
    ) : View(context, attrs, defStyleAttr) {
        private var selectionRect = RectF()
        private var touchMode = TouchMode.NONE

        private var lastTouchX = 0f
        private var lastTouchY = 0f
        private var resizeEdge = ResizeEdge.NONE

        private val initialSelectionRatio = 0.5f

        private val dimPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(150, 0, 0, 0)  // 增加不透明度，使按钮更明显
            style = Paint.Style.FILL
        }

        private val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLUE
            style = Paint.Style.STROKE
            strokeWidth = 4f
        }

        private val clearPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.TRANSPARENT
            xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
        }

        private val handlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            style = Paint.Style.FILL_AND_STROKE
            strokeWidth = 2f
        }

        private val handleStrokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLUE
            style = Paint.Style.STROKE
            strokeWidth = 2f
        }

        private val handleSize = 20f

        val minSelectionSize = 100f

        var onRegionSelected: ((Rect) -> Unit)? = null

        private enum class TouchMode {
            NONE,         // 无操作
            MOVE,         // 移动选择框
            RESIZE,       // 调整大小
            NEW_SELECTION // 创建新选择框
        }

        private enum class ResizeEdge {
            NONE,         // 无
            TOP_LEFT,     // 左上
            TOP,          // 上
            TOP_RIGHT,    // 右上
            RIGHT,        // 右
            BOTTOM_RIGHT, // 右下
            BOTTOM,       // 下
            BOTTOM_LEFT,  // 左下
            LEFT          // 左
        }

        override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
            super.onSizeChanged(w, h, oldw, oldh)

            val lupingquyu = AppPreference.getInstance().getSettingsLupingquyu()

            if (lupingquyu == null) {
                val centerX = w / 2f
                val centerY = h / 2f
                val width = w * initialSelectionRatio
                val height = h * initialSelectionRatio

                selectionRect.set(
                    centerX - width / 2,
                    centerY - height / 2,
                    centerX + width / 2,
                    centerY + height / 2
                )
            } else {
                selectionRect.set(
                    lupingquyu.left.toFloat(),
                    lupingquyu.top.toFloat(),
                    lupingquyu.right.toFloat(),
                    lupingquyu.bottom.toFloat()
                )
            }

            constrainSelectionToBounds()

            onRegionSelected?.invoke(
                Rect(
                    selectionRect.left.toInt(),
                    selectionRect.top.toInt(),
                    selectionRect.right.toInt(),
                    selectionRect.bottom.toInt()
                )
            )
        }

        override fun onDraw(canvas: Canvas) {
            super.onDraw(canvas)

            canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), dimPaint)

            canvas.drawRect(selectionRect, clearPaint)

            canvas.drawRect(selectionRect, borderPaint)

            drawResizeHandles(canvas)
        }

        private fun drawResizeHandles(canvas: Canvas) {
            val halfHandle = handleSize / 2

            val corners = listOf(
                selectionRect.left to selectionRect.top,        // 左上
                selectionRect.right to selectionRect.top,       // 右上
                selectionRect.left to selectionRect.bottom,     // 左下
                selectionRect.right to selectionRect.bottom     // 右下
            )

            val edges = listOf(
                selectionRect.left + (selectionRect.width() / 2) to selectionRect.top,      // 上
                selectionRect.right to selectionRect.top + (selectionRect.height() / 2),    // 右
                selectionRect.left + (selectionRect.width() / 2) to selectionRect.bottom,    // 下
                selectionRect.left to selectionRect.top + (selectionRect.height() / 2)       // 左
            )

            corners.forEach { (x, y) ->
                canvas.drawCircle(x, y, halfHandle, handlePaint)
                canvas.drawCircle(x, y, halfHandle, handleStrokePaint)
            }

            edges.forEach { (x, y) ->
                canvas.drawCircle(x, y, halfHandle, handlePaint)
                canvas.drawCircle(x, y, halfHandle, handleStrokePaint)
            }
        }

        @SuppressLint("ClickableViewAccessibility")
        override fun onTouchEvent(event: MotionEvent): Boolean {
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    lastTouchX = event.x
                    lastTouchY = event.y

                    resizeEdge = getResizeEdge(event.x, event.y)

                    touchMode = when {
                        isTouchOnSelection(event.x, event.y) && resizeEdge == ResizeEdge.NONE -> {
                            TouchMode.MOVE
                        }

                        resizeEdge != ResizeEdge.NONE -> {
                            TouchMode.RESIZE
                        }

                        else -> {
                            selectionRect.set(event.x, event.y, event.x, event.y)
                            TouchMode.NEW_SELECTION
                        }
                    }

                    return true
                }

                MotionEvent.ACTION_MOVE -> {
                    val dx = event.x - lastTouchX
                    val dy = event.y - lastTouchY

                    when (touchMode) {
                        TouchMode.MOVE -> {
                            selectionRect.offset(dx, dy)
                            constrainSelectionToBounds()
                        }

                        TouchMode.RESIZE -> {
                            resizeSelection(dx, dy)
                        }

                        TouchMode.NEW_SELECTION -> {
                            selectionRect.right = event.x
                            selectionRect.bottom = event.y
                            constrainSelectionToBounds()
                        }

                        else -> {}
                    }

                    lastTouchX = event.x
                    lastTouchY = event.y
                    invalidate()

                    onRegionSelected?.invoke(
                        Rect(
                            selectionRect.left.toInt(),
                            selectionRect.top.toInt(),
                            selectionRect.right.toInt(),
                            selectionRect.bottom.toInt()
                        )
                    )

                    return true
                }

                MotionEvent.ACTION_UP -> {
                    touchMode = TouchMode.NONE
                    resizeEdge = ResizeEdge.NONE

                    if (selectionRect.width() > minSelectionSize &&
                        selectionRect.height() > minSelectionSize
                    ) {
                        onRegionSelected?.invoke(
                            Rect(
                                selectionRect.left.toInt(),
                                selectionRect.top.toInt(),
                                selectionRect.right.toInt(),
                                selectionRect.bottom.toInt()
                            )
                        )
                    }
                    return true
                }

                MotionEvent.ACTION_CANCEL -> {
                    touchMode = TouchMode.NONE
                    resizeEdge = ResizeEdge.NONE
                    return true
                }
            }

            return super.onTouchEvent(event)
        }

        private fun isTouchOnSelection(x: Float, y: Float): Boolean {
            return selectionRect.contains(x, y)
        }

        private fun getResizeEdge(x: Float, y: Float): ResizeEdge {
            val tolerance = handleSize * 2

            if (abs(x - selectionRect.left) < tolerance && abs(y - selectionRect.top) < tolerance) {
                return ResizeEdge.TOP_LEFT
            }
            if (abs(x - selectionRect.right) < tolerance && abs(y - selectionRect.top) < tolerance) {
                return ResizeEdge.TOP_RIGHT
            }
            if (abs(x - selectionRect.left) < tolerance && abs(y - selectionRect.bottom) < tolerance) {
                return ResizeEdge.BOTTOM_LEFT
            }
            if (abs(x - selectionRect.right) < tolerance && abs(y - selectionRect.bottom) < tolerance) {
                return ResizeEdge.BOTTOM_RIGHT
            }

            if (abs(y - selectionRect.top) < tolerance &&
                x > selectionRect.left + tolerance &&
                x < selectionRect.right - tolerance
            ) {
                return ResizeEdge.TOP
            }
            if (abs(x - selectionRect.right) < tolerance &&
                y > selectionRect.top + tolerance &&
                y < selectionRect.bottom - tolerance
            ) {
                return ResizeEdge.RIGHT
            }
            if (abs(y - selectionRect.bottom) < tolerance &&
                x > selectionRect.left + tolerance &&
                x < selectionRect.right - tolerance
            ) {
                return ResizeEdge.BOTTOM
            }
            if (abs(x - selectionRect.left) < tolerance &&
                y > selectionRect.top + tolerance &&
                y < selectionRect.bottom - tolerance
            ) {
                return ResizeEdge.LEFT
            }

            return ResizeEdge.NONE
        }

        private fun resizeSelection(dx: Float, dy: Float) {
            val newRect = RectF(selectionRect)

            when (resizeEdge) {
                ResizeEdge.TOP_LEFT -> {
                    newRect.left += dx
                    newRect.top += dy
                }

                ResizeEdge.TOP -> {
                    newRect.top += dy
                }

                ResizeEdge.TOP_RIGHT -> {
                    newRect.right += dx
                    newRect.top += dy
                }

                ResizeEdge.RIGHT -> {
                    newRect.right += dx
                }

                ResizeEdge.BOTTOM_RIGHT -> {
                    newRect.right += dx
                    newRect.bottom += dy
                }

                ResizeEdge.BOTTOM -> {
                    newRect.bottom += dy
                }

                ResizeEdge.BOTTOM_LEFT -> {
                    newRect.left += dx
                    newRect.bottom += dy
                }

                ResizeEdge.LEFT -> {
                    newRect.left += dx
                }

                else -> return
            }

            if (newRect.width() >= minSelectionSize && newRect.height() >= minSelectionSize) {
                if (newRect.left >= 0 && newRect.top >= 0 &&
                    newRect.right <= width && newRect.bottom <= height
                ) {
                    when (resizeEdge) {
                        ResizeEdge.TOP_LEFT, ResizeEdge.LEFT -> {
                            if (newRect.left >= newRect.right) return
                        }

                        ResizeEdge.TOP -> {
                            if (newRect.top >= newRect.bottom) return
                        }

                        ResizeEdge.TOP_RIGHT, ResizeEdge.RIGHT -> {
                            if (newRect.right <= newRect.left) return
                        }

                        ResizeEdge.BOTTOM_RIGHT, ResizeEdge.BOTTOM -> {
                            if (newRect.bottom <= newRect.top) return
                        }

                        ResizeEdge.BOTTOM_LEFT -> {
                            if (newRect.left >= newRect.right || newRect.bottom <= newRect.top) return
                        }

                        else -> {}
                    }

                    selectionRect.set(newRect)
                }
            }
        }

        private fun constrainSelectionToBounds() {
            val left = max(0f, min(selectionRect.left, width.toFloat() - minSelectionSize))
            val top = max(0f, min(selectionRect.top, height.toFloat() - minSelectionSize))
            val right = min(width.toFloat(), max(selectionRect.right, minSelectionSize))
            val bottom = min(height.toFloat(), max(selectionRect.bottom, minSelectionSize))

            val width = right - left
            val height = bottom - top

            if (width >= minSelectionSize && height >= minSelectionSize) {
                selectionRect.set(left, top, right, bottom)
            }
        }
    }
}