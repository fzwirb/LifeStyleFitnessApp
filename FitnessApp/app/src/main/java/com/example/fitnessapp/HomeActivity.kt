package com.example.fitnessapp

import android.content.Intent
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.annotation.RequiresApi
import kotlin.math.roundToInt
import com.google.android.material.bottomnavigation.BottomNavigationView
//https://stackoverflow.com/questions/50897540/how-do-i-implement-serializable-in-kotlin-so-it-also-works-in-java

/**
 * Homepage activity that presents the user KCal and BMR
 */

class HomeActivity : AppCompatActivity(), AdapterView.OnItemSelectedListener {
    var mIvThumbnail: ImageView? = null
    var homeNameTV: TextView? = null
    var homeBMR: TextView? = null
    var homeKCAL: TextView? = null
    var user: User? = null

    private var hikeIntent: Intent? = null
    private var weatherIntent: Intent? = null
    private var mainIntent: Intent? = null
    lateinit var bottomNav : BottomNavigationView

    private var userCountry: String? = null
    private var userCity: String? = null

    //spinner
    private var homeActivitySpinner: Spinner? = null
    private var act_vals = arrayOf<String>("Sedentary", "Lightly active", "Moderately active", "Very active", "Extra active")
    private var userActivityLvl: Int? = null
    @RequiresApi(33)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        //Get the image view
        mIvThumbnail = findViewById<View>(R.id.profile_pic) as ImageView
        homeNameTV = findViewById<View>(R.id.home_name) as TextView
        homeBMR = findViewById<View>(R.id.bmr) as TextView
        homeKCAL = findViewById<View>(R.id.kcal) as TextView

        val receivedIntent = intent

        //assign serialized user to the user object member var
        user = receivedIntent.extras?.getSerializable("user") as User
        user!!.fullName?.let { Log.d("USER_TEST", it) }

        userCity = receivedIntent.getStringExtra("the_city")
        userCountry = receivedIntent.getStringExtra("the_country")

        val imagePath = receivedIntent.getStringExtra("imagePath")
        val thumbnailImage = BitmapFactory.decodeFile(imagePath)
        if (thumbnailImage != null) {
            mIvThumbnail!!.setImageBitmap(thumbnailImage)
        }
        homeNameTV!!.text = ("Welcome " + user!!.fullName)
        updateBMR(user)

        //spinner
        userActivityLvl = user!!.activityLvl
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
        user!!.activityLvl?.let { homeActivitySpinner!!.setSelection(it) }

        // Bottom Nav
        bottomNav = findViewById(R.id.bottomNav)
        bottomNav.selectedItemId = R.id.bottomNav

        hikeIntent = Intent(this, HikesActivity::class.java)
        hikeIntent!!.putExtra("user", user)
        hikeIntent!!.putExtra("imagePath", imagePath)
        hikeIntent!!.putExtra("the_city", userCity)
        hikeIntent!!.putExtra("the_country", userCountry)

        weatherIntent = Intent(this, WeatherActivity::class.java)
        weatherIntent!!.putExtra("user", user)
        weatherIntent!!.putExtra("imagePath", imagePath)
        weatherIntent!!.putExtra("the_city", userCity)
        weatherIntent!!.putExtra("the_country", userCountry)

        mainIntent = Intent(this, MainActivity::class.java)
        mainIntent!!.putExtra("user", user)
        mainIntent!!.putExtra("imagePath", imagePath)
        mainIntent!!.putExtra("the_city", userCity)
        mainIntent!!.putExtra("the_country", userCountry)

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
    // Companion object to allow for unit testing
    companion object {
        /**
         * Helper method for calculateBRM that uses a when statement to calculate
         * the kcal per day based on the activity level of the user
         */
        fun calculateKCAL(user: User, bmr: Double?): Double? {
            var actLvl = user.activityLvl
            //marathon, or triathlon, etc.
            when (actLvl) {
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
        fun calculateBMR(u: User): Double {
            var bmr: Double?

            var heightCM = u.height?.times(2.54)
            var weightKG = u.weight?.div(2.205)
            bmr = if(u.sex == "Male" ){
                (66.47 + (13.75 * weightKG!!) + (5.003 * heightCM!!) - (6.755 * u.age!!))
            } else{
                (655.1 + (9.563  * weightKG!!) + (1.850 * heightCM!!) - (4.676 * u.age!!))
            }
            return bmr
        }
    }
     override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
         //update user's activity lvl
         user = user?.copy(activityLvl = homeActivitySpinner!!.selectedItemPosition)
         Log.d("NEW_LVL", user!!.activityLvl.toString())
         updateBMR(user)
    }
    /**
     * Driver method for computing and displaying the user's BMR and KCAL
     * Called when activity is created and everytime onItemSelected is called
     */
    private fun updateBMR(u: User?) {
        u!!.fullName?.let { Log.d("UPDATE_BMR", it) }
        var bmr: Double? = calculateBMR(u)
        Log.d("BMR", bmr.toString())
        homeBMR!!.text = ("BRM: " + bmr!!.roundToInt())
        var kcal: Double? = calculateKCAL(u, bmr )
        Log.d("KCAL/DAY", kcal.toString())
        homeKCAL!!.text = ("KCAL/Per Day: : " + kcal!!.roundToInt())
    }
    override fun onNothingSelected(p0: AdapterView<*>?) {
        //nothing needed
    }
}
