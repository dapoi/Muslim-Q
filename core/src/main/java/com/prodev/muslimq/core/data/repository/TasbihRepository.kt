package com.prodev.muslimq.core.data.repository

import com.prodev.muslimq.core.data.source.local.model.TasbihEntity
import com.prodev.muslimq.core.utils.DzikirType
import kotlinx.coroutines.flow.Flow

interface TasbihRepository {

    suspend fun insertDzikir(tasbih: TasbihEntity)

    fun getAllDzikir(): Flow<List<TasbihEntity>>

    suspend fun deleteDzikir(dzikirName: String)

    fun getAllDzikirByType(dzikirType: DzikirType): Flow<List<TasbihEntity>>

    fun updateMaxCount(id: Int, maxCount: Int): Flow<Int>

}