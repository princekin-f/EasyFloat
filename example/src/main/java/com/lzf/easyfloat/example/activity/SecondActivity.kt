package com.lzf.easyfloat.example.activity

import android.animation.Animator
import android.annotation.SuppressLint
import android.os.Bundle
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.view.animation.BounceInterpolator
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.lzf.easyfloat.EasyFloat
import com.lzf.easyfloat.anim.DefaultAnimator
import com.lzf.easyfloat.enums.ShowPattern
import com.lzf.easyfloat.enums.SidePattern
import com.lzf.easyfloat.example.R
import com.lzf.easyfloat.example.startActivity
import com.lzf.easyfloat.interfaces.OnDisplayHeight
import com.lzf.easyfloat.interfaces.OnFloatCallbacks
import com.lzf.easyfloat.utils.DisplayUtils
import kotlin.random.Random

/**
 * @author: liuzhenfeng
 * @function:
 * @date: 2019-06-28  16:10
 */
class SecondActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_second)

        findViewById<TextView>(R.id.tvShow).setOnClickListener {
            EasyFloat.with(this)
                .setLayout(R.layout.float_top_dialog) {
                    it.postDelayed({ EasyFloat.dismiss(it.tag.toString()) }, 2333)
                }
                .setMatchParent(true)
                .setSidePattern(SidePattern.TOP)
                .setDragEnable(false)
                .setTag(Random.nextDouble().toString())
                .setAnimator(object : DefaultAnimator() {
                    override fun enterAnim(
                        view: View,
                        params: WindowManager.LayoutParams,
                        windowManager: WindowManager,
                        sidePattern: SidePattern
                    ): Animator? =
                        super.enterAnim(view, params, windowManager, sidePattern)?.apply {
                            interpolator = BounceInterpolator()
                        }

                    override fun exitAnim(
                        view: View,
                        params: WindowManager.LayoutParams,
                        windowManager: WindowManager,
                        sidePattern: SidePattern
                    ): Animator? =
                        super.exitAnim(view, params, windowManager, sidePattern)?.setDuration(200)
                })
                .show()
        }

        findViewById<TextView>(R.id.openEditTextFloat).setOnClickListener { showEditTextFloat() }

        findViewById<TextView>(R.id.openJavaTestActivity).setOnClickListener { startActivity<JavaTestActivity>(this) }

        findViewById<TextView>(R.id.changeBackground).setOnClickListener {
            EasyFloat.getFloatView()?.apply {
                findViewById<RelativeLayout>(R.id.rlContent)
                    .setBackgroundColor(ContextCompat.getColor(this@SecondActivity, R.color.violet))

                // ...其他View操作
            }
        }

        findViewById<TextView>(R.id.recoverBackground).setOnClickListener {
            EasyFloat.getFloatView()?.findViewById<RelativeLayout>(R.id.rlContent)
                ?.setBackgroundColor(ContextCompat.getColor(this, R.color.translucent))
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun showEditTextFloat(tag: String? = "editTextFloat") {
        EasyFloat.with(this)
            .setShowPattern(ShowPattern.ALL_TIME)
            .setGravity(Gravity.CENTER, 0, -300)
            .setTag(tag)
            .hasEditText(true)
            .setLayout(R.layout.float_edit) {
                // 注意看注释！
//                it.findViewById<EditText>(R.id.editText).apply {
//                    setOnTouchListener { _, event ->
//                        // 如果设置了setOnTouchListener，需要在ACTION_DOWN时手动打开软键盘
//                        // 如果未设置触摸监听，无需此操作，EasyFloat内部已经监听
//                        if (event.action == MotionEvent.ACTION_DOWN) {
//                            InputMethodUtils.openInputMethod(this, tag)
//                        }
//
//                        // ....
//                        // 其他业务逻辑....
//                        false
//                    }
//                }

                it.findViewById<TextView>(R.id.tvCloseFloat).setOnClickListener {
                    EasyFloat.dismiss(tag)
                }
            }
            .show()
    }

    private fun showFloat() {

        EasyFloat.with(this).setLayout(R.layout.float_app).show()

        EasyFloat.with(this)
            // 设置浮窗xml布局文件，并可设置详细信息
            .setLayout(R.layout.float_app) { }
            // 设置浮窗显示类型，默认只在当前Activity显示，可选一直显示、仅前台显示
            .setShowPattern(ShowPattern.ALL_TIME)
            // 设置吸附方式，共15种模式，详情参考SidePattern
            .setSidePattern(SidePattern.RESULT_HORIZONTAL)
            // 设置浮窗的标签，用于区分多个浮窗
            .setTag("testFloat")
            // 设置浮窗是否可拖拽
            .setDragEnable(true)
            // 浮窗是否包含EditText，默认不包含
            .hasEditText(false)
            // 设置浮窗固定坐标，ps：设置固定坐标，Gravity属性和offset属性将无效
            .setLocation(100, 200)
            // 设置浮窗的对齐方式和坐标偏移量
            .setGravity(Gravity.END or Gravity.CENTER_VERTICAL, 0, 200)
            // 设置拖拽边界值
            .setBorder(100, 100, 800, 800)
            // 设置宽高是否充满父布局，直接在xml设置match_parent属性无效
            .setMatchParent(widthMatch = false, heightMatch = false)
            // 设置浮窗的出入动画，可自定义，实现相应接口即可（策略模式），无需动画直接设置为null
            .setAnimator(DefaultAnimator())
            // 设置系统浮窗的不需要显示的页面
            .setFilter(MainActivity::class.java, SecondActivity::class.java)
            // 设置系统浮窗的有效显示高度（不包含虚拟导航栏的高度），基本用不到，除非有虚拟导航栏适配问题
            .setDisplayHeight { context -> DisplayUtils.rejectedNavHeight(context) }
            // 浮窗的一些状态回调，如：创建结果、显示、隐藏、销毁、touchEvent、拖拽过程、拖拽结束。
            // ps：通过Kotlin DSL实现的回调，可以按需复写方法，用到哪个写哪个
            .registerCallback {
                createResult { isCreated, msg, view -> }
                show { }
                hide { }
                dismiss { }
                touchEvent { view, motionEvent -> }
                drag { view, motionEvent -> }
                dragEnd { }
            }
            .registerCallbacks(object : OnFloatCallbacks {
                override fun createdResult(isCreated: Boolean, msg: String?, view: View?) {}

                override fun show(view: View) {}

                override fun hide(view: View) {}

                override fun dismiss() {}

                override fun touchEvent(view: View, event: MotionEvent) {}

                override fun drag(view: View, event: MotionEvent) {}

                override fun dragEnd(view: View) {}
            })
            // 创建浮窗（这是关键哦😂）
            .show()
    }

}