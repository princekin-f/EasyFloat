package com.lzf.easyfloat.anim

import android.animation.Animator
import android.animation.ObjectAnimator
import android.graphics.Rect
import android.view.View
import android.view.ViewGroup
import com.lzf.easyfloat.enums.SidePattern
import com.lzf.easyfloat.interfaces.OnFloatAnimator
import kotlin.math.min

/**
 * @author: liuzhenfeng
 * @function: 默认的出入动画：选择距离边框最近的的一个边，进行出入。
 * 想要实现其他动画效果，只需实现OnFloatAnimator接口，自行定义内容；也可为null，不执行动画。
 * @date: 2019-07-19  14:19
 */
open class DefaultAnimator : OnFloatAnimator {

    // 浮窗各边到窗口边框的距离
    private var leftDistance = 0
    private var rightDistance = 0
    private var topDistance = 0
    private var bottomDistance = 0
    // x轴和y轴距离的最小值
    private var minX = 0
    private var minY = 0
    // 浮窗和窗口所在的矩形
    private var floatRect = Rect()
    private var parentRect = Rect()

    override fun enterAnim(
        view: View,
        parentView: ViewGroup,
        sidePattern: SidePattern
    ): Animator? {
        initValue(view, parentView)
        val (animType, startValue, endValue) = animTriple(view, sidePattern)
        return ObjectAnimator.ofFloat(view, animType, startValue, endValue).setDuration(500)
    }

    override fun exitAnim(
        view: View,
        parentView: ViewGroup,
        sidePattern: SidePattern
    ): Animator? {
        initValue(view, parentView)
        val (animType, startValue, endValue) = animTriple(view, sidePattern)
        return ObjectAnimator.ofFloat(view, animType, endValue, startValue).setDuration(500)
    }

    /**
     * 设置动画类型，计算具体数值
     */
    private fun animTriple(view: View, sidePattern: SidePattern): Triple<String, Float, Float> {
        val animType: String
        val startValue: Float = when (sidePattern) {
            SidePattern.LEFT, SidePattern.RESULT_LEFT -> {
                animType = "translationX"
                leftValue(view)
            }
            SidePattern.RIGHT, SidePattern.RESULT_RIGHT -> {
                animType = "translationX"
                rightValue(view)
            }
            SidePattern.TOP, SidePattern.RESULT_TOP -> {
                animType = "translationY"
                topValue(view)
            }
            SidePattern.BOTTOM, SidePattern.RESULT_BOTTOM -> {
                animType = "translationY"
                rightValue(view)
            }

            SidePattern.DEFAULT, SidePattern.AUTO_HORIZONTAL, SidePattern.RESULT_HORIZONTAL -> {
                animType = "translationX"
                if (leftDistance < rightDistance) leftValue(view) else rightValue(view)
            }
            SidePattern.AUTO_VERTICAL, SidePattern.RESULT_VERTICAL -> {
                animType = "translationY"
                if (topDistance < bottomDistance) topValue(view) else bottomValue(view)
            }

            else -> {
                if (minX <= minY) {
                    animType = "translationX"
                    if (leftDistance < rightDistance) leftValue(view) else rightValue(view)
                } else {
                    animType = "translationY"
                    if (topDistance < bottomDistance) topValue(view) else bottomValue(view)
                }
            }
        }

        val endValue = if (animType == "translationX") view.translationX else view.translationY
        return Triple(animType, startValue, endValue)
    }

    private fun leftValue(view: View) = -(leftDistance + view.width) + view.translationX

    private fun rightValue(view: View) = rightDistance + view.width + view.translationX

    private fun topValue(view: View) = -(topDistance + view.height) + view.translationY

    private fun bottomValue(view: View) = bottomDistance + view.height + view.translationY


    /**
     * 计算一些数值，方便使用
     */
    private fun initValue(view: View, parentView: ViewGroup) {
        view.getGlobalVisibleRect(floatRect)
        parentView.getGlobalVisibleRect(parentRect)

        leftDistance = floatRect.left
        rightDistance = parentRect.right - floatRect.right
        topDistance = floatRect.top - parentRect.top
        bottomDistance = parentRect.bottom - floatRect.bottom

        minX = min(leftDistance, rightDistance)
        minY = min(topDistance, bottomDistance)
    }

}