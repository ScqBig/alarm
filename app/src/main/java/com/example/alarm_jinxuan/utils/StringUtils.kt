package com.example.alarm_jinxuan.utils

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

object StringUtils {
    /**
     * 获取最近的响铃时间
     */
    fun formatRemainingTime(days: Int, hours: Int, minutes: Int): String {
        val sb = StringBuilder("")

        // 如果有天数，拼接天数
        if (days > 0) {
            sb.append("${days}天")
        }

        // 如果有小时，或者已经有了天数，就拼接小时
        if (hours > 0 || days > 0) {
            sb.append("${hours}小时")
        }

        if (minutes > 0) {
            sb.append("${minutes}分钟后响铃")
        } else {
            sb.append("1分钟内响铃")
        }

        return sb.toString()
    }

    /**
     * 获取最近的响铃具体时间
     */
    fun formattedTime(nextTriggerTime: Long): String{
        val instant = Instant.ofEpochMilli(nextTriggerTime)
        val localDateTime = instant.atZone(ZoneId.systemDefault()).toLocalDateTime()

        // 2. 创建格式化器
        // "h:mm"：12小时制，分钟补零
        val formatter = DateTimeFormatter.ofPattern("h:mm", Locale.getDefault())
        val formattedTime = localDateTime.format(formatter)

        return formattedTime
    }

}