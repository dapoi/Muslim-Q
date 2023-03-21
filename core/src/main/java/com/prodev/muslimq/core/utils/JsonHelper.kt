package com.prodev.muslimq.core.utils

import android.content.Context
import com.prodev.muslimq.core.R
import com.prodev.muslimq.core.data.source.local.model.DoaBodyEntity
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
            jsonString = context.resources.openRawResource(R.raw.doa).bufferedReader().use {
                it.readText()
            }
        } catch (e: Exception) {
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
            val bodyData = jsonObject.getJSONArray("body")
            val bodyList = mutableListOf<DoaBodyEntity>()
            for (j in 0 until bodyData.length()) {
                val bodyObject = bodyData.getJSONObject(j)
                val arab = bodyObject["arabic"].toString()
                val latin = bodyObject["latin"].toString()
                val translation = bodyObject["translation"].toString()
                val body = DoaBodyEntity(arab, latin, translation)
                bodyList.add(body)
            }
            val doa = DoaEntity(id.toString(), title, bodyList)
            list.add(doa)
        }
        return list
    }
}