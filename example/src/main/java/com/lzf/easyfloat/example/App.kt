package com.lzf.easyfloat.example

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import cn.bingoogolapple.swipebacklayout.BGASwipeBackHelper

/**
 * @author: liuzhenfeng
 * @function:
 * @date: 2019-07-11  15:28
 */
class App : Application() {

    companion object {
        @SuppressLint("StaticFieldLeak")
        lateinit var context: Context
    }

    override fun onCreate() {
        super.onCreate()
        context = this
        BGASwipeBackHelper.init(this, null)
    }

}