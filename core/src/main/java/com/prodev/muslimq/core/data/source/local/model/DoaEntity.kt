package com.prodev.muslimq.core.data.source.local.model

data class DoaEntity(
    val id: String,
    val title: String,
    val arabic: String,
    val latin: String,
    val translation: String,
    var isExpanded: Boolean = false
)
