package com.example.fitnessapp

import android.app.Application
import androidx.lifecycle.MutableLiveData
import androidx.annotation.WorkerThread
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlin.jvm.Synchronized

class AppRepository private constructor(userDao : UserDataDao) {
    val data = MutableLiveData<UserData>()

    val allUserData: Flow<List<UserData>> = userDao.getAllUserData()


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