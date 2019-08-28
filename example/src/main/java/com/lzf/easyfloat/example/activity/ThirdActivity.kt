package com.lzf.easyfloat.example.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.widget.EditText
import android.widget.TextView
import com.lzf.easyfloat.EasyFloat
import com.lzf.easyfloat.anim.AppFloatDefaultAnimator
import com.lzf.easyfloat.anim.DefaultAnimator
import com.lzf.easyfloat.enums.ShowPattern
import com.lzf.easyfloat.enums.SidePattern
import com.lzf.easyfloat.example.R
import com.lzf.easyfloat.interfaces.OnFloatCallbacks
import com.lzf.easyfloat.interfaces.OnInvokeView
import com.lzf.easyfloat.utils.InputMethodUtils
import com.lzf.easyfloat.utils.floatNotification
import kotlinx.android.synthetic.main.activity_third.*

/**
 * @author: liuzhenfeng
 * @function: 测试EditText
 * @date: 2019-07-26  13:13
 */
class ThirdActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_third)

        openEditTextFloat.setOnClickListener {
            showEditTextFloat()
        }

        openJavaTestActivity.setOnClickListener {
            startActivity(Intent(this, JavaTestActivity::class.java))
        }
    }

    private fun showEditTextFloat(tag: String? = "editTextFloat") {
        EasyFloat.with(this)
            .setShowPattern(ShowPattern.ALL_TIME)
            .setGravity(Gravity.CENTER)
            .setTag(tag)
            .setLayout(R.layout.float_edit, OnInvokeView {
                it.findViewById<EditText>(R.id.editText).setOnClickListener { et ->
                    InputMethodUtils.openInputMethod(et as EditText, tag)
                }

                it.findViewById<TextView>(R.id.tvImmClosed).setOnClickListener {
                    InputMethodUtils.closedInputMethod(tag)
                }

                it.findViewById<TextView>(R.id.tvCloseFloat).setOnClickListener {
                    EasyFloat.dismissAppFloat(this, tag)
                }
            })
            .show()
    }

    private fun showFloat() {

        EasyFloat.with(this).setLayout(R.layout.float_app).show()

        EasyFloat.with(this)
            // 设置浮窗xml布局文件，并可设置详细信息
            .setLayout(R.layout.float_app, OnInvokeView { })
            // 设置我们传入xml布局的详细信息
            .invokeView(OnInvokeView { })
            // 设置浮窗显示类型，默认只在当前Activity显示，可选一直显示、仅前台显示
            .setShowPattern(ShowPattern.ALL_TIME)
            // 设置吸附方式，共15种模式，详情参考SidePattern
            .setSidePattern(SidePattern.RESULT_HORIZONTAL)
            // 设置浮窗的标签，用于区分多个浮窗
            .setTag("testFloat")
            // 设置浮窗是否可拖拽
            .setDragEnable(true)
            // 设置浮窗固定坐标，ps：设置固定坐标，Gravity属性和offset属性将无效
            .setLocation(100, 200)
            // 设置浮窗的对齐方式和坐标偏移量
            .setGravity(Gravity.END or Gravity.CENTER_VERTICAL, 0, 200)
            // 设置宽高是否充满父布局，直接在xml设置match_parent属性无效
            .setMatchParent(widthMatch = false, heightMatch = false)
            // 设置Activity浮窗的出入动画，可自定义，实现相应接口即可（策略模式），无需动画直接设置为null
            .setAnimator(DefaultAnimator())
            // 设置系统浮窗的出入动画，使用同上
            .setAppFloatAnimator(AppFloatDefaultAnimator())
            // 设置系统浮窗的不需要显示的页面
            .setFilter(MainActivity::class.java, SecondActivity::class.java)
            // 是否启动前台Service，仅针对系统浮窗；有默认的Notification，可不传
            .startForeground(true, floatNotification(this))
            // 浮窗的一些状态回调，如：创建结果、显示、隐藏、销毁、touchEvent、拖拽过程、拖拽结束。
            // ps：通过Kotlin DSL实现的回调，可以按需复写方法，用到哪个写哪个
            .registerCallbacks {
                createResult { isCreated, msg, view -> }
                show { }
                hide { }
                dismiss { }
                touchEvent { view, motionEvent -> }
                drag { view, motionEvent -> }
                dragEnd { }
            }
            .registerCallbacks(object : OnFloatCallbacks {
                override fun createdResult(isCreated: Boolean, msg: String?, view: View?) {
                    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                }

                override fun show(view: View) {
                    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                }

                override fun hide(view: View) {
                    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                }

                override fun dismiss() {
                    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                }

                override fun touchEvent(view: View, event: MotionEvent) {
                    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                }

                override fun drag(view: View, event: MotionEvent) {
                    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                }

                override fun dragEnd(view: View) {
                    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                }

            })
            // 创建浮窗（这是关键哦😂）
            .show()
    }

}

