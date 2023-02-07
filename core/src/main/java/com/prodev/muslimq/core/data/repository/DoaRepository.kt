package com.prodev.muslimq.core.data.repository

import androidx.lifecycle.LiveData
import com.prodev.muslimq.core.data.source.local.model.DoaEntity
import com.prodev.muslimq.core.utils.Resource

interface DoaRepository {

    fun getDoa(): LiveData<Resource<List<DoaEntity>>>
}