package com.prodev.muslimq.data.source.local.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.prodev.muslimq.data.source.local.model.ShalatEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ShalatDao {

    @Query("SELECT * FROM shalat WHERE id = :id")
    fun getShalatDailyByCity(id: Int): Flow<ShalatEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertShalat(shalat: ShalatEntity)

    @Query("DELETE FROM shalat")
    suspend fun deleteShalat()
}