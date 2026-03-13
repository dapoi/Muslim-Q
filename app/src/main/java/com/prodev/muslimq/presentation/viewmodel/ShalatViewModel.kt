package com.prodev.muslimq.presentation.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.prodev.muslimq.core.data.repository.ShalatRepository
import com.prodev.muslimq.core.data.source.local.model.ShalatEntity
import com.prodev.muslimq.core.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ShalatViewModel @Inject constructor(
    private val repository: ShalatRepository,
) : ViewModel() {

    var location = Pair("", "")

    private val _getShalatTime = MutableLiveData<Resource<ShalatEntity?>>()
    val getShalatTime: LiveData<Resource<ShalatEntity?>> = _getShalatTime

    fun fetchShalatTime(currentLocation: Pair<String, String>) {
        viewModelScope.launch {
            repository.getShalatDaily(currentLocation.first, currentLocation.second).collect {
                _getShalatTime.value = it
            }
        }
    }
}