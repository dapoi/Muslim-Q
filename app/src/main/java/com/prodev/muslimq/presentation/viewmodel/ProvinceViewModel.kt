package com.prodev.muslimq.presentation.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.prodev.muslimq.core.data.repository.ShalatRepository
import com.prodev.muslimq.core.data.source.remote.model.ProvinceResponse
import com.prodev.muslimq.core.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProvinceViewModel @Inject constructor(
    private val shalatRepository: ShalatRepository,
    private val ioDispatcher: CoroutineDispatcher
) : ViewModel() {

    var searchQuery: String = ""
    var filteredData: List<ProvinceResponse> = emptyList()

    private var _getProvince = MutableLiveData<Resource<List<ProvinceResponse>>>()
    val getProvince: LiveData<Resource<List<ProvinceResponse>>> get() = _getProvince

    fun setProvince() {
        viewModelScope.launch(ioDispatcher) {
            shalatRepository.getAllProvince().collect {
                _getProvince.postValue(it)
            }
        }
    }

    init {
        setProvince()
    }
}