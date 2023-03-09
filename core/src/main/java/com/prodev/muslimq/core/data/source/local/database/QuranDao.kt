package com.prodev.muslimq.core.data.source.local.database

import androidx.room.*
import com.prodev.muslimq.core.data.source.local.model.QuranDetailEntity
import com.prodev.muslimq.core.data.source.local.model.QuranEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface QuranDao {

    @Query("SELECT * FROM quran")
    fun getQuran(): Flow<List<QuranEntity>>

    @Query("SELECT * FROM quran_detail WHERE surahId = :surahId")
    fun getQuranDetail(surahId: Int): Flow<QuranDetailEntity>

    @Update
    fun updateBookmark(quran: QuranDetailEntity)

    @Query("SELECT * FROM quran_detail WHERE isBookmarked = 1")
    fun getBookmark(): Flow<List<QuranDetailEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuran(quran: List<QuranEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuranDetail(quran: QuranDetailEntity)

    @Query("DELETE FROM quran")
    suspend fun deleteQuran()

    // delete all bookmark
    @Query("UPDATE quran_detail SET isBookmarked = 0")
    suspend fun deleteAllBookmark()
}