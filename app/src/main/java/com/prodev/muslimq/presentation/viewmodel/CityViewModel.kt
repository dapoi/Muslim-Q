package com.prodev.muslimq.presentation.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.prodev.muslimq.core.data.repository.ShalatRepository
import com.prodev.muslimq.core.data.source.remote.model.CityResponse
import com.prodev.muslimq.core.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CityViewModel @Inject constructor(
    private val shalatRepository: ShalatRepository
) : ViewModel() {

    private var _getCity = MutableLiveData<Resource<List<CityResponse>>>()
    val getCity: LiveData<Resource<List<CityResponse>>> get() = _getCity

    fun setCity(provinceId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            shalatRepository.getAllCity(provinceId).collect {
                _getCity.postValue(it)
            }
        }
    }
}