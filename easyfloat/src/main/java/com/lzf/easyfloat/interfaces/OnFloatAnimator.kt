package com.lzf.easyfloat.interfaces

import android.animation.Animator
import android.view.View
import android.view.ViewGroup
import com.lzf.easyfloat.enums.SidePattern

/**
 * @author: liuzhenfeng
 * @function: 浮窗动画的接口类，实现该接口，可自定义不同的动画效果（策略模式）
 * @date: 2019-07-19  14:14
 */
interface OnFloatAnimator {

    // 入场动画
    fun enterAnim(view: View, parentView: ViewGroup, sidePattern: SidePattern): Animator? = null

    // 退出动画
    fun exitAnim(view: View, parentView: ViewGroup, sidePattern: SidePattern): Animator? = null
}