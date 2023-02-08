package com.prodev.muslimq.core.data.repository

import com.prodev.muslimq.core.data.source.local.model.QuranDetailEntity
import com.prodev.muslimq.core.data.source.local.model.QuranEntity
import com.prodev.muslimq.core.utils.Resource
import kotlinx.coroutines.flow.Flow

interface QuranRepository {
    fun getQuran(): Flow<Resource<List<QuranEntity>>>
    fun getQuranDetail(id: Int): Flow<Resource<QuranDetailEntity>>
}