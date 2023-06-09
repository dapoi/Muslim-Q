package com.prodev.muslimq.presentation.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.prodev.muslimq.core.data.repository.ShalatRepository
import com.prodev.muslimq.core.data.source.local.model.ShalatEntity
import com.prodev.muslimq.core.data.source.remote.model.CityResponse
import com.prodev.muslimq.core.data.source.remote.model.ProvinceResponse
import com.prodev.muslimq.core.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ShalatViewModel @Inject constructor(
    private val repository: ShalatRepository,
) : ViewModel() {

    private var _getShalatTimeResult = MutableLiveData<Resource<ShalatEntity>?>()
    val getShalatTimeResult: LiveData<Resource<ShalatEntity>?> get() = _getShalatTimeResult

    private var _getProvinceResult = MutableLiveData<Resource<List<ProvinceResponse>>>()
    val getProvinceResult: LiveData<Resource<List<ProvinceResponse>>> get() = _getProvinceResult

    private var _getCityResult = MutableLiveData<Resource<List<CityResponse>>>()
    val getCityResult: LiveData<Resource<List<CityResponse>>> get() = _getCityResult

    private var _getShalatJob: Job? = null

    fun getShalatTime(city: String, country: String) {
        viewModelScope.launch {
            repository.getShalatDaily(city, country).collect {
                _getShalatTimeResult.postValue(it)
            }
        }
    }

    fun getAllProvince() {
        viewModelScope.launch {
            repository.getAllProvince().collect {
                _getProvinceResult.postValue(it)
            }
        }
    }

    fun getAllCity(id: String) {
        viewModelScope.launch {
            repository.getAllCity(id).collect {
                _getCityResult.postValue(it)
            }
        }
    }
}