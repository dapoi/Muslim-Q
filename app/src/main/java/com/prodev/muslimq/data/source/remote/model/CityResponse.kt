package com.prodev.muslimq.data.source.remote.model

import com.squareup.moshi.Json

data class CityResponse(

    @Json(name = "lokasi")
    val lokasi: String,

    @Json(name = "id")
    val id: String
)