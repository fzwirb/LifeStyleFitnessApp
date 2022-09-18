package com.example.fitnessapp

import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.provider.MediaStore
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import kotlin.math.roundToInt
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
//https://stackoverflow.com/questions/50897540/how-do-i-implement-serializable-in-kotlin-so-it-also-works-in-java

class HomeActivity : AppCompatActivity(), View.OnClickListener {
    var mIvThumbnail: ImageView? = null
    var homeNameTV: TextView? = null
    var homeBMR: TextView? = null
    var homeKCAL: TextView? = null
    private var hikeIntent: Intent? = null
    lateinit var bottomNav : BottomNavigationView
    @RequiresApi(33)

    //weather stuff
    private var weatherButton: Button? = null
    private var wDisplayIntent: Intent? = null

    private var userCountry: String? = null
    private var userCity: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        //Get the image view
        mIvThumbnail = findViewById<View>(R.id.profile_pic) as ImageView
        homeNameTV = findViewById<View>(R.id.home_name) as TextView
        homeBMR = findViewById<View>(R.id.bmr) as TextView
        homeKCAL = findViewById<View>(R.id.kcal) as TextView

        val receivedIntent = intent

        var user = receivedIntent.extras?.getSerializable("user") as User
        //set up weather intent
        wDisplayIntent = Intent(this, weatherActivity::class.java)

        weatherButton = findViewById<View>(R.id.weather_button) as Button

        weatherButton!!.setOnClickListener(this)

        userCity = receivedIntent.getStringExtra("the_city")
        userCountry = receivedIntent.getStringExtra("the_country")

        val imagePath = receivedIntent.getStringExtra("imagePath")
        val thumbnailImage = BitmapFactory.decodeFile(imagePath)
        if (thumbnailImage != null) {
            mIvThumbnail!!.setImageBitmap(thumbnailImage)
        }
        homeNameTV!!.text = ("Welcome " + user.fullName)

        var bmr: Double? = calculateBMR(user)
        Log.d("BRM", bmr.toString())
        homeBMR!!.text = ("BRM: " + bmr!!.roundToInt())
        var kcal: Double? = calculateKCAL(user, bmr )
        Log.d("KCAL/DAY", kcal.toString())
        homeKCAL!!.text = ("KCAL/Per Day: : " + kcal!!.roundToInt())

        bottomNav = findViewById(R.id.bottomNav)
        bottomNav.selectedItemId = R.id.bottomNav


        hikeIntent = Intent(this, HikesActivity::class.java)
        hikeIntent!!.putExtra("user", user)
        hikeIntent!!.putExtra("imagePath", imagePath)

        bottomNav.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.home -> {
                    return@setOnItemSelectedListener true
                }
                R.id.hikes -> {
                    startActivity(hikeIntent)
                    return@setOnItemSelectedListener true
                }
                else -> {
                    return@setOnItemSelectedListener true
                }
            }
        }
    }

    private fun calculateKCAL(user: User, bmr: Double?): Double? {
        var actLvl = user.activityLvl
        //marathon, or triathlon, etc.
        when (actLvl) {
            //Sedentary = BMR x 1.2 (little or no exercise, desk job)
            1 -> return (bmr?.times(1.2))
            //Lightly active = BMR x 1.375 (light exercise/ sports 1-3 days/week)
            2 -> return (bmr?.times(1.375))
            //Moderately active = BMR x 1.55 (moderate exercise/ sports 6-7 days/week)
            3 -> return (bmr?.times(1.55))
            //Very active = BMR x 1.725 (hard exercise every day, or exercising 2 xs/day)
            4 -> return (bmr?.times(1.725))
            //Extra active = BMR x 1.9 (hard exercise 2 or more times per day, or training for
            5 -> return (bmr?.times(1.9))
        }
        //else
        return null;
    }

    private fun calculateBMR(u: User): Double {
        var bmr: Double?
        var kcal: Double?
        //BMR = 66.47 + ( 13.75 x weight in kg ) + ( 5.003 x height in cm ) - ( 6.755 x age in years )
        //BMR = 655.1 + ( 9.563 x weight in kg ) + ( 1.850 x height in cm ) - ( 4.676 x age in years )

        var heightCM = u.height?.times(2.54)
        var weightKG = u.weight?.div(2.205)



        bmr = if(u.sex == "male" ){
            (66.47 + (13.75 * weightKG!!) + (5.003 * heightCM!!) - (6.755 * u.age!!))

        } else{
            (655.1 + (9.563  * weightKG!!) + (1.850 * heightCM!!) - (4.676 * u.age!!))
        }

        return bmr
    }
}

    override fun onClick(createWeatherView: View?) {
        if (createWeatherView != null) {
            when (createWeatherView.id) {
                R.id.weather_button -> {

                    wDisplayIntent!!.putExtra("the_city", userCity)
                    wDisplayIntent!!.putExtra("the_country", userCountry)

                    startActivity(wDisplayIntent)

                }
            }
        }
    }

}
