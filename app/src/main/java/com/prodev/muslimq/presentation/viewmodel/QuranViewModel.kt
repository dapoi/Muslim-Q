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
    private val repositoryImpl: QuranRepository
) : ViewModel() {

    var searchQuery: String = ""
    var filteredData: List<QuranEntity> = emptyList()

    private var _getListQuran = MutableLiveData<Resource<List<QuranEntity>>>()
    val getListQuran: LiveData<Resource<List<QuranEntity>>> get() = _getListQuran

    private var _isCollapse = MutableLiveData<Boolean>()
    val isCollapse: LiveData<Boolean> get() = _isCollapse

    init {
        viewModelScope.launch {
            repositoryImpl.getQuran().collect {
                _getListQuran.value = it
            }
        }
    }

    fun setCollapseAppbar(isCollapse: Boolean) {
        _isCollapse.value = isCollapse
    }

    fun getQuranDetail(surahId: Int) = repositoryImpl.getQuranDetail(surahId).asLiveData()

    fun getQuranTafsir(
        surahId: Int,
        ayahNumber: Int
    ) = repositoryImpl.getQuranTafsir(surahId, ayahNumber).asLiveData()

    fun getBookmark(): LiveData<List<QuranDetailEntity>> = repositoryImpl.getBookmark().asLiveData()

    fun insertToBookmark(quran: QuranDetailEntity, isBookmarked: Boolean) {
        repositoryImpl.insertToBookmark(quran, isBookmarked)
    }

    fun deleteAllBookmark() {
        repositoryImpl.deleteAllBookmark()
    }

    fun deleteBookmark(surahId: Int) {
        repositoryImpl.deleteBookmark(surahId)
    }
}