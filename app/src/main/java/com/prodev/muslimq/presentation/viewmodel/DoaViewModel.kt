package com.prodev.muslimq.presentation.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.prodev.muslimq.core.data.repository.DoaRepository
import com.prodev.muslimq.core.data.source.local.model.DoaEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class DoaViewModel @Inject constructor(private val doaRepository: DoaRepository) : ViewModel() {

    fun getDoa(): LiveData<List<DoaEntity>> = doaRepository.getDoa().asLiveData()
}