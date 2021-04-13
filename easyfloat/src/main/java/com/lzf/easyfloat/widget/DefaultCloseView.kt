package com.lzf.easyfloat.widget

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import com.lzf.easyfloat.R
import com.lzf.easyfloat.interfaces.OnTouchRangeListener
import com.lzf.easyfloat.utils.DisplayUtils

/**
 * @author: liuzhenfeng
 * @date: 2020/10/25  11:16
 * @Package: com.lzf.easyfloat.widget
 * @Description:
 */
class DefaultCloseView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : BaseSwitchView(context, attrs, defStyleAttr) {

    private var normalColor = Color.parseColor("#99000000")
    private var inRangeColor = Color.parseColor("#99FF0000")
    private var shapeType = 0

    private lateinit var paint: Paint
    private var path = Path()
    private var width = 0f
    private var height = 0f
    private var rectF = RectF()
    private var region = Region()
    private val totalRegion = Region()
    private var inRange = false
    private var zoomSize = DisplayUtils.dp2px(context, 4f).toFloat()
    private var listener: OnTouchRangeListener? = null

    init {
        attrs?.apply { initAttrs(this) }
        initPaint()
        setWillNotDraw(false)
    }

    private fun initAttrs(attrs: AttributeSet) =
        context.theme.obtainStyledAttributes(attrs, R.styleable.DefaultCloseView, 0, 0).apply {
            normalColor = getColor(R.styleable.DefaultCloseView_normalColor, normalColor)
            inRangeColor = getColor(R.styleable.DefaultCloseView_inRangeColor, inRangeColor)
            shapeType = getInt(R.styleable.DefaultCloseView_shapeType, shapeType)
            zoomSize = getDimension(R.styleable.DefaultCloseView_zoomSize, zoomSize)
        }.recycle()


    private fun initPaint() {
        paint = Paint().apply {
            color = normalColor
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
            paint.color = inRangeColor
            when (shapeType) {
                // 半椭圆
                0 -> {
                    rectF.set(paddingLeft.toFloat(), 0f, width - paddingRight, height * 2)
                    path.addOval(rectF, Path.Direction.CW)
                }
                // 矩形
                1 -> {
                    rectF.set(paddingLeft.toFloat(), 0f, width - paddingRight, height)
                    path.addRect(rectF, Path.Direction.CW)
                }
                // 半圆
                2 -> path.addCircle(width / 2, height, height, Path.Direction.CW)
            }
        } else {
            paint.color = normalColor
            when (shapeType) {
                // 半椭圆
                0 -> {
                    rectF.set(
                        paddingLeft + zoomSize,
                        zoomSize,
                        width - paddingRight - zoomSize,
                        (height - zoomSize) * 2
                    )
                    path.addOval(rectF, Path.Direction.CW)
                    totalRegion.set(
                        paddingLeft + zoomSize.toInt(),
                        zoomSize.toInt(),
                        (width - paddingRight - zoomSize).toInt(),
                        height.toInt()
                    )
                }
                // 矩形
                1 -> {
                    rectF.set(
                        paddingLeft.toFloat(),
                        zoomSize,
                        width - paddingRight,
                        height
                    )
                    path.addRect(rectF, Path.Direction.CW)
                    totalRegion.set(
                        paddingLeft,
                        zoomSize.toInt(),
                        width.toInt() - paddingRight,
                        height.toInt()
                    )
                }
                // 半圆
                2 -> {
                    path.addCircle(width / 2, height, height - zoomSize, Path.Direction.CW)
                    totalRegion.set(0, zoomSize.toInt(), width.toInt(), height.toInt())
                }
            }
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