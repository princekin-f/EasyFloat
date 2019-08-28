package com.lzf.easyfloat.interfaces

import android.view.MotionEvent
import android.view.View

/**
 * @author: liuzhenfeng
 * @function: 浮窗的一些状态回调
 * @date: 2019-07-16  14:11
 */
interface OnFloatCallbacks {

    /**
     * 浮窗的创建结果，是否创建成功
     *
     * @param isCreated     是否创建成功
     * @param msg           失败返回的结果
     * @param view          浮窗xml布局
     */
    fun createdResult(isCreated: Boolean, msg: String?, view: View?)

    fun show(view: View)

    fun hide(view: View)

    fun dismiss()

    /**
     * 触摸事件的回调
     */
    fun touchEvent(view: View, event: MotionEvent)

    /**
     * 浮窗被拖拽时的回调，坐标为浮窗的左上角坐标
     */
    fun drag(view: View, event: MotionEvent)

    /**
     * 拖拽结束时的回调，坐标为浮窗的左上角坐标
     */
    fun dragEnd(view: View)

}