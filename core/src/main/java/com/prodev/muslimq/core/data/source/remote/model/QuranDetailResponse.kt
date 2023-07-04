package com.prodev.muslimq.core.data.source.remote.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class QuranDetailResponse(
    @Json(name = "code")
    val code: Int,

    @Json(name = "message")
    val message: String,

    @Json(name = "data")
    val data: QuranDetailItem
)

@JsonClass(generateAdapter = true)
data class QuranDetailItem(

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
    val deskripsi: String,

    @Json(name = "audioFull")
    val audioFull: AudioFull,

    @Json(name = "ayat")
    val ayat: List<AyatItem>
)

@JsonClass(generateAdapter = true)
data class AudioFull(

    @Json(name = "05")
    val audio: String? = "",
)

@JsonClass(generateAdapter = true)
data class AyatItem(

    @Json(name = "nomorAyat")
    val nomorAyat: Int,

    @Json(name = "teksArab")
    val teksArab: String,

    @Json(name = "teksLatin")
    val teksLatin: String,

    @Json(name = "teksIndonesia")
    val teksIndonesia: String,

    @Json(name = "audio")
    val audio: AyahAudio,
)

@JsonClass(generateAdapter = true)
data class AyahAudio(

    @Json(name = "05")
    val ayahAudio: String? = "",
)