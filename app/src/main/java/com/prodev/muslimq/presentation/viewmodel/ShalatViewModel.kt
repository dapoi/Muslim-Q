package com.prodev.muslimq.presentation.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.prodev.muslimq.core.data.repository.ShalatRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

@HiltViewModel
class ShalatViewModel @Inject constructor(
    private val repository: ShalatRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {
    fun getAllProvince() = repository.getAllProvince().asLiveData()

    fun getAllCity(id: String) = repository.getAllCity(id).asLiveData()

    fun getShalatDaily(city: String) = repository.getShalatDaily(city).asLiveData()
}