package com.dapascript.muslimq.data.source.local.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.dapascript.muslimq.data.source.local.model.QuranDetailEntity
import com.dapascript.muslimq.data.source.local.model.QuranEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface QuranDao {

    @Query("SELECT * FROM quran")
    fun getQuran(): Flow<List<QuranEntity>>

    @Query("SELECT * FROM quran_detail WHERE surahId = :surahId")
    fun getQuranDetail(surahId: Int): Flow<QuranDetailEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuran(quran: List<QuranEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuranDetail(quran: QuranDetailEntity)

    @Query("DELETE FROM quran")
    suspend fun deleteQuran()
}