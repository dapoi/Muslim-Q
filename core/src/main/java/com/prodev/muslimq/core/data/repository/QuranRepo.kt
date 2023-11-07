package com.prodev.muslimq.core.data.repository

import com.prodev.muslimq.core.data.source.local.database.QuranDao
import com.prodev.muslimq.core.data.source.local.model.Ayat
import com.prodev.muslimq.core.data.source.local.model.BookmarkEntity
import com.prodev.muslimq.core.data.source.local.model.QuranDetailEntity
import com.prodev.muslimq.core.data.source.local.model.QuranEntity
import com.prodev.muslimq.core.data.source.remote.model.TafsirDetailItem
import com.prodev.muslimq.core.data.source.remote.network.QuranApi
import com.prodev.muslimq.core.utils.Resource
import com.prodev.muslimq.core.utils.networkBoundResource
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class QuranRepositoryImpl @Inject constructor(
    private val service: QuranApi,
    private val dao: QuranDao,
    private val ioDispatcher: CoroutineDispatcher
) : QuranRepository {

    override fun getQuran(): Flow<Resource<List<QuranEntity>>> = networkBoundResource(query = {
        dao.getQuran()
    }, fetch = {
        service.getQuran()
    }, saveFetchResult = { quran ->
        val local = ArrayList<QuranEntity>()
        quran.data.map { response ->
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

        dao.deleteQuran()
        dao.insertQuran(local)
    }, shouldFetch = { listQuran ->
        @Suppress("SENSELESS_COMPARISON") listQuran == null || listQuran.isEmpty()
    }, ioDispatcher = { ioDispatcher })

    override fun getQuranDetail(id: Int): Flow<Resource<QuranDetailEntity>> = networkBoundResource(
        query = {
            dao.getQuranDetail(id)
        },
        fetch = {
            service.getQuranDetail(id)
        },
        saveFetchResult = { response ->
            val quran = response.data
            val local = QuranDetailEntity(id,
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
                        ayatTerjemahan = ayat.teksIndonesia,
                        ayatAudio = ayat.audio.ayahAudio!!
                    )
                })

            dao.insertQuranDetail(local)
        },
        shouldFetch = { data ->
            @Suppress("SENSELESS_COMPARISON") data == null || data.ayat.isEmpty()
        },
        ioDispatcher = { ioDispatcher }
    )

    override fun getQuranTafsir(
        surahId: Int, ayahNumber: Int
    ): Flow<Resource<TafsirDetailItem>> = flow {
        emit(Resource.Loading())

        try {
            service.getQuranTafsir(surahId).data.tafsir.first { tafsir ->
                tafsir.ayat == ayahNumber
            }.let { emit(Resource.Success(it)) }
        } catch (e: Exception) {
            emit(Resource.Error(e))
        }
    }.flowOn(ioDispatcher)

    override fun getBookmark(): Flow<List<BookmarkEntity>> = dao.getBookmark()

    override fun isBookmarked(surahId: Int): Flow<Boolean> = dao.isBookmarked(surahId)

    override suspend fun insertToBookmark(bookmarkEntity: BookmarkEntity) {
        dao.insertBookmark(bookmarkEntity)
    }

    override suspend fun deleteBookmark(bookmarkEntity: BookmarkEntity) {
        dao.deleteBookmark(bookmarkEntity)
    }

    override suspend fun deleteAllBookmark() {
        dao.deleteAllBookmark()
    }
}

interface QuranRepository {
    fun getQuran(): Flow<Resource<List<QuranEntity>>>

    fun getQuranDetail(id: Int): Flow<Resource<QuranDetailEntity>>

    fun getQuranTafsir(surahId: Int, ayahNumber: Int): Flow<Resource<TafsirDetailItem>>

    fun getBookmark(): Flow<List<BookmarkEntity>>

    fun isBookmarked(surahId: Int): Flow<Boolean>

    suspend fun insertToBookmark(bookmarkEntity: BookmarkEntity)

    suspend fun deleteBookmark(bookmarkEntity: BookmarkEntity)

    suspend fun deleteAllBookmark()
}