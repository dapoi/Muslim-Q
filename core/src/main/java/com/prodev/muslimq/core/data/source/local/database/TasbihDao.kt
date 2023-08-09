package com.prodev.muslimq.core.data.source.local.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.prodev.muslimq.core.data.source.local.model.TasbihEntity
import com.prodev.muslimq.core.utils.DzikirType
import kotlinx.coroutines.flow.Flow

@Dao
interface TasbihDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDzikir(tasbih: TasbihEntity)

    @Query("SELECT * FROM tasbih")
    fun getAllDzikir(): Flow<List<TasbihEntity>>

    @Query("DELETE FROM tasbih WHERE dzikirName = :dzikirName")
    suspend fun deleteDzikir(dzikirName: String)
    @Query("SELECT * FROM tasbih WHERE dzikirType = :dzikirType")
    fun getAllDzikirByType(dzikirType: DzikirType): Flow<List<TasbihEntity>>
    @Query("UPDATE tasbih SET maxCount = :maxCount WHERE id = :id")
    suspend fun updateMaxCount(id: Int, maxCount: Int): Int
}