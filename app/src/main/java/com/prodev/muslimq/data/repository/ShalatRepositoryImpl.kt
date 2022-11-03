package com.prodev.muslimq.data.repository

import com.prodev.muslimq.data.source.local.LocalDataSource
import com.prodev.muslimq.data.source.local.model.ShalatEntity
import com.prodev.muslimq.data.source.remote.RemoteDataSource
import com.prodev.muslimq.data.source.remote.model.CityResponse
import com.prodev.muslimq.data.source.remote.model.ProvinceResponse
import com.prodev.muslimq.utils.Resource
import com.prodev.muslimq.utils.networkBoundResource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
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

    override fun getShalatDaily(city: String): Flow<Resource<ShalatEntity>> = networkBoundResource(
        query = { localDataSource.getShalatDailyByCity(city) },
        fetch = { remoteDataSource.getShalatDaily(city) },
        saveFetchResult = { shalat ->
            shalat.items.map { pray ->
                val local = ShalatEntity(
                    city = city,
                    shubuh = pray.fajr,
                    dzuhur = pray.dhuhr,
                    ashar = pray.asr,
                    maghrib = pray.maghrib,
                    isya = pray.isha
                )
                localDataSource.deleteShalatDaily()
                localDataSource.insertShalatDaily(local)
            }
        }
    )
}