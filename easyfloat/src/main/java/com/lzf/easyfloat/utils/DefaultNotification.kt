package com.lzf.easyfloat.utils

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build

/**
 * @author: liuzhenfeng
 * @function: 前台Service的默认通知栏消息
 * @date: 2019-08-20  23:17
 */
fun floatNotification(context: Context): Notification = when {
    Build.VERSION.SDK_INT >= Build.VERSION_CODES.O -> {
        // 创建消息渠道
        val channel = NotificationChannel("EasyFloat", "系统悬浮窗", NotificationManager.IMPORTANCE_MIN)
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(channel)

        Notification.Builder(context, "EasyFloat")
            .setCategory(Notification.CATEGORY_SERVICE)
    }

    Build.VERSION.SDK_INT >= Build.VERSION_CODES.N ->
        Notification.Builder(context)
            .setCategory(Notification.CATEGORY_SERVICE)
            .setPriority(Notification.PRIORITY_MIN)

    else -> Notification.Builder(context)
}
    .setAutoCancel(true)
    .setOngoing(true)
    .build()

