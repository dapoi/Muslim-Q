package com.prodev.muslimq.data.source.remote

import com.prodev.muslimq.data.source.remote.network.AreaApi
import com.prodev.muslimq.data.source.remote.network.QuranApi
import com.prodev.muslimq.data.source.remote.network.ShalatApi
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RemoteDataSource @Inject constructor(
    private val quranApi: QuranApi,
    private val areaApi: AreaApi,
    private val shalatApi: ShalatApi
) {
    suspend fun getQuran() = quranApi.getQuran()

    suspend fun getQuranDetail(id: Int) = quranApi.getQuranDetail(id)

    suspend fun getAllProvince() = areaApi.getAllProvince()

    suspend fun getAllCity(id: String) = areaApi.getAllCity(id)
}