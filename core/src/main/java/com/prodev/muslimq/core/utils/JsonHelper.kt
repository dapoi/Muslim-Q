package com.prodev.muslimq.core.utils

import android.content.Context
import com.prodev.muslimq.core.R
import com.prodev.muslimq.core.data.source.local.model.AsmaulHusnaEntity
import com.prodev.muslimq.core.data.source.local.model.DoaEntity
import dagger.hilt.android.qualifiers.ApplicationContext
import org.json.JSONArray
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class JsonHelper @Inject constructor(@ApplicationContext private val context: Context) {

    private fun getJsonDataFromAsset(isDoa: Boolean): String? {
        val jsonString: String

        try {
            val rawFile = if (isDoa) R.raw.doa else R.raw.asmaul_husna
            jsonString = context.resources.openRawResource(rawFile).bufferedReader().use {
                it.readText()
            }
        } catch (e: Exception) {
            return null
        }
        return jsonString
    }

    fun getDoa(): List<DoaEntity> {
        val list = ArrayList<DoaEntity>()
        val jsonString = getJsonDataFromAsset(true)
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

    fun getAsmaulHusna(): List<AsmaulHusnaEntity> {
        val list = ArrayList<AsmaulHusnaEntity>()
        val jsonString = getJsonDataFromAsset(false)
        val jsonArray = JSONArray(jsonString.toString())
        for (i in 0 until jsonArray.length()) {
            val jsonObject = jsonArray.getJSONObject(i)
            val urutan = jsonObject["urutan"].toString().toInt()
            val latin = jsonObject["latin"].toString()
            val arab = jsonObject["arab"].toString()
            val arti = jsonObject["arti"].toString()
            val asmaulHusna = AsmaulHusnaEntity(urutan, latin, arab, arti)
            list.add(asmaulHusna)
        }
        return list
    }
}