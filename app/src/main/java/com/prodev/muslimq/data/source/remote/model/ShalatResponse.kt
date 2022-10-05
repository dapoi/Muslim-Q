package com.prodev.muslimq.data.source.remote.model

import com.squareup.moshi.Json

data class ShalatResponse(

	@Json(name="data")
	val data: Data,

	@Json(name="status")
	val status: Boolean
)

data class Koordinat(

	@Json(name="lintang")
	val lintang: String,

	@Json(name="lon")
	val lon: Double,

	@Json(name="lat")
	val lat: Double,

	@Json(name="bujur")
	val bujur: String
)

data class Data(

	@Json(name="jadwal")
	val jadwal: Jadwal,

	@Json(name="lokasi")
	val lokasi: String,

	@Json(name="daerah")
	val daerah: String,

	@Json(name="id")
	val id: String,

	@Json(name="koordinat")
	val koordinat: Koordinat
)

data class Jadwal(

	@Json(name="date")
	val date: String,

	@Json(name="imsak")
	val imsak: String,

	@Json(name="isya")
	val isya: String,

	@Json(name="dzuhur")
	val dzuhur: String,

	@Json(name="subuh")
	val subuh: String,

	@Json(name="dhuha")
	val dhuha: String,

	@Json(name="terbit")
	val terbit: String,

	@Json(name="tanggal")
	val tanggal: String,

	@Json(name="ashar")
	val ashar: String,

	@Json(name="maghrib")
	val maghrib: String
)
