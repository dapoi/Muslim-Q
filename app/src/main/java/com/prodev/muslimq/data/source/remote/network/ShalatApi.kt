package com.prodev.muslimq.data.source.remote.network

import com.prodev.muslimq.data.source.remote.model.ShalatResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface ShalatApi {

//    @GET("{city}/daily.json")
//    suspend fun getShalatDaily(
//        @Path("city") city: String
//    ): ShalatResponse

    @GET("v1/calendarByCity")
    suspend fun getShalatDaily(
        @Query("city") city: String,
        @Query("country") country: String = "Indonesia",
    ): ShalatResponse
}