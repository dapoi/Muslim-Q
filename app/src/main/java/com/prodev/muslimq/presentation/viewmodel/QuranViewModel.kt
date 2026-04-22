package com.prodev.muslimq.presentation.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.prodev.muslimq.core.data.repository.QuranRepository
import com.prodev.muslimq.core.data.source.local.model.QuranEntity
import com.prodev.muslimq.core.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.first
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

    init {
        getQuran()
    }

    fun getQuran() {
        viewModelScope.launch {
            quranRepository.getQuran().collect { response ->
                when (response) {
                    is Resource.Loading -> _getListQuran.value = response
                    is Resource.Success -> {
                        _getListQuran.value = Resource.Loading(response.data)
                        val deferredDetails = (1..114).map { surahId ->
                            async {
                                quranRepository.getQuranDetail(surahId).first { result ->
                                    result is Resource.Success || result is Resource.Error
                                }
                            }
                        }
                        val detailsResults = deferredDetails.awaitAll()
                        val findError = detailsResults.any { it is Resource.Error }

                        if (findError) {
                            _getListQuran.value = Resource.Error(Throwable())
                        } else {
                            _getListQuran.value = Resource.Success(response.data!!)
                        }
                    }

                    is Resource.Error -> _getListQuran.value = response
                }
            }
        }
    }

    fun setCollapseAppbar(isCollapse: Boolean) {
        _isCollapse.value = isCollapse
    }
}