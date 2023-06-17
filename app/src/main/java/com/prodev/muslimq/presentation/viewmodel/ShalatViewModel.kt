package com.prodev.muslimq.presentation.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import com.prodev.muslimq.core.data.repository.ShalatRepository
import com.prodev.muslimq.core.data.source.local.model.ShalatEntity
import com.prodev.muslimq.core.data.source.remote.model.CityResponse
import com.prodev.muslimq.core.data.source.remote.model.ProvinceResponse
import com.prodev.muslimq.core.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ShalatViewModel @Inject constructor(
    private val repository: ShalatRepository,
) : ViewModel() {

    var searchQuery: String = ""
    var filteredData: List<ProvinceResponse> = emptyList()

    private val _getTimeShalat = MutableLiveData<Pair<String, String>>()
    val getTimeShalat: LiveData<Resource<ShalatEntity>> = _getTimeShalat.switchMap { pair ->
        repository.getShalatDaily(pair.first, pair.second).asLiveData()
    }

    private var _getProvinceResult = MutableLiveData<Resource<List<ProvinceResponse>>>()
    val getProvinceResult: LiveData<Resource<List<ProvinceResponse>>> get() = _getProvinceResult

    private var _getCityResult = MutableLiveData<Resource<List<CityResponse>>>()
    val getCityResult: LiveData<Resource<List<CityResponse>>> get() = _getCityResult

    fun getShalatTime(location: Pair<String, String>) {
        if (location == _getTimeShalat.value) return

        _getTimeShalat.value = location
    }

    init {
        viewModelScope.launch {
            repository.getAllProvince().collect {
                _getProvinceResult.value = it
            }
        }
    }

    fun getAllCity(id: String) {
        viewModelScope.launch {
            repository.getAllCity(id).collect {
                _getCityResult.value = it
            }
        }
    }

    fun refreshShalatTime() {
        _getTimeShalat.value?.let {
            _getTimeShalat.value = it
        }
    }
}