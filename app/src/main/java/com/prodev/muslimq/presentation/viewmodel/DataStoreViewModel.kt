package com.prodev.muslimq.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.prodev.muslimq.data.preference.DataStorePreference
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DataStoreViewModel @Inject constructor(
    private val dataStorePreference: DataStorePreference
) : ViewModel() {

    fun saveSurah(surahName: String, surahMeaning: String) {
        viewModelScope.launch(Dispatchers.IO) {
            dataStorePreference.saveSurah(surahName, surahMeaning)
        }
    }

    val getSurah = dataStorePreference.getSurah.asLiveData()

    fun saveProvinceData(provinceId: String, provinceName: String) {
        viewModelScope.launch(Dispatchers.IO) {
            dataStorePreference.saveProvinceData(provinceId, provinceName)
        }
    }

    val getProvinceData = dataStorePreference.getProvinceData.asLiveData()

    fun saveCityData(cityName: String) {
        viewModelScope.launch(Dispatchers.IO) {
            dataStorePreference.saveCityData(cityName)
        }
    }

    val getCityData = dataStorePreference.getCityData.asLiveData()
}