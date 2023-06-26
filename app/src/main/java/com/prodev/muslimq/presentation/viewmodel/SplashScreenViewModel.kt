package com.prodev.muslimq.presentation.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SplashScreenViewModel @Inject constructor() : ViewModel() {

    private var _keepSplashScreen = MutableLiveData(true)
    val keepSplashscreen: LiveData<Boolean> get() = _keepSplashScreen

    fun setKeepSplashScreen(keep: Boolean) {
        _keepSplashScreen.value = keep
    }
}