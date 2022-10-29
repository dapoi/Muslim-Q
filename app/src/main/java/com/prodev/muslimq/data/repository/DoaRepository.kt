package com.prodev.muslimq.data.repository

import androidx.lifecycle.LiveData
import com.prodev.muslimq.data.source.local.model.DoaEntity
import com.prodev.muslimq.utils.Resource

interface DoaRepository {

    fun getDoa(): LiveData<Resource<List<DoaEntity>>>
}