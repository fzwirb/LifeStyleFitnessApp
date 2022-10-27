package com.example.fitnessapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import android.view.View
import android.widget.*
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.lifecycle.lifecycleScope
import com.example.fitnessapp.NetworkUtilities.buildURLFromString
import com.example.fitnessapp.NetworkUtilities.getDataFromURL
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.gson.Gson
import kotlinx.coroutines.launch
import java.util.ArrayList
import kotlin.math.roundToInt

/**
 * Activity that displays the weather for the user's location
 */

class WeatherActivity : AppCompatActivity() {
    // shake code
    private lateinit var mSensorManager: SensorManager
    private lateinit var mAccelerometer: Sensor
    private val mThreshold = -1.5

    private val maxHistory = 5
    private var startIndex = 0
    private val measurements: ArrayList<Vec3> = ArrayList()

    private var lastShake = System.currentTimeMillis()
    private var cooldown = 1000; //wait 1S between shakes

    class Vec3(data: FloatArray){
        val x = data[0]
        val y = data[1]
        val z = data[2]
    }

    lateinit var bottomNav : BottomNavigationView
    private var homeIntent: Intent? = null
    private var hikeIntent: Intent? = null
    private var mainIntent: Intent? = null

    private var userTemp: String? = null
    private var emoji: String? = null

    var cityTextView: TextView? = null
    var tempTextView: TextView? = null
    var weatherDescView: TextView? = null
    var humidityView: TextView? = null
    var emojiView: TextView? = null

    private lateinit var userData: UserData

    /**
     * Function run on creation of the activity
     */
    @RequiresApi(33)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_weather)

        // get the received intent and city and country
        val receivedIntent = intent

        val appViewModel: AppViewModel by viewModels {
            AppViewModelFactory((application as FitnessApplication).repository)
        }

        lifecycleScope.launch {
            //get user will happen first
            val u = appViewModel.getUser()
            if (u != null) {
                userData = u
            }
            fillData(userData)
        }

        // shaker
        mSensorManager = getSystemService(SENSOR_SERVICE) as SensorManager

        //Get the virtual accelerometer with gravity subtracted out
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION)

    }

    private fun fillData(u: UserData) {

        // this can access anything on the userdata
        val userCountry = u.country
        val userCity = u.city


        // get all text views to be populated
        cityTextView = findViewById<View>(R.id.city_text_view) as TextView
        tempTextView = findViewById<View>(R.id.temp_text_view) as TextView
        humidityView = findViewById<View>(R.id.humidity_view) as TextView
        weatherDescView = findViewById<View>(R.id.weather_desc_view) as TextView
        emojiView = findViewById<View>(R.id.emoji_view) as TextView

        // set variables to query for the weather
        val countryString = userCountry!!.lowercase()
        val cityString = userCity!!.replace(" ", "&")
        val finalLocString = cityString + "," + countryString
        val weatherDataURL = buildURLFromString(finalLocString)
        var jsonWeatherData: String? = null

        Thread {
            try {
                assert(weatherDataURL != null)
                Log.e("WEATHER", "requesting weather")
                jsonWeatherData = getDataFromURL(weatherDataURL!!)
                val gson = Gson()
                val wdAlt: WeatherDataClass = gson.fromJson(jsonWeatherData?: "", WeatherDataClass::class.java)
                val weatherString = gson.toJson(wdAlt)
                Log.e("WeatherString", weatherString)

                val tempInF = ((1.8 * ((wdAlt.main.temp) - 273)) + 32).roundToInt()
                val finalTempInF = tempInF.toString() + "°"
                userTemp = finalTempInF

                val humidity = "Humidity: " + wdAlt.main.humidity.toString()
                val weatherDesc = wdAlt.weather[0].description

                Log.e("temp in F: ", finalTempInF)

                cityTextView!!.text = userCity
                tempTextView!!.text = userTemp

                humidityView!!.text = humidity
                weatherDescView!!.text = weatherDesc

                //for emojis
                if (weatherDesc.contains("clouds")) {
                    emoji = "⛅"
                }
                else if ((weatherDesc.contains("rain")) or (weatherDesc.contains("drizzle"))) {
                    emoji = "☔️"
                }
                else if (weatherDesc.contains("snow")) {
                    emoji = "❄️"
                }
                else if (weatherDesc.contains("thunderstorm")) {
                    emoji = "⛈"
                }
                else if (weatherDesc.contains("mist")) {
                    emoji = "☁️"
                }
                else {
                    emoji = "☀️"
                }
                emojiView!!.text = emoji
            } catch (e: Exception) {
                val errorString = "Loc not found!"
                cityTextView!!.text = errorString
                e.printStackTrace()
            }
        }.start()

        // Bottom Navigation listener and intents
        bottomNav = findViewById(R.id.bottomNav)
        bottomNav.selectedItemId = R.id.bottomNav

        homeIntent = Intent(this, HomeActivity::class.java)
        hikeIntent = Intent(this, HikesActivity::class.java)
        mainIntent = Intent(this, MainActivity::class.java)

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
                R.id.user_settings -> {
                    startActivity(mainIntent)
                    return@setOnItemSelectedListener true
                }
                else -> {
                    return@setOnItemSelectedListener true
                }
            }
        }
    }

    // shake code
    private val mListener: SensorEventListener = object : SensorEventListener {
        override fun onSensorChanged(sensorEvent: SensorEvent) {

            //Get the accelerations
            val v = WeatherActivity.Vec3(sensorEvent.values)
            addToHistory(v)
            val md = minDot(v)
            Log.d("MINDOT", "$md, ${v.x} ${v.y} ${v.z}")
            var now = System.currentTimeMillis()
            if(now >= (lastShake + cooldown) && md < mThreshold) {
                Log.d("shake_test", "success")
                startActivity(homeIntent)

                lastShake = now
                clearHistory(); //make them shake again later
            }
        }

        override fun onAccuracyChanged(sensor: Sensor, i: Int) {}
    }

    override fun onResume() {
        super.onResume()
        mSensorManager.registerListener(
            mListener,
            mAccelerometer,
            SensorManager.SENSOR_DELAY_NORMAL
        )
    }

    override fun onPause() {
        super.onPause()
        mSensorManager.unregisterListener(mListener)
    }

    private fun dot(u : WeatherActivity.Vec3, v: WeatherActivity.Vec3): Float{
        return u.x*v.x + u.y*v.y +u.z*v.z
    }

    //return the minimum dot product between this and any historical measurement
    private fun minDot(v: WeatherActivity.Vec3): Float {
        return measurements.map{ dot(it,v) }.min()
    }

    private fun addToHistory(v : WeatherActivity.Vec3){
        if(measurements.size < maxHistory){
            measurements.add(v);
        } else {
            measurements[startIndex] = v;
            startIndex = (startIndex + 1) % maxHistory
        }
    }

    private fun clearHistory(){
        measurements.clear();
        startIndex = 0;
    }
}