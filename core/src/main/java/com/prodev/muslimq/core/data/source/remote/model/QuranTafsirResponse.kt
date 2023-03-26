package com.prodev.muslimq.core.data.source.remote.model

import com.squareup.moshi.Json

data class QuranTafsirResponse(

    @Json(name = "code")
    val code: Int,

    @Json(name = "message")
    val message: String,

    @Json(name = "data")
    val data: TafsirItem
)

data class TafsirItem(

    @Json(name = "tafsir")
    val tafsir: List<TafsirDetailItem>
)

data class TafsirDetailItem(

    @Json(name = "ayat")
    val ayat: Int,

    @Json(name = "teks")
    val teks: String,
)
