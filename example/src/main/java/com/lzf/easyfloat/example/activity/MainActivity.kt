package com.lzf.easyfloat.example.activity

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.os.Bundle
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.lzf.easyfloat.EasyFloat
import com.lzf.easyfloat.enums.ShowPattern
import com.lzf.easyfloat.enums.SidePattern
import com.lzf.easyfloat.example.R
import com.lzf.easyfloat.example.logger
import com.lzf.easyfloat.example.startActivity
import com.lzf.easyfloat.example.widget.RoundProgressBar
import com.lzf.easyfloat.example.widget.ScaleImage
import com.lzf.easyfloat.interfaces.OnInvokeView
import com.lzf.easyfloat.interfaces.OnPermissionResult
import com.lzf.easyfloat.permission.PermissionUtils
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.float_seekbar.*
import kotlin.math.max


class MainActivity : AppCompatActivity(), View.OnClickListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        open1.setOnClickListener(this)
        open2.setOnClickListener(this)
        open3.setOnClickListener(this)
        open4.setOnClickListener(this)

        hide1.setOnClickListener(this)
        hide2.setOnClickListener(this)
        hide3.setOnClickListener(this)
        hide4.setOnClickListener(this)

        show1.setOnClickListener(this)
        show2.setOnClickListener(this)
        show3.setOnClickListener(this)
        show4.setOnClickListener(this)

        dismiss1.setOnClickListener(this)
        dismiss2.setOnClickListener(this)
        dismiss3.setOnClickListener(this)
        dismiss4.setOnClickListener(this)

        openSecond.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when (v) {
            open1 -> showActivityFloat()
            hide1 -> EasyFloat.hide(this)
            show1 -> EasyFloat.show(this)
            dismiss1 -> EasyFloat.dismiss(this)

            open2 -> showActivity2()
            hide2 -> EasyFloat.hide(this, "seekBar")
            show2 -> EasyFloat.show(this, "seekBar")
            dismiss2 -> EasyFloat.dismiss(this, "seekBar")

            // 检测权限根据需求考虑有无即可，权限申请为内部进行
            open3 -> checkPermission()
            hide3 -> EasyFloat.hideAppFloat()
            show3 -> EasyFloat.showAppFloat()
            dismiss3 -> EasyFloat.dismissAppFloat()

            open4 -> checkPermission("scaleFloat")
            hide4 -> EasyFloat.hideAppFloat("scaleFloat")
            show4 -> EasyFloat.showAppFloat("scaleFloat")
            dismiss4 -> EasyFloat.dismissAppFloat("scaleFloat")

            openSecond -> startActivity<SecondActivity>(this)

            else -> return
        }
    }

    /**
     * 测试Callback回调
     */
    @SuppressLint("SetTextI18n")
    private fun showActivityFloat() {
        EasyFloat.with(this)
            .setSidePattern(SidePattern.RESULT_HORIZONTAL)
            .setGravity(Gravity.END, 0, 100)
            .setLayout(R.layout.float_custom, OnInvokeView {
                it.findViewById<TextView>(R.id.textView).setOnClickListener { toast() }
            })
            .registerCallback {
                // 在此处设置view也可以，建议在setLayout进行view操作
                createResult { isCreated, msg, _ -> logger.e("DSL:  $isCreated   $msg") }

                show { toast("show") }

                hide { toast("hide") }

                dismiss { toast("dismiss") }

                touchEvent { view, event ->
                    if (event.action == MotionEvent.ACTION_DOWN) {
                        view.findViewById<TextView>(R.id.textView).apply {
                            text = "拖一下试试"
                            setBackgroundResource(R.drawable.corners_green)
                        }
                    }
                }

                drag { view, _ ->
                    view.findViewById<TextView>(R.id.textView).apply {
                        text = "我被拖拽..."
                        setBackgroundResource(R.drawable.corners_red)
                    }
                }

                dragEnd {
                    it.findViewById<TextView>(R.id.textView).apply {
                        text = "拖拽结束"
                        val location = IntArray(2)
                        getLocationOnScreen(location)
                        setBackgroundResource(if (location[0] > 0) R.drawable.corners_left else R.drawable.corners_right)
                    }
                }
            }
            .show()
    }

    private fun showActivity2() {
        // 改变浮窗1的文字
        EasyFloat.getFloatView()?.findViewById<TextView>(R.id.textView)?.text = "恭喜浮窗2"

        EasyFloat.with(this)
            .setTag("seekBar")
            .setGravity(Gravity.CENTER)
            .setLayout(R.layout.float_seekbar, OnInvokeView {
                it.findViewById<ImageView>(R.id.ivClose).setOnClickListener {
                    EasyFloat.dismiss(this@MainActivity, "seekBar")
                }
                it.findViewById<TextView>(R.id.tvProgress).setOnClickListener { tv ->
                    toast((tv as TextView).text.toString())
                }
                it.findViewById<SeekBar>(R.id.seekBar)
                    .setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                        override fun onProgressChanged(
                            seekBar: SeekBar?, progress: Int, fromUser: Boolean
                        ) {
                            tvProgress.text = progress.toString()
                        }

                        override fun onStartTrackingTouch(seekBar: SeekBar?) {}

                        override fun onStopTrackingTouch(seekBar: SeekBar?) {}
                    })
            })
            .show()
    }

    private fun showAppFloat() {
        EasyFloat.with(this)
            .setShowPattern(ShowPattern.ALL_TIME)
            .setSidePattern(SidePattern.RESULT_SIDE)
            .setGravity(Gravity.CENTER)
            .setLayout(R.layout.float_app, OnInvokeView {
                it.findViewById<ImageView>(R.id.ivClose).setOnClickListener {
                    EasyFloat.dismissAppFloat()
                }
                it.findViewById<TextView>(R.id.tvOpenMain).setOnClickListener {
                    startActivity<MainActivity>(this)
                }
                it.findViewById<CheckBox>(R.id.checkbox)
                    .setOnCheckedChangeListener { _, isChecked ->
                        EasyFloat.appFloatDragEnable(isChecked)
                    }

                val progressBar = it.findViewById<RoundProgressBar>(R.id.roundProgressBar).apply {
                    setProgress(66, "66")
                    setOnClickListener { toast(getProgressStr()) }
                }
                it.findViewById<SeekBar>(R.id.seekBar)
                    .setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                        override fun onProgressChanged(
                            seekBar: SeekBar?, progress: Int, fromUser: Boolean
                        ) = progressBar.setProgress(progress, progress.toString())

                        override fun onStartTrackingTouch(seekBar: SeekBar?) {}

                        override fun onStopTrackingTouch(seekBar: SeekBar?) {}
                    })

//                // 解决 ListView 拖拽滑动冲突
//                it.findViewById<ListView>(R.id.lv_test).apply {
//                    adapter = MyAdapter(
//                        this@MainActivity,
//                        arrayOf("A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "...")
//                    )
//
//                    // 监听 ListView 的触摸事件，手指触摸时关闭拖拽，手指离开重新开启拖拽
//                    setOnTouchListener { _, event ->
//                        logger.e("listView: ${event.action}")
//                        EasyFloat.appFloatDragEnable(event?.action == MotionEvent.ACTION_UP)
//                        false
//                    }
//                }
            })
            .show()
    }


    private fun showAppFloat2(tag: String) {
        EasyFloat.with(this)
            .setTag(tag)
            .setShowPattern(ShowPattern.FOREGROUND)
            .setLocation(100, 100)
            .setAppFloatAnimator(null)
            .setFilter(SecondActivity::class.java)
            .setLayout(R.layout.float_app_scale, OnInvokeView {
                val content = it.findViewById<RelativeLayout>(R.id.rlContent)
                val params = content.layoutParams as FrameLayout.LayoutParams
                it.findViewById<ScaleImage>(R.id.ivScale).onScaledListener =
                    object : ScaleImage.OnScaledListener {
                        override fun onScaled(x: Float, y: Float, event: MotionEvent) {
                            params.width = max(params.width + x.toInt(), 100)
                            params.height = max(params.height + y.toInt(), 100)
                            content.layoutParams = params
                        }
                    }

                it.findViewById<ImageView>(R.id.ivClose).setOnClickListener {
                    EasyFloat.dismissAppFloat(tag)
                }
            })
            .show()
    }

    /**
     * 检测浮窗权限是否开启，若没有给与申请提示框（非必须，申请依旧是EasyFloat内部内保进行）
     */
    private fun checkPermission(tag: String? = null) {
        if (PermissionUtils.checkPermission(this)) {
            if (tag == null) showAppFloat() else showAppFloat2(tag)
        } else {
            AlertDialog.Builder(this)
                .setMessage("使用浮窗功能，需要您授权悬浮窗权限。")
                .setPositiveButton("去开启") { _, _ ->
                    if (tag == null) showAppFloat() else showAppFloat2(tag)
                }
                .setNegativeButton("取消") { _, _ -> }
                .show()
        }
    }

    /**
     * 主动申请浮窗权限
     */
    private fun requestPermission() {
        PermissionUtils.requestPermission(this, object : OnPermissionResult {
            override fun permissionResult(isOpen: Boolean) {
                logger.i(isOpen)
            }
        })
    }


    private fun toast(string: String = "onClick") =
        Toast.makeText(this, string, Toast.LENGTH_SHORT).show()

}
