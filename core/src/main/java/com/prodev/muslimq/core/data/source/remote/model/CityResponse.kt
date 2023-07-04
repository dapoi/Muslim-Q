package com.prodev.muslimq.core.data.source.remote.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class CityResponse(

    @Json(name = "province_id")
    val province_id: String,

    @Json(name = "name")
    val name: String,

    @Json(name = "id")
    val id: String
)
