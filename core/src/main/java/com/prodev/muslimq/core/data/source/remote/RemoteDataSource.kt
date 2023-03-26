package com.prodev.muslimq.core.data.source.remote

import com.prodev.muslimq.core.data.source.remote.network.AreaApi
import com.prodev.muslimq.core.data.source.remote.network.QuranApi
import com.prodev.muslimq.core.data.source.remote.network.ShalatApi
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RemoteDataSource @Inject constructor(
    private val quranApi: QuranApi, private val areaApi: AreaApi, private val shalatApi: ShalatApi
) {
    suspend fun getQuran() = quranApi.getQuran().data

    suspend fun getQuranDetail(id: Int) = quranApi.getQuranDetail(id).data

    suspend fun getQuranTafsir(id: Int) = quranApi.getQuranTafsir(id).data

    suspend fun getAllProvince() = areaApi.getAllProvince()

    suspend fun getAllCity(id: String) = areaApi.getAllCity(id)

    suspend fun getShalatDaily(
        city: String, country: String
    ) = shalatApi.getShalatDaily(city, country)
}