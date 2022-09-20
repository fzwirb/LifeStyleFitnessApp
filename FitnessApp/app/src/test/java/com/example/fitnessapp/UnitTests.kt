package com.example.fitnessapp

import android.util.Log
import org.junit.Test

import org.junit.Assert.*

/**
 * Example local unit test, which will execute on the development machine (host).
 */
class UnitTests {
    var user_act_level_0 = User("test", 72, 190, 24, 0, "US", "Salt Lake City", "Male")
    var user_act_level_1 = User("test", 72, 190, 24, 1, "US", "Salt Lake City", "Male")
    var user_act_level_2 = User("test", 72, 190, 24, 2, "US", "Salt Lake City", "Male")
    var user_act_level_3 = User("test", 72, 190, 24, 3, "US", "Salt Lake City", "Male")
    var user_act_level_4 = User("test", 72, 190, 24, 4, "US", "Salt Lake City", "Male")

    @Test
    fun kcalTest(){
        assertEquals(
            HomeActivity.calculateKCAL(user_act_level_0, HomeActivity.calculateBMR(user_act_level_0))
                .toString(), "2046.2720108843537"
        )
        assertEquals(
            HomeActivity.calculateKCAL(user_act_level_1, HomeActivity.calculateBMR(user_act_level_1))
                .toString(), "2344.686679138322"
        )
        assertEquals(
            HomeActivity.calculateKCAL(user_act_level_2, HomeActivity.calculateBMR(user_act_level_2))
                .toString(), "2643.1013473922903"
        )
        assertEquals(
            HomeActivity.calculateKCAL(user_act_level_3, HomeActivity.calculateBMR(user_act_level_3))
                .toString(), "2941.5160156462584"
        )
        assertEquals(
            HomeActivity.calculateKCAL(user_act_level_4, HomeActivity.calculateBMR(user_act_level_4))
                .toString(), "3239.9306839002265"
        )
    }

    @Test
    fun bmrTest(){
        assertEquals(HomeActivity.calculateBMR(user_act_level_0).toString(), "1705.2266757369614")
        assertEquals(HomeActivity.calculateBMR(user_act_level_1).toString(), "1705.2266757369614")
        assertEquals(HomeActivity.calculateBMR(user_act_level_2).toString(), "1705.2266757369614")
        assertEquals(HomeActivity.calculateBMR(user_act_level_3).toString(), "1705.2266757369614")
        assertEquals(HomeActivity.calculateBMR(user_act_level_4).toString(), "1705.2266757369614")


    }
}