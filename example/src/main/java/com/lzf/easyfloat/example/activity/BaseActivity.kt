package com.lzf.easyfloat.example.activity

import android.content.Context
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.KeyEvent
import android.view.MotionEvent
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import cn.bingoogolapple.swipebacklayout.BGASwipeBackHelper
import com.lzf.easyfloat.EasyFloat
import com.lzf.easyfloat.enums.ShowPattern
import com.lzf.easyfloat.example.R
import com.lzf.easyfloat.interfaces.OnTouchRangeListener
import com.lzf.easyfloat.utils.DragUtils
import com.lzf.easyfloat.widget.BaseSwitchView

/**
 * @author: liuzhenfeng
 * @date: 2/8/21  13:39
 * @Package: com.lzf.easyfloat.example.activity
 * @Description:
 */
open class BaseActivity : AppCompatActivity(), BGASwipeBackHelper.Delegate {

    lateinit var bgaSwipeBackHelper: BGASwipeBackHelper
    private lateinit var vibrator: Vibrator
    private var vibrating = false
    var slideOffset = 0f
    private var contractTag = "contractFloat"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        bgaSwipeBackHelper = BGASwipeBackHelper(this, this)
    }

    override fun isSupportSwipeBack(): Boolean = false

    override fun onSwipeBackLayoutSlide(slideOffset: Float) {
        this.slideOffset = slideOffset
    }

    override fun onSwipeBackLayoutCancel() {}

    override fun onSwipeBackLayoutExecuted() {
        bgaSwipeBackHelper.swipeBackward()
    }

    override fun onBackPressed() {
        // 正在滑动返回的时候取消返回按钮事件
        if (bgaSwipeBackHelper.isSliding) return
        bgaSwipeBackHelper.backward()
    }

    fun setVibrator(inRange: Boolean) {
        if (!vibrator.hasVibrator() || (inRange && vibrating)) return
        vibrating = inRange
        if (inRange) if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE))
        } else vibrator.vibrate(100)
        else vibrator.cancel()
    }

    /**
     * 注册拖拽关闭
     */
    fun registerDragClose(event: MotionEvent) =
        DragUtils.registerDragClose(event, object : OnTouchRangeListener {
            override fun touchInRange(inRange: Boolean, view: BaseSwitchView) {
                setVibrator(inRange)
                view.findViewById<TextView>(com.lzf.easyfloat.R.id.tv_delete).text =
                    if (inRange) "松手删除" else "删除浮窗"

                view.findViewById<ImageView>(com.lzf.easyfloat.R.id.iv_delete)
                    .setImageResource(
                        if (inRange) com.lzf.easyfloat.R.drawable.icon_delete_selected
                        else com.lzf.easyfloat.R.drawable.icon_delete_normal
                    )
            }

            override fun touchUpInRange() {
                EasyFloat.dismiss(SwipeTestActivity.FLOAT_TAG, true)
            }
        }, showPattern = ShowPattern.ALL_TIME)


    /**
     * 显示控制中心（假装）
     */
    fun showContractFloat() = EasyFloat.with(application)
        .setTag(contractTag)
        .setLayout(R.layout.float_contract) {
            it.findViewById<TextView>(R.id.tv_back).setOnClickListener {
                EasyFloat.dismiss(contractTag)
            }
        }
        .setShowPattern(ShowPattern.FOREGROUND)
        .setImmersionStatusBar(true)
        .setMatchParent(widthMatch = true, heightMatch = true)
        .setDragEnable(false)
        .setAnimator(null)
        .show()

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (EasyFloat.isShow(contractTag)) {
                EasyFloat.dismiss(contractTag)
                return true
            }
        }
        return super.onKeyDown(keyCode, event)
    }

}