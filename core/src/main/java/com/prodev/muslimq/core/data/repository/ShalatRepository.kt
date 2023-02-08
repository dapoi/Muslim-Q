package com.prodev.muslimq.core.data.repository

import com.prodev.muslimq.core.data.source.local.model.ShalatEntity
import com.prodev.muslimq.core.data.source.remote.model.CityResponse
import com.prodev.muslimq.core.data.source.remote.model.ProvinceResponse
import com.prodev.muslimq.core.utils.Resource
import kotlinx.coroutines.flow.Flow

interface ShalatRepository {

    fun getAllProvince(): Flow<Resource<List<ProvinceResponse>>>

    fun getAllCity(id: String): Flow<Resource<List<CityResponse>>>

    fun getShalatDaily(city: String): Flow<Resource<ShalatEntity>>
}