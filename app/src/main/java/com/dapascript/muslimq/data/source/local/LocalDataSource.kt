package com.dapascript.muslimq.data.source.local

import com.dapascript.muslimq.data.source.local.database.QuranDao
import com.dapascript.muslimq.data.source.local.model.QuranEntity
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocalDataSource @Inject constructor(private val quranDao: QuranDao) {
    fun getQuran() = quranDao.getQuran()
    suspend fun insertQuran(quran: List<QuranEntity>) = quranDao.insertQuran(quran)
    suspend fun deleteQuran() = quranDao.deleteQuran()
}