package com.prodev.muslimq.data.source.remote

import com.prodev.muslimq.data.source.remote.model.CityResponse
import com.prodev.muslimq.data.source.remote.network.QuranApi
import com.prodev.muslimq.data.source.remote.network.ShalatApi
import com.prodev.muslimq.utils.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RemoteDataSource @Inject constructor(
    private val quranApi: QuranApi,
    private val shalatApi: ShalatApi
) {
    suspend fun getQuran() = quranApi.getQuran()
    suspend fun getQuranDetail(id: Int) = quranApi.getQuranDetail(id)

    suspend fun getAllCity(): Flow<Resource<List<CityResponse>>> = flow {
        emit(Resource.Loading())
        try {
            val response = shalatApi.getAllCity()
            emit(Resource.Success(response))
        } catch (e: Exception) {
            emit(Resource.Error(e))
        }
    }

    suspend fun getShalatDailyByCity(
        id: Int,
        year: Int,
        month: Int,
        day: Int
    ) = shalatApi.getShalatDailyByCity(id, year, month, day)
}