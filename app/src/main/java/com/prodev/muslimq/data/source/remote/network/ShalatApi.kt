package com.prodev.muslimq.data.source.remote.network

import com.prodev.muslimq.data.source.remote.model.ShalatResponse
import retrofit2.http.GET
import retrofit2.http.Path

interface ShalatApi {

    @GET("sholat/jadwal/{idkota}/{tahun}/{bulan}/{tanggal}")
    suspend fun getShalatDailyByCity(
        @Path("idkota") id: Int,
        @Path("tahun") year: Int,
        @Path("bulan") month: Int,
        @Path("tanggal") day: Int
    ): ShalatResponse
}