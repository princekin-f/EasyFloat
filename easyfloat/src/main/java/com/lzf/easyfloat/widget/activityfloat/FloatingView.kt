package com.lzf.easyfloat.widget.activityfloat

import android.content.Context
import android.util.AttributeSet
import android.view.View
import com.lzf.easyfloat.widget.activityfloat.AbstractDragFloatingView

/**
 * @author: liuzhenfeng
 * @function: 拖拽布局的实体类
 * @date: 2019-06-21  14:49
 */
class FloatingView(context: Context, attrs: AttributeSet? = null) :
    AbstractDragFloatingView(context, attrs, 0) {

    fun setLayout(layoutResource: Int) {
        config.layoutId = layoutResource
        initView(context)
        requestLayout()
    }

    override fun getLayoutId(): Int? = config.layoutId

    override fun renderView(view: View) {}

}