package com.example.fitnessapp

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.launch
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import java.lang.System.currentTimeMillis
import java.util.*

/**
 * Activity that allows the user to search for hikes nearby them in the Google Maps app
 */

class HikesActivity : AppCompatActivity(), View.OnClickListener {
    // shake code
    private lateinit var mSensorManager: SensorManager
    private lateinit var mAccelerometer: Sensor
    private val mThreshold = -1.5

    private val maxHistory = 5
    private var startIndex = 0
    private val measurements: ArrayList<Vec3>  = ArrayList()

    private var lastShake = currentTimeMillis()
    private var cooldown = 1000; //wait 1S between shakes

    class Vec3(data: FloatArray){
        val x = data[0]
        val y = data[1]
        val z = data[2]
    }

    // bottom nav
    lateinit var bottomNav : BottomNavigationView
    private var homeIntent: Intent? = null
    private var weatherIntent: Intent? = null
    private var mainIntent: Intent? = null

    private var mButtonSubmit: Button? = null
    lateinit var userCity: String
    lateinit var userCountry: String
    private lateinit var appViewModel: AppViewModel

    private lateinit var userData: UserData

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_hikes)
        val appView: AppViewModel by viewModels {
            AppViewModelFactory((application as FitnessApplication).repository)
        }
        appViewModel = appView
        lifecycleScope.launch {
            //get user will happen first
            val u = appViewModel.getUser()
            if (u != null) {
                userData = u
            }
            setLocation(userData)
        }

        //Get the button and set listener to this
        mButtonSubmit = findViewById<View>(R.id.launch_hikes_button) as Button
        mButtonSubmit!!.setOnClickListener(this)

        // shaker
        mSensorManager = getSystemService(SENSOR_SERVICE) as SensorManager

        //Get the virtual accelerometer with gravity subtracted out
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION)

        // bottom nav
        bottomNav = findViewById(R.id.bottomNav)
        bottomNav.selectedItemId = R.id.bottomNav

        homeIntent = Intent(this, HomeActivity::class.java)
        weatherIntent = Intent(this, WeatherActivity::class.java)
        mainIntent = Intent(this, MainActivity::class.java)

        bottomNav.setOnItemSelectedListener {
            Log.d("it.itemId: ", it.itemId.toString())
            when (it.itemId) {
                R.id.home -> {
                    startActivity(homeIntent)
                    return@setOnItemSelectedListener true
                }
                R.id.hikes -> {
                    return@setOnItemSelectedListener true
                }
                R.id.weather -> {
                    startActivity(weatherIntent)
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
    /**
     * If the launch hikes button is pressed, open google maps and search the user's country and city
     */
    override fun onClick(view: View) {
        when (view.id) {
            R.id.launch_hikes_button -> {
                val searchString = "geo:0,0?q=$userCity+$userCountry+hikes"
                val searchUri = Uri.parse(searchString)
                val mapIntent = Intent(Intent.ACTION_VIEW, searchUri)
                mapIntent.setPackage("com.google.android.apps.maps")

                startActivity(mapIntent)
            }
        }
    }
    private fun setLocation(u: UserData){
        userCity = u.city.toString()
        userCountry = u.country.toString()
    }

    // shake code
    private val mListener: SensorEventListener = object : SensorEventListener {
        override fun onSensorChanged(sensorEvent: SensorEvent) {

            //Get the accelerations
            val v = Vec3(sensorEvent.values)
            addToHistory(v)
            val md = minDot(v)
            Log.d("MINDOT", "$md, ${v.x} ${v.y} ${v.z}")
            var now = currentTimeMillis()
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

    private fun dot(u : Vec3, v: Vec3): Float{
        return u.x*v.x + u.y*v.y +u.z*v.z
    }

    //return the minimum dot product between this and any historical measurement
    private fun minDot(v: Vec3): Float {
        return measurements.map{ dot(it,v) }.min()
    }

    private fun addToHistory(v : Vec3){
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