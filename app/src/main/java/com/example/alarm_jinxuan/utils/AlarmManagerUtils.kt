package com.example.alarm_jinxuan.utils

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.alarm_jinxuan.model.AlarmEntity
import com.example.alarm_jinxuan.receiver.AlarmReceiver
import java.util.Calendar

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

    /**
     * 闹钟下次的响铃时间
     */
    fun calculateNextTriggerTime(alarm: AlarmEntity): Long {
        return calculateNextTriggerTimeByTime(alarm.hour24,alarm.minute,alarm.repeatData)
    }

    /**
     * 主要是为了save()初始化直接调用的
     */
    fun calculateNextTriggerTimeByTime(hour24: Int,minute: Int,repeatData: String): Long {
        val now = Calendar.getInstance()
        val target = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour24)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val repeatDays = repeatData.split(",").map { it == "1" }
        val isRepeat = repeatDays.contains(true)

        if (!isRepeat) {
            // 不重复的闹钟
            if (target.before(now)) {
                target.add(Calendar.DAY_OF_YEAR, 1)
            }
        } else {
            // 重复闹钟
            for (i in 0..7) {
                // 获取 target 当前是周几（周日1，周六7）
                val dayOfWeek = target.get(Calendar.DAY_OF_WEEK)

                // 需要映射到我的数组索引
                val index = dayOfWeek - 1

                // 如果这一天是选中的重复日，时间必须在now之后
                if (repeatDays[index] && target.after(now)) {
                    break
                }

                // 没找到就加一天
                target.add(Calendar.DAY_OF_YEAR, 1)
            }
        }

        return target.timeInMillis
    }

    /**
     * 对于小睡模式后的时间计算
     */
    fun calculateNextSnoozeTime(alarm: AlarmEntity): Calendar {
        val calendar = Calendar.getInstance()

        val diff = alarm.snoozeCount - alarm.computeSnoozeCount
        // 先设定到当前的这个闹钟时间点
        calendar.set(Calendar.HOUR_OF_DAY, alarm.hour24)
        calendar.set(Calendar.MINUTE, alarm.minute)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)

        // 添加时间间隔
        calendar.add(Calendar.MINUTE, alarm.snoozeInterval * (diff))

        return calendar
    }

    /**
     * 和当前时间（now）计算还有多久
     */
    fun getRemainingTime(triggerTime: Long): Triple<Int, Int, Int> {
        val diff = triggerTime - System.currentTimeMillis()

        // 如果时间已经过了，直接返回全 0
        if (diff <= 0) return Triple(0, 0, 0)

        // 先算出总分钟数
        val totalMinutes = diff / (1000 * 60)

        // 算出总小时数
        val totalHours = totalMinutes / 60

        // 级联取余计算
        val remainMinutes = (totalMinutes % 60).toInt()
        val remainHours = (totalHours % 24).toInt() // 小时对 24 取余，保证不超过 24
        val days = (totalHours / 24).toInt()        // 总小时除以 24 得到天数

        return Triple(days, remainHours, remainMinutes)
    }

    /**
     * 在当前的时间上添加响铃间隔
     */
    fun getSnoozeTriggerTime(alarm: AlarmEntity): Long {
        val calendar = Calendar.getInstance()

        // 在当前时间的基础上，增加指定的分钟数
        calendar.add(Calendar.MINUTE, alarm.snoozeInterval)

        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)

        return calendar.timeInMillis
    }

}