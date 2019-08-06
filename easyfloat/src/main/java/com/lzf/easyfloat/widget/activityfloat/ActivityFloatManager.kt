package com.lzf.easyfloat.widget.activityfloat

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import com.lzf.easyfloat.R
import com.lzf.easyfloat.data.FloatConfig
import com.lzf.easyfloat.utils.logger

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
        // 获取可拖拽浮窗的外壳
        val shell =
            LayoutInflater.from(activity).inflate(R.layout.float_layout, parentFrame, false)
        // 为浮窗打上tag，如果未设置tag，使用类名作为tag
        shell.tag = config.floatTag ?: activity.componentName.className
        // 默认wrap_content，会导致子view的match_parent无效，所以手动设置params
        shell.layoutParams = FrameLayout.LayoutParams(
            if (config.widthMatch) FrameLayout.LayoutParams.MATCH_PARENT else FrameLayout.LayoutParams.WRAP_CONTENT,
            if (config.heightMatch) FrameLayout.LayoutParams.MATCH_PARENT else FrameLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            // 如若未设置固定坐标，设置浮窗Gravity
            if (config.locationPair == Pair(0, 0)) gravity = config.gravity
        }
        // 将浮窗外壳添加到根布局中
        parentFrame.addView(shell)

        // 获取浮窗对象，即自定义的FloatingView
        val floatingView = shell.findViewById<FloatingView>(R.id.floatingView).also {
            // 同步配置
            it.config = config
            // 设置浮窗的布局文件，即我们传递过来的xml布局文件
            it.setLayout(config.layoutId!!)
            // 设置空点击事件，用于接收触摸事件
            it.setOnClickListener {}
        }

        // 设置Callbacks
        config.callbacks?.createdResult(true, null, floatingView)
    }

    /**
     * 关闭activity浮窗
     */
    fun dismiss(tag: String?) {
        val view = getShellyView(tag) ?: return
        val floatingView: FloatingView = view.findViewById(R.id.floatingView)
        logger.i("dismiss: ${getTag(tag)}")
        floatingView.exitAnim()
    }

    /**
     * 设置浮窗的可见性
     */
    fun setVisibility(tag: String?, visibility: Int) {
        val view = getShellyView(tag) ?: return
        view.visibility = visibility
        val floatingView: FloatingView? = view.findViewById(R.id.floatingView)
        if (visibility == View.GONE) {
            floatingView?.config?.callbacks?.hide(floatingView)
        } else {
            floatingView?.config?.callbacks?.show(floatingView)
        }
    }

    /**
     * 获取浮窗是否显示
     */
    fun isShow(tag: String? = null): Boolean {
        val view = getShellyView(tag) ?: return false
        return view.visibility == View.VISIBLE
    }

    /**
     * 设置是否可拖拽
     */
    fun setDragEnable(dragEnable: Boolean, tag: String? = null) {
        val view = getShellyView(tag) ?: return
        view.findViewById<FloatingView>(R.id.floatingView).also {
            it?.config?.dragEnable = dragEnable
        }
    }

    /**
     * 获取浮窗的外壳view
     */
    private fun getShellyView(tag: String?): View? = parentFrame.findViewWithTag(getTag(tag))

    private fun getTag(tag: String?) = tag ?: activity.componentName.className

}