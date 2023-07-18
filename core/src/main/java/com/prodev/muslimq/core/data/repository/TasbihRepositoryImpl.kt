package com.prodev.muslimq.core.data.repository

import com.prodev.muslimq.core.data.source.local.database.TasbihDao
import com.prodev.muslimq.core.data.source.local.model.TasbihEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TasbihRepositoryImpl @Inject constructor(
    private val tasbihDao: TasbihDao
) : TasbihRepository {

    override suspend fun insertDzikir(tasbih: TasbihEntity) {
        tasbihDao.insertDzikir(tasbih)
    }

    override fun getAllDzikir(): Flow<List<TasbihEntity>> {
        return tasbihDao.getAllDzikir()
    }

    override suspend fun deleteDzikir(dzikirName: String) {
        tasbihDao.deleteDzikir(dzikirName)
    }
}