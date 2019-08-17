package com.lzf.easyfloat.utils

import android.content.Context.INPUT_METHOD_SERVICE
import android.os.Handler
import android.os.Looper
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import com.lzf.easyfloat.service.FloatService

/**
 * @author: liuzhenfeng
 * @function: 软键盘工具类
 * 1，解决系统浮窗内的EditText，无法弹起软键盘的问题；
 * 2，暂未提供软键盘关闭的监听，需要使用者自行处理，希望各位大佬共同提供解决方案。
 * @date: 2019-08-17  11:11
 */
object InputMethodUtils {

    /**
     * 让系统浮窗获取焦点，并打开软键盘
     */
    @JvmStatic
    @JvmOverloads
    fun openInputMethod(editText: EditText, tag: String? = null) {
        FloatService.floatMap[tag ?: FloatService.DEFAULT_TAG]?.run {
            // 更改flags，并刷新布局，让系统浮窗获取焦点
            params.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
            windowManager.updateViewLayout(frameLayout, params)
        }

        Handler(Looper.getMainLooper()).postDelayed({
            // 打开软键盘
            val inputManager =
                editText.context.getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager?
            inputManager?.showSoftInput(editText, 0)
        }, 100)
    }

    /**
     * 当软键盘关闭时，调用此方法，移除系统浮窗的焦点，不然系统返回键无效
     */
    @JvmStatic
    @JvmOverloads
    fun closedInputMethod(tag: String? = null) {
        FloatService.floatMap[tag ?: FloatService.DEFAULT_TAG]?.run {
            params.flags =
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
            windowManager.updateViewLayout(frameLayout, params)
        }
    }

}