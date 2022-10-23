package com.prodev.muslimq.data.source.remote.network

import com.prodev.muslimq.data.source.remote.model.ShalatResponse
import retrofit2.http.GET
import retrofit2.http.Path

interface ShalatApi {

    @GET("{city}/daily.json")
    suspend fun getShalatDaily(
        @Path("city") city: String
    ): ShalatResponse
}