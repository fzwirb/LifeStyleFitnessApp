package com.example.fitnessapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Environment
import android.provider.MediaStore
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import com.example.fitnessapp.NetworkUtilities.buildURLFromString
import com.example.fitnessapp.NetworkUtilities.getDataFromURL
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*
import com.google.gson.Gson
import kotlin.math.roundToInt


class weatherActivity : AppCompatActivity() {

    private var userCountry: String? = null
    private var userCity: String? = null
    private var userTemp: String? = null

    var cityTextView: TextView? = null
    var tempTextView: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.weather_activity)

        val receivedIntent = intent

        userCountry = receivedIntent.getStringExtra("the_country")
        userCity = receivedIntent.getStringExtra("the_city")

        cityTextView = findViewById<View>(R.id.city_text_view) as TextView

        tempTextView = findViewById<View>(R.id.temp_text_view) as TextView

        //-----------------------------//
        val location = "Salt&Lake&City,us"

        val testCountry = "US"
        val testLoc = "Salt Lake City" //replace testLoc with city

        val countryString = userCountry!!.lowercase() //replace testCountry with country
        val cityString = userCity!!.replace(" ", "&") //replace testLoc with city
        val finalLocString = cityString + "," + countryString

        val weatherDataURL = buildURLFromString(finalLocString)
        var jsonWeatherData: String? = null

        Thread {
            try {
                assert(weatherDataURL != null)
                Log.e("WEATHER", "requesting weather")
                jsonWeatherData = getDataFromURL(weatherDataURL!!)
                //runOnUiThread {
//                Log.e("WEATHER", jsonWeatherData ?: "DIDN'T GET DATA!!!")
                //}
                val gson = Gson()
                val wdAlt: WeatherDataClass = gson.fromJson(jsonWeatherData?: "", WeatherDataClass::class.java)
//                Log.e("WEATHER", gson.toJson(wdAlt))
                val weatherString = gson.toJson(wdAlt)
                Log.e("WeatherString", weatherString)

                Log.e("location!!!!: ", finalLocString )

                val tempInF = ((1.8 * ((wdAlt.main.temp) - 273)) + 32).roundToInt()

                val finalTempInF = tempInF.toString() + "°"

                userTemp = finalTempInF

                Log.e("temp in F: ", finalTempInF)

                cityTextView!!.text = userCity
                tempTextView!!.text = userTemp


            } catch (e: Exception) {
                e.printStackTrace()
            }
        }.start()
        


        //----------------------------------------//


    }

    private fun getWeather () {

    }

}