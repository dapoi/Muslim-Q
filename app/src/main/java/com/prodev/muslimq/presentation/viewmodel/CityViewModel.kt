package com.prodev.muslimq.presentation.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.prodev.muslimq.core.data.repository.ShalatRepository
import com.prodev.muslimq.core.data.source.remote.model.CityResponse
import com.prodev.muslimq.core.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CityViewModel @Inject constructor(
    private val shalatRepository: ShalatRepository,
    private val ioDispatcher: CoroutineDispatcher,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val provinceId = savedStateHandle.get<String>("provinceId") ?: ""

    private val _getCity = MutableLiveData<Resource<List<CityResponse>>>()
    val getCity: LiveData<Resource<List<CityResponse>>> get() = _getCity

    fun setCity() {
        viewModelScope.launch(ioDispatcher) {
            shalatRepository.getAllCity(provinceId).collect { response ->
                _getCity.postValue(response)
            }
        }
    }

    init {
        setCity()
    }
}