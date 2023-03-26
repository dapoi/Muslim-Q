package com.prodev.muslimq.core.data.repository

import com.prodev.muslimq.core.data.source.local.LocalDataSource
import com.prodev.muslimq.core.data.source.local.model.Ayat
import com.prodev.muslimq.core.data.source.local.model.QuranDetailEntity
import com.prodev.muslimq.core.data.source.local.model.QuranEntity
import com.prodev.muslimq.core.data.source.remote.RemoteDataSource
import com.prodev.muslimq.core.data.source.remote.model.TafsirDetailItem
import com.prodev.muslimq.core.utils.Resource
import com.prodev.muslimq.core.utils.networkBoundResource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class QuranRepositoryImpl @Inject constructor(
    private val remoteDataSource: RemoteDataSource, private val localDataSource: LocalDataSource
) : QuranRepository {

    override fun getQuran(): Flow<Resource<List<QuranEntity>>> = networkBoundResource(
        query = {
            localDataSource.getQuran()
        },
        fetch = {
            delay(2000)
            remoteDataSource.getQuran()
        },
        saveFetchResult = { quran ->
            val local = ArrayList<QuranEntity>()
            quran.map { response ->
                val data = QuranEntity(
                    response.nomor,
                    response.nama,
                    response.namaLatin,
                    response.jumlahAyat,
                    response.tempatTurun,
                    response.arti,
                    response.deskripsi
                )
                local.add(data)
            }
            localDataSource.deleteQuran()
            localDataSource.insertQuran(local)
        },
    )

    override fun getQuranDetail(id: Int): Flow<Resource<QuranDetailEntity>> = networkBoundResource(
        query = {
            localDataSource.getQuranDetail(id)
        },
        fetch = {
            delay(2000)
            remoteDataSource.getQuranDetail(id)
        },
        saveFetchResult = { quran ->
            val local = QuranDetailEntity(
                id,
                quran.nama,
                quran.namaLatin,
                quran.jumlahAyat,
                quran.tempatTurun,
                quran.arti,
                quran.deskripsi,
                quran.audioFull.audio!!,
                quran.ayat.map { ayat ->
                    Ayat(
                        ayatNumber = ayat.nomorAyat,
                        ayatArab = ayat.teksArab,
                        ayatLatin = ayat.teksLatin,
                        ayatTerjemahan = ayat.teksIndonesia
                    )
                },
                isBookmarked = false
            )
            localDataSource.insertQuranDetail(local)
        }, shouldFetch = { listAyah ->
            @Suppress("SENSELESS_COMPARISON")
            listAyah == null || listAyah.ayat.isEmpty()
        }
    )

    override fun getQuranTafsir(surahId: Int, ayahNumber: Int): Flow<Resource<TafsirDetailItem>> {
        return flow<Resource<TafsirDetailItem>> {
            emit(Resource.Loading())
            try {
                val response = remoteDataSource.getQuranTafsir(surahId)
                response.tafsir.filter {
                    it.ayat == ayahNumber
                }.map {
                    emit(Resource.Success(it))
                }
            } catch (e: Exception) {
                emit(Resource.Error(e))
            }
        }.flowOn(Dispatchers.IO)
    }

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