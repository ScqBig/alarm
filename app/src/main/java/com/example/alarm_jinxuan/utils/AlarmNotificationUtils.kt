package com.example.alarm_jinxuan.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.alarm_jinxuan.R
import com.example.alarm_jinxuan.model.AlarmEntity
import com.example.alarm_jinxuan.receiver.AlarmReceiver
import com.example.alarm_jinxuan.view.ring.RingActivity

object AlarmNotificationUtils {
    const val CHANNEL_ID = "ALARM_CHANNEL_ID"

    private var smallIcon: Int = R.drawable.ic_alarm

    /**
     * 创建通知通道
     */
    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "闹钟提醒",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                setSound(null, null) // 通知需要静音
                enableVibration(true)
            }
            val manager = context.getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    /**
     * 获取 Notification 构建器
     * 返回类型：NotificationCompat.Builder
     */
    fun getNotificationBuilder(
        context: Context,
        alarm: AlarmEntity,
        fullScreenPI: PendingIntent,
        dismissPI: PendingIntent,
        snoozePI: PendingIntent
    ): NotificationCompat.Builder {
        // 获取格式化时间
        val formattedTime = StringUtils.formattedTime(alarm.nextTriggerTime)

        return NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(smallIcon)
            .setContentTitle(alarm.label)
            // 2. 这里直接拼接即可，比如 "上午 7:02"
            .setContentText("${alarm.period} $formattedTime")
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            // 注意：现在 RingActivity 由 AlarmReceiver 直接启动，不需要 fullScreenIntent 触发
            // fullScreenPI 保留用于未来可能的点击通知跳转功能
            .setContentIntent(fullScreenPI)  // 改用 setContentIntent 作为点击通知时的跳转
            .setOngoing(true)
            .addAction(0, "关闭", dismissPI)
            .addAction(0, "稍后提醒", snoozePI)
            .setAutoCancel(false)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
    }

    /**
     * 专门用于稍后提醒的相关后台通知
     */
    fun getSnoozeBuilder(context: Context, alarm: AlarmEntity, dismissPI: PendingIntent): NotificationCompat.Builder {
        // 直接使用已有的最新时间戳转换
        val formattedTime = StringUtils.formattedTime(alarm.nextTriggerTime)

        return NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(smallIcon)
            .setContentTitle("${alarm.label} (稍后提醒)")
            .setContentText("${alarm.period}$formattedTime 再响 - ${alarm.label}")
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .addAction(0, "关闭", dismissPI)
            .setAutoCancel(false)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
    }

    /**
     * 获取全屏跳转的 PendingIntent
     */
    fun getFullScreenIntent(context: Context, alarm: AlarmEntity): PendingIntent {
        val intent = Intent(context, RingActivity::class.java).apply {
            putExtra("ALARM_OBJ", alarm)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        // 全屏 Intent 需要使用 FLAG_UPDATE_CURRENT 或 FLAG_IMMUTABLE
        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }
        return PendingIntent.getActivity(context, alarm.id, intent, flags)
    }

    /**
     * 获取广播动作的 PendingIntent (关闭/稍后提醒)
     * @param action 动作字符串
     * @param requestCodeOffset 偏移量
     */
    fun getBroadcastIntent(context: Context, alarm: AlarmEntity, action: String, requestCodeOffset: Int): PendingIntent {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            this.action = action
            putExtra("ALARM_OBJ", alarm)
        }
        return PendingIntent.getBroadcast(
            context, alarm.id + requestCodeOffset, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
}