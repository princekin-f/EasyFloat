package com.lzf.easyfloat.utils

import android.view.*
import com.lzf.easyfloat.EasyFloat
import com.lzf.easyfloat.R
import com.lzf.easyfloat.anim.DefaultAnimator
import com.lzf.easyfloat.enums.ShowPattern
import com.lzf.easyfloat.enums.SidePattern
import com.lzf.easyfloat.interfaces.OnFloatAnimator
import com.lzf.easyfloat.interfaces.OnTouchRangeListener
import com.lzf.easyfloat.widget.BaseSwitchView

/**
 * @author: liuzhenfeng
 * @date: 2020/10/24  21:29
 * @Package: com.lzf.easyfloat.utils
 * @Description: 拖拽打开、关闭浮窗
 */
object DragUtils {

    private const val ADD_TAG = "ADD_TAG"
    private const val CLOSE_TAG = "CLOSE_TAG"
    private var addView: BaseSwitchView? = null
    private var closeView: BaseSwitchView? = null
    private var downX = 0f
    private var screenWidth = 0
    private var offset = 0f

    /**
     * 注册侧滑创建浮窗
     * @param event Activity 的触摸事件
     * @param listener 右下角区域触摸事件回调
     * @param layoutId 右下角区域的布局文件
     * @param slideOffset 当前屏幕侧滑进度
     * @param start 动画开始阈值
     * @param end 动画结束阈值
     */
    @JvmOverloads
    fun registerSwipeAdd(
        event: MotionEvent?,
        listener: OnTouchRangeListener? = null,
        layoutId: Int = R.layout.default_add_layout,
        slideOffset: Float = -1f,
        start: Float = 0.1f,
        end: Float = 0.5f
    ) {
        if (event == null) return

        // 设置了侧滑监听，使用侧滑数据
        if (slideOffset != -1f) {
            // 如果滑动偏移，超过了动画起始位置，开始显示浮窗，并执行偏移动画
            if (slideOffset >= start) {
                val progress = minOf((slideOffset - start) / (end - start), 1f)
                setAddView(event, progress, listener, layoutId)
            } else dismissAdd()
        } else {
            // 未提供侧滑监听，根据手指坐标信息，判断浮窗信息
            screenWidth = DisplayUtils.getScreenWidth(LifecycleUtils.application)
            offset = event.rawX / screenWidth
            when (event.action) {
                MotionEvent.ACTION_DOWN -> downX = event.rawX
                MotionEvent.ACTION_MOVE -> {
                    // 起始值小于最小边界值，并且当前偏离量大于最小边界
                    if (downX < start * screenWidth && offset >= start) {
                        val progress = minOf((offset - start) / (end - start), 1f)
                        setAddView(event, progress, listener, layoutId)
                    } else dismissAdd()
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    downX = 0f
                    setAddView(event, offset, listener, layoutId)
                }
            }
        }
    }

    private fun setAddView(
        event: MotionEvent,
        progress: Float,
        listener: OnTouchRangeListener? = null,
        layoutId: Int
    ) {
        // 设置触摸状态监听
        addView?.let {
            it.setTouchRangeListener(event, listener)
            it.translationX = it.width * (1 - progress)
            it.translationY = it.width * (1 - progress)
        }
        // 手指抬起或者事件取消，关闭添加浮窗
        if (event.action == MotionEvent.ACTION_UP || event.action == MotionEvent.ACTION_CANCEL) dismissAdd()
        else showAdd(layoutId)
    }

    private fun showAdd(layoutId: Int) {
        if (EasyFloat.isShow(ADD_TAG)) return
        EasyFloat.with(LifecycleUtils.application)
            .setLayout(layoutId)
            .setShowPattern(ShowPattern.CURRENT_ACTIVITY)
            .setTag(ADD_TAG)
            .setDragEnable(false)
            .setSidePattern(SidePattern.BOTTOM)
            .setGravity(Gravity.BOTTOM or Gravity.END)
            .setAnimator(null)
            .registerCallback {
                createResult { isCreated, _, view ->
                    if (!isCreated || view == null) return@createResult
                    if ((view as ViewGroup).childCount > 0) {
                        // 获取区间判断布局
                        view.getChildAt(0).apply {
                            if (this is BaseSwitchView) {
                                addView = this
                                translationX = width.toFloat()
                                translationY = width.toFloat()
                            }
                        }
                    }
                }
                dismiss { addView = null }
            }
            .show()
    }

    /**
     * 注册侧滑关闭浮窗
     * @param event 浮窗的触摸事件
     * @param listener 关闭区域触摸事件回调
     * @param layoutId 关闭区域的布局文件
     * @param showPattern 关闭区域的浮窗类型
     * @param appFloatAnimator 关闭区域的浮窗出入动画
     */
    @JvmOverloads
    fun registerDragClose(
        event: MotionEvent,
        listener: OnTouchRangeListener? = null,
        layoutId: Int = R.layout.default_close_layout,
        showPattern: ShowPattern = ShowPattern.CURRENT_ACTIVITY,
        appFloatAnimator: OnFloatAnimator? = DefaultAnimator()
    ) {
        showClose(layoutId, showPattern, appFloatAnimator)
        // 设置触摸状态监听
        closeView?.setTouchRangeListener(event, listener)
        // 抬起手指时，关闭删除选项
        if (event.action == MotionEvent.ACTION_UP || event.action == MotionEvent.ACTION_CANCEL) dismissClose()
    }

    private fun showClose(
        layoutId: Int,
        showPattern: ShowPattern,
        appFloatAnimator: OnFloatAnimator?
    ) {
        if (EasyFloat.isShow(CLOSE_TAG)) return
        EasyFloat.with(LifecycleUtils.application)
            .setLayout(layoutId)
            .setShowPattern(showPattern)
            .setMatchParent(widthMatch = true)
            .setTag(CLOSE_TAG)
            .setSidePattern(SidePattern.BOTTOM)
            .setGravity(Gravity.BOTTOM)
            .setAnimator(appFloatAnimator)
            .registerCallback {
                createResult { isCreated, _, view ->
                    if (!isCreated || view == null) return@createResult
                    if ((view as ViewGroup).childCount > 0) {
                        // 获取区间判断布局
                        view.getChildAt(0).apply { if (this is BaseSwitchView) closeView = this }
                    }
                }
                dismiss { closeView = null }
            }
            .show()
    }

    private fun dismissAdd() = EasyFloat.dismiss(ADD_TAG)

    private fun dismissClose() = EasyFloat.dismiss(CLOSE_TAG)

}