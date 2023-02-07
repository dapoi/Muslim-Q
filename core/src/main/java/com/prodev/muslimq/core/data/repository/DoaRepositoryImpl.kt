package com.prodev.muslimq.core.data.repository

import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.prodev.muslimq.core.data.source.local.model.DoaEntity
import com.prodev.muslimq.core.utils.JsonHelper
import com.prodev.muslimq.core.utils.Resource
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DoaRepositoryImpl @Inject constructor(
    private val jsonHelper: JsonHelper
) : DoaRepository {

    override fun getDoa(): LiveData<Resource<List<DoaEntity>>> {
        val mutableDoa = MutableLiveData<Resource<List<DoaEntity>>>()
        val immutableDoa: LiveData<Resource<List<DoaEntity>>> = mutableDoa

        val handler = Handler(Looper.getMainLooper())
        handler.postDelayed({
            mutableDoa.value = Resource.Loading()
            try {
                val data = jsonHelper.getDoa()
                if (data.isNotEmpty()) {
                    mutableDoa.value = Resource.Success(data)
                } else {
                    mutableDoa.value = Resource.Error(Throwable("Data is empty"))
                }
                Log.d("DoaRepositoryImpl", "getDoa: $data")
            } catch (e: Exception) {
                mutableDoa.value = Resource.Error(e)
                Log.e("DoaRepositoryImpl", "getDoa: ${e.message}")
            }
        }, 100)

        return immutableDoa
    }
}