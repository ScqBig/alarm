package com.example.alarm_jinxuan.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.alarm_jinxuan.model.AlarmEntity
import com.example.alarm_jinxuan.service.AlarmService

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

        // Todo，计算下次响铃的日期

        // 对action进行判断执行对应的相关逻辑
        when (action) {
            "ACTION_DISMISS" -> {
                // 💡 用户点了“关闭”：停止服务
                val stopIntent = Intent(context, AlarmService::class.java)
                context.stopService(stopIntent)
            }
            "ACTION_SNOOZE" -> {
                // 💡 用户点了“稍后提醒”：停止当前，并定个新闹钟
                context.stopService(Intent(context, AlarmService::class.java))
                // 这里调用你写的 snooze 逻辑（比如 10 分钟后重响）
                if (alarm != null) {
//                    snoozeAlarm(context, alarm)
                }
            }
            else -> {
                //  启动服务响铃并且弹出通知
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
}