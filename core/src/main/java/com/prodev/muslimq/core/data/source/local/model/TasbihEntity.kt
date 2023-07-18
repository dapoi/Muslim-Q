package com.prodev.muslimq.core.data.source.local.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasbih")
data class TasbihEntity(
    @PrimaryKey
    val dzikirName: String
)
