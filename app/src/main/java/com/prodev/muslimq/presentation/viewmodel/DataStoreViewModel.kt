package com.prodev.muslimq.presentation.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.distinctUntilChanged
import androidx.lifecycle.viewModelScope
import com.prodev.muslimq.core.data.preference.DataStorePreference
import com.prodev.muslimq.core.utils.DzikirType
import com.prodev.muslimq.core.utils.UITheme
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DataStoreViewModel @Inject constructor(
    private val dataStorePref: DataStorePreference,
    private val ioDispatcher: CoroutineDispatcher
) : ViewModel() {

    fun saveSurah(
        surahId: Int,
        surahNameArabic: String,
        surahName: String,
        ayahNumber: Int
    ) {
        viewModelScope.launch(ioDispatcher) {
            dataStorePref.saveSurah(surahId, surahNameArabic, surahName, ayahNumber)
        }
    }

    fun saveAreaData(cityName: String, countryName: String) {
        viewModelScope.launch {
            dataStorePref.saveCityAndCountryData(cityName, countryName)
        }
    }

    fun saveAyahSize(ayahSize: Int) {
        viewModelScope.launch(ioDispatcher) {
            dataStorePref.saveAyahSize(ayahSize)
        }
    }

    fun saveSwitchDarkMode(uiTheme: UITheme) {
        viewModelScope.launch(ioDispatcher) {
            dataStorePref.saveSwitchDarkModeState(uiTheme)
        }
    }

    fun saveSwitchState(switchName: String, isChecked: Boolean) {
        viewModelScope.launch(ioDispatcher) {
            dataStorePref.saveSwitchState(switchName, isChecked)
        }
    }

    fun saveOnboardingState(isOnboarding: Boolean) {
        viewModelScope.launch {
            dataStorePref.saveOnboardingState(isOnboarding)
        }
    }

    fun saveTapPromptState(isTapPrompt: Boolean) {
        viewModelScope.launch(ioDispatcher) {
            dataStorePref.saveTapPromptState(isTapPrompt)
        }
    }

    fun saveHapticFeedbackState(isHapticFeedback: Boolean) {
        viewModelScope.launch(ioDispatcher) {
            dataStorePref.saveHapticFeedbackState(isHapticFeedback)
        }
    }

    fun saveSelectedDzikirType(dzikirType: DzikirType) {
        viewModelScope.launch(ioDispatcher) {
            dataStorePref.saveSelectedDzikirType(dzikirType)
        }
    }

    fun saveAdzanSoundState(isAdzanSound: Boolean) {
        viewModelScope.launch(ioDispatcher) {
            dataStorePref.saveAdzanSoundState(isAdzanSound)
        }
    }

    fun saveMuadzin(muadzinRegular: String, muadzinShubuh: String) {
        viewModelScope.launch(ioDispatcher) {
            dataStorePref.saveMuadzin(muadzinRegular, muadzinShubuh)
        }
    }

    private val getSurah = dataStorePref.getSurah

    private val getDetailSurahAyah = dataStorePref.getDetailSurahAyah

    val mergeData = getSurah.combine(getDetailSurahAyah) { data1, data2 ->
        CombineLastRead(data1, data2)
    }.asLiveData()

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

    val getSelectedDzikirType = dataStorePref.getSelectedDzikirType.asLiveData()

    val getAdzanSoundState =
        dataStorePref.getAdzanSoundStateAndMuadzin.asLiveData().distinctUntilChanged()

    val getMuadzin = dataStorePref.getMuadzin

    data class CombineLastRead(
        val detailSurah: Pair<String, String>,
        val surah: Pair<Int, Int>
    )
}