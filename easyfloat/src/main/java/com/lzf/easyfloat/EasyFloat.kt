package com.lzf.easyfloat

import android.app.Activity
import android.content.Context
import android.view.View
import com.lzf.easyfloat.core.FloatingWindowManager
import com.lzf.easyfloat.data.FloatConfig
import com.lzf.easyfloat.enums.ShowPattern
import com.lzf.easyfloat.enums.SidePattern
import com.lzf.easyfloat.interfaces.*
import com.lzf.easyfloat.interfaces.OnPermissionResult
import com.lzf.easyfloat.permission.PermissionUtils
import com.lzf.easyfloat.utils.LifecycleUtils
import com.lzf.easyfloat.interfaces.FloatCallbacks
import com.lzf.easyfloat.utils.DisplayUtils
import com.lzf.easyfloat.utils.Logger
import java.lang.Exception

/**
 * @author: liuzhenfeng
 * @github：https://github.com/princekin-f
 * @function: 悬浮窗使用工具类
 * @date: 2019-06-27  15:22
 */
class EasyFloat {

    companion object {

        /**
         * 通过上下文，创建浮窗的构建者信息，使浮窗拥有一些默认属性
         * @param activity  上下文信息，优先使用Activity上下文，因为系统浮窗权限的自动申请，需要使用Activity信息
         * @return  浮窗属性构建者
         */
        @JvmStatic
        fun with(activity: Context): Builder = if (activity is Activity) Builder(activity)
        else Builder(LifecycleUtils.getTopActivity() ?: activity)

        /**
         * 关闭当前浮窗
         * @param tag       浮窗标签
         * @param force     立即关闭，有退出动画也不执行
         */
        @JvmStatic
        @JvmOverloads
        fun dismiss(tag: String? = null, force: Boolean = false) =
            FloatingWindowManager.dismiss(tag, force)

        /**
         * 隐藏当前浮窗
         * @param tag   浮窗标签
         */
        @JvmStatic
        @JvmOverloads
        fun hide(tag: String? = null) = FloatingWindowManager.visible(false, tag, false)

        /**
         * 设置当前浮窗可见
         * @param tag   浮窗标签
         */
        @JvmStatic
        @JvmOverloads
        fun show(tag: String? = null) = FloatingWindowManager.visible(true, tag, true)

        /**
         * 设置当前浮窗是否可拖拽，先获取浮窗的config，后修改相应属性
         * @param dragEnable    是否可拖拽
         * @param tag           浮窗标签
         */
        @JvmStatic
        @JvmOverloads
        fun dragEnable(dragEnable: Boolean, tag: String? = null) =
            getConfig(tag)?.let { it.dragEnable = dragEnable }

        /**
         * 获取当前浮窗是否显示，通过浮窗的config，获取显示状态
         * @param tag   浮窗标签
         * @return  当前浮窗是否显示
         */
        @JvmStatic
        @JvmOverloads
        fun isShow(tag: String? = null) = getConfig(tag)?.isShow ?: false

        /**
         * 获取当前浮窗中，我们传入的View
         * @param tag   浮窗标签
         */
        @JvmStatic
        @JvmOverloads
        fun getFloatView(tag: String? = null): View? = getConfig(tag)?.layoutView

        /**
         * 更新浮窗坐标、以及大小，未指定数值（全为-1）执行吸附动画；
         * 需要修改的参数，传入具体数值，不需要修改的参数保持-1即可
         * @param tag       浮窗标签
         * @param x         更新后的X轴坐标
         * @param y         更新后的Y轴坐标
         * @param width     更新后的宽度
         * @param height    更新后的高度
         */
        @JvmStatic
        @JvmOverloads
        fun updateFloat(
            tag: String? = null,
            x: Int = -1,
            y: Int = -1,
            width: Int = -1,
            height: Int = -1
        ) = FloatingWindowManager.getHelper(tag)?.updateFloat(x, y, width, height)

        // 以下几个方法为：系统浮窗过滤页面的添加、移除、清空
        /**
         * 为当前浮窗过滤，设置需要过滤的Activity
         * @param activity  需要过滤的Activity
         * @param tag       浮窗标签
         */
        @JvmStatic
        @JvmOverloads
        fun filterActivity(activity: Activity, tag: String? = null) =
            getFilterSet(tag)?.add(activity.componentName.className)

        /**
         * 为当前浮窗，设置需要过滤的Activity类名（一个或者多个）
         * @param tag       浮窗标签
         * @param clazz     需要过滤的Activity类名，一个或者多个
         */
        @JvmStatic
        @JvmOverloads
        fun filterActivities(tag: String? = null, vararg clazz: Class<*>) =
            getFilterSet(tag)?.addAll(clazz.map { it.name })

        /**
         * 为当前浮窗，移除需要过滤的Activity
         * @param activity  需要移除过滤的Activity
         * @param tag       浮窗标签
         */
        @JvmStatic
        @JvmOverloads
        fun removeFilter(activity: Activity, tag: String? = null) =
            getFilterSet(tag)?.remove(activity.componentName.className)

        /**
         * 为当前浮窗，移除需要过滤的Activity类名（一个或者多个）
         * @param tag       浮窗标签
         * @param clazz     需要移除过滤的Activity类名，一个或者多个
         */
        @JvmStatic
        @JvmOverloads
        fun removeFilters(tag: String? = null, vararg clazz: Class<*>) =
            getFilterSet(tag)?.removeAll(clazz.map { it.name })

        /**
         * 清除当前浮窗的所有过滤信息
         * @param tag   浮窗标签
         */
        @JvmStatic
        @JvmOverloads
        fun clearFilters(tag: String? = null) = getFilterSet(tag)?.clear()

        /**
         * 获取当前浮窗的config
         * @param tag   浮窗标签
         */
        private fun getConfig(tag: String?) = FloatingWindowManager.getHelper(tag)?.config

        /**
         * 获取当前浮窗的过滤集合
         * @param tag   浮窗标签
         */
        private fun getFilterSet(tag: String?) = getConfig(tag)?.filterSet
    }


    /**
     * 浮窗的属性构建类，支持链式调用
     */
    class Builder(private val activity: Context) : OnPermissionResult {

        // 创建浮窗数据类，方便管理配置
        private val config = FloatConfig()

        /**
         * 设置浮窗的吸附模式
         * @param sidePattern   浮窗吸附模式
         */
        fun setSidePattern(sidePattern: SidePattern) = apply { config.sidePattern = sidePattern }

        /**
         * 设置浮窗的显示模式
         * @param showPattern   浮窗显示模式
         */
        fun setShowPattern(showPattern: ShowPattern) = apply { config.showPattern = showPattern }

        /**
         * 设置浮窗的布局文件，以及布局的操作接口
         * @param layoutId      布局文件的资源Id
         * @param invokeView    布局文件的操作接口
         */
        @JvmOverloads
        fun setLayout(layoutId: Int, invokeView: OnInvokeView? = null) = apply {
            config.layoutId = layoutId
            config.invokeView = invokeView
        }

        /**
         * 设置浮窗的布局视图，以及布局的操作接口
         * @param layoutView    自定义的布局视图
         * @param invokeView    布局视图的操作接口
         */
        @JvmOverloads
        fun setLayout(layoutView: View, invokeView: OnInvokeView? = null) = apply {
            config.layoutView = layoutView
            config.invokeView = invokeView
        }

        /**
         * 设置浮窗的对齐方式，以及偏移量
         * @param gravity   对齐方式
         * @param offsetX   目标坐标的水平偏移量
         * @param offsetY   目标坐标的竖直偏移量
         */
        @JvmOverloads
        fun setGravity(gravity: Int, offsetX: Int = 0, offsetY: Int = 0) = apply {
            config.gravity = gravity
            config.offsetPair = Pair(offsetX, offsetY)
        }

        /**
         * 当layout大小变化后，整体view的位置的对齐方式
         * 比如，当设置为 Gravity.END 时，当view的宽度变小或者变大时，都将会以原有的右边对齐 <br/>
         * 默认对齐方式为左上角
         * @param gravity   对齐方式
         */
        fun setLayoutChangedGravity(gravity: Int) = apply {
            config.layoutChangedGravity = gravity;
        }

        /**
         * 设置浮窗的起始坐标，优先级高于setGravity
         * @param x     起始水平坐标
         * @param y     起始竖直坐标
         */
        fun setLocation(x: Int, y: Int) = apply { config.locationPair = Pair(x, y) }

        /**
         * 设置浮窗的拖拽边距值
         * @param left      浮窗左侧边距
         * @param top       浮窗顶部边距
         * @param right     浮窗右侧边距
         * @param bottom    浮窗底部边距
         */
        @JvmOverloads
        fun setBorder(
            left: Int = 0,
            top: Int = -DisplayUtils.getStatusBarHeight(activity),
            right: Int = DisplayUtils.getScreenWidth(activity),
            bottom: Int = DisplayUtils.getScreenHeight(activity)
        ) = apply {
            config.leftBorder = left
            config.topBorder = top
            config.rightBorder = right
            config.bottomBorder = bottom
        }

        /**
         * 设置浮窗的标签：只有一个浮窗时，可以不设置；
         * 有多个浮窗必须设置不容的浮窗，不然没法管理，所以禁止创建相同标签的浮窗
         * @param floatTag      浮窗标签
         */
        fun setTag(floatTag: String?) = apply { config.floatTag = floatTag }

        /**
         * 设置浮窗是否可拖拽
         * @param dragEnable    是否可拖拽
         */
        fun setDragEnable(dragEnable: Boolean) = apply { config.dragEnable = dragEnable }

        /**
         * 设置浮窗是否状态栏沉浸
         * @param immersionStatusBar    是否状态栏沉浸
         */
        fun setImmersionStatusBar(immersionStatusBar: Boolean) =
            apply { config.immersionStatusBar = immersionStatusBar }

        /**
         * 浮窗是否包含EditText，浮窗默认不获取焦点，无法弹起软键盘，所以需要适配
         * @param hasEditText   是否包含EditText
         */
        fun hasEditText(hasEditText: Boolean) = apply { config.hasEditText = hasEditText }

        /**
         * 通过传统接口，进行浮窗的各种状态回调
         * @param callbacks     浮窗的各种事件回调
         */
        fun registerCallbacks(callbacks: OnFloatCallbacks) = apply { config.callbacks = callbacks }

        /**
         * 针对kotlin 用户，传入带FloatCallbacks.Builder 返回值的 lambda，可按需回调
         * 为了避免方法重载时 出现编译错误的情况，更改了方法名
         * @param builder   事件回调的构建者
         */
        fun registerCallback(builder: FloatCallbacks.Builder.() -> Unit) =
            apply { config.floatCallbacks = FloatCallbacks().apply { registerListener(builder) } }

        /**
         * 设置浮窗的出入动画
         * @param floatAnimator     浮窗的出入动画，为空时不执行动画
         */
        fun setAnimator(floatAnimator: OnFloatAnimator?) =
            apply { config.floatAnimator = floatAnimator }

        /**
         * 设置屏幕的有效显示高度（不包含虚拟导航栏的高度）
         * @param displayHeight     屏幕的有效高度
         */
        fun setDisplayHeight(displayHeight: OnDisplayHeight) =
            apply { config.displayHeight = displayHeight }

        /**
         * 设置浮窗宽高是否充满屏幕
         * @param widthMatch    宽度是否充满屏幕
         * @param heightMatch   高度是否充满屏幕
         */
        fun setMatchParent(widthMatch: Boolean = false, heightMatch: Boolean = false) = apply {
            config.widthMatch = widthMatch
            config.heightMatch = heightMatch
        }

        /**
         * 设置需要过滤的Activity类名，仅对系统浮窗有效
         * @param clazz     需要过滤的Activity类名
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
            // 未设置浮窗布局文件/布局视图，不予创建
            config.layoutId == null && config.layoutView == null ->
                callbackCreateFailed(WARN_NO_LAYOUT)
            // 仅当页显示，则直接创建activity浮窗
            config.showPattern == ShowPattern.CURRENT_ACTIVITY -> createFloat()
            // 系统浮窗需要先进行权限审核，有权限则创建app浮窗
            PermissionUtils.checkPermission(activity) -> createFloat()
            // 申请浮窗权限
            else -> requestPermission()
        }

        /**
         * 通过浮窗管理类，统一创建浮窗
         */
        private fun createFloat() = FloatingWindowManager.create(activity, config)

        /**
         * 通过Fragment去申请系统悬浮窗权限
         */
        private fun requestPermission() =
            if (activity is Activity) PermissionUtils.requestPermission(activity, this)
            else callbackCreateFailed(WARN_CONTEXT_REQUEST)

        /**
         * 申请浮窗权限的结果回调
         * @param isOpen    悬浮窗权限是否打开
         */
        override fun permissionResult(isOpen: Boolean) =
            if (isOpen) createFloat() else callbackCreateFailed(WARN_PERMISSION)

        /**
         * 回调创建失败
         * @param reason    失败原因
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