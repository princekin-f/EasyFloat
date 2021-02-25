package com.lzf.easyfloat.anim

import android.animation.Animator
import android.view.View
import android.view.WindowManager
import com.lzf.easyfloat.data.FloatConfig

/**
 * @author: liuzhenfeng
 * @function: App浮窗的出入动画管理类，只需传入具体的动画实现类（策略模式）
 * @date: 2019-07-22  16:44
 */
internal class AnimatorManager(
    private val view: View,
    private val params: WindowManager.LayoutParams,
    private val windowManager: WindowManager,
    private val config: FloatConfig
) {

    fun enterAnim(): Animator? =
        config.floatAnimator?.enterAnim(view, params, windowManager, config.sidePattern)

    fun exitAnim(): Animator? =
        config.floatAnimator?.exitAnim(view, params, windowManager, config.sidePattern)
}