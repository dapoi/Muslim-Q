package com.prodev.muslimq.core.data.repository

import com.prodev.muslimq.core.data.source.local.database.TasbihDao
import com.prodev.muslimq.core.data.source.local.model.TasbihEntity
import com.prodev.muslimq.core.utils.DzikirType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.runBlocking
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TasbihRepositoryImpl @Inject constructor(
    private val tasbihDao: TasbihDao
) : TasbihRepository {

    override suspend fun insertDzikir(tasbih: TasbihEntity) {
        runBlocking { tasbihDao.insertDzikir(tasbih) }
    }

    override fun getAllDzikir(): Flow<List<TasbihEntity>> {
        return tasbihDao.getAllDzikir()
    }

    override suspend fun deleteDzikir(dzikirName: String) {
        tasbihDao.deleteDzikir(dzikirName)
    }

    override fun getAllDzikirByType(dzikirType: DzikirType): Flow<List<TasbihEntity>> {
        return tasbihDao.getAllDzikirByType(dzikirType)
    }

    override fun updateMaxCount(id: Int, maxCount: Int) = flow {
        emit(tasbihDao.updateMaxCount(id, maxCount))
    }
}

interface TasbihRepository {

    suspend fun insertDzikir(tasbih: TasbihEntity)

    fun getAllDzikir(): Flow<List<TasbihEntity>>

    suspend fun deleteDzikir(dzikirName: String)

    fun getAllDzikirByType(dzikirType: DzikirType): Flow<List<TasbihEntity>>

    fun updateMaxCount(id: Int, maxCount: Int): Flow<Int>

}