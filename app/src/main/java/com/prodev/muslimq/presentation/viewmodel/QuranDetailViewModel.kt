package com.prodev.muslimq.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.prodev.muslimq.data.repository.QuranRepositoryImpl
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class QuranDetailViewModel @Inject constructor(
    private val repositoryImpl: QuranRepositoryImpl
) : ViewModel() {
    fun getQuranDetail(surahNumber: Int) = repositoryImpl.getQuranDetail(surahNumber).asLiveData()
}