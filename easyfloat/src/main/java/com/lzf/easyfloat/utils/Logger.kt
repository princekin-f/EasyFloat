package com.lzf.easyfloat.utils

import android.util.Log
import com.lzf.easyfloat.BuildConfig

/**
 * @author: liuzhenfeng
 * @function:
 * @date: 2019-05-27  16:48
 */
internal object Logger {

    private var tag = "EasyFloat--->"

    // 设为false关闭日志
    private var logEnable = BuildConfig.DEBUG

    fun i(msg: Any) = i(tag, msg.toString())

    fun v(msg: Any) = v(tag, msg.toString())

    fun d(msg: Any) = d(tag, msg.toString())

    fun w(msg: Any) = w(tag, msg.toString())

    fun e(msg: Any) = e(tag, msg.toString())

    fun i(tag: String, msg: String) {
        if (logEnable) Log.i(tag, msg)
    }

    fun v(tag: String, msg: String) {
        if (logEnable) Log.v(tag, msg)
    }

    fun d(tag: String, msg: String) {
        if (logEnable) Log.d(tag, msg)
    }

    fun w(tag: String, msg: String) {
        if (logEnable) Log.w(tag, msg)
    }

    fun e(tag: String, msg: String) {
        if (logEnable) Log.e(tag, msg)
    }

}