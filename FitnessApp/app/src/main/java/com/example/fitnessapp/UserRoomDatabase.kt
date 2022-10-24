package com.example.fitnessapp

import android.content.Context
import android.util.Log
import androidx.room.Database
import androidx.room.RoomDatabase
import kotlin.jvm.Volatile
import androidx.room.Room
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 *  Database class that is used as a singleton in the Fitness Application
 */

@Database(entities = [UserData::class], version = 1, exportSchema = false)
abstract class UserRoomDatabase : RoomDatabase() {
    abstract fun userDataDao(): UserDataDao

    companion object {
        @Volatile
        private var mInstance: UserRoomDatabase? = null
        fun getDatabase(
            context: Context,
            scope : CoroutineScope
        ): UserRoomDatabase {
            return mInstance?: synchronized(this){
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    UserRoomDatabase::class.java, "application.db"
                )
                    .addCallback(RoomDatabaseCallback(scope))
                    .fallbackToDestructiveMigration()
                    .build()
                mInstance = instance
                instance
            }
        }

        private class RoomDatabaseCallback(
            private val scope: CoroutineScope
        ): RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                mInstance?.let { database ->
                    scope.launch(Dispatchers.IO){
                        Log.d("TEST_DATABASE", "POPULATE DB TASK")
                        populateDbTask(database.userDataDao())
                    }
                }
            }
        }

        suspend fun populateDbTask (userDataDao: UserDataDao) {
            userDataDao.insert(UserData("test", 72, 190, 24, 0, "US", "Salt Lake City", "Male","test/path"))

        }
    }
}