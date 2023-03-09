package com.prodev.muslimq.core.data.repository

import com.prodev.muslimq.core.data.source.local.LocalDataSource
import com.prodev.muslimq.core.data.source.local.model.Ayat
import com.prodev.muslimq.core.data.source.local.model.QuranDetailEntity
import com.prodev.muslimq.core.data.source.local.model.QuranEntity
import com.prodev.muslimq.core.data.source.remote.RemoteDataSource
import com.prodev.muslimq.core.utils.Resource
import com.prodev.muslimq.core.utils.networkBoundResource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class QuranRepositoryImpl @Inject constructor(
    private val remoteDataSource: RemoteDataSource, private val localDataSource: LocalDataSource
) : QuranRepository {

    override fun getQuran(): Flow<Resource<List<QuranEntity>>> = networkBoundResource(query = {
        localDataSource.getQuran()
    }, fetch = {
        delay(2000)
        remoteDataSource.getQuran()
    }, saveFetchResult = { quran ->
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
    })

    override fun getQuranDetail(id: Int): Flow<Resource<QuranDetailEntity>> =
        networkBoundResource(query = {
            localDataSource.getQuranDetail(id)
        }, fetch = {
            delay(2000)
            remoteDataSource.getQuranDetail(id)
        }, saveFetchResult = { quran ->
            val local = QuranDetailEntity(
                quran.nomor,
                id,
                quran.nama,
                quran.nama_latin,
                quran.jumlah_ayat,
                quran.tempat_turun,
                quran.arti,
                quran.deskripsi,
                quran.audio,
                quran.ayat.map { ayat ->
                    Ayat(
                        ayat.nomor, ayat.ar, ayat.tr, ayat.idn
                    )
                },
                isBookmarked = false
            )
            localDataSource.insertQuranDetail(local)
        }, shouldFetch = {
            it == null || it.ayat.isEmpty()
        })

    override fun getBookmark(): Flow<List<QuranDetailEntity>> {
        return localDataSource.getBookmark()
    }

    override fun insertToBookmark(quran: QuranDetailEntity, isBookmarked: Boolean) {
        CoroutineScope(Dispatchers.IO).launch {
            localDataSource.insertToBookmark(quran, isBookmarked)
        }
    }

    override fun deleteAllBookmark() {
        CoroutineScope(Dispatchers.IO).launch {
            localDataSource.deleteAllBookmark()
        }
    }
}