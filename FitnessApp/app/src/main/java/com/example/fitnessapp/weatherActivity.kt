package com.example.fitnessapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.content.Intent
import android.util.Log
import android.view.View
import android.widget.*
import com.example.fitnessapp.NetworkUtilities.buildURLFromString
import com.example.fitnessapp.NetworkUtilities.getDataFromURL
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.gson.Gson
import kotlin.math.roundToInt


class weatherActivity : AppCompatActivity() {
    lateinit var bottomNav : BottomNavigationView
    private var homeIntent: Intent? = null
    private var weatherIntent: Intent? = null
    private var hikeIntent: Intent? = null

    private var userCountry: String? = null
    private var userCity: String? = null
    private var userTemp: String? = null

    var cityTextView: TextView? = null
    var tempTextView: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_weather)

        val receivedIntent = intent
        var user = receivedIntent.extras?.getSerializable("user") as User
        val imagePath = receivedIntent.getStringExtra("imagePath")
        val userCity = receivedIntent.getStringExtra("the_city")
        val userCountry = receivedIntent.getStringExtra("the_country")

        homeIntent = Intent(this, HomeActivity::class.java)
        homeIntent!!.putExtra("user", user)
        homeIntent!!.putExtra("imagePath", imagePath)
        homeIntent!!.putExtra("the_city", userCity)
        homeIntent!!.putExtra("the_country", userCountry)

        hikeIntent = Intent(this, HikesActivity::class.java)
        hikeIntent!!.putExtra("user", user)
        hikeIntent!!.putExtra("imagePath", imagePath)
        hikeIntent!!.putExtra("the_city", userCity)
        hikeIntent!!.putExtra("the_country", userCountry)

        bottomNav = findViewById(R.id.bottomNav)
        bottomNav.selectedItemId = R.id.bottomNav

        bottomNav.setOnItemSelectedListener {
            Log.d("it.itemId: ", it.itemId.toString())
            when (it.itemId) {
                R.id.home -> {
                    startActivity(homeIntent)
                    return@setOnItemSelectedListener true
                }
                R.id.hikes -> {
                    startActivity(hikeIntent)
                    return@setOnItemSelectedListener true
                }
                R.id.weather -> {
                    return@setOnItemSelectedListener true
                }
                else -> {
                    return@setOnItemSelectedListener true
                }
            }
        }





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