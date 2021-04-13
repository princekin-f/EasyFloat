package com.lzf.easyfloat.widget

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import com.lzf.easyfloat.interfaces.OnTouchRangeListener

/**
 * @author: liuzhenfeng
 * @date: 11/21/20  17:49
 * @Package: com.lzf.easyfloat.widget
 * @Description:
 */
class DefaultAddView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : BaseSwitchView(context, attrs, defStyleAttr) {

    private lateinit var paint: Paint
    private var path = Path()
    private var width = 0f
    private var height = 0f
    private var region = Region()
    private val totalRegion = Region()
    private var inRange = false
    private var zoomSize = 18f
    private var listener: OnTouchRangeListener? = null

    init {
        initPath()
        setWillNotDraw(false)
    }

    private fun initPath() {
        paint = Paint().apply {
            color = Color.parseColor("#AA000000")
            strokeWidth = 10f
            style = Paint.Style.FILL
            isAntiAlias = true
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        width = w.toFloat()
        height = h.toFloat()
    }

    override fun onDraw(canvas: Canvas?) {
        path.reset()
        if (inRange) {
            path.addCircle(width, height, minOf(width, height), Path.Direction.CW)
        } else {
            path.addCircle(width, height, minOf(width, height) - zoomSize, Path.Direction.CW)
            totalRegion.set(zoomSize.toInt(), zoomSize.toInt(), width.toInt(), height.toInt())
            region.setPath(path, totalRegion)
        }
        canvas?.drawPath(path, paint)
        super.onDraw(canvas)
    }

    override fun setTouchRangeListener(event: MotionEvent, listener: OnTouchRangeListener?) {
        this.listener = listener
        initTouchRange(event)
    }

    private fun initTouchRange(event: MotionEvent): Boolean {
        val location = IntArray(2)
        // 获取在整个屏幕内的绝对坐标
        getLocationOnScreen(location)
        val currentInRange = region.contains(
            event.rawX.toInt() - location[0], event.rawY.toInt() - location[1]
        )
        if (currentInRange != inRange) {
            inRange = currentInRange
            invalidate()
        }
        listener?.touchInRange(currentInRange, this)
        if (event.action == MotionEvent.ACTION_UP && currentInRange) {
            listener?.touchUpInRange()
        }
        return currentInRange
    }

}