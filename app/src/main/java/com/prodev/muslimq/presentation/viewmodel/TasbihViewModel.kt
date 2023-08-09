package com.prodev.muslimq.presentation.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.prodev.muslimq.core.data.preference.DataStorePreference
import com.prodev.muslimq.core.data.repository.TasbihRepository
import com.prodev.muslimq.core.data.source.local.model.TasbihEntity
import com.prodev.muslimq.core.utils.DzikirType
import com.prodev.muslimq.core.utils.defaultDzikir
import com.prodev.muslimq.core.utils.defaultDzikirPagi
import com.prodev.muslimq.core.utils.defaultDzikirShalat
import com.prodev.muslimq.core.utils.defaultDzikirSore
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

    private var _getDzikirType = MutableLiveData<String>()
    val getDzikirType: LiveData<String> get() = _getDzikirType

    private var _successUpdateList = MutableLiveData<Boolean>()
    val successUpdateList : LiveData<Boolean> get() = _successUpdateList

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

    fun deleteDzikir(dzikirName: String) {
        viewModelScope.launch {
            tasbihRepository.deleteDzikir(dzikirName)
        }
    }

    fun getDzikirByType(dzikirType: DzikirType){
        viewModelScope.launch {
            tasbihRepository.getAllDzikirByType(dzikirType).collect{
                _getDzikirList.value = it
            }
        }
    }

//    fun updateMaxCount(id: Int, maxCount: Int){
//        CoroutineScope(Dispatchers.IO).launch {
//            tasbihRepository.updateMaxCount(id, maxCount).collect{
//                if (it > 0){
//                    _successUpdate.value = true
//                }
//            }
//        }
//    }
    fun updateMaxCount(id: Int, maxCount: Int) =  tasbihRepository.updateMaxCount(id, maxCount).asLiveData()

    fun successUpdateList(boolean: Boolean){
        _successUpdateList.value = boolean
    }
}