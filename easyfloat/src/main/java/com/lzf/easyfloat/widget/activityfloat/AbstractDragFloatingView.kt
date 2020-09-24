package com.lzf.easyfloat.widget.activityfloat

import android.animation.Animator
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Rect
import android.util.AttributeSet
import android.view.*
import android.widget.FrameLayout
import com.lzf.easyfloat.anim.AnimatorManager
import com.lzf.easyfloat.data.FloatConfig
import com.lzf.easyfloat.enums.SidePattern
import com.lzf.easyfloat.utils.Logger
import kotlin.math.min

/**
 * @author: liuzhenfeng
 * @function: 拖拽控件的抽象类
 * @date: 2019-06-21  10:40
 */
abstract class AbstractDragFloatingView(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    abstract fun getLayoutId(): Int?

    abstract fun renderView(view: View)

    // 浮窗配置
    var config: FloatConfig

    // 悬浮的父布局高度、宽度
    private var parentHeight = 0
    private var parentWidth = 0

    // 终点坐标
    private var lastX = 0
    private var lastY = 0

    // 浮窗各边距离父布局的距离
    private var leftDistance = 0
    private var rightDistance = 0
    private var topDistance = 0
    private var bottomDistance = 0
    private var minX = 0
    private var minY = 0
    private var parentRect = Rect()
    private var floatRect = Rect()
    private var parentView: ViewGroup? = null
    private var isCreated = false

    init {
        FrameLayout(context, attrs, defStyleAttr)
        config = FloatConfig()
        initView(context)
        // 设置空点击事件，用于接收触摸事件
        setOnClickListener { }
    }

    protected fun initView(context: Context) {
        if (getLayoutId() != null) {
            val view: View = LayoutInflater.from(context).inflate(getLayoutId()!!, this)
            this.renderView(view)
            config.invokeView?.invoke(this)
        }
    }

    @SuppressLint("DrawAllocation")
    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        super.onLayout(changed, l, t, r, b)

        // 初次显示，设置默认坐标、入场动画
        if (!isCreated) {
            isCreated = true
            // 有固定坐标使用固定坐标，没有固定坐标设置偏移量
            if (config.locationPair != Pair(0, 0)) {
                x = config.locationPair.first.toFloat()
                y = config.locationPair.second.toFloat()
            } else {
                x += config.offsetPair.first
                y += config.offsetPair.second
            }

            initParent()
            initDistanceValue()
            enterAnim()
        }
    }

    private fun initParent() {
        if (parent != null && parent is ViewGroup) {
            parentView = parent as ViewGroup
            parentHeight = parentView!!.height
            parentWidth = parentView!!.width
            parentView!!.getGlobalVisibleRect(parentRect)
            Logger.e("parentRect: $parentRect")
        }
    }

    override fun onInterceptTouchEvent(event: MotionEvent?): Boolean {
        if (event != null) updateView(event)
        // 是拖拽事件就进行拦截，反之不拦截
        // ps：拦截后将不再回调该方法，所以后续事件需要在onTouchEvent中回调
        return config.isDrag || super.onInterceptTouchEvent(event)
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        // updateView(event)是拖拽功能的具体实现
        if (event != null) updateView(event)
        // 如果是拖拽，这消费此事件，否则返回默认情况，防止影响子View事件的消费
        return config.isDrag || super.onTouchEvent(event)
    }

    /**
     * 更新位置信息
     */
    private fun updateView(event: MotionEvent) {
        config.callbacks?.touchEvent(this, event)
        config.floatCallbacks?.builder?.touchEvent?.invoke(this, event)
        // 关闭拖拽/执行动画阶段，不可拖动
        if (!config.dragEnable || config.isAnim) {
            config.isDrag = false
            isPressed = true
            return
        }

        val rawX = event.rawX.toInt()
        val rawY = event.rawY.toInt()
        when (event.action and MotionEvent.ACTION_MASK) {
            MotionEvent.ACTION_DOWN -> {
                // 默认是点击事件，而非拖拽事件
                config.isDrag = false
                isPressed = true
                lastX = rawX
                lastY = rawY
                // 父布局不要拦截子布局的监听
                parent.requestDisallowInterceptTouchEvent(true)
                initParent()
            }

            MotionEvent.ACTION_MOVE -> {
                // 只有父布局存在才可以拖动
                if (parentHeight <= 0 || parentWidth <= 0) return

                val dx = rawX - lastX
                val dy = rawY - lastY
                // 忽略过小的移动，防止点击无效
                if (!config.isDrag && dx * dx + dy * dy < 81) return
                config.isDrag = true

                var tempX = x + dx
                var tempY = y + dy
                // 检测是否到达边缘
                tempX = when {
                    tempX < 0 -> 0f
                    tempX > parentWidth - width -> parentWidth - width.toFloat()
                    else -> tempX
                }
                tempY = when {
                    tempY < 0 -> 0f
                    tempY > parentHeight - height -> parentHeight - height.toFloat()
                    else -> tempY
                }

                when (config.sidePattern) {
                    SidePattern.LEFT -> tempX = 0f
                    SidePattern.RIGHT -> tempX = parentRect.right - width.toFloat()
                    SidePattern.TOP -> tempY = 0f
                    SidePattern.BOTTOM -> tempY = parentRect.bottom - height.toFloat()

                    SidePattern.AUTO_HORIZONTAL ->
                        tempX = if (rawX * 2 - parentRect.left > parentRect.right)
                            parentRect.right - width.toFloat() else 0f

                    SidePattern.AUTO_VERTICAL ->
                        tempY = if (rawY - parentRect.top > parentRect.bottom - rawY)
                            parentRect.bottom - height.toFloat() else 0f

                    SidePattern.AUTO_SIDE -> {
                        leftDistance = rawX - parentRect.left
                        rightDistance = parentRect.right - rawX
                        topDistance = rawY - parentRect.top
                        bottomDistance = parentRect.bottom - rawY

                        minX = min(leftDistance, rightDistance)
                        minY = min(topDistance, bottomDistance)

                        val pair = sideForLatest(tempX, tempY)
                        tempX = pair.first
                        tempY = pair.second
                    }
                    else -> Unit
                }
                // 更新位置
                x = tempX
                y = tempY
                lastX = rawX
                lastY = rawY
                config.callbacks?.drag(this, event)
                config.floatCallbacks?.builder?.drag?.invoke(this, event)
            }

            MotionEvent.ACTION_UP -> {
                // 如果是拖动状态下即非点击按压事件
                isPressed = !config.isDrag

                if (config.isDrag) {
                    when (config.sidePattern) {
                        SidePattern.RESULT_LEFT,
                        SidePattern.RESULT_RIGHT,
                        SidePattern.RESULT_TOP,
                        SidePattern.RESULT_BOTTOM,
                        SidePattern.RESULT_HORIZONTAL,
                        SidePattern.RESULT_VERTICAL,
                        SidePattern.RESULT_SIDE -> sideAnim()
                        else -> touchOver()
                    }
                }
            }

            else -> return
        }
    }

    /**
     * 拖拽结束或者吸附动画执行结束，更新配置
     */
    private fun touchOver() {
        config.isAnim = false
        config.isDrag = false
        config.callbacks?.dragEnd(this)
        config.floatCallbacks?.builder?.dragEnd?.invoke(this)
    }

    /**
     * 拖拽结束，吸附屏幕边缘
     */
    private fun sideAnim() {
        // 计算一些数据
        initDistanceValue()
        var animType = "translationX"
        var startValue = 0f
        val endValue: Float = when (config.sidePattern) {
            SidePattern.RESULT_LEFT -> {
                animType = "translationX"
                startValue = translationX
                -leftDistance + translationX
            }
            SidePattern.RESULT_RIGHT -> {
                animType = "translationX"
                startValue = translationX
                rightDistance + translationX
            }
            SidePattern.RESULT_HORIZONTAL -> {
                animType = "translationX"
                startValue = translationX
                if (leftDistance < rightDistance) -leftDistance + translationX else rightDistance + translationX
            }

            SidePattern.RESULT_TOP -> {
                animType = "translationY"
                startValue = translationY
                -topDistance + translationY
            }
            SidePattern.RESULT_BOTTOM -> {
                animType = "translationY"
                startValue = translationY
                bottomDistance + translationY
            }
            SidePattern.RESULT_VERTICAL -> {
                animType = "translationY"
                startValue = translationY
                if (topDistance < bottomDistance) -topDistance + translationY else bottomDistance + translationY
            }

            SidePattern.RESULT_SIDE -> {
                if (minX < minY) {
                    animType = "translationX"
                    startValue = translationX
                    if (leftDistance < rightDistance) -leftDistance + translationX else rightDistance + translationX
                } else {
                    animType = "translationY"
                    startValue = translationY
                    if (topDistance < bottomDistance) -topDistance + translationY else bottomDistance + translationY
                }
            }
            else -> 0f
        }

        val animator = ObjectAnimator.ofFloat(this, animType, startValue, endValue)
        animator.addListener(object : Animator.AnimatorListener {
            override fun onAnimationRepeat(animation: Animator?) {}

            override fun onAnimationEnd(animation: Animator?) {
                touchOver()
            }

            override fun onAnimationCancel(animation: Animator?) {}

            override fun onAnimationStart(animation: Animator?) {
                config.isAnim = true
            }
        })
        animator.start()
    }

    /**
     * 吸附在距离最近的那个边
     */
    private fun sideForLatest(x: Float, y: Float): Pair<Float, Float> {
        var x1 = x
        var y1 = y
        if (minX < minY) {
            x1 = if (leftDistance == minX) 0f else parentWidth - width.toFloat()
        } else {
            y1 = if (topDistance == minY) 0f else parentHeight - height.toFloat()
        }
        return Pair(x1, y1)
    }

    /**
     * 计算一些边界距离数据
     */
    private fun initDistanceValue() {
        // 获取 floatingView 所显示的矩形
        getGlobalVisibleRect(floatRect)

        leftDistance = floatRect.left - parentRect.left
        rightDistance = parentRect.right - floatRect.right
        topDistance = floatRect.top - parentRect.top
        bottomDistance = parentRect.bottom - floatRect.bottom

        minX = min(leftDistance, rightDistance)
        minY = min(topDistance, bottomDistance)
        Logger.i("$leftDistance   $rightDistance   $topDistance   $bottomDistance")
    }

    /**
     * 入场动画
     */
    private fun enterAnim() {
        if (parentView == null) return
        val manager: AnimatorManager? =
            AnimatorManager(config.floatAnimator, this, parentView!!, config.sidePattern)
        val animator: Animator? = manager?.enterAnim()
        animator?.addListener(object : Animator.AnimatorListener {
            override fun onAnimationRepeat(animation: Animator?) {}

            override fun onAnimationEnd(animation: Animator?) {
                config.isAnim = false
            }

            override fun onAnimationCancel(animation: Animator?) {}

            override fun onAnimationStart(animation: Animator?) {
                config.isAnim = true
            }
        })
        animator?.start()
    }

    /**
     * 退出动画
     */
    internal fun exitAnim() {
        // 正在执行动画，防止重复调用
        if (config.isAnim || parentView == null) return
        val manager: AnimatorManager? =
            AnimatorManager(config.floatAnimator, this, parentView!!, config.sidePattern)
        val animator: Animator? = manager?.exitAnim()
        // 动画为空，直接移除浮窗视图
        if (animator == null) parentView?.removeView(this)
        else {
            animator.addListener(object : Animator.AnimatorListener {
                override fun onAnimationRepeat(animation: Animator?) {}

                override fun onAnimationEnd(animation: Animator?) {
                    config.isAnim = false
                    parentView?.removeView(this@AbstractDragFloatingView)
                }

                override fun onAnimationCancel(animation: Animator?) {}

                override fun onAnimationStart(animation: Animator?) {
                    config.isAnim = true
                }
            })
            animator.start()
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        config.callbacks?.dismiss()
        config.floatCallbacks?.builder?.dismiss?.invoke()
    }

}