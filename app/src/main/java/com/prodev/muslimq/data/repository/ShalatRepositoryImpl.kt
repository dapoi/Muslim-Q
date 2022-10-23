package com.prodev.muslimq.data.repository

import com.prodev.muslimq.data.source.remote.RemoteDataSource
import com.prodev.muslimq.data.source.remote.model.CityResponse
import com.prodev.muslimq.data.source.remote.model.ProvinceResponse
import com.prodev.muslimq.utils.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ShalatRepositoryImpl @Inject constructor(
    private val remoteDataSource: RemoteDataSource
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
}