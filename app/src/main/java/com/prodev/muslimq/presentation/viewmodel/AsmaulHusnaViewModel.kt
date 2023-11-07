package com.prodev.muslimq.presentation.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.prodev.muslimq.core.data.repository.AsmaulHusnaRepository
import com.prodev.muslimq.core.data.source.local.model.AsmaulHusnaEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class AsmaulHusnaViewModel @Inject constructor(
    private val asmaulHusnaRepository: AsmaulHusnaRepository
) : ViewModel() {

    fun getAsmaulHusna(): LiveData<List<AsmaulHusnaEntity>> =
        asmaulHusnaRepository.getAsmaulHusna().asLiveData()
}