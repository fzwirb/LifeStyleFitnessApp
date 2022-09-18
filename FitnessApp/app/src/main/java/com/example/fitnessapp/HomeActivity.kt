package com.example.fitnessapp

import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView

class HomeActivity : AppCompatActivity(), View.OnClickListener {
    var mIvThumbnail: ImageView? = null
    var homeNameTV: TextView? = null

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

        val receivedIntent = intent

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
        homeNameTV!!.text = ("Welcome " + receivedIntent.getStringExtra("full_name"))


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