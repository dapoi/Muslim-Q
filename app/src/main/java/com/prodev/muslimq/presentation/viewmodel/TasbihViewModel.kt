package com.prodev.muslimq.presentation.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.prodev.muslimq.core.data.preference.DataStorePreference
import com.prodev.muslimq.core.data.repository.TasbihRepository
import com.prodev.muslimq.core.data.source.local.model.TasbihEntity
import com.prodev.muslimq.core.utils.defaultDzikir
import dagger.hilt.android.lifecycle.HiltViewModel
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

    fun insertDzikir(tasbihEntity: TasbihEntity) {
        viewModelScope.launch {
            tasbihRepository.insertDzikir(tasbihEntity)
        }
    }

    init {
        viewModelScope.launch {
            dataStorePreference.getInputDzikirOnceState.collect { hasInput ->
                if (!hasInput) {
                    val defaultDzikir = defaultDzikir()
                    defaultDzikir.forEach { dzikir ->
                        tasbihRepository.insertDzikir(TasbihEntity(dzikirName = dzikir))
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

    fun deleteDzikir(dzikirName: String) {
        viewModelScope.launch {
            tasbihRepository.deleteDzikir(dzikirName)
        }
    }
}