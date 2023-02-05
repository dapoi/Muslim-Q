package com.prodev.muslimq.data.source.remote.model

import com.squareup.moshi.Json

data class ShalatResponse(

	@Json(name="data")
	val data: List<ShalatItem>
//	@Json(name="items")
//	val items: List<ShalatItem>
)

data class ShalatItem(

	@Json(name="timings")
	val timings: Timing,

	@Json(name="date")
	val date: Date

//	@Json(name="fajr")
//	val fajr: String,
//
//	@Json(name="dhuhr")
//	val dhuhr: String,
//
//	@Json(name="asr")
//	val asr: String,
//
//	@Json(name="maghrib")
//	val maghrib: String,
//
//	@Json(name="isha")
//	val isha: String,
)

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

data class Date(

	@Json(name="readable")
	val readable: String,
)