package com.prodev.muslimq.presentation.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.distinctUntilChanged
import androidx.lifecycle.viewModelScope
import com.prodev.muslimq.core.data.preference.DataStorePreference
import com.prodev.muslimq.core.utils.DzikirType
import com.prodev.muslimq.core.utils.uitheme.UITheme
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DataStoreViewModel @Inject constructor(
    private val dataStorePref: DataStorePreference
) : ViewModel() {

    fun saveSurah(
        surahId: Int,
        surahNameArabic: String,
        surahName: String,
        surahDesc: String,
        ayahNumber: Int
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            dataStorePref.saveSurah(surahId, surahNameArabic, surahName, surahDesc, ayahNumber)
        }
    }

    fun saveAreaData(cityName: String, countryName: String) {
        viewModelScope.launch {
            dataStorePref.saveCityAndCountryData(cityName, countryName)
        }
    }

    fun saveAyahSize(ayahSize: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            dataStorePref.saveAyahSize(ayahSize)
        }
    }

    fun saveSwitchDarkMode(uiTheme: UITheme) {
        viewModelScope.launch(Dispatchers.IO) {
            dataStorePref.saveSwitchDarkModeState(uiTheme)
        }
    }

    fun saveSwitchState(switchName: String, isChecked: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            dataStorePref.saveSwitchState(switchName, isChecked)
        }
    }

    fun saveOnboardingState(isOnboarding: Boolean) {
        viewModelScope.launch {
            dataStorePref.saveOnboardingState(isOnboarding)
        }
    }

    fun saveTapPromptState(isTapPrompt: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            dataStorePref.saveTapPromptState(isTapPrompt)
        }
    }

    fun saveHapticFeedbackState(isHapticFeedback: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            dataStorePref.saveHapticFeedbackState(isHapticFeedback)
        }
    }

    fun saveDzikirMaxCount(count: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            dataStorePref.saveDzikirMaxCount(count)
        }
    }

    fun saveSelectedDzikirType(dzikirType: DzikirType){
        viewModelScope.launch(Dispatchers.IO){
            dataStorePref.saveSelectedDzikirType(dzikirType)
        }
    }

    val getSurah = dataStorePref.getSurah.asLiveData()

    val getDetailSurahAyah = dataStorePref.getDetailSurahAyah.asLiveData()

    val getAreaData = dataStorePref.getCityAndCountryData.asLiveData().distinctUntilChanged()

    val getAyahSize = dataStorePref.getAyahSize.asLiveData()

    val getSwitchDarkMode = dataStorePref.getSwitchDarkMode.asLiveData()

    val getSwitchState: (String) -> LiveData<Boolean> = { switchName ->
        dataStorePref.getSwitchState(switchName).asLiveData()
    }

    val getOnboardingState = dataStorePref.getOnboardingState.asLiveData().distinctUntilChanged()

    val getTapPromptState = dataStorePref.getTapPromptState.asLiveData().distinctUntilChanged()

    val getHapticFeedbackState =
        dataStorePref.getHapticFeedbackState.asLiveData().distinctUntilChanged()

    val getDzikirMaxCount = dataStorePref.getDzikirMaxCount.asLiveData()
    val getSelectedDzikirType = dataStorePref.getSelectedDzikirType.asLiveData()

    @ExperimentalCoroutinesApi
    val getCombineHapticAndMaxDzikirCount =
        dataStorePref.getHapticFeedbackState.flatMapLatest { hapticFeedback ->
            dataStorePref.getDzikirMaxCount.map { maxDzikirCount ->
                hapticFeedback to maxDzikirCount
            }
        }.asLiveData()
}