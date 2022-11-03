package com.example.fitnessapp

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.launch
import kotlin.math.roundToInt


//https://stackoverflow.com/questions/50897540/how-do-i-implement-serializable-in-kotlin-so-it-also-works-in-java

/**
 * Homepage activity that presents the user KCal and BMR
 */

class HomeActivity : AppCompatActivity(), AdapterView.OnItemSelectedListener {
    var mIvThumbnail: ImageView? = null
    var homeNameTV: TextView? = null
    var homeBMR: TextView? = null
    var homeKCAL: TextView? = null
    var userData: UserData? = null

    private var hikeIntent: Intent? = null
    private var weatherIntent: Intent? = null
    private var mainIntent: Intent? = null
    lateinit var bottomNav : BottomNavigationView


    //step
    private lateinit var mSensorManager: SensorManager
    private lateinit var mTvData: TextView
    private var mStepCounter: Sensor? = null
    private var newSteps = true
    private var countingSteps = true
    private var pausedSteps = 0

    //spinner
    private var homeActivitySpinner: Spinner? = null
    private var act_vals = arrayOf<String>("Sedentary", "Lightly active", "Moderately active", "Very active", "Extra active")
    private lateinit var appViewModel: AppViewModel

    @RequiresApi(33)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        val appView: AppViewModel by viewModels {
            AppViewModelFactory((application as FitnessApplication).repository)
        }
        appViewModel = appView

        //steps
        mTvData = findViewById(R.id.step_view)
        mSensorManager = getSystemService(SENSOR_SERVICE) as SensorManager

        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACTIVITY_RECOGNITION
            ) == PackageManager.PERMISSION_GRANTED -> {
                mStepCounter = mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
            }
            else -> {
                // You can directly ask for the permission.
                // The registered ActivityResultCallback gets the result of this request.
                requestPermissionLauncher.launch(
                    Manifest.permission.ACTIVITY_RECOGNITION)
            }
        }

        //Get the image view
        mIvThumbnail = findViewById<View>(R.id.profile_pic) as ImageView
        homeNameTV = findViewById<View>(R.id.home_name) as TextView
        homeBMR = findViewById<View>(R.id.bmr) as TextView
        homeKCAL = findViewById<View>(R.id.kcal) as TextView

        val receivedIntent = intent

        lifecycleScope.launch {
            //get user will happen first
            val u = appViewModel.getUser()
            userData = u
            //once user has been retrieved from db and userData is init, then fill in ui components with data
            fillData(userData)
        }
    }

    private fun fillData(u: UserData?) {
        val imagePath = u?.imagePath

        val thumbnailImage = BitmapFactory.decodeFile(imagePath)
        if (thumbnailImage != null) {
            mIvThumbnail!!.setImageBitmap(thumbnailImage)
        }
        homeNameTV!!.text = ("Welcome " + u?.fullName)

        //spinner
        homeActivitySpinner = findViewById<View>(R.id.activity_spinner) as Spinner

        homeActivitySpinner!!.onItemSelectedListener = this

        // Create the instance of ArrayAdapter
        val ad: ArrayAdapter<*> = ArrayAdapter<Any?>(
            this,
            android.R.layout.simple_spinner_item,
            act_vals)

        // set simple layout resource file
        // for each item of spinner
        ad.setDropDownViewResource(
            android.R.layout.simple_spinner_dropdown_item)

        // Set the ArrayAdapter (ad) data on the
        // Spinner which binds data to spinner
        homeActivitySpinner!!.adapter = ad

        // Bottom Nav
        bottomNav = findViewById(R.id.bottomNav)
        bottomNav.selectedItemId = R.id.bottomNav

        hikeIntent = Intent(this, HikesActivity::class.java)
        weatherIntent = Intent(this, WeatherActivity::class.java)
        mainIntent = Intent(this, MainActivity::class.java)


        bottomNav.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.home -> {
                    return@setOnItemSelectedListener true
                }
                R.id.hikes -> {
                    startActivity(hikeIntent)
                    return@setOnItemSelectedListener true
                }
                R.id.weather -> {
                    startActivity(weatherIntent)
                    return@setOnItemSelectedListener true
                }
                R.id.user_settings -> {
                    Log.d("HOME_ACT", "SENT MAIN INTENET")
                    startActivity(mainIntent)
                    return@setOnItemSelectedListener true
                }
                else -> {
                    return@setOnItemSelectedListener true
                }
            }
        }
    }

    //steps
    private val mListener: SensorEventListener = object : SensorEventListener {
        override fun onSensorChanged(sensorEvent: SensorEvent) {
            //set step counter to 0 if first time using it
            if ( newSteps ) {
                sensorEvent.values[0] = 0.toFloat()
            }
            //if user has step tracking turned on or off
            if ( countingSteps ) {
                pausedSteps = sensorEvent.values[0].toInt()
                mTvData.text = "${sensorEvent.values[0]}"
            }
            else {
                mTvData.text = pausedSteps.toString()
            }
            //will no longer be first time using step counter
            newSteps = false
        }

        override fun onAccuracyChanged(sensor: Sensor, i: Int) {}
    }

    override fun onResume() {
        super.onResume()
        if(mStepCounter != null){
            registerListener()
        }
    }

    private fun registerListener() {
        mSensorManager.registerListener(
            mListener,
            mStepCounter,
            SensorManager.SENSOR_DELAY_NORMAL
        )
    }

    override fun onPause() {
        super.onPause()
        if(mStepCounter != null) {
            mSensorManager.unregisterListener(mListener)
        }
    }

    // two-finger gestures modeled off of https://stackoverflow.com/questions/26215769/how-to-detect-a-two-finger-swipe-gesture-on-android
    private val NONE = 0
    private val SWIPE = 1
    private var mode = NONE
    private var startY = 0f
    private var stopY = 0f

    // We will only detect a swipe if the difference is at least 100 pixels
    // Change this value to your needs
    private val TRESHOLD = 100

    override fun onTouchEvent(event: MotionEvent): Boolean  {
        when (event.action and MotionEvent.ACTION_MASK) {

            MotionEvent.ACTION_POINTER_DOWN -> {
                // This happens when you touch the screen with two fingers
                mode = SWIPE
                // You can also use event.getY(1) or the average of the two
                startY = event.getY(0)
            }
            MotionEvent.ACTION_POINTER_UP -> {
                // This happens when you release the second finger
                mode = NONE
                if (Math.abs(startY - stopY) > TRESHOLD) {
                    if (startY > stopY) {
                        Toast.makeText(applicationContext, "Step counter running.", Toast.LENGTH_LONG).show()
                        countingSteps = true
                    } else {
                        Toast.makeText(applicationContext, "Step counter stopped.", Toast.LENGTH_LONG).show()
                        countingSteps = false
                    }
                }
                mode = NONE
            }
            MotionEvent.ACTION_MOVE -> if (mode == SWIPE) {
                stopY = event.getY(0)
            }
        }
        return true
    }


    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                mStepCounter = mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
                registerListener()
            } else {
                mTvData.text = "UH OH, COULDN'T GET PERMISSION FOR THE STEP COUNTER!"
            }
        }


    // Companion object to allow for unit testing
    companion object {
        /**
         * Helper method for calculateBMR that uses a when statement to calculate
         * the kcal per day based on the activity level of the user
         */
        fun calculateKCAL(u: UserData?, bmr: Double?): Double? {
            when (u?.activityLvl) {
                //Sedentary = BMR x 1.2 (little or no exercise, desk job)
                0 -> return (bmr?.times(1.2))
                //Lightly active = BMR x 1.375 (light exercise/ sports 1-3 days/week)
                1 -> return (bmr?.times(1.375))
                //Moderately active = BMR x 1.55 (moderate exercise/ sports 6-7 days/week)
                2 -> return (bmr?.times(1.55))
                //Very active = BMR x 1.725 (hard exercise every day, or exercising 2 xs/day)
                3 -> return (bmr?.times(1.725))
                //Extra active = BMR x 1.9 (hard exercise 2 or more times per day, or training for
                4 -> return (bmr?.times(1.9))
            }
            return null;
        }
        /**
         * Takes in the user object and calculates the BMR and KCAL based in the user data
         */
        fun calculateBMR(u: UserData?): Double {
            val bmr: Double?
            val heightCM = u?.height?.times(2.54)
            val weightKG = u?.weight?.div(2.205)
            bmr = if(u?.sex == "Male" ){
                (66.47 + (13.75 * weightKG!!) + (5.003 * heightCM!!) - (6.755 * u?.age!!))
            } else{
                (655.1 + (9.563  * weightKG!!) + (1.850 * heightCM!!) - (4.676 * u?.age!!))
            }
            return bmr
        }
    }
    override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
        val newLvl = homeActivitySpinner!!.selectedItemPosition
        userData = UserData( userData?.fullName, userData?.height, userData?.weight, userData?.age, newLvl, userData?.country, userData?.city, userData?.sex, userData?.imagePath)

        updateBMR(userData)
    }
    /**
     * Driver method for computing and displaying the user's BMR and KCAL
     * Called when activity is created and everytime onItemSelected is called
     */
    private fun updateBMR(u: UserData?) {
        var bmr: Double? = calculateBMR(u)
        Log.d("BMR", bmr.toString())
        homeBMR!!.text = ("BMR: " + bmr!!.roundToInt())
        var kcal: Double? = calculateKCAL(u, bmr )
        Log.d("KCAL/DAY", kcal.toString())
        homeKCAL!!.text = ("KCAL/Per Day: : " + kcal!!.roundToInt())
    }
    override fun onNothingSelected(p0: AdapterView<*>?) {
        //nothing needed
    }
}
