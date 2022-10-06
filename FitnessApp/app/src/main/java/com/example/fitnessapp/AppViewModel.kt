package com.example.fitnessapp

import androidx.lifecycle.LiveData
import androidx.lifecycle.*
import kotlinx.coroutines.flow.Flow

class AppViewModel(repository: AppRepository) : ViewModel() {
    private val jsonData: LiveData<UserData> = repository.data
    val allUserData: Flow<List<UserData>> = repository.allUserData

    private var appRepository: AppRepository = repository

    fun setUser(user: UserData?) {
        if (user != null) {
            appRepository.setUserData(user)
        }
    }

    val data: LiveData<UserData>
    get() = jsonData
}

class AppViewModelFactory(private val repository: AppRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AppViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AppViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}