package com.prodev.muslimq.core.data.source.remote.network

import com.prodev.muslimq.core.data.source.remote.model.CityResponse
import com.prodev.muslimq.core.data.source.remote.model.ProvinceResponse
import retrofit2.http.GET
import retrofit2.http.Path

interface AreaApi {

    @GET("provinces.json")
    suspend fun getAllProvince(): List<ProvinceResponse>

    @GET("regencies/{provinceId}.json")
    suspend fun getAllCity(
        @Path("provinceId") provinceId: String
    ): List<CityResponse>
}