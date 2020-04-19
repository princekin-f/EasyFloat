package com.lzf.easyfloat.example

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.CheckBox
import android.widget.TextView
import com.lzf.easyfloat.EasyFloat

/**
 * @author: liuzhenfeng
 * @github：https://github.com/princekin-f
 * @function:
 * @date: 2020/4/16  13:57
 */
class MyAdapter(context: Context, private val stringArray: Array<String>) :
    ArrayAdapter<String>(context, R.layout.item_simple_list, stringArray) {

    @SuppressLint("ViewHolder")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = LayoutInflater.from(context).inflate(R.layout.item_simple_list, null)
        view.findViewById<TextView>(R.id.tv_item).text = stringArray[position]
        view.findViewById<CheckBox>(R.id.checkbox).apply {
            setOnTouchListener { _, event ->
                logger.e("setOnTouchListener: ${event.action}")
                EasyFloat.appFloatDragEnable(event?.action == MotionEvent.ACTION_CANCEL)
                false
            }

            setOnCheckedChangeListener { _, isChecked ->
                logger.e("点击了：$position   isChecked：$isChecked")
            }
        }
//        view.setOnClickListener {
//            logger.e("点击了item： $position")
//        }
        return view
    }
}