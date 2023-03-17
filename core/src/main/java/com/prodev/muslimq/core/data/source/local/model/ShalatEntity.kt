package com.prodev.muslimq.core.data.source.local.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "shalat")
data class ShalatEntity(

    @PrimaryKey val city: String,
    val country: String,
    val shubuh: String,
    val dzuhur: String,
    val ashar: String,
    val maghrib: String,
    val isya: String
)
