package com.lzf.easyfloat.utils

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Point
import android.os.Build
import android.provider.Settings
import android.util.Log
import android.view.*

/**
 * @author: liuzhenfeng
 * @function:
 * @date: 2019-05-23  15:23
 */
object DisplayUtils {

    private const val TAG = "DisplayUtils--->"

    fun px2dp(context: Context, pxVal: Float): Int {
        val density = context.resources.displayMetrics.density
        return (pxVal / density + 0.5f).toInt()
    }

    fun dp2px(context: Context, dpVal: Float): Int {
        val density = context.resources.displayMetrics.density
        return (dpVal * density + 0.5f).toInt()
    }

    fun px2sp(context: Context, pxValue: Float): Int {
        val fontScale = context.resources.displayMetrics.scaledDensity
        return (pxValue / fontScale + 0.5f).toInt()
    }

    fun sp2px(context: Context, spValue: Float): Int {
        val fontScale = context.resources.displayMetrics.scaledDensity
        return (spValue * fontScale + 0.5f).toInt()
    }

    /**
     * 获取屏幕宽度
     */
    fun getScreenWidth(context: Context): Int {
        val resources = context.resources
        val dm = resources.displayMetrics
        return dm.widthPixels
    }

    /**
     * 获取屏幕高度
     */
    fun getScreenHeight(context: Context): Int {
        val display =
            (context.getSystemService(Context.WINDOW_SERVICE) as WindowManager).defaultDisplay
        val point = Point()
        display.getRealSize(point)
        return point.y
    }

    /**
     * 获取状态栏高度
     */
    fun getStatusBarHeight(context: Context): Int {
        var result = 0
        val resources = context.resources
        val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
        if (resourceId > 0) {
            result = resources.getDimensionPixelSize(resourceId)
        }
        return result
    }

    fun statusBarHeight(view: View) = getStatusBarHeight(view.context.applicationContext)

    /**
     * 获取导航栏当前的高度
     */
    fun getNavigationBarCurrentHeight(context: Context) =
        if (isNavigationBarShow(context)) getNavigationBarHeight(context) else 0

    /**
     * 获取导航栏真实的高度（可能未显示）
     */
    fun getNavigationBarHeight(context: Context): Int {
        var result = 0
        val resources = context.resources
        val resourceId =
            resources.getIdentifier("navigation_bar_height", "dimen", "android")
        if (resourceId > 0) {
            result = resources.getDimensionPixelSize(resourceId)
        }
        Log.d(TAG, "getNavigationBarHeight = $result")
        return result
    }

    /**
     * 判断虚拟导航栏是否显示
     *
     * @param context 上下文对象
     * @return true(显示虚拟导航栏)，false(不显示或不支持虚拟导航栏)
     */
    @SuppressLint("PrivateApi")
    fun isNavigationBarShow(context: Context): Boolean {
        var hasNavigationBar = false
        val rs = context.resources
        val id = rs.getIdentifier("config_showNavigationBar", "bool", "android")
        if (id > 0) hasNavigationBar = rs.getBoolean(id)
        try {
            val systemPropertiesClass = Class.forName("android.os.SystemProperties")
            val m = systemPropertiesClass.getMethod("get", String::class.java)
            val navBarOverride = m.invoke(systemPropertiesClass, "qemu.hw.mainkeys") as String
            // 判断是否隐藏了底部虚拟导航
            val navigationBarIsMin = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                Settings.System.getInt(context.contentResolver, "navigationbar_is_min", 0)
            } else {
                Settings.Global.getInt(context.contentResolver, "navigationbar_is_min", 0)
            }
            if ("1" == navBarOverride || 1 == navigationBarIsMin) {
                hasNavigationBar = false
            } else if ("0" == navBarOverride) {
                hasNavigationBar = true
            }
        } catch (e: Exception) {
        }
        return hasNavigationBar
    }

}