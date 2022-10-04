package com.example.fitnessapp

import android.app.Application
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

class FitnessApplication : Application() {
    private val applicationScope = CoroutineScope(SupervisorJob())

    private val database by lazy{ UserRoomDatabase.getDatabase(this,applicationScope)}

    val repository by lazy{ AppRepository.getInstance(database.userDataDao(),applicationScope)}
}