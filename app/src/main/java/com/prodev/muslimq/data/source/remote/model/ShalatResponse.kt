package com.prodev.muslimq.data.source.remote.model

import com.squareup.moshi.Json

data class ShalatResponse(

	@Json(name="items")
	val items: List<ShalatItem>
)

data class ShalatItem(

	@Json(name="fajr")
	val fajr: String,

	@Json(name="dhuhr")
	val dhuhr: String,

	@Json(name="asr")
	val asr: String,

	@Json(name="maghrib")
	val maghrib: String,

	@Json(name="isha")
	val isha: String,
)