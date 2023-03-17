package com.prodev.muslimq.core.data.repository

import com.prodev.muslimq.core.data.source.local.LocalDataSource
import com.prodev.muslimq.core.data.source.local.model.ShalatEntity
import com.prodev.muslimq.core.data.source.remote.RemoteDataSource
import com.prodev.muslimq.core.data.source.remote.model.CityResponse
import com.prodev.muslimq.core.data.source.remote.model.ProvinceResponse
import com.prodev.muslimq.core.utils.Resource
import com.prodev.muslimq.core.utils.networkBoundResource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ShalatRepositoryImpl @Inject constructor(
    private val remoteDataSource: RemoteDataSource,
    private val localDataSource: LocalDataSource
) : ShalatRepository {

    override fun getAllProvince(): Flow<Resource<List<ProvinceResponse>>> = flow {
        emit(Resource.Loading())
        try {
            val response = remoteDataSource.getAllProvince()
            emit(Resource.Success(response))
        } catch (e: Exception) {
            emit(Resource.Error(e))
        }
    }.flowOn(Dispatchers.IO)

    override fun getAllCity(id: String): Flow<Resource<List<CityResponse>>> = flow {
        emit(Resource.Loading())
        try {
            val response = remoteDataSource.getAllCity(id)
            emit(Resource.Success(response))
        } catch (e: Exception) {
            emit(Resource.Error(e))
        }
    }.flowOn(Dispatchers.IO)

    override fun getShalatDaily(
        city: String, country: String
    ): Flow<Resource<ShalatEntity>> = networkBoundResource(
        query = { localDataSource.getShalatDailyByCity(city, country) },
        fetch = { remoteDataSource.getShalatDaily(city, country) },
        saveFetchResult = { shalat ->
            val simpleDateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
            shalat.data.filter {
                it.date.readable.contains(simpleDateFormat.format(Date()))
            }.map { pray ->
                val local = ShalatEntity(
                    city = city,
                    country = country,
                    shubuh = pray.timings.Fajr,
                    dzuhur = pray.timings.Dhuhr,
                    ashar = pray.timings.Asr,
                    maghrib = pray.timings.Maghrib,
                    isya = pray.timings.Isha
                )
                localDataSource.deleteShalatDaily()
                localDataSource.insertShalatDaily(local)
            }
        }
    )
}