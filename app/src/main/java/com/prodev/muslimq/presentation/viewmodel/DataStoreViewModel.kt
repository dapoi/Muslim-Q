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

    fun saveSurah(surahName: String, surahMeaning: String) {
        viewModelScope.launch(Dispatchers.IO) {
            dataStorePreference.saveSurah(surahName, surahMeaning)
        }
    }

    fun saveProvinceData(provinceId: String, provinceName: String) {
        viewModelScope.launch(Dispatchers.IO) {
            dataStorePreference.saveProvinceData(provinceId, provinceName)
        }
    }

    fun saveCityData(cityName: String) {
        viewModelScope.launch(Dispatchers.IO) {
            dataStorePreference.saveCityData(cityName)
        }
    }

    fun saveAyahSize(ayahSize: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            dataStorePreference.saveAyahSize(ayahSize)
        }
    }

//    fun saveNotifState(shalatState: Boolean) {
//        viewModelScope.launch(Dispatchers.IO) {
//            dataStorePreference.saveNotifState(shalatState)
//        }
//    }

    fun saveSwitchState(switchName: String, isChecked: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            dataStorePreference.saveSwitchState(switchName, isChecked)
        }
    }

    val getSurah = dataStorePreference.getSurah.asLiveData()

    val getProvinceData = dataStorePreference.getProvinceData.asLiveData()

    val getCityData = dataStorePreference.getCityData.asLiveData()

    val getAyahSize = dataStorePreference.getAyahSize.asLiveData()

    fun getSwitchState(switchName: String): LiveData<Boolean> =
        dataStorePreference.getSwitchState(switchName).asLiveData()

//    val getNotifState = dataStorePreference.getNotifState.asLiveData()
}