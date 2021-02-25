package com.lzf.easyfloat.example

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
class MyAdapter(
    context: Context,
    stringArray: Array<String>,
    private val resourceId: Int = R.layout.item_simple_list
) : ArrayAdapter<String>(context, resourceId, stringArray) {

    inner class ViewHolder(view: View) {
        val textView: TextView = view.findViewById(R.id.tv_item)
        val checkBox: CheckBox = view.findViewById(R.id.checkbox)
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view: View
        val viewHolder: ViewHolder
        if (convertView == null) {
            view = LayoutInflater.from(context).inflate(resourceId, parent, false)
            viewHolder = ViewHolder(view)
            view.tag = viewHolder
        } else {
            view = convertView
            viewHolder = view.tag as ViewHolder
        }

        viewHolder.textView.text = getItem(position)
        viewHolder.checkBox.apply {
            setOnTouchListener { _, event ->
                logger.e("setOnTouchListener: ${event.action}")
                EasyFloat.dragEnable(event?.action == MotionEvent.ACTION_CANCEL)
                false
            }

            setOnCheckedChangeListener { _, isChecked ->
                logger.e("点击了：$position   isChecked：$isChecked")
            }
        }
        return view
    }

}