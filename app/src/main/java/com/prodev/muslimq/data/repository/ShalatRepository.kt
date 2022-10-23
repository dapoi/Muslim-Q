package com.prodev.muslimq.data.repository

import com.prodev.muslimq.data.source.local.model.ShalatEntity
import com.prodev.muslimq.data.source.remote.model.CityResponse
import com.prodev.muslimq.data.source.remote.model.ProvinceResponse
import com.prodev.muslimq.data.source.remote.model.ShalatResponse
import com.prodev.muslimq.utils.Resource
import kotlinx.coroutines.flow.Flow

interface ShalatRepository {

    fun getAllProvince(): Flow<Resource<List<ProvinceResponse>>>

    fun getAllCity(id: String): Flow<Resource<List<CityResponse>>>

    fun getShalatDaily(city: String): Flow<Resource<ShalatEntity>>
}