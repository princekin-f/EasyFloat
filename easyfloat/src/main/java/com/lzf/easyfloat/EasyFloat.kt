package com.lzf.easyfloat

import android.app.Activity
import android.app.Application
import android.content.Context
import android.view.View
import com.lzf.easyfloat.data.FloatConfig
import com.lzf.easyfloat.enums.ShowPattern
import com.lzf.easyfloat.enums.SidePattern
import com.lzf.easyfloat.interfaces.*
import com.lzf.easyfloat.interfaces.OnPermissionResult
import com.lzf.easyfloat.permission.PermissionUtils
import com.lzf.easyfloat.utils.LifecycleUtils
import com.lzf.easyfloat.interfaces.FloatCallbacks
import com.lzf.easyfloat.utils.Logger
import com.lzf.easyfloat.widget.activityfloat.ActivityFloatManager
import com.lzf.easyfloat.widget.appfloat.FloatManager
import java.lang.Exception
import java.lang.ref.WeakReference

/**
 * @author: liuzhenfeng
 * @github：https://github.com/princekin-f
 * @function: 悬浮窗使用工具类
 * @date: 2019-06-27  15:22
 */
class EasyFloat {

    companion object {
        internal var isDebug = false
        // 通过弱引用持有Activity，防止内容泄漏，适用于只在一个Activity创建浮窗的情况
        private var activityWr: WeakReference<Activity>? = null
        private var isInitialized = false

        @JvmStatic
        @JvmOverloads
        fun init(application: Application, isDebug: Boolean = false) {
            this.isDebug = isDebug
            isInitialized = true
            // 注册Activity生命周期回调
            LifecycleUtils.setLifecycleCallbacks(application)
        }

        @JvmStatic
        fun with(activity: Context): Builder {
            if (activity is Activity) activityWr = WeakReference(activity)
            return Builder(activity)
        }

        // *************************** Activity浮窗的相关方法 ***************************
        // 通过浮窗管理类，实现相应的功能，详情参考ActivityFloatManager
        @JvmStatic
        @JvmOverloads
        fun dismiss(activity: Activity? = null, tag: String? = null) =
            manager(activity)?.dismiss(tag)

        @JvmStatic
        @JvmOverloads
        fun hide(activity: Activity? = null, tag: String? = null) =
            manager(activity)?.setVisibility(tag, View.GONE)

        @JvmStatic
        @JvmOverloads
        fun show(activity: Activity? = null, tag: String? = null) =
            manager(activity)?.setVisibility(tag, View.VISIBLE)

        @JvmStatic
        @JvmOverloads
        fun setDragEnable(activity: Activity? = null, dragEnable: Boolean, tag: String? = null) =
            manager(activity)?.setDragEnable(dragEnable, tag)

        @JvmStatic
        @JvmOverloads
        fun isShow(activity: Activity? = null, tag: String? = null) = manager(activity)?.isShow(tag)

        /**
         * 获取我们传入的浮窗View
         */
        @JvmStatic
        @JvmOverloads
        fun getFloatView(activity: Activity? = null, tag: String? = null) =
            manager(activity)?.getFloatView(tag)

        /**
         * 获取Activity浮窗管理类
         */
        private fun manager(activity: Activity?) =
            (activity ?: activityWr?.get())?.run { ActivityFloatManager(this) }

        // *************************** 以下系统浮窗的相关方法 ***************************
        /**
         * 关闭系统级浮窗，发送广播消息，在Service内部接收广播
         */
        @JvmStatic
        @JvmOverloads
        fun dismissAppFloat(tag: String? = null) = FloatManager.dismiss(tag)

        /**
         * 隐藏系统浮窗，发送广播消息，在Service内部接收广播
         */
        @JvmStatic
        @JvmOverloads
        fun hideAppFloat(tag: String? = null) = FloatManager.visible(false, tag, false)

        /**
         * 显示系统浮窗，发送广播消息，在Service内部接收广播
         */
        @JvmStatic
        @JvmOverloads
        fun showAppFloat(tag: String? = null) = FloatManager.visible(true, tag, true)

        /**
         * 设置系统浮窗是否可拖拽，先获取浮窗的config，后修改相应属性
         */
        @JvmStatic
        @JvmOverloads
        fun appFloatDragEnable(dragEnable: Boolean, tag: String? = null) =
            getConfig(tag).let { it?.dragEnable = dragEnable }

        /**
         * 获取系统浮窗是否显示，通过浮窗的config，获取显示状态
         */
        @JvmStatic
        @JvmOverloads
        fun appFloatIsShow(tag: String? = null) = getConfig(tag) != null && getConfig(tag)!!.isShow

        /**
         * 获取系统浮窗中，我们传入的View
         */
        @JvmStatic
        @JvmOverloads
        fun getAppFloatView(tag: String? = null): View? = getConfig(tag)?.layoutView

        /**
         * 以下几个方法为：系统浮窗过滤页面的添加、移除、清空
         */
        @JvmStatic
        @JvmOverloads
        fun filterActivity(activity: Activity, tag: String? = null) =
            getFilterSet(tag)?.add(activity.componentName.className)

        @JvmStatic
        @JvmOverloads
        fun filterActivities(tag: String? = null, vararg clazz: Class<*>) =
            getFilterSet(tag)?.addAll(clazz.map { it.name })

        @JvmStatic
        @JvmOverloads
        fun removeFilter(activity: Activity, tag: String? = null) =
            getFilterSet(tag)?.remove(activity.componentName.className)

        @JvmStatic
        @JvmOverloads
        fun removeFilters(tag: String? = null, vararg clazz: Class<*>) =
            getFilterSet(tag)?.removeAll(clazz.map { it.name })

        @JvmStatic
        @JvmOverloads
        fun clearFilters(tag: String? = null) = getFilterSet(tag)?.clear()

        /**
         * 获取系统浮窗的config
         */
        private fun getConfig(tag: String?) = FloatManager.getAppFloatManager(tag)?.config

        private fun getFilterSet(tag: String?) = getConfig(tag)?.filterSet
    }


    /**
     * 浮窗的属性构建类，支持链式调用
     */
    class Builder(private val activity: Context) : OnPermissionResult {

        // 创建浮窗数据类，方便管理配置
        private val config = FloatConfig()

        fun setSidePattern(sidePattern: SidePattern) = apply { config.sidePattern = sidePattern }

        fun setShowPattern(showPattern: ShowPattern) = apply { config.showPattern = showPattern }

        @JvmOverloads
        fun setLayout(layoutId: Int, invokeView: OnInvokeView? = null) = apply {
            config.layoutId = layoutId
            config.invokeView = invokeView
        }

        @JvmOverloads
        fun setGravity(gravity: Int, offsetX: Int = 0, offsetY: Int = 0) = apply {
            config.gravity = gravity
            config.offsetPair = Pair(offsetX, offsetY)
        }

        fun setLocation(x: Int, y: Int) = apply { config.locationPair = Pair(x, y) }

        fun setTag(floatTag: String?) = apply { config.floatTag = floatTag }

        fun setDragEnable(dragEnable: Boolean) = apply { config.dragEnable = dragEnable }

        /**
         * 该方法针对系统浮窗，单页面浮窗无需设置
         */
        fun hasEditText(hasEditText: Boolean) = apply { config.hasEditText = hasEditText }

        @Deprecated("建议直接在 setLayout 设置详细布局")
        fun invokeView(invokeView: OnInvokeView) = apply { config.invokeView = invokeView }

        /**
         * 通过传统接口，进行浮窗的各种状态回调
         */
        fun registerCallbacks(callbacks: OnFloatCallbacks) = apply { config.callbacks = callbacks }

        /**
         * 针对kotlin 用户，传入带FloatCallbacks.Builder 返回值的 lambda，可按需回调
         * 为了避免方法重载时 出现编译错误的情况，更改了方法名
         */
        fun registerCallback(builder: FloatCallbacks.Builder.() -> Unit) =
            apply { config.floatCallbacks = FloatCallbacks().apply { registerListener(builder) } }

        fun setAnimator(floatAnimator: OnFloatAnimator?) =
            apply { config.floatAnimator = floatAnimator }

        fun setAppFloatAnimator(appFloatAnimator: OnAppFloatAnimator?) =
            apply { config.appFloatAnimator = appFloatAnimator }

        /**
         * 设置屏幕的有效显示高度（不包含虚拟导航栏的高度）
         */
        fun setDisplayHeight(displayHeight: OnDisplayHeight) =
            apply { config.displayHeight = displayHeight }

        fun setMatchParent(widthMatch: Boolean = false, heightMatch: Boolean = false) = apply {
            config.widthMatch = widthMatch
            config.heightMatch = heightMatch
        }

        /**
         * 设置需要过滤的Activity，仅对系统浮窗有效
         */
        fun setFilter(vararg clazz: Class<*>) = apply {
            clazz.forEach {
                config.filterSet.add(it.name)
                if (activity is Activity) {
                    // 过滤掉当前Activity
                    if (it.name == activity.componentName.className) config.filterSelf = true
                }
            }
        }

        /**
         * 创建浮窗，包括Activity浮窗和系统浮窗，如若系统浮窗无权限，先进行权限申请
         */
        fun show() = when {
            // 未设置浮窗布局文件，不予创建
            config.layoutId == null -> callbackCreateFailed(WARN_NO_LAYOUT)
            // 检测是否有初始化异常的情况，防止显示/过滤异常
            checkUninitialized() -> callbackCreateFailed(WARN_UNINITIALIZED)
            // 仅当页显示，则直接创建activity浮窗
            config.showPattern == ShowPattern.CURRENT_ACTIVITY -> createActivityFloat()
            // 系统浮窗需要先进行权限审核，有权限则创建app浮窗
            PermissionUtils.checkPermission(activity) -> createAppFloat()
            // 申请浮窗权限
            else -> requestPermission()
        }

        /**
         * 检测是否存在全局初始化的异常
         */
        private fun checkUninitialized() = when (config.showPattern) {
            ShowPattern.CURRENT_ACTIVITY -> false
            ShowPattern.FOREGROUND, ShowPattern.BACKGROUND -> !isInitialized
            ShowPattern.ALL_TIME -> config.filterSet.isNotEmpty() && !isInitialized
        }

        /**
         * 通过Activity浮窗管理类，创建Activity浮窗
         */
        private fun createActivityFloat() =
            if (activity is Activity) ActivityFloatManager(activity).createFloat(config)
            else callbackCreateFailed(WARN_CONTEXT_ACTIVITY)

        /**
         * 通过Service创建系统浮窗
         */
        private fun createAppFloat() = FloatManager.create(activity, config)

        /**
         * 通过Fragment去申请系统悬浮窗权限
         */
        private fun requestPermission() =
            if (activity is Activity) PermissionUtils.requestPermission(activity, this)
            else callbackCreateFailed(WARN_CONTEXT_REQUEST)

        /**
         * 申请浮窗权限的结果回调
         */
        override fun permissionResult(isOpen: Boolean) =
            if (isOpen) createAppFloat() else callbackCreateFailed(WARN_PERMISSION)

        /**
         * 回调创建失败
         */
        private fun callbackCreateFailed(reason: String) {
            config.callbacks?.createdResult(false, reason, null)
            config.floatCallbacks?.builder?.createdResult?.invoke(false, reason, null)
            Logger.w(reason)
            if (reason == WARN_NO_LAYOUT || reason == WARN_UNINITIALIZED || reason == WARN_CONTEXT_ACTIVITY) {
                // 针对无布局、未按需初始化、Activity浮窗上下文错误，直接抛异常
                throw Exception(reason)
            }
        }
    }

}