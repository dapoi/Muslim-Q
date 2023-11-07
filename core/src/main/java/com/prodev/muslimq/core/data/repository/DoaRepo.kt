package com.prodev.muslimq.core.data.repository

import com.prodev.muslimq.core.data.source.local.model.DoaEntity
import com.prodev.muslimq.core.utils.JsonHelper
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DoaRepositoryImpl @Inject constructor(
    private val jsonHelper: JsonHelper
) : DoaRepository {

    override fun getDoa(): Flow<List<DoaEntity>> = flow {
        emit(jsonHelper.getDoa())
    }
}

interface DoaRepository {

    fun getDoa(): Flow<List<DoaEntity>>
}