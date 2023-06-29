package com.prodev.muslimq.presentation.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SplashScreenViewModel @Inject constructor() : ViewModel() {

    private var _keepSplashScreen = MutableLiveData(true)
    val keepSplashscreen: LiveData<Boolean> get() = _keepSplashScreen

    fun setKeepSplashScreen(keep: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            delay(600)
            _keepSplashScreen.postValue(keep)
        }
    }
}