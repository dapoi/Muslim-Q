package com.dapascript.muslimq.data.source.remote.model

import com.squareup.moshi.Json

data class QuranResponse(

    @Json(name = "nomor")
    val nomor: Int,

	@Json(name = "nama")
    val nama: String,

	@Json(name = "nama_latin")
    val nama_latin: String,

	@Json(name = "jumlah_ayat")
    val jumlah_ayat: Int,

	@Json(name = "tempat_turun")
    val tempat_turun: String,

	@Json(name = "arti")
    val arti: String,

	@Json(name = "deskripsi")
    val deskripsi: String,

	@Json(name = "audio")
    val audio: String
)
