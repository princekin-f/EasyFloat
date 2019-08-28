package com.lzf.easyfloat.widget.appfloat

import android.animation.Animator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Rect
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.view.WindowManager.LayoutParams
import com.lzf.easyfloat.data.FloatConfig
import com.lzf.easyfloat.enums.SidePattern
import com.lzf.easyfloat.utils.DisplayUtils
import kotlin.math.min

/**
 * @author: liuzhenfeng
 * @function: 根据吸附模式，实现相应的拖拽效果
 * @date: 2019-07-05  14:24
 */
internal class TouchUtils(val context: Context, val config: FloatConfig) {

    // 窗口所在的矩形
    private var parentRect: Rect = Rect()
    // 悬浮的父布局高度、宽度
    private var parentHeight = 0
    private var parentWidth = 0
    // 起点坐标
    private var lastX = 0f
    private var lastY = 0f
    // 浮窗各边距离父布局的距离
    private var leftDistance = 0
    private var rightDistance = 0
    private var topDistance = 0
    private var bottomDistance = 0
    // x轴、y轴的最小距离值
    private var minX = 0
    private var minY = 0

    private var emptyHeight = 0
    private val screenHeight = DisplayUtils.getScreenHeight(context)
    private val navigationBarHeight = DisplayUtils.getNavigationBarHeight(context)
    private var hasStatusBar = true

    /**
     * 根据吸附模式，实现相应的拖拽效果
     */
    fun updateFloat(
        view: View,
        event: MotionEvent,
        windowManager: WindowManager,
        params: LayoutParams
    ) {
        config.callbacks?.touchEvent(view, event)
        config.floatCallbacks?.builder?.touchEvent?.invoke(view, event)
        // 不可拖拽、或者正在执行动画，不做处理
        if (!config.dragEnable || config.isAnim) {
            config.isDrag = false
            return
        }

        when (event.action and MotionEvent.ACTION_MASK) {
            MotionEvent.ACTION_DOWN -> {
                config.isDrag = false
                // 记录触摸点的位置
                lastX = event.rawX
                lastY = event.rawY
                windowManager.defaultDisplay.getRectSize(parentRect)
                parentWidth = parentRect.width()
                parentHeight = parentRect.height()
                // 当前高度是否包含顶部状态栏
                hasStatusBar =
                    parentHeight == screenHeight || parentHeight + navigationBarHeight == screenHeight
                emptyHeight = parentHeight - view.height
            }

            MotionEvent.ACTION_MOVE -> {
                // 移动值 = 本次触摸值 - 上次触摸值
                val dx = event.rawX - lastX
                val dy = event.rawY - lastY
                // 忽略过小的移动，防止点击无效
                if (!config.isDrag && dx * dx + dy * dy < 81) return
                config.isDrag = true

                var x = params.x + dx.toInt()
                var y = params.y + dy.toInt()
                // 检测浮窗是否到达边缘
                x = when {
                    x < 0 -> 0
                    x > parentWidth - view.width -> parentWidth - view.width
                    else -> x
                }
                y = when {
                    y < 0 -> 0
                    y > emptyHeight - statusBarHeight(view) -> {
                        when {
                            hasStatusBar -> emptyHeight - statusBarHeight(view)
                            y > emptyHeight -> emptyHeight
                            else -> y
                        }
                    }
                    else -> y
                }

                when (config.sidePattern) {
                    SidePattern.LEFT -> x = 0
                    SidePattern.RIGHT -> x = parentWidth - view.width
                    SidePattern.TOP -> y = 0
                    SidePattern.BOTTOM -> y = parentHeight - view.height

                    SidePattern.AUTO_HORIZONTAL ->
                        x = if (event.rawX * 2 > parentWidth) parentWidth - view.width else 0

                    SidePattern.AUTO_VERTICAL ->
                        y = if ((event.rawY - parentRect.top) * 2 > parentHeight)
                            parentHeight - view.height else 0

                    SidePattern.AUTO_SIDE -> {
                        leftDistance = event.rawX.toInt()
                        rightDistance = parentWidth - event.rawX.toInt()
                        topDistance = event.rawY.toInt() - parentRect.top
                        bottomDistance = parentHeight + parentRect.top - event.rawY.toInt()

                        minX = min(leftDistance, rightDistance)
                        minY = min(topDistance, bottomDistance)
                        if (minX < minY) {
                            x = if (leftDistance == minX) 0 else parentWidth - view.width
                        } else {
                            y = if (topDistance == minY) 0 else parentHeight - view.height
                        }
                    }
                    else -> {
                    }
                }

                // 重新设置坐标信息
                params.x = x
                params.y = y
                windowManager.updateViewLayout(view, params)
                config.callbacks?.drag(view, event)
                config.floatCallbacks?.builder?.drag?.invoke(view, event)
                // 更新上次触摸点的数据
                lastX = event.rawX
                lastY = event.rawY
            }

            MotionEvent.ACTION_UP -> {
                if (!config.isDrag) return
                when (config.sidePattern) {
                    SidePattern.RESULT_LEFT,
                    SidePattern.RESULT_RIGHT,
                    SidePattern.RESULT_TOP,
                    SidePattern.RESULT_BOTTOM,
                    SidePattern.RESULT_HORIZONTAL,
                    SidePattern.RESULT_VERTICAL,
                    SidePattern.RESULT_SIDE -> sideAnim(view, params, windowManager)
                    else -> {
                        config.callbacks?.dragEnd(view)
                        config.floatCallbacks?.builder?.dragEnd?.invoke(view)
                    }
                }
            }

            else -> return
        }
    }

    private fun sideAnim(
        view: View,
        params: LayoutParams,
        windowManager: WindowManager
    ) {
        initDistanceValue(params, view)
        val isX: Boolean
        val end = when (config.sidePattern) {
            SidePattern.RESULT_LEFT -> {
                isX = true
                0
            }
            SidePattern.RESULT_RIGHT -> {
                isX = true
                params.x + rightDistance
            }
            SidePattern.RESULT_HORIZONTAL -> {
                isX = true
                if (leftDistance < rightDistance) 0 else params.x + rightDistance
            }

            SidePattern.RESULT_TOP -> {
                isX = false
                0
            }
            SidePattern.RESULT_BOTTOM -> {
                isX = false
                // 不要轻易使用此相关模式，需要考虑虚拟导航栏的情况
                if (hasStatusBar) emptyHeight - statusBarHeight(view) else emptyHeight
            }
            SidePattern.RESULT_VERTICAL -> {
                isX = false
                if (topDistance < bottomDistance) 0 else {
                    if (hasStatusBar) emptyHeight - statusBarHeight(view) else emptyHeight
                }
            }

            SidePattern.RESULT_SIDE -> {
                if (minX < minY) {
                    isX = true
                    if (leftDistance < rightDistance) 0 else params.x + rightDistance
                } else {
                    isX = false
                    if (topDistance < bottomDistance) 0 else {
                        if (hasStatusBar) emptyHeight - statusBarHeight(view) else emptyHeight
                    }
                }
            }
            else -> return
        }

        val animator = ValueAnimator.ofInt(if (isX) params.x else params.y, end)
        animator.addUpdateListener {
            if (isX) params.x = it.animatedValue as Int else params.y = it.animatedValue as Int
            windowManager.updateViewLayout(view, params)
        }
        animator.addListener(object : Animator.AnimatorListener {
            override fun onAnimationRepeat(animation: Animator?) {}

            override fun onAnimationEnd(animation: Animator?) {
                config.isAnim = false
                config.callbacks?.dragEnd(view)
                config.floatCallbacks?.builder?.dragEnd?.invoke(view)
            }

            override fun onAnimationCancel(animation: Animator?) {}

            override fun onAnimationStart(animation: Animator?) {
                config.isAnim = true
            }
        })
        animator.start()
    }

    /**
     * 计算一些边界距离数据
     */
    private fun initDistanceValue(params: LayoutParams, view: View) {
        leftDistance = params.x
        rightDistance = parentWidth - (leftDistance + view.right)
        topDistance = params.y
        bottomDistance = if (hasStatusBar) {
            parentHeight - statusBarHeight(view) - topDistance - view.height
        } else parentHeight - topDistance - view.height

        minX = min(leftDistance, rightDistance)
        minY = min(topDistance, bottomDistance)
    }

    private fun statusBarHeight(view: View) = DisplayUtils.statusBarHeight(view)

}