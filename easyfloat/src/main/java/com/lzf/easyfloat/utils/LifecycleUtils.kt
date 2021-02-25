package com.lzf.easyfloat.utils

import android.app.Activity
import android.app.Application
import android.os.Bundle
import com.lzf.easyfloat.core.FloatingWindowManager
import com.lzf.easyfloat.enums.ShowPattern
import java.lang.ref.WeakReference

/**
 * @author: liuzhenfeng
 * @function: 通过生命周期回调，判断系统浮窗的过滤信息，以及app是否位于前台，控制浮窗显隐
 * @date: 2019-07-11  15:51
 */
internal object LifecycleUtils {

    lateinit var application: Application
    private var activityCount = 0
    private var mTopActivity: WeakReference<Activity>? = null

    fun getTopActivity(): Activity? = mTopActivity?.get()

    fun setLifecycleCallbacks(application: Application) {
        this.application = application
        application.registerActivityLifecycleCallbacks(object :
            Application.ActivityLifecycleCallbacks {

            override fun onActivityCreated(activity: Activity?, savedInstanceState: Bundle?) {}

            override fun onActivityStarted(activity: Activity?) {
                // 计算启动的activity数目
                activity?.let { activityCount++ }
            }

            override fun onActivityResumed(activity: Activity?) {
                activity?.let {
                    mTopActivity?.clear()
                    mTopActivity = WeakReference<Activity>(it)
                    // 每次都要判断当前页面是否需要显示
                    checkShow(it)
                }
            }

            override fun onActivityPaused(activity: Activity?) {}

            override fun onActivityStopped(activity: Activity?) {
                activity?.let {
                    // 计算关闭的activity数目，并判断当前App是否处于后台
                    activityCount--
                    checkHide(it)
                }
            }

            override fun onActivityDestroyed(activity: Activity?) {}

            override fun onActivitySaveInstanceState(activity: Activity?, outState: Bundle?) {}
        })
    }

    /**
     * 判断浮窗是否需要显示
     */
    private fun checkShow(activity: Activity) =
        FloatingWindowManager.windowMap.forEach { (tag, manager) ->
            manager.config.apply {
                when {
                    // 当前页面的浮窗，不需要处理
                    showPattern == ShowPattern.CURRENT_ACTIVITY -> return@apply
                    // 仅后台显示模式下，隐藏浮窗
                    showPattern == ShowPattern.BACKGROUND -> setVisible(false, tag)
                    // 如果没有手动隐藏浮窗，需要考虑过滤信息
                    needShow -> setVisible(activity.componentName.className !in filterSet, tag)
                }
            }
        }

    /**
     * 判断浮窗是否需要隐藏
     */
    private fun checkHide(activity: Activity) {
        // 如果不是finish，并且处于前台，无需判断
        if (!activity.isFinishing && isForeground()) return
        FloatingWindowManager.windowMap.forEach { (tag, manager) ->
            // 判断浮窗是否需要关闭
            if (activity.isFinishing) manager.params.token?.let {
                // 如果token不为空，并且是当前销毁的Activity，关闭浮窗，防止窗口泄漏
                if (it == activity.window?.decorView?.windowToken) {
                    FloatingWindowManager.dismiss(tag, true)
                }
            }

            manager.config.apply {
                if (!isForeground() && manager.config.showPattern != ShowPattern.CURRENT_ACTIVITY) {
                    // 当app处于后台时，全局、仅后台显示的浮窗，如果没有手动隐藏，需要显示
                    setVisible(showPattern != ShowPattern.FOREGROUND && needShow, tag)
                }
            }
        }
    }

    fun isForeground() = activityCount > 0

    private fun setVisible(isShow: Boolean = isForeground(), tag: String?) =
        FloatingWindowManager.visible(isShow, tag)

}