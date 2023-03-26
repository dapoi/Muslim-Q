package com.prodev.muslimq.presentation.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.prodev.muslimq.core.data.repository.QuranRepository
import com.prodev.muslimq.core.data.source.local.model.QuranDetailEntity
import com.prodev.muslimq.core.data.source.local.model.QuranEntity
import com.prodev.muslimq.core.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class QuranViewModel @Inject constructor(
    private val repositoryImpl: QuranRepository
) : ViewModel() {
    fun getSurah(): LiveData<Resource<List<QuranEntity>>> = repositoryImpl.getQuran().asLiveData()

    fun getQuranDetail(surahId: Int) = repositoryImpl.getQuranDetail(surahId).asLiveData()

    fun getQuranTafsir(
        surahId: Int,
        ayahNumber: Int
    ) = repositoryImpl.getQuranTafsir(surahId, ayahNumber).asLiveData()

    fun getBookmark(): LiveData<List<QuranDetailEntity>> = repositoryImpl.getBookmark().asLiveData()

    fun insertToBookmark(quran: QuranDetailEntity, isBookmarked: Boolean) {
        repositoryImpl.insertToBookmark(quran, isBookmarked)
    }

    fun deleteAllBookmark() {
        repositoryImpl.deleteAllBookmark()
    }
}