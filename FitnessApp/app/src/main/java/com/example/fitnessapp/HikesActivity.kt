package com.example.fitnessapp

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import com.google.android.material.bottomnavigation.BottomNavigationView

/**
 * Activity that allows the user to search for hikes nearby them in the Google Maps app
 */

class HikesActivity : AppCompatActivity(), View.OnClickListener {
    lateinit var bottomNav : BottomNavigationView
    private var homeIntent: Intent? = null
    private var weatherIntent: Intent? = null
    private var mainIntent: Intent? = null

    private var mButtonSubmit: Button? = null
    lateinit var cityString: String
    lateinit var countryString: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_hikes)
        val receivedIntent = intent

        //Get the button and set listener to this
        mButtonSubmit = findViewById<View>(R.id.launch_hikes_button) as Button
        mButtonSubmit!!.setOnClickListener(this)

        // bottom nav
        var user = receivedIntent.extras?.getSerializable("user") as UserData
        val imagePath = receivedIntent.getStringExtra("imagePath")
        val userCity = receivedIntent.getStringExtra("the_city")
        val userCountry = receivedIntent.getStringExtra("the_country")
        cityString = receivedIntent.getStringExtra("the_city").toString()
        countryString = receivedIntent.getStringExtra("the_country").toString()

        bottomNav = findViewById(R.id.bottomNav)
        bottomNav.selectedItemId = R.id.bottomNav

        homeIntent = Intent(this, HomeActivity::class.java)
//        homeIntent!!.putExtra("user", user)
        homeIntent!!.putExtra("imagePath", imagePath)
        homeIntent!!.putExtra("the_city", userCity)
        homeIntent!!.putExtra("the_country", userCountry)

        weatherIntent = Intent(this, WeatherActivity::class.java)
//        weatherIntent!!.putExtra("user", user)
        weatherIntent!!.putExtra("imagePath", imagePath)
        weatherIntent!!.putExtra("the_city", userCity)
        weatherIntent!!.putExtra("the_country", userCountry)

        mainIntent = Intent(this, MainActivity::class.java)
//        mainIntent!!.putExtra("user", user)
        mainIntent!!.putExtra("imagePath", imagePath)
        mainIntent!!.putExtra("the_city", userCity)
        mainIntent!!.putExtra("the_country", userCountry)

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
                val searchString = "geo:0,0?q=$cityString+$countryString+hikes"
                val searchUri = Uri.parse(searchString)
                val mapIntent = Intent(Intent.ACTION_VIEW, searchUri)
                mapIntent.setPackage("com.google.android.apps.maps")

                startActivity(mapIntent)
            }
        }
    }
}