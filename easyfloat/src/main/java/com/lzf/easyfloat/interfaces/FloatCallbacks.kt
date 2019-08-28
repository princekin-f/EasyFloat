package com.lzf.easyfloat.interfaces

import android.view.MotionEvent
import android.view.View

/**
 * @author: liuzhenfeng
 * @function: 通过Kotlin DSL实现接口回调效果
 * @date: 2019-08-26  17:06
 */
class FloatCallbacks {

    lateinit var builder: Builder

    // 带Builder返回值的lambda
    fun registerListener(builder: Builder.() -> Unit) {
        this.builder = Builder().also(builder)
    }

    inner class Builder {
        internal var createdResult: ((Boolean, String?, View?) -> Unit)? = null
        internal var show: ((View) -> Unit)? = null
        internal var hide: ((View) -> Unit)? = null
        internal var dismiss: (() -> Unit)? = null
        internal var touchEvent: ((View, MotionEvent) -> Unit)? = null
        internal var drag: ((View, MotionEvent) -> Unit)? = null
        internal var dragEnd: ((View) -> Unit)? = null

        fun createResult(action: (Boolean, String?, View?) -> Unit) {
            createdResult = action
        }

        fun show(action: (View) -> Unit) {
            show = action
        }

        fun hide(action: (View) -> Unit) {
            hide = action
        }

        fun dismiss(action: () -> Unit) {
            dismiss = action
        }

        fun touchEvent(action: (View, MotionEvent) -> Unit) {
            touchEvent = action
        }

        fun drag(action: (View, MotionEvent) -> Unit) {
            drag = action
        }

        fun dragEnd(action: (View) -> Unit) {
            dragEnd = action
        }
    }

}