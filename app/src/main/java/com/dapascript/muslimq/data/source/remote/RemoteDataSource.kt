package com.dapascript.muslimq.data.source.remote

import com.dapascript.muslimq.data.source.remote.network.QuranApi
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RemoteDataSource @Inject constructor(private val quranApi: QuranApi) {
    suspend fun getQuran() = quranApi.getSurah()
}