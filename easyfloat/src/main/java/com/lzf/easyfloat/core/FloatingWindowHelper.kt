package com.lzf.easyfloat.core

import android.animation.Animator
import android.annotation.SuppressLint
import android.app.Activity
import android.app.Service
import android.content.Context
import android.graphics.PixelFormat
import android.graphics.Rect
import android.os.Build
import android.os.IBinder
import android.view.*
import android.view.WindowManager.LayoutParams.*
import android.widget.EditText
import com.lzf.easyfloat.anim.AnimatorManager
import com.lzf.easyfloat.data.FloatConfig
import com.lzf.easyfloat.enums.ShowPattern
import com.lzf.easyfloat.interfaces.OnFloatTouchListener
import com.lzf.easyfloat.utils.DisplayUtils
import com.lzf.easyfloat.utils.InputMethodUtils
import com.lzf.easyfloat.utils.LifecycleUtils
import com.lzf.easyfloat.utils.Logger
import com.lzf.easyfloat.widget.ParentFrameLayout

/**
 * @author: Liuzhenfeng
 * @date: 12/1/20  23:40
 * @Description:
 */
internal class FloatingWindowHelper(val context: Context, var config: FloatConfig) {

    lateinit var windowManager: WindowManager
    lateinit var params: WindowManager.LayoutParams
    var frameLayout: ParentFrameLayout? = null
    private lateinit var touchUtils: TouchUtils
    private var enterAnimator: Animator? = null
    private var lastLayoutMeasureWidth = -1
    private var lastLayoutMeasureHeight = -1

    fun createWindow(): Boolean = if (getToken() == null) {
        val activity = if (context is Activity) context else LifecycleUtils.getTopActivity()
        activity?.findViewById<View>(android.R.id.content)?.post { createWindowInner() } ?: false
    } else {
        createWindowInner()
    }

    private fun createWindowInner(): Boolean = try {
        touchUtils = TouchUtils(context, config)
        initParams()
        addView()
        config.isShow = true
        true
    } catch (e: Exception) {
        config.callbacks?.createdResult(false, "$e", null)
        config.floatCallbacks?.builder?.createdResult?.invoke(false, "$e", null)
        false
    }

    private fun initParams() {
        windowManager = context.getSystemService(Service.WINDOW_SERVICE) as WindowManager
        params = WindowManager.LayoutParams().apply {
            if (config.showPattern == ShowPattern.CURRENT_ACTIVITY) {
                // 设置窗口类型为应用子窗口，和PopupWindow同类型
                type = TYPE_APPLICATION_PANEL
                // 子窗口必须和创建它的Activity的windowToken绑定
                token = getToken()
            } else {
                // 系统全局窗口，可覆盖在任何应用之上，以及单独显示在桌面上
                // 安卓6.0 以后，全局的Window类别，必须使用TYPE_APPLICATION_OVERLAY
                type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) TYPE_APPLICATION_OVERLAY
                else TYPE_PHONE
            }
            format = PixelFormat.RGBA_8888
            gravity = Gravity.START or Gravity.TOP
            // 设置浮窗以外的触摸事件可以传递给后面的窗口、不自动获取焦点
            flags = if (config.immersionStatusBar)
            // 没有边界限制，允许窗口扩展到屏幕外
                FLAG_NOT_TOUCH_MODAL or FLAG_NOT_FOCUSABLE or FLAG_LAYOUT_NO_LIMITS
            else FLAG_NOT_TOUCH_MODAL or FLAG_NOT_FOCUSABLE
            width = if (config.widthMatch) MATCH_PARENT else WRAP_CONTENT
            height = if (config.heightMatch) MATCH_PARENT else WRAP_CONTENT

            if (config.immersionStatusBar && config.heightMatch) {
                height = DisplayUtils.getScreenHeight(context)
            }

            // 如若设置了固定坐标，直接定位
            if (config.locationPair != Pair(0, 0)) {
                x = config.locationPair.first
                y = config.locationPair.second
            }
        }
    }

    private fun getToken(): IBinder? {
        val activity = if (context is Activity) context else LifecycleUtils.getTopActivity()
        return activity?.window?.decorView?.windowToken
    }

    /**
     * 将自定义的布局，作为xml布局的父布局，添加到windowManager中，
     * 重写自定义布局的touch事件，实现拖拽效果。
     */
    private fun addView() {
        // 创建一个frameLayout作为浮窗布局的父容器
        frameLayout = ParentFrameLayout(context, config)
        frameLayout?.tag = config.floatTag
        // 将浮窗布局文件添加到父容器frameLayout中，并返回该浮窗文件
        val floatingView = config.layoutView?.also { frameLayout?.addView(it) }
            ?: LayoutInflater.from(context).inflate(config.layoutId!!, frameLayout, true)
        // 为了避免创建的时候闪一下，我们先隐藏视图，不能直接设置GONE，否则定位会出现问题
        floatingView.visibility = View.INVISIBLE
        // 将frameLayout添加到系统windowManager中
        windowManager.addView(frameLayout, params)

        // 通过重写frameLayout的Touch事件，实现拖拽效果
        frameLayout?.touchListener = object : OnFloatTouchListener {
            override fun onTouch(event: MotionEvent) =
                touchUtils.updateFloat(frameLayout!!, event, windowManager, params)
        }

        // 在浮窗绘制完成的时候，设置初始坐标、执行入场动画
        frameLayout?.layoutListener = object : ParentFrameLayout.OnLayoutListener {
            override fun onLayout() {
                setGravity(frameLayout)
                lastLayoutMeasureWidth = frameLayout?.measuredWidth ?: -1
                lastLayoutMeasureHeight = frameLayout?.measuredHeight ?: -1
                config.apply {
                    // 如果设置了过滤当前页，或者后台显示前台创建、前台显示后台创建，隐藏浮窗，否则执行入场动画
                    if (filterSelf
                        || (showPattern == ShowPattern.BACKGROUND && LifecycleUtils.isForeground())
                        || (showPattern == ShowPattern.FOREGROUND && !LifecycleUtils.isForeground())
                    ) {
                        setVisible(View.GONE)
                        initEditText()
                    } else enterAnim(floatingView)

                    // 设置callbacks
                    layoutView = floatingView
                    invokeView?.invoke(floatingView)
                    callbacks?.createdResult(true, null, floatingView)
                    floatCallbacks?.builder?.createdResult?.invoke(true, null, floatingView)
                }
            }
        }

        setChangedListener()
    }

    /**
     * 设置布局变化监听，根据变化时的对齐方式，设置浮窗位置
     */
    private fun setChangedListener() {
        frameLayout?.apply {
            // 监听frameLayout布局完成
            viewTreeObserver?.addOnGlobalLayoutListener {
                val filterInvalidVal = lastLayoutMeasureWidth == -1 || lastLayoutMeasureHeight == -1
                val filterEqualVal =
                    lastLayoutMeasureWidth == this.measuredWidth && lastLayoutMeasureHeight == this.measuredHeight
                if (filterInvalidVal || filterEqualVal) {
                    return@addOnGlobalLayoutListener
                }

                // 水平方向
                if (config.layoutChangedGravity.and(Gravity.START) == Gravity.START) {
                    // ignore

                } else if (config.layoutChangedGravity.and(Gravity.END) == Gravity.END) {
                    val diffChangedSize = this.measuredWidth - lastLayoutMeasureWidth
                    params.x = params.x - diffChangedSize

                } else if (config.layoutChangedGravity.and(Gravity.CENTER_HORIZONTAL) == Gravity.CENTER_HORIZONTAL
                    || config.layoutChangedGravity.and(Gravity.CENTER) == Gravity.CENTER
                ) {
                    val diffChangedCenter = lastLayoutMeasureWidth / 2 - this.measuredWidth / 2
                    params.x += diffChangedCenter
                }

                // 垂直方向
                if (config.layoutChangedGravity.and(Gravity.TOP) == Gravity.TOP) {
                    // ignore

                } else if (config.layoutChangedGravity.and(Gravity.BOTTOM) == Gravity.BOTTOM) {
                    val diffChangedSize = this.measuredHeight - lastLayoutMeasureHeight
                    params.y = params.y - diffChangedSize

                } else if (config.layoutChangedGravity.and(Gravity.CENTER_VERTICAL) == Gravity.CENTER_VERTICAL
                    || config.layoutChangedGravity.and(Gravity.CENTER) == Gravity.CENTER
                ) {
                    val diffChangedCenter = lastLayoutMeasureHeight / 2 - this.measuredHeight / 2
                    params.y += diffChangedCenter
                }

                lastLayoutMeasureWidth = this.measuredWidth
                lastLayoutMeasureHeight = this.measuredHeight

                // 更新浮窗位置信息
                windowManager.updateViewLayout(frameLayout, params)
            }
        }
    }

    private fun initEditText() {
        if (config.hasEditText) frameLayout?.let { traverseViewGroup(it) }
    }

    private fun traverseViewGroup(view: View?) {
        view?.let {
            // 遍历ViewGroup，是子view判断是否是EditText，是ViewGroup递归调用
            if (it is ViewGroup) for (i in 0 until it.childCount) {
                val child = it.getChildAt(i)
                if (child is ViewGroup) traverseViewGroup(child) else checkEditText(child)
            } else checkEditText(it)
        }
    }

    private fun checkEditText(view: View) {
        if (view is EditText) InputMethodUtils.initInputMethod(view, config.floatTag)
    }


    /**
     * 设置浮窗的对齐方式，支持上下左右、居中、上中、下中、左中和右中，默认左上角
     * 支持手动设置的偏移量
     */
    @SuppressLint("RtlHardcoded")
    private fun setGravity(view: View?) {
        if (config.locationPair != Pair(0, 0) || view == null) return
        val parentRect = Rect()
        // 获取浮窗所在的矩形
        windowManager.defaultDisplay.getRectSize(parentRect)
        val location = IntArray(2)
        // 获取在整个屏幕内的绝对坐标
        view.getLocationOnScreen(location)
        // 通过绝对高度和相对高度比较，判断包含顶部状态栏
        val statusBarHeight = if (location[1] > params.y) DisplayUtils.statusBarHeight(view) else 0
        val parentBottom =
            config.displayHeight.getDisplayRealHeight(context) - statusBarHeight
        when (config.gravity) {
            // 右上
            Gravity.END, Gravity.END or Gravity.TOP, Gravity.RIGHT, Gravity.RIGHT or Gravity.TOP ->
                params.x = parentRect.right - view.width
            // 左下
            Gravity.START or Gravity.BOTTOM, Gravity.BOTTOM, Gravity.LEFT or Gravity.BOTTOM ->
                params.y = parentBottom - view.height
            // 右下
            Gravity.END or Gravity.BOTTOM, Gravity.RIGHT or Gravity.BOTTOM -> {
                params.x = parentRect.right - view.width
                params.y = parentBottom - view.height
            }
            // 居中
            Gravity.CENTER -> {
                params.x = (parentRect.right - view.width).shr(1)
                params.y = (parentBottom - view.height).shr(1)
            }
            // 上中
            Gravity.CENTER_HORIZONTAL, Gravity.TOP or Gravity.CENTER_HORIZONTAL ->
                params.x = (parentRect.right - view.width).shr(1)
            // 下中
            Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL -> {
                params.x = (parentRect.right - view.width).shr(1)
                params.y = parentBottom - view.height
            }
            // 左中
            Gravity.CENTER_VERTICAL, Gravity.START or Gravity.CENTER_VERTICAL, Gravity.LEFT or Gravity.CENTER_VERTICAL ->
                params.y = (parentBottom - view.height).shr(1)
            // 右中
            Gravity.END or Gravity.CENTER_VERTICAL, Gravity.RIGHT or Gravity.CENTER_VERTICAL -> {
                params.x = parentRect.right - view.width
                params.y = (parentBottom - view.height).shr(1)
            }
            // 其他情况，均视为左上
            else -> {
            }
        }

        // 设置偏移量
        params.x += config.offsetPair.first
        params.y += config.offsetPair.second

        if (config.immersionStatusBar) {
            if (config.showPattern != ShowPattern.CURRENT_ACTIVITY) {
                params.y -= statusBarHeight
            }
        } else {
            if (config.showPattern == ShowPattern.CURRENT_ACTIVITY) {
                params.y += statusBarHeight
            }
        }
        // 更新浮窗位置信息
        windowManager.updateViewLayout(view, params)
    }

    /**
     * 设置浮窗的可见性
     */
    fun setVisible(visible: Int, needShow: Boolean = true) {
        if (frameLayout == null || frameLayout!!.childCount < 1) return
        // 如果用户主动隐藏浮窗，则该值为false
        config.needShow = needShow
        frameLayout!!.visibility = visible
        val view = frameLayout!!.getChildAt(0)
        if (visible == View.VISIBLE) {
            config.isShow = true
            config.callbacks?.show(view)
            config.floatCallbacks?.builder?.show?.invoke(view)
        } else {
            config.isShow = false
            config.callbacks?.hide(view)
            config.floatCallbacks?.builder?.hide?.invoke(view)
        }
    }

    /**
     * 入场动画
     */
    private fun enterAnim(floatingView: View) {
        if (frameLayout == null || config.isAnim) return
        enterAnimator = AnimatorManager(frameLayout!!, params, windowManager, config)
            .enterAnim()?.apply {
                // 可以延伸到屏幕外，动画结束按需去除该属性，不然旋转屏幕可能置于屏幕外部
                params.flags =
                    FLAG_NOT_TOUCH_MODAL or FLAG_NOT_FOCUSABLE or FLAG_LAYOUT_NO_LIMITS

                addListener(object : Animator.AnimatorListener {
                    override fun onAnimationRepeat(animation: Animator?) {}

                    override fun onAnimationEnd(animation: Animator?) {
                        config.isAnim = false
                        if (!config.immersionStatusBar) {
                            // 不需要延伸到屏幕外了，防止屏幕旋转的时候，浮窗处于屏幕外
                            params.flags = FLAG_NOT_TOUCH_MODAL or FLAG_NOT_FOCUSABLE
                        }
                        initEditText()
                    }

                    override fun onAnimationCancel(animation: Animator?) {}

                    override fun onAnimationStart(animation: Animator?) {
                        floatingView.visibility = View.VISIBLE
                        config.isAnim = true
                    }
                })
                start()
            }
        if (enterAnimator == null) {
            floatingView.visibility = View.VISIBLE
            windowManager.updateViewLayout(floatingView, params)
        }
    }

    /**
     * 退出动画
     */
    fun exitAnim() {
        if (frameLayout == null || (config.isAnim && enterAnimator == null)) return
        enterAnimator?.cancel()
        val animator: Animator? =
            AnimatorManager(frameLayout!!, params, windowManager, config).exitAnim()
        if (animator == null) remove() else {
            // 二次判断，防止重复调用引发异常
            if (config.isAnim) return
            config.isAnim = true
            params.flags = FLAG_NOT_TOUCH_MODAL or FLAG_NOT_FOCUSABLE or FLAG_LAYOUT_NO_LIMITS
            animator.addListener(object : Animator.AnimatorListener {
                override fun onAnimationRepeat(animation: Animator?) {}

                override fun onAnimationEnd(animation: Animator?) = remove()

                override fun onAnimationCancel(animation: Animator?) {}

                override fun onAnimationStart(animation: Animator?) {}
            })
            animator.start()
        }
    }

    /**
     * 退出动画执行结束/没有退出动画，进行回调、移除等操作
     */
    fun remove(force: Boolean = false) = try {
        config.isAnim = false
        FloatingWindowManager.remove(config.floatTag)
        // removeView是异步删除，在Activity销毁的时候会导致窗口泄漏，所以使用removeViewImmediate直接删除view
        windowManager.run { if (force) removeViewImmediate(frameLayout) else removeView(frameLayout) }
    } catch (e: Exception) {
        Logger.e("浮窗关闭出现异常：$e")
    }

    /**
     * 更新浮窗坐标
     */
    fun updateFloat(x: Int, y: Int) {
        frameLayout?.let {
            if (x == -1 && y == -1) {
                // 未指定具体坐标，执行吸附动画
                it.postDelayed({ touchUtils.updateFloat(it, params, windowManager) }, 200)
            } else {
                params.x = x
                params.y = y
                windowManager.updateViewLayout(it, params)
            }
        }
    }
}