package com.prodev.muslimq.core.data.source.remote.network

import com.prodev.muslimq.core.data.source.remote.model.QuranDetailResponse
import com.prodev.muslimq.core.data.source.remote.model.QuranResponse
import com.prodev.muslimq.core.data.source.remote.model.QuranTafsirResponse
import retrofit2.http.GET
import retrofit2.http.Path

interface QuranApi {

    @GET("api/v2/surat")
    suspend fun getQuran(): QuranResponse

    @GET("api/v2/surat/{nomor}")
    suspend fun getQuranDetail(@Path("nomor") number: Int): QuranDetailResponse

    @GET("api/v2/tafsir/{nomor}")
    suspend fun getQuranTafsir(@Path("nomor") number: Int): QuranTafsirResponse
}