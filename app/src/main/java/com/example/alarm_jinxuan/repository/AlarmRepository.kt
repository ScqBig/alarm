package com.example.alarm_jinxuan.repository

import android.content.Context
import com.example.alarm_jinxuan.dao.AlarmDao
import com.example.alarm_jinxuan.dao.AppDatabase
import com.example.alarm_jinxuan.model.AlarmEntity
import com.example.alarm_jinxuan.utils.AlarmManagerUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

// 主要用于在receiver、service里面对数据库进行操作
object AlarmRepository {
    private var alarmDao: AlarmDao? = null

    // 在 Application 类的 onCreate 里初始化一次即可
    fun init(context: Context) {
        if (alarmDao == null) {
            val db = AppDatabase.getDatabase(context)
            alarmDao = db.alarm()
        }
    }

    /**
     * 关闭闹钟时修改闹钟状态（仅在不重复时关闭）
      */
    fun dismissAlarm(alarm: AlarmEntity) {
        // 同时也要修改闹钟的重复响应次数以及相应的时间戳
        val computeSnoozeCount = alarm.snoozeCount
        val nextTriggerTime = AlarmManagerUtils.calculateNextTriggerTime(alarm)

        CoroutineScope(Dispatchers.IO).launch {
            alarmDao?.updateEnabledStatus(alarm.id,false,nextTriggerTime,computeSnoozeCount)
        }
    }

    /**
     * 修改闹钟状态，直接覆盖
     */
    fun updateAlarm(alarm: AlarmEntity) {
        CoroutineScope(Dispatchers.IO).launch {
            alarmDao?.insertAlarm(alarm)
        }
    }

    /**
     * 根据 ID 查询闹钟（同步方法，用于 Service 中）
     */
    fun getAlarmById(alarmId: Int): AlarmEntity? {
        return kotlinx.coroutines.runBlocking {
            alarmDao?.getAlarmById(alarmId)
        }
    }
}