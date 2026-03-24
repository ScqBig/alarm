package com.example.alarm_jinxuan.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.alarm_jinxuan.repository.AlarmRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            // 初始化 Repository
            AlarmRepository.init(context)

            // 建议开启一个协程或工作线程处理，因为查询数据库是耗时的
            CoroutineScope(Dispatchers.IO).launch {
                // 需要获取所有闹钟数据时间

            }
        }
    }
}