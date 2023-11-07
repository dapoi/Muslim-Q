package com.prodev.muslimq.core.data.preference

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.prodev.muslimq.core.utils.DzikirType
import com.prodev.muslimq.core.utils.UITheme
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

private val SURAH_ID = intPreferencesKey("surah_id")
private val SURAH_NAME_ARABIC = stringPreferencesKey("surah_name_arabic")
private val SURAH_NAME = stringPreferencesKey("surah_name")
private val SURAH_DESC = stringPreferencesKey("surah_desc")
private val AYAH_NUMBER = intPreferencesKey("ayah_number")
private val AYAH_SIZE = intPreferencesKey("ayah_size")
private val CITY_NAME = stringPreferencesKey("city_name")
private val COUNTRY_NAME = stringPreferencesKey("country_name")
private val SWITCH_NAME_KEY = stringPreferencesKey("switch_name")
private val SWITCH_DARK_MODE = booleanPreferencesKey("switch_dark_mode")
private val ONBOARDING_STATE = booleanPreferencesKey("onboarding_state")
private val TAP_PROMPT_STATE = booleanPreferencesKey("tap_prompt_state")
private val INPUT_DZIKIR_ONCE = booleanPreferencesKey("input_dzikir_once")
private val STATE_HAPTIC_FEEDBACK = booleanPreferencesKey("state_haptic_feedback")
private val ADZAN_SOUND_STATE = booleanPreferencesKey("adzan_sound_state")
private val DZIKIR_TYPE = intPreferencesKey("dzikir_type")
private val MUADZIN_REGULAR = stringPreferencesKey("muadzin_regular")
private val MUADZIN_SHUBUH = stringPreferencesKey("muadzin_shubuh")

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

@Singleton
class DataStorePreference @Inject constructor(context: Context) {

    private val dataStore = context.dataStore

    /**
     * Save surah name  & meaning to data store
     */
    suspend fun saveSurah(
        surahId: Int,
        surahNameArabic: String,
        surahName: String,
        ayahNumber: Int
    ) {
        dataStore.edit { preferences ->
            preferences[SURAH_ID] = surahId
            preferences[SURAH_NAME_ARABIC] = surahNameArabic
            preferences[SURAH_NAME] = surahName
            preferences[AYAH_NUMBER] = ayahNumber
        }
    }

    /**
     * Get surah name & meaning from data store
     */
    val getSurah = dataStore.data.map { preferences ->
        val surahNameArabic = preferences[SURAH_NAME_ARABIC] ?: ""
        val surahName = preferences[SURAH_NAME] ?: ""
        Pair(surahNameArabic, surahName)
    }

    /**
     * Get ayah number from data store
     */
    val getDetailSurahAyah = dataStore.data.map { preferences ->
        val surahId = preferences[SURAH_ID] ?: 0
        val ayahNumber = preferences[AYAH_NUMBER] ?: 0
        Pair(surahId, ayahNumber)
    }

    /**
     * Save ayah size to data store
     */
    suspend fun saveAyahSize(ayahSize: Int) {
        dataStore.edit { preferences ->
            preferences[AYAH_SIZE] = ayahSize
        }
    }

    /**
     * Get ayah size from data store
     */
    val getAyahSize = dataStore.data.map { preferences ->
        preferences[AYAH_SIZE] ?: 34
    }

    /**
     * Save city and country name to data store
     */
    suspend fun saveCityAndCountryData(cityName: String, countryName: String) {
        runBlocking {
            dataStore.edit { preferences ->
                preferences[CITY_NAME] = cityName
                preferences[COUNTRY_NAME] = countryName
            }
        }
    }

    /**
     * Get city and country name from data store
     */
    val getCityAndCountryData = dataStore.data.map { preferences ->
        val cityName = preferences[CITY_NAME] ?: "DKI Jakarta"
        val countryName = preferences[COUNTRY_NAME] ?: "Indonesia"
        Pair(cityName, countryName)
    }

    /**
     * Save dark mode switch state to data store
     */
    suspend fun saveSwitchDarkModeState(uiTheme: UITheme) {
        runBlocking {
            dataStore.edit { preferences ->
                preferences[SWITCH_DARK_MODE] = when (uiTheme) {
                    UITheme.LIGHT -> false
                    UITheme.DARK -> true
                }
            }
        }
    }

    /**
     * Get dark mode switch state from data store
     */
    val getSwitchDarkMode: Flow<UITheme> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
                exception.printStackTrace()
            } else {
                throw exception
            }
        }
        .map { preferences ->
            when (preferences[SWITCH_DARK_MODE] ?: false) {
                true -> UITheme.DARK
                false -> UITheme.LIGHT
            }
        }

    /**
     * Save shalat switch state to data store
     */
    suspend fun saveSwitchState(switchName: String, switchState: Boolean) {
        dataStore.edit { preferences ->
            preferences[SWITCH_NAME_KEY] = switchName
            preferences[booleanPreferencesKey(switchName)] = switchState
        }
    }

    /**
     * Get shalat switch state from data store
     */
    fun getSwitchState(switchName: String): Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[booleanPreferencesKey(switchName)] ?: false
    }

    /**
     * Save onboarding state to data store
     */
    suspend fun saveOnboardingState(onboardingState: Boolean) {
        runBlocking {
            dataStore.edit { preferences ->
                preferences[ONBOARDING_STATE] = onboardingState
            }
        }
    }

    /**
     * Get onboarding state from data store
     */
    val getOnboardingState = dataStore.data.map { preferences ->
        preferences[ONBOARDING_STATE] ?: false
    }

    /**
     * Save tap prompt state to data store
     */
    suspend fun saveTapPromptState(tapPromptState: Boolean) {
        dataStore.edit { preferences ->
            preferences[TAP_PROMPT_STATE] = tapPromptState
        }
    }

    /**
     * Get tap prompt state from data store
     */
    val getTapPromptState = dataStore.data.map { preferences ->
        preferences[TAP_PROMPT_STATE] ?: false
    }

    /**
     * Save input dzikir once state to data store
     */
    suspend fun saveInputDzikirOnceState(inputDzikirOnceState: Boolean) {
        dataStore.edit { preferences ->
            preferences[INPUT_DZIKIR_ONCE] = inputDzikirOnceState
        }
    }

    /**
     * Get input dzikir once state from data store
     */
    val getInputDzikirOnceState = dataStore.data.map { preferences ->
        preferences[INPUT_DZIKIR_ONCE] ?: false
    }

    /**
     * Save haptic feedback state to data store
     */
    suspend fun saveHapticFeedbackState(hapticFeedbackState: Boolean) {
        dataStore.edit { preferences ->
            preferences[STATE_HAPTIC_FEEDBACK] = hapticFeedbackState
        }
    }

    /**
     * Get haptic feedback state from data store
     */
    val getHapticFeedbackState = dataStore.data.map { preferences ->
        preferences[STATE_HAPTIC_FEEDBACK] ?: true
    }

    /**
     * Save adzan sound state to data store
     */
    suspend fun saveAdzanSoundState(adzanSoundState: Boolean) {
        dataStore.edit { preferences ->
            preferences[ADZAN_SOUND_STATE] = adzanSoundState
        }
    }

    /**
     * Get adzan sound state and also muadzin
     */
    val getAdzanSoundStateAndMuadzin = dataStore.data.map { preferences ->
        val adzanSoundState = preferences[ADZAN_SOUND_STATE] ?: true
        val muadzinRegular = preferences[MUADZIN_REGULAR] ?: "Ali Ahmad Mullah"
        val muadzinShubuh = preferences[MUADZIN_SHUBUH] ?: "Abu Hazim"
        Triple(adzanSoundState, muadzinRegular, muadzinShubuh)
    }

    /**
     * Save selected dzikir type
     */
    suspend fun saveSelectedDzikirType(dzikirType: DzikirType) {
        dataStore.edit { preferences ->
            preferences[DZIKIR_TYPE] = dzikirType.ordinal
        }
    }

    /**
     * Get selected dzikir type
     */
    val getSelectedDzikirType = dataStore.data.map { preferences ->
        preferences[DZIKIR_TYPE] ?: 0
    }

    /**
     * Save muadzin regular & shubuh
     */
    suspend fun saveMuadzin(muadzinRegular: String, muadzinShubuh: String) {
        dataStore.edit { preferences ->
            preferences[MUADZIN_REGULAR] = muadzinRegular
            preferences[MUADZIN_SHUBUH] = muadzinShubuh
        }
    }

    /**
     * Get muadzin regular & shubuh
     */
    val getMuadzin = dataStore.data.map { preferences ->
        val muadzinRegular = preferences[MUADZIN_REGULAR] ?: "Ali Ahmad Mullah"
        val muadzinShubuh = preferences[MUADZIN_SHUBUH] ?: "Abu Hazim"
        Pair(muadzinRegular, muadzinShubuh)
    }
}