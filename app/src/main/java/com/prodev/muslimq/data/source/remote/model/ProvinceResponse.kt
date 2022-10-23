package com.prodev.muslimq.data.source.remote.model

import com.squareup.moshi.Json

data class ProvinceResponse(

	@Json(name="name")
	val name: String,

	@Json(name="id")
	val id: String
)