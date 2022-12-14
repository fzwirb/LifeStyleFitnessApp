package com.example.fitnessapp

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Data class to store user data
 */

@Entity(tableName = "user_table")
data class UserData(
    val fullName: String?,
    val height: Int?,
    val weight: Int?,
    val age: Int?,
    val activityLvl: Int?,
    val country: String?,
    val city: String?,
    val sex: String?,
    val imagePath: String?
    ) {

    @field:PrimaryKey(autoGenerate = true)
    var id : Int = 0
}