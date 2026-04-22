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
import com.prodev.muslimq.presentation.view.quran.QuranDetailFragmentArgs
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class QuranDetailViewModel @Inject constructor(
    private val quranRepository: QuranRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    val args by lazy { QuranDetailFragmentArgs.fromSavedStateHandle(savedStateHandle) }

    private val _getQuranDetail = MutableLiveData<QuranDetailEntity?>()
    val getQuranDetail: LiveData<QuranDetailEntity?> = _getQuranDetail

    private val _getQuranTafsir = MutableLiveData<Pair<Resource<TafsirDetailItem>, Int>>()
    val getQuranTafsir: LiveData<Pair<Resource<TafsirDetailItem>, Int>> = _getQuranTafsir

    init {
        viewModelScope.launch {
            quranRepository.getQuranDetail(args.surahId).collect { response ->
                _getQuranDetail.value = response.data
            }
        }
    }

    fun getQuranTafsir(ayahNumber: Int) {
        viewModelScope.launch {
            quranRepository.getQuranTafsir(args.surahId, ayahNumber).collect { response ->
                _getQuranTafsir.value = Pair(response, ayahNumber)
            }
        }
    }
}