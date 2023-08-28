package com.prodev.muslimq.core.data.source.remote.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ShalatResponse(

    @Json(name = "data")
    val data: List<ShalatItem>
)

@JsonClass(generateAdapter = true)
data class ShalatItem(

    @Json(name = "timings")
    val timings: Timing,

    @Json(name = "meta")
    val meta: Meta
)

@JsonClass(generateAdapter = true)
data class Timing(

    @Json(name = "Fajr")
    val Fajr: String? = null,

    @Json(name = "Dhuhr")
    val Dhuhr: String? = null,

    @Json(name = "Asr")
    val Asr: String? = null,

    @Json(name = "Maghrib")
    val Maghrib: String? = null,

    @Json(name = "Isha")
    val Isha: String? = null,
)

@JsonClass(generateAdapter = true)
data class Meta(

    @Json(name = "latitude")
    val latitude: Double? = null,

    @Json(name = "longitude")
    val longitude: Double? = null
)