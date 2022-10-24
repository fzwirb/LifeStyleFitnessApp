package com.example.fitnessapp

import androidx.room.*

/**
 * DAO (data access object) to access the room database storing user data
 */

@Dao
interface UserDataDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(userData: UserData)

    @Query("DELETE FROM user_table")
    suspend fun deleteAll()

    @Query("SELECT * FROM user_table LIMIT 1")
    suspend fun readFromDB(): UserData
}