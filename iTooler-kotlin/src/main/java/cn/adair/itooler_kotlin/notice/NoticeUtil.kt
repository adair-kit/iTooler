package cn.adair.itooler_kotlin.notice

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.support.annotation.RequiresApi
import android.support.v4.app.NotificationCompat
import android.support.v4.content.FileProvider
import cn.adair.itooler_kotlin.tool.iLogger
import java.io.File

/**
 * cn.adair.itooler_kotlin.notice
 * Created by Administrator on 2018/7/20/020.
 * slight negligence may lead to great disaster~
 */
object NoticeUtil {

    /**
     * 构建一个消息
     */
    private fun builderNotification(context: Context, icon: Int, title: String, content: String): NotificationCompat.Builder {
        var channelId = ""
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            channelId = getNoticeChannelId()
        }
        return NotificationCompat.Builder(context, channelId).setSmallIcon(icon).setAutoCancel(false).setContentTitle(title)
                .setWhen(System.currentTimeMillis()).setContentText(content).setOngoing(true)
    }

    /**
     * 展示消息通知
     */
    fun showNotification(context: Context, icon: Int, title: String, content: String) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            afterO(manager)
        }
        val builder = builderNotification(context, icon, title, content).setDefaults(Notification.DEFAULT_SOUND)
        manager.notify(1001, builder.build())
    }

    /**
     * 展示带进度条通知
     */
    fun showProgressNotification(context: Context, icon: Int, title: String, content: String, max: Int, progress: Int) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val builder = builderNotification(context, icon, title, content).setProgress(max, progress, false)
        manager.notify(1001, builder.build())
    }

    /**
     * 下载完成通知
     */
    fun showDoneNotification(context: Context, icon: Int, title: String, content: String, authorities: String, apk: File) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        //不知道为什么需要先取消之前的进度通知，才能显示完成的通知。
        manager.cancel(1001)
        val intent = Intent(Intent.ACTION_VIEW)
        intent.action = Intent.ACTION_VIEW
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        intent.addCategory(Intent.CATEGORY_DEFAULT)
        val uri: Uri
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            iLogger.e("----->" + apk.path)
            uri = FileProvider.getUriForFile(context.applicationContext, "$authorities.fileprovider", apk)
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        } else {
            uri = Uri.fromFile(apk)
        }
        intent.setDataAndType(uri, "application/vnd.android.package-archive")
        val pi = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT)
        val builder = builderNotification(context, icon, title, content).setContentIntent(pi)
        val notification = builder.build()
        notification.flags = notification.flags or Notification.FLAG_AUTO_CANCEL
        manager.notify(1001, notification)
    }


    /**
     * 适配android O 版本消息通知
     */
    @RequiresApi(Build.VERSION_CODES.O)
    fun afterO(manager: NotificationManager) {
        var channel = NotificationChannel("app_update", "版本更新", NotificationManager.IMPORTANCE_LOW);
        channel.enableLights(true)
        channel.setShowBadge(true)
        manager.createNotificationChannel(channel)
    }

    /**
     * 获取通知渠道id
     */
    @RequiresApi(Build.VERSION_CODES.O)
    private fun getNoticeChannelId(): String {
        return "app_update";
    }

}