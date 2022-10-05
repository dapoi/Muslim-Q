package com.prodev.muslimq.data.repository

import com.prodev.muslimq.data.source.local.model.ShalatEntity
import com.prodev.muslimq.data.source.remote.model.CityResponse
import com.prodev.muslimq.utils.Resource
import kotlinx.coroutines.flow.Flow

interface ShalatRepository {

    suspend fun getAllCity(): Flow<Resource<List<CityResponse>>>

    fun getShalatDailyByCity(
        id: Int,
        year: Int,
        month: Int,
        day: Int
    ): Flow<Resource<List<ShalatEntity>>>
}