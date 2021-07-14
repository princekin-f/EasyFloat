package com.lzf.easyfloat.utils

import android.app.Service
import android.content.Context
import android.content.res.Configuration
import android.graphics.Point
import android.os.Build
import android.provider.Settings
import android.util.DisplayMetrics
import android.view.View
import android.view.WindowManager
import com.lzf.easyfloat.permission.rom.RomUtils

/**
 * @author: liuzhenfeng
 * @function: 屏幕显示相关工具类
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
     * 获取屏幕宽度（显示宽度，横屏的时候可能会小于物理像素值）
     */
    fun getScreenWidth(context: Context): Int {
        val manager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val metrics = DisplayMetrics()
        manager.defaultDisplay.getRealMetrics(metrics)
        return if (context.resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
            metrics.widthPixels
        } else {
            metrics.widthPixels - getNavigationBarCurrentHeight(context)
        }
    }

    /**
     * 获取屏幕高度（物理像素值的高度）
     */
    fun getScreenHeight(context: Context) = getScreenSize(context).y

    /**
     * 获取屏幕宽高
     */
    fun getScreenSize(context: Context) = Point().apply {
        val windowManager = context.getSystemService(Service.WINDOW_SERVICE) as WindowManager
        val display = windowManager.defaultDisplay
        display.getRealSize(this)
    }

    /**
     * 获取状态栏高度
     */
    fun getStatusBarHeight(context: Context): Int {
        var result = 0
        val resources = context.resources
        val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
        if (resourceId > 0) result = resources.getDimensionPixelSize(resourceId)
        return result
    }

    fun statusBarHeight(view: View) = getStatusBarHeight(view.context.applicationContext)

    /**
     * 获取导航栏真实的高度（可能未显示）
     */
    fun getNavigationBarHeight(context: Context): Int {
        var result = 0
        val resources = context.resources
        val resourceId =
            resources.getIdentifier("navigation_bar_height", "dimen", "android")
        if (resourceId > 0) result = resources.getDimensionPixelSize(resourceId)
        return result
    }

    /**
     * 获取导航栏当前的高度
     */
    fun getNavigationBarCurrentHeight(context: Context) =
        if (hasNavigationBar(context)) getNavigationBarHeight(context) else 0

    /**
     * 判断虚拟导航栏是否显示
     *
     * @param context 上下文对象
     * @return true(显示虚拟导航栏)，false(不显示或不支持虚拟导航栏)
     */
    fun hasNavigationBar(context: Context) = when {
        getNavigationBarHeight(context) == 0 -> false
        RomUtils.checkIsHuaweiRom() && isHuaWeiHideNav(context) -> false
        RomUtils.checkIsMiuiRom() && isMiuiFullScreen(context) -> false
        RomUtils.checkIsVivoRom() && isVivoFullScreen(context) -> false
        else -> isHasNavigationBar(context)
    }

    /**
     * 不包含导航栏的有效高度（没有导航栏，或者已去除导航栏的高度）
     */
    fun rejectedNavHeight(context: Context): Int {
        val point = getScreenSize(context)
        if (point.x > point.y) return point.y
        return point.y - getNavigationBarCurrentHeight(context)
    }

    /**
     * 华为手机是否隐藏了虚拟导航栏
     * @return true 表示隐藏了，false 表示未隐藏
     */
    private fun isHuaWeiHideNav(context: Context) =
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            Settings.System.getInt(context.contentResolver, "navigationbar_is_min", 0)
        } else {
            Settings.Global.getInt(context.contentResolver, "navigationbar_is_min", 0)
        } != 0

    /**
     * 小米手机是否开启手势操作
     * @return false 表示使用的是虚拟导航键(NavigationBar)， true 表示使用的是手势， 默认是false
     */
    private fun isMiuiFullScreen(context: Context) =
        Settings.Global.getInt(context.contentResolver, "force_fsg_nav_bar", 0) != 0

    /**
     * Vivo手机是否开启手势操作
     * @return false 表示使用的是虚拟导航键(NavigationBar)， true 表示使用的是手势， 默认是false
     */
    private fun isVivoFullScreen(context: Context): Boolean =
        Settings.Secure.getInt(context.contentResolver, "navigation_gesture_on", 0) != 0

    /**
     * 其他手机根据屏幕真实高度与显示高度是否相同来判断
     */
    private fun isHasNavigationBar(context: Context): Boolean {
        val windowManager: WindowManager =
            context.getSystemService(Service.WINDOW_SERVICE) as WindowManager
        val d = windowManager.defaultDisplay

        val realDisplayMetrics = DisplayMetrics()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            d.getRealMetrics(realDisplayMetrics)
        }
        val realHeight = realDisplayMetrics.heightPixels
        val realWidth = realDisplayMetrics.widthPixels

        val displayMetrics = DisplayMetrics()
        d.getMetrics(displayMetrics)
        val displayHeight = displayMetrics.heightPixels
        val displayWidth = displayMetrics.widthPixels

        // 部分无良厂商的手势操作，显示高度 + 导航栏高度，竟然大于物理高度，对于这种情况，直接默认未启用导航栏
        if (displayHeight + getNavigationBarHeight(context) > realHeight) return false

        return realWidth - displayWidth > 0 || realHeight - displayHeight > 0
    }

}