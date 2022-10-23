package com.prodev.muslimq.data.preference

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val SURAH_NAME = stringPreferencesKey("surah_name")
private val SURAH_MEANING = stringPreferencesKey("surah_meaning")
private val PROVINCE_ID = stringPreferencesKey("province_id")
private val PROVINCE_NAME = stringPreferencesKey("province_name")
private val CITY_NAME = stringPreferencesKey("city_name")
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

@Singleton
class DataStorePreference @Inject constructor(@ApplicationContext context: Context) {

    private val dataStore = context.dataStore

    /**
     * Save surah name  & meaning to data store
     */
    suspend fun saveSurah(surahName: String, surahMeaning: String) {
        dataStore.edit { preferences ->
            preferences[SURAH_NAME] = surahName
            preferences[SURAH_MEANING] = surahMeaning
        }
    }

    /**
     * Get surah name & meaning from data store
     */
    val getSurah = dataStore.data.map { preferences ->
        val surahName = preferences[SURAH_NAME] ?: ""
        val surahMeaning = preferences[SURAH_MEANING] ?: ""
        Pair(surahName, surahMeaning)
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
     * Save city name to data store
     */
    suspend fun saveCityData(cityName: String) {
        dataStore.edit { preferences ->
            preferences[CITY_NAME] = cityName
        }
    }

    /**
     * Get city name from data store
     */
    val getCityData = dataStore.data.map { preferences ->
        preferences[CITY_NAME] ?: ""
    }
}