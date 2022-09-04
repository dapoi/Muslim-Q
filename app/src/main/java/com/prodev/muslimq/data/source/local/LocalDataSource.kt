package com.prodev.muslimq.data.source.local

import com.prodev.muslimq.data.source.local.database.QuranDao
import com.prodev.muslimq.data.source.local.model.QuranDetailEntity
import com.prodev.muslimq.data.source.local.model.QuranEntity
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocalDataSource @Inject constructor(private val quranDao: QuranDao) {
    fun getQuran() = quranDao.getQuran()
    fun getQuranDetail(id: Int) = quranDao.getQuranDetail(id)
    suspend fun insertQuran(quran: List<QuranEntity>) = quranDao.insertQuran(quran)
    suspend fun insertQuranDetail(quran: QuranDetailEntity) =
        quranDao.insertQuranDetail(quran)
    suspend fun deleteQuran() = quranDao.deleteQuran()
}