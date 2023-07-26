package com.prodev.muslimq.presentation.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.switchMap
import com.prodev.muslimq.core.data.repository.ShalatRepository
import com.prodev.muslimq.core.data.source.local.model.ShalatEntity
import com.prodev.muslimq.core.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ShalatViewModel @Inject constructor(
    private val repository: ShalatRepository,
) : ViewModel() {

    private val _getShalatTime = MutableLiveData<Pair<String, String>>()
    val getShalatTime: LiveData<Resource<ShalatEntity>> = _getShalatTime.switchMap { pair ->
        val city = if (pair.first.contains("Ambon", true)) "Ibu Kota Kep. Maluku" else pair.first
        val country = pair.second
        repository.getShalatDaily(city, country).asLiveData()
    }

    fun setShalatTime(location: Pair<String, String>) {
        if (location == _getShalatTime.value) return

        _getShalatTime.value = location
    }

    fun refreshShalatTime() {
        _getShalatTime.value?.let {
            _getShalatTime.value = it
        }
    }
}