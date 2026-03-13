package com.example.alarm_jinxuan.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.alarm_jinxuan.model.LapRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface LapDao {
    @Query("SELECT * FROM lap_records ORDER BY uid DESC")
    fun getAllLaps(): Flow<List<LapRecord>>

    @Insert
    suspend fun insert(lap: LapRecord)

    @Query("DELETE FROM lap_records")
    suspend fun deleteAll()
}