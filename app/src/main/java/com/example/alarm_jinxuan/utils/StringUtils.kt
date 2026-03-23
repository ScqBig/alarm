package com.example.alarm_jinxuan.utils

object StringUtils {
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
}