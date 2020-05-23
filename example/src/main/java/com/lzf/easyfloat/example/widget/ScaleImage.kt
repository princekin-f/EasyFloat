package com.lzf.easyfloat.example.widget

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.appcompat.widget.AppCompatImageView

/**
 * @author: liuzhenfeng
 * @function: 通过消费触摸事件，监听手指滑动距离的变化，设置浮窗的大小
 * @date: 2019-08-05  09:55
 */
class ScaleImage(context: Context, attrs: AttributeSet? = null) : AppCompatImageView(context, attrs) {

    private var touchDownX = 0f
    private var touchDownY = 0f

    var onScaledListener: OnScaledListener? = null

    interface OnScaledListener {
        fun onScaled(x: Float, y: Float, event: MotionEvent)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {

        if (event == null) return super.onTouchEvent(event)

        // 屏蔽掉浮窗的事件拦截，仅由自身消费
        parent?.requestDisallowInterceptTouchEvent(true)

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                touchDownX = event.x
                touchDownY = event.y
            }

            MotionEvent.ACTION_MOVE ->
                onScaledListener?.onScaled(event.x - touchDownX, event.y - touchDownY, event)

        }
        return true
    }

}