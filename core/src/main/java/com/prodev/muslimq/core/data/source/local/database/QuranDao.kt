package com.prodev.muslimq.core.data.source.local.database

import androidx.room.*
import com.prodev.muslimq.core.data.source.local.model.BookmarkEntity
import com.prodev.muslimq.core.data.source.local.model.QuranDetailEntity
import com.prodev.muslimq.core.data.source.local.model.QuranEntity
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

    /**
     * Bookmark
     */
    @Query("SELECT * FROM bookmark")
    fun getBookmark(): Flow<List<BookmarkEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM bookmark WHERE surahId = :surahId)")
    fun isBookmarked(surahId: Int): Flow<Boolean>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBookmark(bookmark: BookmarkEntity)

    @Delete
    suspend fun deleteBookmark(bookmarkEntity: BookmarkEntity)

    @Query("DELETE FROM bookmark")
    suspend fun deleteAllBookmark()
}