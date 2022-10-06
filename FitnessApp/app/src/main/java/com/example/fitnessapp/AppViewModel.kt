package com.example.fitnessapp

import androidx.lifecycle.LiveData
import androidx.lifecycle.*
import kotlinx.coroutines.flow.Flow

/**
 * App view model used to access the database.
 */

class AppViewModel(repository: AppRepository) : ViewModel() {
    private val jsonData: LiveData<UserData> = repository.data
    private var appRepository: AppRepository = repository

    // api to set user data using the repository in the database
    fun setUser(user: UserData?) {
        if (user != null) {
            appRepository.setUserData(user)
        }
    }

    val data: LiveData<UserData>
    get() = jsonData
}

/**
 * Function to get the view model singleton when given the repository singleton
 */
class AppViewModelFactory(private val repository: AppRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AppViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AppViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}