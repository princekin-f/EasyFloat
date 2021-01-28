package com.lzf.easyfloat.permission

import android.app.Activity
import android.app.Fragment
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import com.lzf.easyfloat.interfaces.OnPermissionResult
import com.lzf.easyfloat.utils.Logger

/**
 * @author: liuzhenfeng
 * @function: 用于浮窗权限的申请，自动处理回调结果
 * @date: 2019-07-15  10:36
 */
internal class PermissionFragment : Fragment() {

    companion object {
        private var onPermissionResult: OnPermissionResult? = null

        fun requestPermission(activity: Activity, onPermissionResult: OnPermissionResult) {
            this.onPermissionResult = onPermissionResult
            activity.fragmentManager
                .beginTransaction()
                .add(PermissionFragment(), activity.localClassName)
                .commitAllowingStateLoss()
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        // 权限申请
        PermissionUtils.requestPermission(this)
        Logger.i("PermissionFragment：requestPermission")
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == PermissionUtils.requestCode) {
            // 需要延迟执行，不然即使授权，仍有部分机型获取不到权限
            Handler(Looper.getMainLooper()).postDelayed({
                val activity = activity ?: return@postDelayed
                val check = PermissionUtils.checkPermission(activity)
                Logger.i("PermissionFragment onActivityResult: $check")
                // 回调权限结果
                onPermissionResult?.permissionResult(check)
                onPermissionResult = null
                // 将Fragment移除
                fragmentManager.beginTransaction().remove(this).commitAllowingStateLoss()
            }, 500)
        }
    }

}
