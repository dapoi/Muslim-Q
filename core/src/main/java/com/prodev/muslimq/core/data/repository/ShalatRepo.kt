package com.prodev.muslimq.core.data.repository

import com.prodev.muslimq.core.data.source.local.database.ShalatDao
import com.prodev.muslimq.core.data.source.local.model.ShalatEntity
import com.prodev.muslimq.core.data.source.remote.network.ShalatApi
import com.prodev.muslimq.core.utils.Resource
import com.prodev.muslimq.core.utils.networkBoundResource
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ShalatRepositoryImpl @Inject constructor(
    private val shalatService: ShalatApi,
    private val dao: ShalatDao,
    private val ioDispatcher: CoroutineDispatcher,
    private val calendar: Calendar
) : ShalatRepository {

    override fun getShalatDaily(
        city: String, country: String
    ): Flow<Resource<ShalatEntity>> = networkBoundResource(
        query = {
            dao.getShalatDailyByCity()
        },
        fetch = {
            val currentYear = calendar.get(Calendar.YEAR)
            val currentMonth = calendar.get(Calendar.MONTH) + 1
            shalatService.getShalatDaily(currentYear, currentMonth, city, country)
        },
        saveFetchResult = { shalat ->
            val today = calendar.get(Calendar.DAY_OF_MONTH)
            shalat.data.filter {
                it.date.gregorian.day?.toInt() == today
            }.map { pray ->
                val local = ShalatEntity(
                    day = pray.date.gregorian.day.toString(),
                    city = city,
                    country = country,
                    shubuh = pray.timings.Fajr.toString(),
                    dzuhur = pray.timings.Dhuhr.toString(),
                    ashar = pray.timings.Asr.toString(),
                    maghrib = pray.timings.Maghrib.toString(),
                    isya = pray.timings.Isha.toString(),
                    lat = pray.meta.latitude,
                    lon = pray.meta.longitude
                )

                dao.deleteShalat()
                dao.insertShalat(local)
            }
        },
        ioDispatcher = { ioDispatcher }
    )
}

interface ShalatRepository {
    fun getShalatDaily(city: String, country: String): Flow<Resource<ShalatEntity>>
}