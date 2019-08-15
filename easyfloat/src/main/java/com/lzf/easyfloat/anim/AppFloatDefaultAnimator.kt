package com.lzf.easyfloat.anim

import android.animation.Animator
import android.animation.ValueAnimator
import android.graphics.Rect
import android.view.View
import android.view.WindowManager
import com.lzf.easyfloat.enums.SidePattern
import com.lzf.easyfloat.interfaces.OnAppFloatAnimator

/**
 * @author: liuzhenfeng
 * @function: 系统浮窗的默认效果，选择靠近左右侧的一边进行出入
 * @date: 2019-07-22  17:22
 */
open class AppFloatDefaultAnimator : OnAppFloatAnimator {

    override fun enterAnim(
        view: View,
        params: WindowManager.LayoutParams,
        windowManager: WindowManager,
        sidePattern: SidePattern
    ): Animator? {
        val value = initValue(view, params, windowManager)
        val animator = ValueAnimator.ofInt(value, params.x).setDuration(500)
        animator.addUpdateListener {
            params.x = it.animatedValue as Int
            windowManager.updateViewLayout(view, params)
        }
        return animator
    }

    override fun exitAnim(
        view: View,
        params: WindowManager.LayoutParams,
        windowManager: WindowManager,
        sidePattern: SidePattern
    ): Animator? {
        val value = initValue(view, params, windowManager)
        val animator = ValueAnimator.ofInt(params.x, value).setDuration(500)
        animator.addUpdateListener {
            params.x = it.animatedValue as Int
            windowManager.updateViewLayout(view, params)
        }
        return animator
    }

    private fun initValue(
        view: View,
        params: WindowManager.LayoutParams,
        windowManager: WindowManager
    ): Int {
        val parentRect = Rect()
        windowManager.defaultDisplay.getRectSize(parentRect)
        val leftDistance = params.x
        val rightDistance = parentRect.right - (leftDistance + view.right)
        return if (leftDistance < rightDistance) -view.right else parentRect.right
    }

}