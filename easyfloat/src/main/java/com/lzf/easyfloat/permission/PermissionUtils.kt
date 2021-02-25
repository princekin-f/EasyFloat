package com.lzf.easyfloat.permission

import android.app.Activity
import android.app.Fragment
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log
import com.lzf.easyfloat.interfaces.OnPermissionResult
import com.lzf.easyfloat.permission.rom.*
import com.lzf.easyfloat.utils.Logger

/**
 * @author: liuzhenfeng
 * @function: 悬浮窗权限工具类
 * @date: 2019-07-15  10:22
 */
object PermissionUtils {

    internal const val requestCode = 199
    private const val TAG = "PermissionUtils--->"

    /**
     * 检测是否有悬浮窗权限
     * 6.0 版本之后由于 google 增加了对悬浮窗权限的管理，所以方式就统一了
     */
    @JvmStatic
    fun checkPermission(context: Context): Boolean =
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) when {
            RomUtils.checkIsHuaweiRom() -> huaweiPermissionCheck(context)
            RomUtils.checkIsMiuiRom() -> miuiPermissionCheck(context)
            RomUtils.checkIsOppoRom() -> oppoROMPermissionCheck(context)
            RomUtils.checkIsMeizuRom() -> meizuPermissionCheck(context)
            RomUtils.checkIs360Rom() -> qikuPermissionCheck(context)
            else -> true
        } else commonROMPermissionCheck(context)

    /**
     * 申请悬浮窗权限
     */
    @JvmStatic
    fun requestPermission(activity: Activity, onPermissionResult: OnPermissionResult) =
        PermissionFragment.requestPermission(activity, onPermissionResult)

    internal fun requestPermission(fragment: Fragment) =
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) when {
            RomUtils.checkIsHuaweiRom() -> HuaweiUtils.applyPermission(fragment)
            RomUtils.checkIsMiuiRom() -> MiuiUtils.applyMiuiPermission(fragment)
            RomUtils.checkIsOppoRom() -> OppoUtils.applyOppoPermission(fragment)
            RomUtils.checkIsMeizuRom() -> MeizuUtils.applyPermission(fragment)
            RomUtils.checkIs360Rom() -> QikuUtils.applyPermission(fragment)
            else -> Logger.i(TAG, "原生 Android 6.0 以下无需权限申请")
        } else commonROMPermissionApply(fragment)

    private fun huaweiPermissionCheck(context: Context) =
        HuaweiUtils.checkFloatWindowPermission(context)

    private fun miuiPermissionCheck(context: Context) =
        MiuiUtils.checkFloatWindowPermission(context)

    private fun meizuPermissionCheck(context: Context) =
        MeizuUtils.checkFloatWindowPermission(context)

    private fun qikuPermissionCheck(context: Context) =
        QikuUtils.checkFloatWindowPermission(context)

    private fun oppoROMPermissionCheck(context: Context) =
        OppoUtils.checkFloatWindowPermission(context)

    /**
     * 6.0以后，通用悬浮窗权限检测
     * 但是魅族6.0的系统这种方式不好用，需要单独适配一下
     */
    private fun commonROMPermissionCheck(context: Context): Boolean =
        if (RomUtils.checkIsMeizuRom()) meizuPermissionCheck(context) else {
            var result = true
            if (Build.VERSION.SDK_INT >= 23) try {
                val clazz = Settings::class.java
                val canDrawOverlays =
                    clazz.getDeclaredMethod("canDrawOverlays", Context::class.java)
                result = canDrawOverlays.invoke(null, context) as Boolean
            } catch (e: Exception) {
                Log.e(TAG, Log.getStackTraceString(e))
            }
            result
        }

    /**
     * 通用 rom 权限申请
     */
    private fun commonROMPermissionApply(fragment: Fragment) = when {
        // 这里也一样，魅族系统需要单独适配
        RomUtils.checkIsMeizuRom() -> MeizuUtils.applyPermission(fragment)
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.M -> try {
            commonROMPermissionApplyInternal(fragment)
        } catch (e: Exception) {
            Logger.e(TAG, Log.getStackTraceString(e))
        }
        // 需要做统计效果
        else -> Logger.d(TAG, "user manually refuse OVERLAY_PERMISSION")
    }

    @JvmStatic
    fun commonROMPermissionApplyInternal(fragment: Fragment) = try {
        val clazz = Settings::class.java
        val field = clazz.getDeclaredField("ACTION_MANAGE_OVERLAY_PERMISSION")
        val intent = Intent(field.get(null).toString())
        intent.data = Uri.parse("package:${fragment.activity.packageName}")
        fragment.startActivityForResult(intent, requestCode)
    } catch (e: Exception) {
        Logger.e(TAG, "$e")
    }

}

