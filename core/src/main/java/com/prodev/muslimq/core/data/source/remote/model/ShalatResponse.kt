package com.prodev.muslimq.core.data.source.remote.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ShalatResponse(

	@Json(name="data")
	val data: List<ShalatItem>
//	@Json(name="items")
//	val items: List<ShalatItem>
)

@JsonClass(generateAdapter = true)
data class ShalatItem(

	@Json(name="timings")
	val timings: Timing,

	@Json(name="date")
	val date: Date
)

@JsonClass(generateAdapter = true)
data class Timing(

	@Json(name="Fajr")
	val Fajr: String,

	@Json(name="Dhuhr")
	val Dhuhr: String,

	@Json(name="Asr")
	val Asr: String,

	@Json(name="Maghrib")
	val Maghrib: String,

	@Json(name="Isha")
	val Isha: String,
)

@JsonClass(generateAdapter = true)
data class Date(

	@Json(name="readable")
	val readable: String,
)