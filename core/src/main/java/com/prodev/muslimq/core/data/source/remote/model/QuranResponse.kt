package com.prodev.muslimq.core.data.source.remote.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class QuranResponse(

    @Json(name = "code")
    val code: Int,

    @Json(name = "message")
    val message: String,

    @Json(name = "data")
    val data: List<QuranItem>
)

@JsonClass(generateAdapter = true)
data class QuranItem(
    @Json(name = "nomor")
    val nomor: Int,

    @Json(name = "nama")
    val nama: String,

    @Json(name = "namaLatin")
    val namaLatin: String,

    @Json(name = "jumlahAyat")
    val jumlahAyat: Int,

    @Json(name = "tempatTurun")
    val tempatTurun: String,

    @Json(name = "arti")
    val arti: String,

    @Json(name = "deskripsi")
    val deskripsi: String
)
