package com.prodev.muslimq.core.data.repository

import com.prodev.muslimq.core.data.source.local.model.AsmaulHusnaEntity
import com.prodev.muslimq.core.utils.JsonHelper
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class AsmaulHusnaRepositoryImpl @Inject constructor(
    private val jsonHelper: JsonHelper
) : AsmaulHusnaRepository {

    override fun getAsmaulHusna(): Flow<List<AsmaulHusnaEntity>> = flow {
        emit(jsonHelper.getAsmaulHusna())
    }
}

interface AsmaulHusnaRepository {
    fun getAsmaulHusna(): Flow<List<AsmaulHusnaEntity>>
}