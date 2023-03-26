package com.prodev.muslimq.core.data.preference

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private var SURAH_ID = intPreferencesKey("surah_id")
private val SURAH_NAME = stringPreferencesKey("surah_name")
private val SURAH_MEANING = stringPreferencesKey("surah_meaning")
private val SURAH_DESC = stringPreferencesKey("surah_desc")
private val AYAH_NUMBER = intPreferencesKey("ayah_number")
private val PROVINCE_ID = stringPreferencesKey("province_id")
private val AYAH_SIZE = intPreferencesKey("ayah_size")
private val PROVINCE_NAME = stringPreferencesKey("province_name")
private val CITY_NAME = stringPreferencesKey("city_name")
private val COUNTRY_NAME = stringPreferencesKey("country_name")
private val SWITCH_NAME_KEY = stringPreferencesKey("switch_name")
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

@Singleton
class DataStorePreference @Inject constructor(@ApplicationContext context: Context) {

    private val dataStore = context.dataStore

    /**
     * Save surah name  & meaning to data store
     */
    suspend fun saveSurah(
        surahId: Int,
        surahName: String,
        surahMeaning: String,
        surahDesc: String,
        ayahNumber: Int
    ) {
        dataStore.edit { preferences ->
            preferences[SURAH_ID] = surahId
            preferences[SURAH_NAME] = surahName
            preferences[SURAH_MEANING] = surahMeaning
            preferences[SURAH_DESC] = surahDesc
            preferences[AYAH_NUMBER] = ayahNumber
        }
    }

    /**
     * Get surah name & meaning from data store
     */
    val getSurah = dataStore.data.map { preferences ->
        val surahName = preferences[SURAH_NAME] ?: ""
        val surahMeaning = preferences[SURAH_MEANING] ?: ""
        val surahDesc = preferences[SURAH_DESC] ?: ""
        Triple(surahName, surahMeaning, surahDesc)
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
        dataStore.edit { preferences ->
            preferences[CITY_NAME] = cityName
            preferences[COUNTRY_NAME] = countryName
        }
    }

    /**
     * Get city and country name from data store
     */
    val getCityAndCountryData = dataStore.data.map { preferences ->
        val cityName = preferences[CITY_NAME] ?: ""
        val countryName = preferences[COUNTRY_NAME] ?: ""
        Pair(cityName, countryName)
    }

//    /**
//     * Save city name to data store
//     */
//    suspend fun saveCityData(cityName: String) {
//        dataStore.edit { preferences ->
//            preferences[CITY_NAME] = cityName
//        }
//    }
//
//    /**
//     * Get city name from data store
//     */
//    val getCityData = dataStore.data.map { preferences ->
//        preferences[CITY_NAME] ?: ""
//    }
//
//    /**
//     * Save country name to data store
//     */
//    suspend fun saveCountryData(countryName: String) {
//        dataStore.edit { preferences ->
//            preferences[COUNTRY_NAME] = countryName
//        }
//    }
//
//    /**
//     * Get country name from data store
//     */
//    val getCountryData = dataStore.data.map { preferences ->
//        preferences[COUNTRY_NAME] ?: ""
//    }

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
    fun getSwitchState(switchName: String): Flow<Boolean> {
        return dataStore.data.map { preferences ->
            preferences[booleanPreferencesKey(switchName)] ?: false
        }
    }
}