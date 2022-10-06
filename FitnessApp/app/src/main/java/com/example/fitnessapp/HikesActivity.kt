package com.example.fitnessapp

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import androidx.activity.viewModels
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
    lateinit var userCity: String
    lateinit var userCountry: String
    private lateinit var appViewModel: AppViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_hikes)
        val appView: AppViewModel by viewModels {
            AppViewModelFactory((application as FitnessApplication).repository)
        }
        appViewModel = appView

        //Get the button and set listener to this
        mButtonSubmit = findViewById<View>(R.id.launch_hikes_button) as Button
        mButtonSubmit!!.setOnClickListener(this)

        // bottom nav
        userCity = appViewModel.data.value?.city.toString()
        userCountry = appViewModel.data.value?.country.toString()

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
}