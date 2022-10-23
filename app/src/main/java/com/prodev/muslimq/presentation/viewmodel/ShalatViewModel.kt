package com.prodev.muslimq.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.prodev.muslimq.data.repository.ShalatRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ShalatViewModel @Inject constructor(
    private val repository: ShalatRepository
) : ViewModel() {

    fun getAllProvince() = repository.getAllProvince().asLiveData()

    fun getAllCity(id: String) = repository.getAllCity(id).asLiveData()
}