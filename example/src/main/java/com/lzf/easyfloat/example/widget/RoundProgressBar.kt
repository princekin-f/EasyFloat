package com.lzf.easyfloat.example.widget

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import com.lzf.easyfloat.example.R
import com.lzf.easyfloat.utils.DisplayUtils
import kotlin.math.ceil
import kotlin.math.min

/**
 * @author: liuzhenfeng
 * @function: 圆形进度条
 * @date: 2019-06-04  09:31
 */
class RoundProgressBar(context: Context, attrs: AttributeSet? = null) : View(context, attrs) {

    // 内圆
    private var circlePaint = Paint()
    private var circleColor = ContextCompat.getColor(context,
        R.color.smallCircle
    )
    private var circleRadius = dp2px(20f)
    // 进度条
    private var progressPaint = Paint()
    private var progressColor = ContextCompat.getColor(context,
        R.color.colorPrimary
    )
    private var progressTextColor = ContextCompat.getColor(context,
        R.color.colorPrimary
    )
    private var progressRadius = dp2px(21f)
    private var progressWidth = dp2px(2f)
    // 进度条背景
    private var progressBgPaint = Paint()
    private var progressBgColor = ContextCompat.getColor(context,
        R.color.progressBgColor
    )
    // 进度条文字
    private var progressTextPaint = Paint()
    private var progressStr = ""
    // 圆心坐标
    private var circleX = 0f
    private var circleY = 0f
    // 字的宽高
    private var mTxtWidth: Float = 0.toFloat()
    private var mTxtHeight: Float = 0.toFloat()
    // 总进度
    private val mTotalProgress = 100
    // 当前进度
    private var mProgress = 0
    // 默认宽高
    private var mWidth = dp2px(60f).toInt()
    private var mHeight = dp2px(60f).toInt()

    init {
        View(context, attrs)
        // 获取自定义的属性
        initAttrs(context, attrs)
        initVariable()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        mWidth = getMySize(widthMeasureSpec, mWidth)
        mHeight = getMySize(heightMeasureSpec, mHeight)
    }

    private fun getMySize(measureSpec: Int, defaultSize: Int): Int {
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

    //属性
    private fun initAttrs(context: Context, attrs: AttributeSet?) {
        if (attrs == null) return
        val typeArray = context.theme.obtainStyledAttributes(
            attrs, R.styleable.TasksCompletedView, 0, 0
        )
        // 内圆
        circleColor = typeArray.getColor(R.styleable.TasksCompletedView_circleColor, circleColor)
        circleRadius = typeArray.getDimension(R.styleable.TasksCompletedView_radius, circleRadius)
        // 进度条
        progressWidth =
            typeArray.getDimension(R.styleable.TasksCompletedView_progressWidth, progressWidth)
        progressColor =
            typeArray.getColor(R.styleable.TasksCompletedView_progressColor, progressColor)
        progressTextColor =
            typeArray.getColor(R.styleable.TasksCompletedView_progressTextColor, progressTextColor)
        progressBgColor =
            typeArray.getColor(R.styleable.TasksCompletedView_progressBgColor, progressBgColor)
        progressRadius = circleRadius + progressWidth / 2
    }

    // 初始化画笔
    private fun initVariable() {
        // 内圆
        circlePaint.isAntiAlias = true
        circlePaint.color = circleColor
        circlePaint.style = Paint.Style.FILL
        circlePaint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_OVER)
        // 进度条背景
        progressBgPaint.isAntiAlias = true
        progressBgPaint.color = progressBgColor
        progressBgPaint.style = Paint.Style.STROKE
        progressBgPaint.strokeWidth = progressWidth
        // 进度条
        progressPaint.isAntiAlias = true
        progressPaint.color = progressColor
        progressPaint.style = Paint.Style.STROKE
        progressPaint.strokeWidth = progressWidth
        // 进度条文字
        progressTextPaint.isAntiAlias = true
        progressTextPaint.style = Paint.Style.FILL
        progressTextPaint.color = progressTextColor
        progressTextPaint.textSize = circleRadius * 0.7f
        progressTextPaint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_OVER)
        // 进行文字测量
        val fm = progressTextPaint.fontMetrics
        // 文字顶部 - 底部，然后进一法处理
        mTxtHeight = ceil((fm.descent - fm.ascent).toDouble()).toFloat()
    }

    //画图
    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas) {
        circleY = mHeight * 0.5f
        circleX = mWidth * 0.5f

        // 内圆
        canvas.drawCircle(circleX, circleY, circleRadius, circlePaint)

        // 进度条背景
        val rectF = RectF(
            circleX - progressRadius,
            circleY - progressRadius,
            progressRadius * 2 + (circleX - progressRadius),
            progressRadius * 2 + (circleY - progressRadius)
        )

        // 圆弧所在的椭圆对象、圆弧的起始角度、圆弧的角度、是否显示半径连线
        canvas.drawArc(rectF, 0f, 360f, false, progressBgPaint)

        // 进度条，矩形边框和背景是同一个，只是绘制角度范围不同
        canvas.drawArc(
            rectF,
            -90f,
            (mProgress * 360 / mTotalProgress).toFloat(),
            false,
            progressPaint
        )

        // 进度条文字
        mTxtWidth = progressTextPaint.measureText(progressStr, 0, progressStr.length)
        canvas.drawText(
            progressStr,
            circleX - mTxtWidth / 2,
            circleY + mTxtHeight * 0.3f,
            progressTextPaint
        )
    }

    // 设置进度
    fun setProgress(progress: Int) {
        mProgress = progress
        //重绘
        postInvalidate()
    }

    fun setProgress(progress: Int, progressStr: String) {
        mProgress = progress
        this.progressStr = progressStr
        postInvalidate()
    }

    fun getProgress(): Int = mProgress

    fun getProgressStr(): String = progressStr

    private fun dp2px(dpValue: Float): Float = DisplayUtils.dp2px(context, dpValue).toFloat()

}
