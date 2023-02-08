package com.prodev.muslimq.core.data.preference

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val SURAH_NAME = stringPreferencesKey("surah_name")
private val SURAH_MEANING = stringPreferencesKey("surah_meaning")
private val PROVINCE_ID = stringPreferencesKey("province_id")
private val AYAH_SIZE = intPreferencesKey("ayah_size")
private val PROVINCE_NAME = stringPreferencesKey("province_name")
private val CITY_NAME = stringPreferencesKey("city_name")
private val SHUBUH_STATE = booleanPreferencesKey("shubuh_state")
private val DZUHUR_STATE = booleanPreferencesKey("dzuhur_state")
private val ASHAR_STATE = booleanPreferencesKey("ashar_state")
private val MAGHRIB_STATE = booleanPreferencesKey("maghrib_state")
private val ISYA_STATE = booleanPreferencesKey("isya_state")
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
        preferences[AYAH_SIZE] ?: 24
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

    /**
     * Save shubuh state to data store
     */
    suspend fun saveShubuhState(state: Boolean) {
        dataStore.edit { preferences ->
            preferences[SHUBUH_STATE] = state
        }
    }

    /**
     * Get shubuh state from data store
     */
    val getShubuhState = dataStore.data.map { preferences ->
        preferences[SHUBUH_STATE] ?: true
    }

    /**
     * Save dzuhur state to data store
     */
    suspend fun saveDzuhurState(state: Boolean) {
        dataStore.edit { preferences ->
            preferences[DZUHUR_STATE] = state
        }
    }

    /**
     * Get dzuhur state from data store
     */
    val getDzuhurState = dataStore.data.map { preferences ->
        preferences[DZUHUR_STATE] ?: true
    }

    /**
     * Save ashar state to data store
     */
    suspend fun saveAsharState(state: Boolean) {
        dataStore.edit { preferences ->
            preferences[ASHAR_STATE] = state
        }
    }

    /**
     * Get ashar state from data store
     */
    val getAsharState = dataStore.data.map { preferences ->
        preferences[ASHAR_STATE] ?: true
    }

    /**
     * Save maghrib state to data store
     */
    suspend fun saveMaghribState(state: Boolean) {
        dataStore.edit { preferences ->
            preferences[MAGHRIB_STATE] = state
        }
    }

    /**
     * Get maghrib state from data store
     */
    val getMaghribState = dataStore.data.map { preferences ->
        preferences[MAGHRIB_STATE] ?: true
    }

    /**
     * Save isya state to data store
     */
    suspend fun saveIsyaState(state: Boolean) {
        dataStore.edit { preferences ->
            preferences[ISYA_STATE] = state
        }
    }

    /**
     * Get isya state from data store
     */
    val getIsyaState = dataStore.data.map { preferences ->
        preferences[ISYA_STATE] ?: true
    }
}