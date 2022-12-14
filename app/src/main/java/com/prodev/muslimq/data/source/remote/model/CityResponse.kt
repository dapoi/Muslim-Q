package com.prodev.muslimq.data.source.remote.model

import com.squareup.moshi.Json

data class CityResponse(

    @Json(name = "province_id")
    val province_id: String,

    @Json(name = "name")
    val name: String,

    @Json(name = "id")
    val id: String
)
