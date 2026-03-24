package com.example.alarm_jinxuan.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.alarm_jinxuan.model.AlarmEntity
import com.example.alarm_jinxuan.repository.AlarmRepository
import com.example.alarm_jinxuan.service.AlarmService
import com.example.alarm_jinxuan.utils.AlarmManagerUtils
import com.example.alarm_jinxuan.utils.AlarmNotificationUtils
import com.example.alarm_jinxuan.utils.MediaUtils
import com.example.alarm_jinxuan.utils.VibrationUtils

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        // 先获取对应的action
        val action = intent.action
        // 获取 alarmManager 传递的数据
        val alarm = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra("ALARM_OBJ", AlarmEntity::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra("ALARM_OBJ")
        }

        // 对action进行判断执行对应的相关逻辑
        when (action) {
            "ACTION_DISMISS" -> {
                // 关闭闹钟停止服务
                val stopIntent = Intent(context, AlarmService::class.java)
                context.stopService(stopIntent)
                // 这里关闭闹钟后，还需要根据闹钟的重复时间来设置下一个alarmManager
                if (alarm?.repeatText == "不重复") {
                    AlarmRepository.dismissAlarm(alarm)
                } else {
                    // 那就说明为重复，需要创建alarmManager设置下一次的闹钟
                    alarm?.let {
                        setAlarm(context,alarm)
                    }
                }
            }
            "ACTION_SNOOZE" -> {
                // 停止当前闹钟服务
                context.stopService(Intent(context, AlarmService::class.java))
                // 这里调用小睡模式
                if (alarm != null) {
                    snoozeAlarm(context, alarm)
                }
            }
            else -> {
                // 1. 先启动 RingActivity 唤醒屏幕
                val activityIntent = Intent(context, com.example.alarm_jinxuan.view.ring.RingActivity::class.java).apply {
                    putExtra("ALARM_OBJ", alarm)
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                }
                context.startActivity(activityIntent)

                // 2. 再启动服务响铃并且弹出通知
                val serviceIntent = Intent(context, AlarmService::class.java).apply {
                    putExtra("ALARM_OBJ", alarm)
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(serviceIntent)
                } else {
                    context.startService(serviceIntent)
                }
            }
        }
    }

    /**
     * 稍后提醒模式
     */
    private fun snoozeAlarm(context: Context, alarm: AlarmEntity) {
        // 先停止振动和铃声
        MediaUtils.stop(context)
        VibrationUtils.stop(context)
        // 这里的稍后提醒没有任何的次数限制，只要用户愿意可以一直稍后提醒
        alarm.computeSnoozeCount--

        val triggerTime = AlarmManagerUtils.getSnoozeTriggerTime(alarm)
        alarm.nextTriggerTime = triggerTime
        // 修改数据库更改下一次响铃的时间（这里修改了响应更换次数，后续关闭闹钟需要修改回来）
        AlarmRepository.updateAlarm(alarm)
        // 只需要再设置一个再响间隔的闹铃即可
        AlarmManagerUtils.setAlarm(context,alarm,triggerTime)

        // 设置稍后提醒的闹钟日志
        val dismissPI = AlarmNotificationUtils.getBroadcastIntent(context, alarm, "ACTION_DISMISS", 1000)
        val snoozeBuilder = AlarmNotificationUtils.getSnoozeBuilder(context, alarm, dismissPI)

        // 获取 NotificationManager 并更新通知
        val notificationManager = context.getSystemService(android.app.NotificationManager::class.java)
        notificationManager.notify(alarm.id, snoozeBuilder.build())
    }

    /**
     * 关闭闹铃后还要设置下一次的alarmManager
     */
    private fun setAlarm(context: Context, alarm: AlarmEntity) {
        val triggerTime = AlarmManagerUtils.calculateNextTriggerTime(alarm)
        // 设置下一次的闹铃
        AlarmManagerUtils.setAlarm(context,alarm,triggerTime)
    }
}