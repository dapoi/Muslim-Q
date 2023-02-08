package com.prodev.muslimq.core.utils

import android.content.Context
import android.util.Log
import com.prodev.muslimq.core.R
import com.prodev.muslimq.core.data.source.local.model.DoaEntity
import dagger.hilt.android.qualifiers.ApplicationContext
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class JsonHelper @Inject constructor(@ApplicationContext private val context: Context) {

    private fun getJsonDataFromAsset(): String? {
        val jsonString: String
        try {
            jsonString =
                context.resources.openRawResource(R.raw.doa).bufferedReader().use { it.readText() }
            Log.d("JsonHelper", "getJsonDataFromAsset: $jsonString")
        } catch (e: Exception) {
            Log.e("JsonHelper", "getJsonDataFromAsset: ${e.message}")
            return null
        }
        return jsonString
    }

    fun getDoa(): List<DoaEntity> {
        val list = ArrayList<DoaEntity>()
        val jsonString = getJsonDataFromAsset()
        val responseObject = JSONObject(jsonString.toString())
        val jsonArray = responseObject.getJSONArray("data")
        for (i in 0 until jsonArray.length()) {
            val jsonObject = jsonArray.getJSONObject(i)
            val id = jsonObject["id"].toString().toInt()
            val title = jsonObject["title"].toString()
            val arab = jsonObject["arabic"].toString()
            val latin = jsonObject["latin"].toString()
            val translation = jsonObject["translation"].toString()
            val doa = DoaEntity(id.toString(), title, arab, latin, translation)
            list.add(doa)
        }
        return list
    }
}