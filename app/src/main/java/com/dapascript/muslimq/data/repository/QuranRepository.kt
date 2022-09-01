package com.dapascript.muslimq.data.repository

import com.dapascript.muslimq.data.source.local.model.QuranEntity
import com.dapascript.muslimq.utils.Resource
import kotlinx.coroutines.flow.Flow

interface QuranRepository {
    fun getSurah(): Flow<Resource<List<QuranEntity>>>
}