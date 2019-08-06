package com.lzf.easyfloat.interfaces

import android.view.MotionEvent

/**
 * @author: liuzhenfeng
 * @function: 系统浮窗的触摸事件
 * @date: 2019-07-11  11:06
 */
internal interface OnFloatTouchListener {

    fun onTouch(event: MotionEvent)
}