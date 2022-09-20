package com.example.fitnessapp

import android.content.Intent
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class HikesActivity : AppCompatActivity() {
    lateinit var bottomNav : BottomNavigationView
    private var homeIntent: Intent? = null
    private var weatherIntent: Intent? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_hikes)

        bottomNav = findViewById(R.id.bottomNav)
        bottomNav.selectedItemId = R.id.bottomNav

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

        weatherIntent = Intent(this, weatherActivity::class.java)
        weatherIntent!!.putExtra("user", user)
        weatherIntent!!.putExtra("imagePath", imagePath)
        weatherIntent!!.putExtra("the_city", userCity)
        weatherIntent!!.putExtra("the_country", userCountry)

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
                else -> {
                    return@setOnItemSelectedListener true
                }
            }
        }
    }
}