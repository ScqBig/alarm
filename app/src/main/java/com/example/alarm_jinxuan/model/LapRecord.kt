package com.example.alarm_jinxuan.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "lap_records")
data class LapRecord(
    @PrimaryKey(autoGenerate = true)
    val uid: Int = 0,

    val id: Int,
    val lapTimeNanos: Long,
    val durationNanos: Long
) {
    val formattedTotalTime: String get() = formatNanos(lapTimeNanos)
    val formattedDuration: String get() = "间隔 ${formatNanos(durationNanos)}"

    private fun formatNanos(nanos: Long): String {
        val totalMillis = nanos / 1_000_000
        val minutes = (totalMillis / 1000) / 60
        val seconds = (totalMillis / 1000) % 60
        val centiSeconds = (totalMillis % 1000) / 10
        return String.format("%02d:%02d.%02d", minutes, seconds, centiSeconds)
    }
}
