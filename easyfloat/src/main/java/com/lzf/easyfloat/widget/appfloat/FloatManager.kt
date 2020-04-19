package com.lzf.easyfloat.widget.appfloat

import android.content.Context
import android.view.View
import com.lzf.easyfloat.WARN_REPEATED_TAG
import com.lzf.easyfloat.data.FloatConfig
import com.lzf.easyfloat.utils.Logger

/**
 * @author: liuzhenfeng
 * @function: 系统浮窗的集合管理类，通过浮窗tag管理各个浮窗
 * @date: 2019-12-06  10:14
 */
internal object FloatManager {

    private const val DEFAULT_TAG = "default"
    val floatMap = mutableMapOf<String, AppFloatManager>()

    /**
     * 创建系统浮窗，首先检查浮窗是否存在：不存在则创建，存在则回调提示
     */
    fun create(context: Context, config: FloatConfig) = if (checkTag(config)) {
        // 通过floatManager创建浮窗，并将floatManager添加到map中
        floatMap[config.floatTag!!] = AppFloatManager(context.applicationContext, config)
            .apply { createFloat() }
    } else {
        config.callbacks?.createdResult(false, WARN_REPEATED_TAG, null)
        Logger.w(WARN_REPEATED_TAG)
    }

    /**
     * 设置浮窗的显隐，用户主动调用隐藏时，needShow需要为false
     */
    fun visible(
        isShow: Boolean,
        tag: String? = null,
        needShow: Boolean = floatMap[tag]?.config?.needShow ?: true
    ) = floatMap[getTag(tag)]?.setVisible(if (isShow) View.VISIBLE else View.GONE, needShow)

    /**
     * 关闭浮窗，执行浮窗的退出动画
     */
    fun dismiss(tag: String? = null) = floatMap[getTag(tag)]?.exitAnim()

    /**
     * 移除当条浮窗信息，在退出完成后调用
     */
    fun remove(floatTag: String?) = floatMap.remove(floatTag)

    /**
     * 获取浮窗tag，为空则使用默认值
     */
    fun getTag(tag: String?) = tag ?: DEFAULT_TAG

    /**
     * 获取具体的系统浮窗管理类
     */
    fun getAppFloatManager(tag: String?) = floatMap[getTag(tag)]

    /**
     * 检测浮窗的tag是否有效，不同的浮窗必须设置不同的tag
     */
    private fun checkTag(config: FloatConfig): Boolean {
        // 如果未设置tag，设置默认tag
        config.floatTag = getTag(config.floatTag)
        return !floatMap.containsKey(config.floatTag!!)
    }

}