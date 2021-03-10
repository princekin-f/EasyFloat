package com.lzf.easyfloat.core

import android.animation.Animator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Rect
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.view.WindowManager.LayoutParams
import com.lzf.easyfloat.data.FloatConfig
import com.lzf.easyfloat.enums.ShowPattern
import com.lzf.easyfloat.enums.SidePattern
import com.lzf.easyfloat.utils.DisplayUtils
import kotlin.math.max
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

    // 四周坐标边界值
    private var leftBorder = 0
    private var topBorder = 0
    private var rightBorder = 0
    private var bottomBorder = 0

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
    private val location = IntArray(2)
    private var statusBarHeight = 0

    // 屏幕可用高度 - 浮窗自身高度 的剩余高度
    private var emptyHeight = 0

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
                // 初始化一些边界数据
                initBoarderValue(view, params)
            }

            MotionEvent.ACTION_MOVE -> {
                // 过滤边界值之外的拖拽
                if (event.rawX < leftBorder || event.rawX > rightBorder + view.width
                    || event.rawY < topBorder || event.rawY > bottomBorder + view.height
                ) return

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
                    x < leftBorder -> leftBorder
                    x > rightBorder -> rightBorder
                    else -> x
                }

                if (config.showPattern == ShowPattern.CURRENT_ACTIVITY) {
                    // 单页面浮窗，设置状态栏不沉浸时，最小高度为状态栏高度
                    if (y < statusBarHeight(view) && !config.immersionStatusBar) y =
                        statusBarHeight(view)
                }

                y = when {
                    y < topBorder -> topBorder
                    // 状态栏沉浸时，最小高度为-statusBarHeight，反之最小高度为0
                    y < 0 -> if (config.immersionStatusBar) {
                        if (y < -statusBarHeight) -statusBarHeight else y
                    } else 0
                    y > bottomBorder -> bottomBorder
                    else -> y
                }

                when (config.sidePattern) {
                    SidePattern.LEFT -> x = 0
                    SidePattern.RIGHT -> x = parentWidth - view.width
                    SidePattern.TOP -> y = 0
                    SidePattern.BOTTOM -> y = emptyHeight

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
                            y = if (topDistance == minY) 0 else emptyHeight
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

            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                if (!config.isDrag) return
                // 回调拖拽事件的ACTION_UP
                config.callbacks?.drag(view, event)
                config.floatCallbacks?.builder?.drag?.invoke(view, event)
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

    /**
     * 根据吸附类别，更新浮窗位置
     */
    fun updateFloat(
        view: View,
        params: LayoutParams,
        windowManager: WindowManager
    ) {
        initBoarderValue(view, params)
        sideAnim(view, params, windowManager)
    }

    /**
     * 初始化边界值等数据
     */
    private fun initBoarderValue(view: View, params: LayoutParams) {
        // 屏幕宽高需要每次获取，可能会有屏幕旋转、虚拟导航栏的状态变化
        parentWidth = DisplayUtils.getScreenWidth(context)
        parentHeight = config.displayHeight.getDisplayRealHeight(context)
        // 获取在整个屏幕内的绝对坐标
        view.getLocationOnScreen(location)
        // 通过绝对高度和相对高度比较，判断包含顶部状态栏
        statusBarHeight = if (location[1] > params.y) statusBarHeight(view) else 0
        emptyHeight = parentHeight - view.height - statusBarHeight

        leftBorder = max(0, config.leftBorder)
        rightBorder = min(parentWidth, config.rightBorder) - view.width
        topBorder = if (config.showPattern == ShowPattern.CURRENT_ACTIVITY) {
            // 单页面浮窗，坐标屏幕顶部计算
            if (config.immersionStatusBar) config.topBorder
            else config.topBorder + statusBarHeight(view)
        } else {
            // 系统浮窗，坐标从状态栏底部开始，沉浸时坐标为负
            if (config.immersionStatusBar) config.topBorder - statusBarHeight(view) else config.topBorder
        }
        bottomBorder = if (config.showPattern == ShowPattern.CURRENT_ACTIVITY) {
            // 单页面浮窗，坐标屏幕顶部计算
            if (config.immersionStatusBar)
                min(emptyHeight, config.bottomBorder - view.height)
            else
                min(emptyHeight, config.bottomBorder + statusBarHeight(view) - view.height)
        } else {
            // 系统浮窗，坐标从状态栏底部开始，沉浸时坐标为负
            if (config.immersionStatusBar)
                min(emptyHeight, config.bottomBorder - statusBarHeight(view) - view.height)
            else
                min(emptyHeight, config.bottomBorder - view.height)
        }
    }

    private fun sideAnim(
        view: View,
        params: LayoutParams,
        windowManager: WindowManager
    ) {
        initDistanceValue(params)
        val isX: Boolean
        val end = when (config.sidePattern) {
            SidePattern.RESULT_LEFT -> {
                isX = true
                leftBorder
            }
            SidePattern.RESULT_RIGHT -> {
                isX = true
                params.x + rightDistance
            }
            SidePattern.RESULT_HORIZONTAL -> {
                isX = true
                if (leftDistance < rightDistance) leftBorder else params.x + rightDistance
            }

            SidePattern.RESULT_TOP -> {
                isX = false
                topBorder
            }
            SidePattern.RESULT_BOTTOM -> {
                isX = false
                // 不要轻易使用此相关模式，需要考虑虚拟导航栏的情况
                bottomBorder
            }
            SidePattern.RESULT_VERTICAL -> {
                isX = false
                if (topDistance < bottomDistance) topBorder else bottomBorder
            }

            SidePattern.RESULT_SIDE -> {
                if (minX < minY) {
                    isX = true
                    if (leftDistance < rightDistance) leftBorder else params.x + rightDistance
                } else {
                    isX = false
                    if (topDistance < bottomDistance) topBorder else bottomBorder
                }
            }
            else -> return
        }

        val animator = ValueAnimator.ofInt(if (isX) params.x else params.y, end)
        animator.addUpdateListener {
            try {
                if (isX) params.x = it.animatedValue as Int else params.y = it.animatedValue as Int
                // 极端情况，还没吸附就调用了关闭浮窗，会导致吸附闪退
                windowManager.updateViewLayout(view, params)
            } catch (e: Exception) {
                animator.cancel()
            }
        }
        animator.addListener(object : Animator.AnimatorListener {
            override fun onAnimationRepeat(animation: Animator?) {}

            override fun onAnimationEnd(animation: Animator?) {
                dragEnd(view)
            }

            override fun onAnimationCancel(animation: Animator?) {
                dragEnd(view)
            }

            override fun onAnimationStart(animation: Animator?) {
                config.isAnim = true
            }
        })
        animator.start()
    }

    private fun dragEnd(view: View) {
        config.isAnim = false
        config.callbacks?.dragEnd(view)
        config.floatCallbacks?.builder?.dragEnd?.invoke(view)
    }

    /**
     * 计算一些边界距离数据
     */
    private fun initDistanceValue(params: LayoutParams) {
        leftDistance = params.x - leftBorder
        rightDistance = rightBorder - params.x
        topDistance = params.y - topBorder
        bottomDistance = bottomBorder - params.y

        minX = min(leftDistance, rightDistance)
        minY = min(topDistance, bottomDistance)
    }

    private fun statusBarHeight(view: View) = DisplayUtils.statusBarHeight(view)

}