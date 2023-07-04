package com.prodev.muslimq.core.data.repository

import com.prodev.muslimq.core.data.source.local.model.DoaEntity
import com.prodev.muslimq.core.utils.Resource
import kotlinx.coroutines.flow.Flow

interface DoaRepository {

    fun getDoa(): Flow<Resource<List<DoaEntity>>>
}