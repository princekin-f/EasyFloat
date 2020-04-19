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
    ): Animator? = ValueAnimator.ofInt(initValue(view, params, windowManager), params.x).apply {
        duration = 500
        addUpdateListener {
            params.x = it.animatedValue as Int
            windowManager.updateViewLayout(view, params)
        }
    }

    override fun exitAnim(
        view: View,
        params: WindowManager.LayoutParams,
        windowManager: WindowManager,
        sidePattern: SidePattern
    ): Animator? = ValueAnimator.ofInt(params.x, initValue(view, params, windowManager)).apply {
        addUpdateListener {
            params.x = it.animatedValue as Int
            windowManager.updateViewLayout(view, params)
        }
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