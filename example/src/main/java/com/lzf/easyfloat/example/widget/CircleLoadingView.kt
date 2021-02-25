package com.lzf.easyfloat.example.widget

import android.animation.Animator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import android.view.animation.LinearInterpolator
import com.lzf.easyfloat.example.R
import com.lzf.easyfloat.utils.DisplayUtils
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

/**
 * @author: liuzhenfeng
 * @date: 12/24/20  12:20
 * @Package: com.lzf.easyfloat.example.widget
 * @Description:
 */
class CircleLoadingView @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    // 环形圆点的相关属性
    private var dotRadius = 0f
    private var dotPI = 0f
    private var dotRotatePI = 0f
    private var dotRotateStandard = 0f
    private var dotRealRotatePI = 0f
    private lateinit var dotPaint: Paint

    // 圆弧的相关属性
    private var arcRadius = 0f
    private var startAngle = -30f
    private var sweepAngle = 240f
    private lateinit var arcPaint: Paint
    private lateinit var rectF: RectF

    // 可自定义的属性
    private var arcWidth = dp2px(context, 2f)
    private var loadingColor = Color.WHITE
    private var dotSize = 16
    private var durationTime = 1500L
    // 圆点每周期旋转角度
    private var dotAngle = 90f

    private var centX = 0
    private var centY = 0
    private var animatorValue = 0f
    private var animator: ValueAnimator? = null

    init {
        attrs?.apply { initAttrs(this) }
        initValue()
        initPaint()
    }

    private fun initAttrs(attrs: AttributeSet) =
            context.theme.obtainStyledAttributes(attrs, R.styleable.CircleLoadingView, 0, 0).apply {
                arcWidth = getDimension(R.styleable.CircleLoadingView_arcWidth, arcWidth)
                loadingColor = getColor(R.styleable.CircleLoadingView_loadingColor, loadingColor)
                dotSize = getInt(R.styleable.CircleLoadingView_dotSize, dotSize)
                durationTime =
                        getFloat(R.styleable.CircleLoadingView_durationTime, durationTime.toFloat()).toLong()
                dotAngle = getFloat(R.styleable.CircleLoadingView_dotAngle, dotAngle)
            }.recycle()

    private fun initValue() {
        dotPI = Math.PI.toFloat() * 2 / dotSize
        dotRadius = arcWidth * 0.5f
    }

    private fun initPaint() {
        dotPaint = Paint().apply {
            color = loadingColor
            style = Paint.Style.FILL
            isAntiAlias = true
        }

        arcPaint = Paint().apply {
            color = loadingColor
            style = Paint.Style.STROKE
            strokeCap = Paint.Cap.ROUND
            strokeWidth = arcWidth
            isAntiAlias = true
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        initAnimator()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        animator?.cancel()
    }

    private fun initAnimator() {
        animator = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = durationTime
            // 匀速运动，圆弧单独计算插值器
            interpolator = LinearInterpolator()
            repeatCount = -1
            addUpdateListener { animation ->
                animatorValue = animation.animatedValue as Float
                // 计算环形圆点的旋转角度，再转成圆周率格式
                dotRotatePI =
                        (dotRotateStandard + dotAngle * animatorValue) / 180f * Math.PI.toFloat()
                // 为圆弧设置插值器，计算逻辑和设置动画插值器相同，这里为了能和环形圆点共用一个动画，计算移到内部
                animatorValue = getInterpolation(animatorValue) * 120f
                if (animatorValue <= 60f) {
                    startAngle = -30f + animatorValue * 5
                    sweepAngle = 240f - animatorValue * 4
                } else {
                    startAngle = animatorValue - 150f
                    sweepAngle = (animatorValue - 60f) * 4
                }
                invalidate()
            }
            addListener(object : Animator.AnimatorListener {
                override fun onAnimationRepeat(p0: Animator?) {
                    dotRotateStandard += dotAngle
                    if (dotRotateStandard >= 360f) dotRotateStandard -= 360f
                }

                override fun onAnimationEnd(p0: Animator?) {}
                override fun onAnimationCancel(p0: Animator?) {}
                override fun onAnimationStart(p0: Animator?) {}
            })
            start()
        }
    }

    private fun getInterpolation(t: Float) = if (t > 0.5f) 1 - 2 * (1 - t) * t else 2 * (1 - t) * t

    @SuppressLint("DrawAllocation")
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        var width = getMeasureSize(widthMeasureSpec, dp2pxInt(context, 120f))
        var height = getMeasureSize(heightMeasureSpec, dp2pxInt(context, 120f))
        setMeasuredDimension(width, height)

        width = width - paddingLeft - paddingRight
        height = height - paddingTop - paddingBottom
        centX = width.shr(1) + paddingLeft
        centY = height.shr(1) + paddingTop
        arcRadius = (min(width, height) - arcWidth) * 0.5f
        rectF = RectF(centX - arcRadius, centY - arcRadius, centX + arcRadius, centY + arcRadius)
    }

    private fun getMeasureSize(measureSpec: Int, defaultSize: Int): Int {
        val specMode = MeasureSpec.getMode(measureSpec)
        val specSize = MeasureSpec.getSize(measureSpec)
        return when (specMode) {
            // 确切大小，所以将得到的尺寸给view
            MeasureSpec.EXACTLY -> specSize
            // 默认值为 xxx px，此处要结合父控件给子控件的最多大小(要不然会填充父控件)，所以采用最小值
            MeasureSpec.AT_MOST -> min(defaultSize, specSize)
            else -> defaultSize
        }
    }

    override fun onDraw(canvas: Canvas?) {
        canvas?.apply {
            for (i in 1..dotSize) {
                dotRealRotatePI = i * dotPI + dotRotatePI
                drawCircle(
                        arcRadius * cos(dotRealRotatePI) + centX,
                        arcRadius * sin(dotRealRotatePI) + centY,
                        dotRadius, dotPaint
                )
            }
            drawArc(rectF, startAngle, sweepAngle, false, arcPaint)
        }
    }

}

fun dp2px(context: Context, dpVal: Float) = DisplayUtils.dp2px(context, dpVal).toFloat()

fun dp2pxInt(context: Context, dpVal: Float): Int = DisplayUtils.dp2px(context, dpVal)
