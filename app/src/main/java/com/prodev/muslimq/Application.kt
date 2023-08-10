package com.prodev.muslimq

import android.app.Application
import com.prodev.muslimq.core.data.preference.DataStorePreference
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
open class Application : Application() {

    @Inject
    lateinit var dataStorePreference: DataStorePreference

    override fun onCreate() {
        super.onCreate()

        // temporary solution for delete database
        CoroutineScope(SupervisorJob()).launch {
            dataStorePreference.getIsTasbihDbCreated.collect { isCreated ->
                if (isCreated) {
                    deleteDatabase("tasbih.db")
                    dataStorePreference.saveIsTasbihDbCreated(false)
                    dataStorePreference.saveInputDzikirOnceState(false)
                }
            }
        }
    }
}