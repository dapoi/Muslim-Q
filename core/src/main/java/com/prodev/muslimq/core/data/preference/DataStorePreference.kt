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
import com.prodev.muslimq.core.utils.uitheme.UITheme
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

private var SURAH_ID = intPreferencesKey("surah_id")
private var SURAH_NAME_ARABIC = stringPreferencesKey("surah_name_arabic")
private val SURAH_NAME = stringPreferencesKey("surah_name")
private val SURAH_DESC = stringPreferencesKey("surah_desc")
private val AYAH_NUMBER = intPreferencesKey("ayah_number")
private val PROVINCE_ID = stringPreferencesKey("province_id")
private val AYAH_SIZE = intPreferencesKey("ayah_size")
private val PROVINCE_NAME = stringPreferencesKey("province_name")
private val CITY_NAME = stringPreferencesKey("city_name")
private val COUNTRY_NAME = stringPreferencesKey("country_name")
private val SWITCH_NAME_KEY = stringPreferencesKey("switch_name")
private val SWITCH_DARK_MODE = booleanPreferencesKey("switch_dark_mode")
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

@Singleton
class DataStorePreference @Inject constructor(@ApplicationContext context: Context) {

    private val dataStore = context.dataStore

    /**
     * Save surah name  & meaning to data store
     */
    suspend fun saveSurah(
        surahId: Int,
        surahNameArabic: String,
        surahName: String,
        surahDesc: String,
        ayahNumber: Int
    ) {
        dataStore.edit { preferences ->
            preferences[SURAH_ID] = surahId
            preferences[SURAH_NAME_ARABIC] = surahNameArabic
            preferences[SURAH_NAME] = surahName
            preferences[SURAH_DESC] = surahDesc
            preferences[AYAH_NUMBER] = ayahNumber
        }
    }

    /**
     * Get surah name & meaning from data store
     */
    val getSurah = dataStore.data.map { preferences ->
        val surahNameArabic = preferences[SURAH_NAME_ARABIC] ?: ""
        val surahName = preferences[SURAH_NAME] ?: ""
        val surahDesc = preferences[SURAH_DESC] ?: ""
        Triple(surahNameArabic, surahName, surahDesc)
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
     * Save province id & nameto data store
     */
    suspend fun saveProvinceData(provinceId: String, provinceName: String) {
        dataStore.edit { preferences ->
            preferences[PROVINCE_ID] = provinceId
            preferences[PROVINCE_NAME] = provinceName
        }
    }

    /**
     * Get province id & name from data store
     */
    val getProvinceData = dataStore.data.map { preferences ->
        val provinceId = preferences[PROVINCE_ID] ?: ""
        val provinceName = preferences[PROVINCE_NAME] ?: ""
        Pair(provinceId, provinceName)
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
        preferences[AYAH_SIZE] ?: 26
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
}