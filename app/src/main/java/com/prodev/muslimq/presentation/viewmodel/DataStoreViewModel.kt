package com.prodev.muslimq.presentation.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.prodev.muslimq.core.data.preference.DataStorePreference
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DataStoreViewModel @Inject constructor(
    private val dataStorePreference: DataStorePreference
) : ViewModel() {

    fun saveSurah(
        surahId: Int,
        surahName: String,
        surahMeaning: String,
        surahDesc: String,
        ayahNumber: Int
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            dataStorePreference.saveSurah(surahId, surahName, surahMeaning, surahDesc, ayahNumber)
        }
    }

    fun saveProvinceData(provinceId: String, provinceName: String) {
        viewModelScope.launch(Dispatchers.IO) {
            dataStorePreference.saveProvinceData(provinceId, provinceName)
        }
    }

    fun saveAreaData(cityName: String, countryName: String) {
        viewModelScope.launch(Dispatchers.IO) {
            dataStorePreference.saveCityAndCountryData(cityName, countryName)
        }
    }

    fun saveAyahSize(ayahSize: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            dataStorePreference.saveAyahSize(ayahSize)
        }
    }

    fun saveSwitchState(switchName: String, isChecked: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            dataStorePreference.saveSwitchState(switchName, isChecked)
        }
    }

    val getSurah = dataStorePreference.getSurah.asLiveData()

    val getDetailSurahAyah = dataStorePreference.getDetailSurahAyah.asLiveData()

    val getProvinceData = dataStorePreference.getProvinceData.asLiveData()

    val getAreaData = dataStorePreference.getCityAndCountryData.asLiveData()

    val getAyahSize = dataStorePreference.getAyahSize.asLiveData()

    fun getSwitchState(switchName: String): LiveData<Boolean> =
        dataStorePreference.getSwitchState(switchName).asLiveData()
}