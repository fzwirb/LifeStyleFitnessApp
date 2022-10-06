package com.example.fitnessapp

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDataDao {
    // Insert ignore
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(userData: UserData)

    // Delete all
    @Query("DELETE FROM user_table")
    suspend fun deleteAll()

    @Query("SELECT * from user_table  ORDER BY id DESC")
    fun getAllUserData(): Flow<List<UserData>>
}