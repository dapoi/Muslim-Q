package com.prodev.muslimq.presentation.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.prodev.muslimq.core.data.preference.DataStorePreference
import com.prodev.muslimq.core.data.repository.ShalatRepository
import com.prodev.muslimq.core.data.source.local.model.ShalatEntity
import com.prodev.muslimq.core.di.IoDispatcher
import com.prodev.muslimq.core.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ShalatViewModel @Inject constructor(
    private val repository: ShalatRepository,
    private val dataStorePreference: DataStorePreference,
    @IoDispatcher private val dispatcher: CoroutineDispatcher
) : ViewModel() {

    private val _getShalatTime = MutableLiveData<Resource<ShalatEntity>>()
    val getShalatTime: LiveData<Resource<ShalatEntity>> get() = _getShalatTime

    @OptIn(ExperimentalCoroutinesApi::class)
    fun fetchShalatTime() {
        viewModelScope.launch(dispatcher) {
            dataStorePreference.getCityAndCountryData.flatMapLatest { pair ->
                val city = if (pair.first.contains("Ambon", true)) "Ibu Kota Kep. Maluku"
                else pair.first
                val country = pair.second

                repository.getShalatDaily(city, country)
            }.collect { result ->
                _getShalatTime.postValue(result)
            }
        }
    }

    init {
        fetchShalatTime()
    }
}