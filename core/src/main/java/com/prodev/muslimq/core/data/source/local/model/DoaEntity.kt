package com.prodev.muslimq.core.data.source.local.model

data class DoaEntity(
    val id: String,
    val title: String,
    val body : List<DoaBodyEntity>,
)

data class DoaBodyEntity(
    val arab: String,
    val latin: String,
    val translate: String
)
