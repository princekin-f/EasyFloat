package com.lzf.easyfloat.example.widget

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.SurfaceView
import androidx.core.content.ContextCompat
import com.lzf.easyfloat.example.R
import kotlinx.coroutines.*

/**
 * @Description: Copy https://github.com/longway777/CustomViews
 */
class BubbleSurfaceView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : SurfaceView(context, attrs, defStyleAttr), CoroutineScope by MainScope() {

    private var isRunning = false

    var deviation = 50f
        set(value) {
            field = value
            paint.pathEffect =
                ComposePathEffect(CornerPathEffect(50f), DiscretePathEffect(30f, deviation / 2))
        }

    private val paintColors = arrayOf(
        Color.CYAN,
        Color.RED,
        Color.GREEN,
        Color.MAGENTA,
        Color.YELLOW,
        Color.WHITE,
        Color.GRAY
    )

    private var drawingCirclesList = mutableListOf<DrawingCircle>()
    private var job: Job? = null
    private var touchX = 0f
    private var touchY = 0f

    private val paint = Paint().apply {
        style = Paint.Style.STROKE
        strokeWidth = 10f
    }

    private data class DrawingCircle(val x: Float, val y: Float, val color: Int, var radius: Float)

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        performClick()
        touchX = (event?.x) ?: 0f
        touchY = (event?.y) ?: 0f
        val drawingCircle = DrawingCircle(touchX, touchY, paintColors.random(), 1f)
        drawingCirclesList.add(drawingCircle)
        if (drawingCirclesList.size > 20) drawingCirclesList.removeAt(0)
        return super.onTouchEvent(event)
    }

    private fun createJob() {
        job?.cancel()
        job = launch(Dispatchers.Default) {
            while (isRunning) {
                if (holder.surface.isValid) {
                    val canvas = holder.lockCanvas()
                    canvas?.drawColor(ContextCompat.getColor(context, R.color.light_gray))
                    drawingCirclesList.toList().filter { it.radius < 3000 }.forEach {
                        paint.color = it.color
                        canvas?.drawCircle(it.x, it.y, it.radius, paint)
                        it.radius += 10f
                    }
                    if (holder.surface.isValid)
                        holder?.unlockCanvasAndPost(canvas)
                }
            }
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
            isRunning = true
            createJob()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        isRunning = false
        job?.cancel()
    }

}