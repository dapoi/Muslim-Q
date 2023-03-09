package com.prodev.muslimq.core.data.source.local

import com.prodev.muslimq.core.data.source.local.database.QuranDao
import com.prodev.muslimq.core.data.source.local.database.ShalatDao
import com.prodev.muslimq.core.data.source.local.model.QuranDetailEntity
import com.prodev.muslimq.core.data.source.local.model.QuranEntity
import com.prodev.muslimq.core.data.source.local.model.ShalatEntity
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocalDataSource @Inject constructor(
    private val quranDao: QuranDao,
    private val shalatDao: ShalatDao
) {
    fun getQuran() = quranDao.getQuran()
    suspend fun insertQuran(quran: List<QuranEntity>) = quranDao.insertQuran(quran)
    suspend fun deleteQuran() = quranDao.deleteQuran()

    fun getQuranDetail(id: Int) = quranDao.getQuranDetail(id)
    suspend fun insertQuranDetail(quran: QuranDetailEntity) =
        quranDao.insertQuranDetail(quran)

    fun getBookmark() = quranDao.getBookmark()

    fun insertToBookmark(quran: QuranDetailEntity, isBookmark: Boolean) {
        quran.isBookmarked = isBookmark
        quranDao.updateBookmark(quran)
    }

    fun getShalatDailyByCity(city: String) = shalatDao.getShalatDailyByCity(city)
    suspend fun insertShalatDaily(shalat: ShalatEntity) = shalatDao.insertShalat(shalat)
    suspend fun deleteShalatDaily() = shalatDao.deleteShalat()
    suspend fun deleteAllBookmark() = quranDao.deleteAllBookmark()
}