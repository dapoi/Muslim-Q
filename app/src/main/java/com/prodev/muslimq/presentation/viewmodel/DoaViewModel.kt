package com.prodev.muslimq.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.prodev.muslimq.core.data.repository.DoaRepository
import com.prodev.muslimq.core.data.source.local.model.DoaEntity
import com.prodev.muslimq.core.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class DoaViewModel @Inject constructor(private val doaRepository: DoaRepository) : ViewModel() {

    fun getDoa(): LiveData<Resource<List<DoaEntity>>> {
        Log.d("DoaViewModel", "getDoa: ${doaRepository.getDoa()}")
        return doaRepository.getDoa()
    }
}