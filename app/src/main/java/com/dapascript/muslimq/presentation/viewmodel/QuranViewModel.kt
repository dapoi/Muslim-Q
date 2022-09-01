package com.dapascript.muslimq.presentation.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.dapascript.muslimq.data.repository.QuranRepositoryImpl
import com.dapascript.muslimq.data.source.local.model.QuranEntity
import com.dapascript.muslimq.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class QuranViewModel @Inject constructor(
    repositoryImpl: QuranRepositoryImpl
) : ViewModel() {
    val getSurah: LiveData<Resource<List<QuranEntity>>> = repositoryImpl.getSurah().asLiveData()
}