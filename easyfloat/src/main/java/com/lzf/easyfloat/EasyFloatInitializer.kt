package com.lzf.easyfloat

import android.app.Application
import android.content.ContentProvider
import android.content.ContentValues
import android.database.Cursor
import android.net.Uri
import com.lzf.easyfloat.utils.LifecycleUtils

/**
 * @author: liuzhenfeng
 * @github：https://github.com/princekin-f
 * @function: 通过内容提供者的上下文，进行生命周期回调的初始化
 * @date: 2020/10/23  13:41
 */
class EasyFloatInitializer : ContentProvider() {

    override fun onCreate(): Boolean {
        LifecycleUtils.setLifecycleCallbacks(context!!.applicationContext as Application)
        return true
    }

    override fun query(
        uri: Uri,
        projection: Array<String>?,
        selection: String?,
        selectionArgs: Array<String>?,
        sortOrder: String?
    ): Cursor? = null

    override fun getType(uri: Uri): String? = null

    override fun insert(uri: Uri, values: ContentValues?): Uri? = null

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<String>?): Int = 0

    override fun update(
        uri: Uri,
        values: ContentValues?,
        selection: String?,
        selectionArgs: Array<String>?
    ): Int = 0
}