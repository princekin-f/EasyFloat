package com.lzf.easyfloat.utils

import android.app.Activity
import android.app.Application
import android.os.Bundle
import com.lzf.easyfloat.enums.ShowPattern
import com.lzf.easyfloat.service.FloatService

/**
 * @author: liuzhenfeng
 * @function: 通过生命周期回调，判断系统浮窗的过滤信息，以及app是否位于前台，通过广播通知浮窗service
 * @date: 2019-07-11  15:51
 */
internal object LifecycleUtils {

    private var activityCount = 0
    private lateinit var application: Application

    fun setLifecycleCallbacks(application: Application) {
        this.application = application
        application.registerActivityLifecycleCallbacks(object :
            Application.ActivityLifecycleCallbacks {

            override fun onActivityPaused(activity: Activity?) {}

            override fun onActivityResumed(activity: Activity?) {}

            override fun onActivityStarted(activity: Activity?) {
                if (activity == null) return
                activityCount++
                FloatService.floatMap.forEach { (tag, manager) ->
                    run {
                        // 过滤不需要显示浮窗的页面
                        manager.config.filterSet.forEach filterSet@{
                            if (it == activity.componentName.className) {
                                setVisible(false, tag)
                                manager.config.needShow = false
                                logger.i("过滤浮窗显示: $it, tag: $tag")
                                return@filterSet
                            }
                        }

                        // 当过滤信息没有匹配上时，需要发送广播，反之修改needShow为默认值
                        if (manager.config.needShow) setVisible(tag = tag)
                        else manager.config.needShow = true
                    }
                }
            }

            override fun onActivityDestroyed(activity: Activity?) {}

            override fun onActivitySaveInstanceState(activity: Activity?, outState: Bundle?) {}

            override fun onActivityStopped(activity: Activity?) {
                if (activity == null) return
                activityCount--
                if (isForeground()) return
                // 当app处于后台时，检测是否有仅前台显示的系统浮窗
                FloatService.floatMap.forEach { (tag, manager) ->
                    run {
                        when (manager.config.showPattern) {
                            ShowPattern.ALL_TIME -> setVisible(true, tag)
                            ShowPattern.FOREGROUND -> setVisible(tag = tag)
                            else -> { }
                        }
                    }
                }
            }

            override fun onActivityCreated(activity: Activity?, savedInstanceState: Bundle?) {}
        })
    }

    private fun isForeground() = activityCount > 0

    private fun setVisible(boolean: Boolean = isForeground(), tag: String?) =
        FloatService.setVisible(application, boolean, tag)

}