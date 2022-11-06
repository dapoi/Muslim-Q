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

    fun saveShubuhState(state: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            dataStorePreference.saveShubuhState(state)
        }
    }

    fun saveDzuhurState(alarmDzuhur: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            dataStorePreference.saveDzuhurState(alarmDzuhur)
        }
    }

    fun saveAsharState(alarmAshar: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            dataStorePreference.saveAsharState(alarmAshar)
        }
    }

    fun saveMaghribState(alarmMaghrib: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            dataStorePreference.saveMaghribState(alarmMaghrib)
        }
    }

    fun saveIsyaState(alarmIsya: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            dataStorePreference.saveIsyaState(alarmIsya)
        }
    }

    val getSurah = dataStorePreference.getSurah.asLiveData()

    val getProvinceData = dataStorePreference.getProvinceData.asLiveData()

    val getCityData = dataStorePreference.getCityData.asLiveData()

    val getShubuhState = dataStorePreference.getShubuhState.asLiveData()

    val getDzuhurState = dataStorePreference.getDzuhurState.asLiveData()

    val getAsharState = dataStorePreference.getAsharState.asLiveData()

    val getMaghribState = dataStorePreference.getMaghribState.asLiveData()

    val getIsyaState = dataStorePreference.getIsyaState.asLiveData()
}