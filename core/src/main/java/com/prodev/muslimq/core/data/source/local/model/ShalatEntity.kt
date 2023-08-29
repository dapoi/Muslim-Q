package com.prodev.muslimq.core.data.source.local.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import javax.annotation.Nonnull

@Entity(tableName = "shalat")
data class ShalatEntity(
    @PrimaryKey
    val day: String = "",
    val city: String,
    val country: String? = "",
    val shubuh: String? = "",
    val dzuhur: String? = "",
    val ashar: String? = "",
    val maghrib: String? = "",
    val isya: String? = "",
    val lat: Double? = 0.0,
    val lon: Double? = 0.0
)
