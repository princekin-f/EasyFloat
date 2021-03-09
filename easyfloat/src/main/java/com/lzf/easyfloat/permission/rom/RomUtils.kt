package com.lzf.easyfloat.permission.rom

import android.os.Build
import android.text.TextUtils
import android.util.Log
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader

/**
 * @author: liuzhenfeng
 * @github：https://github.com/princekin-f/EasyFloat
 * @function: 判断手机ROM
 * @date: 2020-01-07  22:30
 */
object RomUtils {
    private const val TAG = "RomUtils--->"

    /**
     * 获取 emui 版本号
     */
    @JvmStatic
    fun getEmuiVersion(): Double {
        try {
            val emuiVersion = getSystemProperty("ro.build.version.emui")
            val version = emuiVersion!!.substring(emuiVersion.indexOf("_") + 1)
            return version.toDouble()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return 4.0
    }

    @JvmStatic
    fun getSystemProperty(propName: String): String? {
        val line: String
        var input: BufferedReader? = null
        try {
            val p = Runtime.getRuntime().exec("getprop $propName")
            input = BufferedReader(InputStreamReader(p.inputStream), 1024)
            line = input.readLine()
            input.close()
        } catch (ex: Exception) {
            Log.e(TAG, "Unable to read sysprop $propName", ex)
            return null
        } finally {
            if (input != null) {
                try {
                    input.close()
                } catch (e: IOException) {
                    Log.e(TAG, "Exception while closing InputStream", e)
                }
            }
        }
        return line
    }

    fun checkIsHuaweiRom() = Build.MANUFACTURER.contains("HUAWEI")

    fun checkIsMiuiRom() = !TextUtils.isEmpty(getSystemProperty("ro.miui.ui.version.name"))

    fun checkIsMeizuRom(): Boolean {
        val systemProperty = getSystemProperty("ro.build.display.id")
        return if (TextUtils.isEmpty(systemProperty)) false
        else systemProperty!!.contains("flyme") || systemProperty.toLowerCase().contains("flyme")
    }

    fun checkIs360Rom(): Boolean =
        Build.MANUFACTURER.contains("QiKU") || Build.MANUFACTURER.contains("360")

    fun checkIsOppoRom() =
        Build.MANUFACTURER.contains("OPPO") || Build.MANUFACTURER.contains("oppo")

    fun checkIsVivoRom() =
        Build.MANUFACTURER.contains("VIVO") || Build.MANUFACTURER.contains("vivo")

}