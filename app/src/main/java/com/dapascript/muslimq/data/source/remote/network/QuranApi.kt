package com.dapascript.muslimq.data.source.remote.network

import com.dapascript.muslimq.data.source.remote.model.QuranDetailResponse
import com.dapascript.muslimq.data.source.remote.model.QuranResponse
import retrofit2.http.GET
import retrofit2.http.Path

interface QuranApi {

    @GET("surat")
    suspend fun getQuran(): List<QuranResponse>

    @GET("surat/{nomor}")
    suspend fun getQuranDetail(@Path("nomor") number: Int): QuranDetailResponse
}