package com.example.alarm_jinxuan.utils

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.alarm_jinxuan.model.AlarmEntity
import com.example.alarm_jinxuan.receiver.AlarmReceiver

object AlarmManagerUtils {
    /**
     * 设置闹钟管理
     */
    fun setAlarm(context: Context, alarm: AlarmEntity, timeInMills: Long) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        
        // 创建一个 Intent，传递对象数据所使用
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra("ALARM_OBJ", alarm)
        }

        // PendingIntent 是交给系统托管的 Intent
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            alarm.id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // 设置精准闹钟 (setExactAndAllowWhileIdle 保证省电模式也准时)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                timeInMills,
                pendingIntent
            )
        } else {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, timeInMills, pendingIntent)
        }
    }

    /**
     * 主要是删除闹钟使用
     */
    fun cancelAlarm(context: Context, alarmId: Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        // 1. 创建一个目的地完全相同的 Intent
        val intent = Intent(context, AlarmReceiver::class.java)

        // 2. 这里的 requestCode 必须和你当初 setAlarm 时传入的 alarmId 一模一样！
        // 标志位使用 FLAG_NO_CREATE：如果这个闹钟不存在，就别创建新的，直接返回 null
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            alarmId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // 3. 如果找到了这个待执行的闹钟，就取消它
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
            // 记得还要把这个 PendingIntent 彻底废弃
            pendingIntent.cancel()
        }
    }
}