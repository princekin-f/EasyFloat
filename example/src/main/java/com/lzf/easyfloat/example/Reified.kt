package com.lzf.easyfloat.example

import android.content.Context
import android.content.Intent

/**
 * @author: liuzhenfeng
 * @github：https://github.com/princekin-f
 * @function:
 * @date: 2020/4/23  17:27
 */
inline fun <reified T> startActivity(context: Context, block: Intent.() -> Unit = {}) =
    context.startActivity(Intent(context, T::class.java).apply(block))