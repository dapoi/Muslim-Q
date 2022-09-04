package com.prodev.muslimq.data.repository

import com.prodev.muslimq.data.source.local.model.QuranDetailEntity
import com.prodev.muslimq.data.source.local.model.QuranEntity
import com.prodev.muslimq.utils.Resource
import kotlinx.coroutines.flow.Flow

interface QuranRepository {
    fun getQuran(): Flow<Resource<List<QuranEntity>>>
    fun getQuranDetail(id: Int): Flow<Resource<QuranDetailEntity>>
}