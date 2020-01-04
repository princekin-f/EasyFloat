package com.lzf.easyfloat.utils

import android.app.Activity
import android.app.Application
import android.os.Bundle
import com.lzf.easyfloat.enums.ShowPattern
import com.lzf.easyfloat.widget.appfloat.FloatManager

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
                FloatManager.floatMap.forEach { (tag, manager) ->
                    // 仅后台显示模式下，隐藏浮窗
                    if (manager.config.showPattern == ShowPattern.BACKGROUND) {
                        setVisible(false, tag)
                        return
                    }

                    // 如果手动隐藏浮窗，不再考虑过滤信息
                    if (!manager.config.needShow) return

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

            override fun onActivityDestroyed(activity: Activity?) {}

            override fun onActivitySaveInstanceState(activity: Activity?, outState: Bundle?) {}

            override fun onActivityStopped(activity: Activity?) {
                if (activity == null) return
                activityCount--
                if (isForeground()) return
                FloatManager.floatMap.forEach { (tag, manager) ->
                    // 当app处于后台时，不是仅前台显示的浮窗，都需要显示
                    setVisible(manager.config.showPattern != ShowPattern.FOREGROUND, tag)
                }
            }

            override fun onActivityCreated(activity: Activity?, savedInstanceState: Bundle?) {}
        })
    }

    private fun isForeground() = activityCount > 0

    private fun setVisible(boolean: Boolean = isForeground(), tag: String?) =
        FloatManager.visible(boolean, tag)

}