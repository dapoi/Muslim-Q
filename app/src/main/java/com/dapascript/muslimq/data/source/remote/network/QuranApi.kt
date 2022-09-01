package com.dapascript.muslimq.data.source.remote.network

import com.dapascript.muslimq.data.source.remote.model.SurahResponse
import retrofit2.http.GET

interface QuranApi {

    @GET("surat")
    suspend fun getSurah(): List<SurahResponse>
}