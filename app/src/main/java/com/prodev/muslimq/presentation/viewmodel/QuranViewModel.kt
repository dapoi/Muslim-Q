package com.prodev.muslimq.presentation.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.prodev.muslimq.core.data.repository.QuranRepository
import com.prodev.muslimq.core.data.source.local.model.QuranDetailEntity
import com.prodev.muslimq.core.data.source.local.model.QuranEntity
import com.prodev.muslimq.core.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class QuranViewModel @Inject constructor(
    private val quranRepository: QuranRepository
) : ViewModel() {

    var searchQuery: String = ""
    var filteredData: List<QuranEntity> = emptyList()

    private val _getListQuran = MutableLiveData<Resource<List<QuranEntity>>>()
    val getListQuran: LiveData<Resource<List<QuranEntity>>> get() = _getListQuran

    private val _isCollapse = MutableLiveData<Boolean>()
    val isCollapse: LiveData<Boolean> get() = _isCollapse

    private val getDetail = MutableLiveData<Resource<QuranDetailEntity>>()
    val getDetailQuran: LiveData<Resource<QuranDetailEntity>> get() = getDetail

    init {
        viewModelScope.launch {
            quranRepository.getQuran().collect {
                _getListQuran.value = it
            }
        }
    }

    fun setCollapseAppbar(isCollapse: Boolean) {
        _isCollapse.value = isCollapse
    }

    fun getQuranDetail(surahId: Int) {
        viewModelScope.launch {
            quranRepository.getQuranDetail(surahId).collect {
                getDetail.value = it
            }
        }
    }

    fun getQuranTafsir(
        surahId: Int,
        ayahNumber: Int
    ) = quranRepository.getQuranTafsir(surahId, ayahNumber).asLiveData()
}