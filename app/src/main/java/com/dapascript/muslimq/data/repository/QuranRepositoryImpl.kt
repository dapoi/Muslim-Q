package com.dapascript.muslimq.data.repository

import com.dapascript.muslimq.data.source.local.LocalDataSource
import com.dapascript.muslimq.data.source.local.model.QuranEntity
import com.dapascript.muslimq.data.source.remote.RemoteDataSource
import com.dapascript.muslimq.utils.Resource
import com.dapascript.muslimq.utils.networkBoundResource
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class QuranRepositoryImpl @Inject constructor(
    private val remoteDataSource: RemoteDataSource,
    private val localDataSource: LocalDataSource
) : QuranRepository {

    override fun getSurah(): Flow<Resource<List<QuranEntity>>> = networkBoundResource(
        query = {
            localDataSource.getQuran()
        },
        fetch = {
            delay(2000)
            remoteDataSource.getQuran()
        },
        saveFetchResult = { quran ->
            val local = ArrayList<QuranEntity>()
            quran.map {
                val data = QuranEntity(
                    it.nomor,
                    it.nama,
                    it.nama_latin,
                    it.jumlah_ayat,
                    it.tempat_turun,
                    it.arti,
                    it.deskripsi,
                    it.audio
                )
                local.add(data)
            }
            localDataSource.deleteQuran()
            localDataSource.insertQuran(local)
        }
    )
}