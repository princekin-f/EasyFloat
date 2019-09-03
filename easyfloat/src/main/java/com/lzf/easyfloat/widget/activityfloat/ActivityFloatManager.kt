package com.lzf.easyfloat.widget.activityfloat

import android.app.Activity
import android.view.View
import android.widget.FrameLayout
import com.lzf.easyfloat.data.FloatConfig

/**
 * @author: liuzhenfeng
 * @function: Activity浮窗管理类，包括浮窗的创建、销毁、可见性等操作。
 * @date: 2019-07-31  09:10
 */
internal class ActivityFloatManager(val activity: Activity) {

    // 通过DecorView 获取屏幕底层FrameLayout，即activity的根布局，作为浮窗的父布局
    private var parentFrame: FrameLayout =
        activity.window.decorView.findViewById(android.R.id.content)

    /**
     * 创建Activity浮窗
     * 拖拽效果由自定义的拖拽布局实现；
     * 将拖拽布局，添加到Activity的根布局；
     * 再将浮窗的xml布局，添加到拖拽布局中，从而实现拖拽效果。
     */
    fun createFloat(config: FloatConfig) {
        // 设置浮窗的拖拽外壳FloatingView
        val floatingView = FloatingView(activity).apply {
            // 为浮窗打上tag，如果未设置tag，使用类名作为tag
            tag = config.floatTag ?: activity.componentName.className
            // 默认wrap_content，会导致子view的match_parent无效，所以手动设置params
            layoutParams = FrameLayout.LayoutParams(
                if (config.widthMatch) FrameLayout.LayoutParams.MATCH_PARENT else FrameLayout.LayoutParams.WRAP_CONTENT,
                if (config.heightMatch) FrameLayout.LayoutParams.MATCH_PARENT else FrameLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                // 如若未设置固定坐标，设置浮窗Gravity
                if (config.locationPair == Pair(0, 0)) gravity = config.gravity
            }
            // 同步配置
            setFloatConfig(config)
        }

        // 将FloatingView添加到根布局中
        parentFrame.addView(floatingView)

        // 设置Callbacks
        config.layoutView = floatingView
        config.callbacks?.createdResult(true, null, floatingView)
        config.floatCallbacks?.builder?.createdResult?.invoke(true, null, floatingView)
    }

    /**
     * 关闭activity浮窗
     */
    fun dismiss(tag: String?) = floatingView(tag)?.exitAnim()

    /**
     * 设置浮窗的可见性
     */
    fun setVisibility(tag: String?, visibility: Int) = floatingView(tag)?.apply {
        this.visibility = visibility
        if (visibility == View.GONE) {
            config.callbacks?.hide(this)
            config.floatCallbacks?.builder?.hide?.invoke(this)
        } else {
            config.callbacks?.show(this)
            config.floatCallbacks?.builder?.show?.invoke(this)
        }
    }

    /**
     * 获取浮窗是否显示
     */
    fun isShow(tag: String? = null): Boolean = floatingView(tag)?.visibility == View.VISIBLE

    /**
     * 设置是否可拖拽
     */
    fun setDragEnable(dragEnable: Boolean, tag: String? = null) {
        floatingView(tag)?.config?.dragEnable = dragEnable
    }

    /**
     * 获取我们传入的浮窗View
     */
    fun getFloatView(tag: String? = null): View? = floatingView(tag)?.config?.layoutView

    /**
     * 获取浮窗的拖拽外壳FloatingView
     */
    private fun floatingView(tag: String?): FloatingView? =
        parentFrame.findViewWithTag(tag ?: activity.componentName.className)

}