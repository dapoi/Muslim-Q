package com.prodev.muslimq.data.source.local.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "shalat")
data class ShalatEntity(

    @PrimaryKey val id: String,
    val subuh: String,
    val dzuhur: String,
    val ashar: String,
    val maghrib: String,
    val isya: String
)
