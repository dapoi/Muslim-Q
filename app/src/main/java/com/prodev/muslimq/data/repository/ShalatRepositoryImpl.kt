package com.prodev.muslimq.data.repository

import com.prodev.muslimq.data.source.local.LocalDataSource
import com.prodev.muslimq.data.source.local.model.ShalatEntity
import com.prodev.muslimq.data.source.remote.RemoteDataSource
import com.prodev.muslimq.data.source.remote.model.CityResponse
import com.prodev.muslimq.utils.Resource
import com.prodev.muslimq.utils.networkBoundResource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ShalatRepositoryImpl @Inject constructor(
    private val remoteDataSource: RemoteDataSource, private val localDataSource: LocalDataSource
) : ShalatRepository {

    override suspend fun getAllCity(): Flow<Resource<List<CityResponse>>> {
        return remoteDataSource.getAllCity()
    }

    override fun getShalatDailyByCity(
        id: Int, year: Int, month: Int, day: Int
    ): Flow<Resource<List<ShalatEntity>>> = networkBoundResource(
        query = {
            localDataSource.getShalatDailyByCity(id)
        },
        fetch = {
            remoteDataSource.getShalatDailyByCity(id, year, month, day)
        },
        saveFetchResult = { shalat ->
            val local = ArrayList<ShalatEntity>()
            val cityId = shalat.data.id
            shalat.data.jadwal.let {
                local.add(
                    ShalatEntity(
                        id = cityId.toInt(),
                        imsak = it.imsak,
                        subuh = it.subuh,
                        dzuhur = it.dzuhur,
                        ashar = it.ashar,
                        maghrib = it.maghrib,
                        isya = it.isya,
                    )
                )
            }
            localDataSource.deleteShalatDaily()
            localDataSource.insertShalatDaily(local)
        }
    )
}