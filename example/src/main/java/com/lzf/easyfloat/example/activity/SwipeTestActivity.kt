package com.lzf.easyfloat.example.activity

import android.os.Bundle
import android.view.Gravity
import android.view.MotionEvent
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import com.lzf.easyfloat.EasyFloat
import com.lzf.easyfloat.enums.ShowPattern
import com.lzf.easyfloat.enums.SidePattern
import com.lzf.easyfloat.example.R
import com.lzf.easyfloat.interfaces.OnTouchRangeListener
import com.lzf.easyfloat.permission.PermissionUtils
import com.lzf.easyfloat.utils.DragUtils
import com.lzf.easyfloat.widget.BaseSwitchView
import kotlinx.android.synthetic.main.activity_swipe_test.*

/**
 * @author: liuzhenfeng
 * @date: 2020/10/26  18:21
 * @Package: com.lzf.easyfloat.example.activity
 * @Description:
 */
class SwipeTestActivity : BaseActivity() {

    companion object {
        const val FLOAT_TAG = "SwipeTestActivity"
    }

    private var noPermission = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_swipe_test)

    }

    override fun isSupportSwipeBack(): Boolean = true

    override fun onSwipeBackLayoutExecuted() {
        if (!noPermission) bgaSwipeBackHelper.swipeBackward()
    }

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        DragUtils.registerSwipeAdd(ev, object : OnTouchRangeListener {
            override fun touchInRange(inRange: Boolean, view: BaseSwitchView) {
                setVibrator(inRange)
                view.findViewById<ImageView>(com.lzf.easyfloat.R.id.iv_add)
                    .setImageResource(
                        if (inRange) com.lzf.easyfloat.R.drawable.add_selected else com.lzf.easyfloat.R.drawable.add_normal
                    )
            }

            override fun touchUpInRange() {
                noPermission = !PermissionUtils.checkPermission(this@SwipeTestActivity)
                showFloat()
            }
        }, slideOffset = slideOffset)
        return super.dispatchTouchEvent(ev)
    }

    private fun showFloat() = EasyFloat.with(this.applicationContext)
        .setTag(FLOAT_TAG)
        .setShowPattern(ShowPattern.FOREGROUND)
        .setImmersionStatusBar(true)
        .setGravity(Gravity.END, 0, 500)
        .setSidePattern(SidePattern.RESULT_HORIZONTAL)
        .setFilter(SecondActivity::class.java)
        .setLayout(R.layout.float_swipe) {
            it.findViewById<ConstraintLayout>(R.id.cl_content).setOnClickListener {
                showContractFloat()
            }
        }
        .registerCallback {
            createResult { _, _, _ ->
                if (noPermission && !this@SwipeTestActivity.isFinishing) bgaSwipeBackHelper.swipeBackward()
            }

            drag { view, event ->
                // 注册拖拽关闭
                registerDragClose(event)

                view.findViewById<ConstraintLayout>(R.id.cl_content)
                    .setBackgroundResource(R.drawable.corners_red)
            }

            dragEnd {
                it.findViewById<ConstraintLayout>(R.id.cl_content).apply {
                    val location = IntArray(2)
                    getLocationOnScreen(location)
                    setBackgroundResource(if (location[0] > 10) R.drawable.corners_left else R.drawable.corners_right)
                }
            }
        }
        .show()

}
