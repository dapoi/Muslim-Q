package com.prodev.muslimq.core.data.repository

import android.util.Log
import com.prodev.muslimq.core.data.source.local.model.DoaEntity
import com.prodev.muslimq.core.utils.JsonHelper
import com.prodev.muslimq.core.utils.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DoaRepositoryImpl @Inject constructor(
    private val jsonHelper: JsonHelper
) : DoaRepository {

    override fun getDoa(): Flow<Resource<List<DoaEntity>>> {
        return flow {
            emit(Resource.Loading())
            try {
                val data = jsonHelper.getDoa()
                if (data.isNotEmpty()) {
                    emit(Resource.Success(data))
                } else {
                    emit(Resource.Error(Throwable("Data is empty")))
                }
            } catch (e: Exception) {
                emit(Resource.Error(e))
            }
        }
    }
}