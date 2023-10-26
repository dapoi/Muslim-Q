package com.prodev.muslimq.presentation.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.prodev.muslimq.core.data.preference.DataStorePreference
import com.prodev.muslimq.core.data.repository.TasbihRepository
import com.prodev.muslimq.core.data.source.local.model.TasbihEntity
import com.prodev.muslimq.core.utils.DzikirType
import com.prodev.muslimq.helper.defaultDzikir
import com.prodev.muslimq.helper.defaultDzikirPagi
import com.prodev.muslimq.helper.defaultDzikirShalat
import com.prodev.muslimq.helper.defaultDzikirSore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TasbihViewModel @Inject constructor(
    private val tasbihRepository: TasbihRepository,
    private val dataStorePreference: DataStorePreference
) : ViewModel() {

    var totalSizeVM = 0
    var selectedItemIndexVM = 0
    var dzikirCountVM = 0
    var dzikirNameVM = ""

    private var _getDzikirList = MutableLiveData<List<TasbihEntity>>()
    val getDzikirList: LiveData<List<TasbihEntity>> get() = _getDzikirList

    private var _isFocus = MutableLiveData(false)
    val isFocus: LiveData<Boolean> get() = _isFocus

    init {
        viewModelScope.launch {
            dataStorePreference.getInputDzikirOnceState.collect { hasInput ->
                if (!hasInput) {
                    val defaultDzikir = defaultDzikir()
                    defaultDzikir.forEach { dzikir ->
                        tasbihRepository.insertDzikir(dzikir)
                        dataStorePreference.saveInputDzikirOnceState(true)
                    }
                    val defaultDzikirPagi = defaultDzikirPagi()
                    defaultDzikirPagi.forEach { dzikirPagi ->
                        tasbihRepository.insertDzikir(dzikirPagi)
                        dataStorePreference.saveInputDzikirOnceState(true)
                    }
                    val defaultDzikirSore = defaultDzikirSore()
                    defaultDzikirSore.forEach { dzikirSore ->
                        tasbihRepository.insertDzikir(dzikirSore)
                        dataStorePreference.saveInputDzikirOnceState(true)
                    }
                    val defaultDzikirShalat = defaultDzikirShalat()
                    defaultDzikirShalat.forEach { dzikirShalat ->
                        tasbihRepository.insertDzikir(dzikirShalat)
                        dataStorePreference.saveInputDzikirOnceState(true)
                    }
                }
            }
        }

        viewModelScope.launch {
            tasbihRepository.getAllDzikir().collect {
                _getDzikirList.value = it
            }
        }
    }

    fun insertDzikir(tasbihEntity: TasbihEntity) {
        viewModelScope.launch { tasbihRepository.insertDzikir(tasbihEntity) }
    }

    fun deleteDzikir(dzikirName: String) {
        viewModelScope.launch(Dispatchers.IO) { tasbihRepository.deleteDzikir(dzikirName) }
    }

    fun getDzikirByType(dzikirType: DzikirType) {
        viewModelScope.launch {
            tasbihRepository.getAllDzikirByType(dzikirType).collect {
                _getDzikirList.value = it
            }
        }
    }

    fun setFocus(isFocus: Boolean) {
        _isFocus.value = isFocus
    }
}