package com.prodev.muslimq.presentation.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.prodev.muslimq.core.data.repository.QuranRepository
import com.prodev.muslimq.core.data.source.local.model.BookmarkEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BookmarkViewModel @Inject constructor(
    private val quranRepository: QuranRepository,
    private val ioDispatcher: CoroutineDispatcher
) : ViewModel() {

    private val _isBookmarked = MutableLiveData<Boolean>()
    val isBookmarked: LiveData<Boolean> get() = _isBookmarked

    fun insertBookmark(bookmarkEntity: BookmarkEntity) {
        viewModelScope.launch(ioDispatcher) {
            quranRepository.insertToBookmark(bookmarkEntity)
        }
    }

    fun getBookmark(): LiveData<List<BookmarkEntity>> =
        quranRepository.getBookmark().asLiveData()

    fun setBookmark(surahId: Int) {
        viewModelScope.launch {
            delay(200)
            quranRepository.isBookmarked(surahId).collect {
                _isBookmarked.value = it
            }
        }
    }

    fun deleteBookmark(bookmarkEntity: BookmarkEntity) {
        viewModelScope.launch(ioDispatcher) {
            quranRepository.deleteBookmark(bookmarkEntity)
        }
    }

    fun deleteAllBookmark() {
        viewModelScope.launch(ioDispatcher) {
            quranRepository.deleteAllBookmark()
        }
    }
}