package com.prodev.muslimq.presentation.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.prodev.muslimq.core.data.repository.QuranRepository
import com.prodev.muslimq.core.data.source.local.model.QuranDetailEntity
import com.prodev.muslimq.core.data.source.remote.model.TafsirDetailItem
import com.prodev.muslimq.core.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class QuranDetailViewModel @Inject constructor(
    private val quranRepository: QuranRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _getQuranDetail = MutableLiveData<Resource<QuranDetailEntity>>()
    val getQuranDetail: LiveData<Resource<QuranDetailEntity>> = _getQuranDetail

    private val _getQuranTafsir = MutableLiveData<Pair<Resource<TafsirDetailItem>, Int>>()
    val getQuranTafsir: LiveData<Pair<Resource<TafsirDetailItem>, Int>> = _getQuranTafsir

    private val surahId = savedStateHandle.get<Int>("surahId")

    init {
        fetchQuranDetail()
    }

    fun fetchQuranDetail() {
        viewModelScope.launch {
            if (surahId != null) {
                quranRepository.getQuranDetail(surahId).collect {
                    _getQuranDetail.value = it
                }
            }
        }
    }

    fun fetchQuranTafsir(
        surahId: Int = this.surahId!!,
        ayahNumber: Int
    ) {
        viewModelScope.launch {
            quranRepository.getQuranTafsir(surahId, ayahNumber).collect { response ->
                _getQuranTafsir.value = Pair(response, ayahNumber)
            }
        }
    }
}