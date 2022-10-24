package com.example.fitnessapp

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.annotation.WorkerThread
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.jvm.Synchronized

/**
 * Repository accessed by the viewmodel to access the user data
 */

class AppRepository private constructor(userDao : UserDataDao) {
    // data stores the mutable (changeable) user data object
    val data = MutableLiveData<UserData>()
    private var mUserDataDao: UserDataDao = userDao

    // insert the data into the database using the Dao
    @WorkerThread
    suspend fun insert(userData: UserData) {
        Log.d("TEST_DATABASE", "INSERT USER DATA")
        mUserDataDao.insert(userData)
    }

    // post the user data
    fun setUserData(user: UserData){
        mScope.launch(Dispatchers.IO){
            data.postValue(user)
            insert(user)
        }
    }

    suspend fun getUserData(): UserData? {
        var user: UserData? = null
        user = mUserDataDao.readFromDB()


        return user

    }
    companion object {
        private var mInstance: AppRepository? = null
        private lateinit var mScope: CoroutineScope
        @Synchronized
        fun getInstance(userDao: UserDataDao,
                        scope: CoroutineScope
        ): AppRepository {
            mScope = scope
            return mInstance?: synchronized(this){
                val instance = AppRepository(userDao)
                mInstance = instance
                instance
            }
        }
    }
}