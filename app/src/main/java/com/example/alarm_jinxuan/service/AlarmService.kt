package com.example.alarm_jinxuan.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.alarm_jinxuan.R
import com.example.alarm_jinxuan.model.AddAlarmClockManager
import com.example.alarm_jinxuan.model.AlarmEntity
import com.example.alarm_jinxuan.receiver.AlarmReceiver
import com.example.alarm_jinxuan.utils.MediaUtils
import com.example.alarm_jinxuan.utils.VibrationUtils
import com.example.alarm_jinxuan.view.ring.RingActivity

class AlarmService : Service() {
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // 获取闹钟数据
        val alarm = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent?.getParcelableExtra("ALARM_OBJ", AlarmEntity::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent?.getParcelableExtra("ALARM_OBJ")
        }

        if (alarm == null) {
            Log.e("service出现问题","接收广播传服务的是空数据")
            return -1
        }
        // 创建通知通道，为后续调用通知使用
        createNotificationChannel()

        // 显示对应通知
        showNotification(alarm)
        // 开始响铃振动
        startForegroundResouce(alarm)

        return START_STICKY
    }

    /**
     * 创建通知通道
     */
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "ALARM_CHANNEL_ID",
                "闹钟提醒",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                setSound(null, null) // 通知需要静音
                enableVibration(true)
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun startForegroundResouce(alarm: AlarmEntity) {
        // 先开启振动
        val vibrationOption = AddAlarmClockManager.vibrationList[alarm.vibrationId]
        VibrationUtils.vibrate(this,vibrationOption.pattern)
        // 再播放铃声
        val resId =
            this.resources.getIdentifier(alarm.ringtoneFileName, "raw", this.packageName)

        MediaUtils.startRingtonePreview(resId,this)
    }

    private fun showNotification(alarm: AlarmEntity) {
        // 全屏跳转意图
        val ringIntent = Intent(this, RingActivity::class.java).apply {
            putExtra("ALARM_OBJ", alarm)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        // 使用 FLAG_UPDATE_CURRENT 确保数据能正确传递
        val fullScreenPI = PendingIntent.getActivity(this, alarm.id, ringIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        // 2. 点击“关闭”按钮的意图 (发送给 Receiver 处理 stopService)
        val dismissIntent = Intent(this, AlarmReceiver::class.java).apply {
            action = "ACTION_DISMISS"
            putExtra("ALARM_ID", alarm.id)
        }
        val dismissPI = PendingIntent.getBroadcast(this, alarm.id + 1, dismissIntent, PendingIntent.FLAG_IMMUTABLE)

        // 3. 构建通知
        val builder = NotificationCompat.Builder(this, "ALARM_CHANNEL_ID")
            .setSmallIcon(R.drawable.ic_alarm)
            .setContentTitle(alarm.label)
            .setContentText("${alarm.period}${alarm.hour}:${String.format("%02d", alarm.minute)}")
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setFullScreenIntent(fullScreenPI, true)
            .setOngoing(true)
            .addAction(0, "关闭", dismissPI)
            .addAction(0, "稍后提醒", null) // 暂时传 null，逻辑同上

        // 启动前台服务通知
        startForeground(alarm.id, builder.build())
    }

    override fun onDestroy() {
        super.onDestroy()
        // 服务关闭时，务必释放资源
        MediaUtils.stop(this)
        VibrationUtils.stop(this)

        // 显式移除前台通知（参数 true 表示移除通知）
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            stopForeground(STOP_FOREGROUND_REMOVE)
        } else {
            @Suppress("DEPRECATION")
            stopForeground(true)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null
}