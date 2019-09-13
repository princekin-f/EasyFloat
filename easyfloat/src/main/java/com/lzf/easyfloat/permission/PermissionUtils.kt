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
import com.lzf.easyfloat.utils.logger

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
     */
    @JvmStatic
    fun checkPermission(context: Context): Boolean {
        // 6.0 版本之后由于 google 增加了对悬浮窗权限的管理，所以方式就统一了
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            when {
                RomUtils.checkIsHuaweiRom() -> return huaweiPermissionCheck(context)
                RomUtils.checkIsMiuiRom() -> return miuiPermissionCheck(context)
                RomUtils.checkIsOppoRom() -> return oppoROMPermissionCheck(context)
                RomUtils.checkIsMeizuRom() -> return meizuPermissionCheck(context)
                RomUtils.checkIs360Rom() -> return qikuPermissionCheck(context)
            }
        }
        return commonROMPermissionCheck(context)
    }

    /**
     * 申请悬浮窗权限
     */
    fun requestPermission(activity: Activity, onPermissionResult: OnPermissionResult) {
        PermissionFragment.requestPermission(activity, onPermissionResult)
    }

    internal fun requestPermission(fragment: Fragment) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            when {
                RomUtils.checkIsHuaweiRom() -> HuaweiUtils.applyPermission(fragment.activity)
                RomUtils.checkIsMiuiRom() -> MiuiUtils.applyMiuiPermission(fragment.activity)
                RomUtils.checkIsOppoRom() -> OppoUtils.applyOppoPermission(fragment.activity)
                RomUtils.checkIsMeizuRom() -> MeizuUtils.applyPermission(fragment)
                RomUtils.checkIs360Rom() -> QikuUtils.applyPermission(fragment.activity)
            }
        } else {
            commonROMPermissionApply(fragment)
        }
    }

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

    private fun commonROMPermissionCheck(context: Context): Boolean {
        // 最新发现魅族6.0的系统这种方式不好用，天杀的，只有你是奇葩，没办法，单独适配一下
        if (RomUtils.checkIsMeizuRom()) {
            return meizuPermissionCheck(context)
        } else {
            var result = true
            if (Build.VERSION.SDK_INT >= 23) {
                try {
                    val clazz = Settings::class.java
                    val canDrawOverlays =
                        clazz.getDeclaredMethod("canDrawOverlays", Context::class.java)
                    result = canDrawOverlays.invoke(null, context) as Boolean
                } catch (e: Exception) {
                    Log.e(TAG, Log.getStackTraceString(e))
                }
            }
            return result
        }
    }

    /**
     * 通用 rom 权限申请
     */
    private fun commonROMPermissionApply(fragment: Fragment) {
        when {
            // 这里也一样，魅族系统需要单独适配
            RomUtils.checkIsMeizuRom() -> MeizuUtils.applyPermission(fragment)
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.M -> try {
                commonROMPermissionApplyInternal(fragment)
            } catch (e: Exception) {
                logger.e(TAG, Log.getStackTraceString(e))
            }
            // 需要做统计效果
            else -> logger.d(TAG, "user manually refuse OVERLAY_PERMISSION")
        }
    }

    @JvmStatic
    fun commonROMPermissionApplyInternal(fragment: Fragment) {
        try {
            val clazz = Settings::class.java
            val field = clazz.getDeclaredField("ACTION_MANAGE_OVERLAY_PERMISSION")
            val intent = Intent(field.get(null).toString())
            intent.data = Uri.parse("package:${fragment.activity.packageName}")
            fragment.startActivityForResult(intent, requestCode)
        } catch (e: Exception) {
            logger.e(TAG, "$e")
        }
    }
}

