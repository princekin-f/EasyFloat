package com.lzf.easyfloat.utils

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Point
import android.os.Build
import android.util.Log
import android.view.*
import android.widget.FrameLayout

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
        val resources = context.resources
        val dm = resources.displayMetrics
        return dm.heightPixels
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

    @SuppressLint("ObsoleteSdkInt")
    fun isNavigationBarShow(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            val display =
                (context.getSystemService(Context.WINDOW_SERVICE) as WindowManager).defaultDisplay
            val size = Point()
            val realSize = Point()
            display.getSize(size)
            display.getRealSize(realSize)
            realSize.y != size.y
        } else {
            val menu = ViewConfiguration.get(context).hasPermanentMenuKey()
            val back = KeyCharacterMap.deviceHasKey(KeyEvent.KEYCODE_BACK)
            !(menu || back)
        }
    }

    /**
     *  设置相对于原点坐标的偏移量
     */
    fun setLayoutLocation(view: View, pair: Pair<Int, Int>) {
        val margin = ViewGroup.MarginLayoutParams(view.layoutParams)
        margin.setMargins(pair.first, pair.second, 0, 0)
        val layoutParams = FrameLayout.LayoutParams(margin)
        view.layoutParams = layoutParams
    }

}