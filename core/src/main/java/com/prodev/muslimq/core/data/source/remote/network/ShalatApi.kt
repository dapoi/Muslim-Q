package com.prodev.muslimq.core.data.source.remote.network

import com.prodev.muslimq.core.data.source.remote.model.ShalatResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface ShalatApi {

    @GET("v1/calendarByCity")
    suspend fun getShalatDaily(
        @Query("city") city: String,
        @Query("country") country: String = "",
    ): ShalatResponse
}