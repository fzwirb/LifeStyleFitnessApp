package com.example.fitnessapp

import android.content.Intent
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
//https://stackoverflow.com/questions/50897540/how-do-i-implement-serializable-in-kotlin-so-it-also-works-in-java
import java.io.Serializable

class HomeActivity : AppCompatActivity() {
    var mIvThumbnail: ImageView? = null
    var homeNameTV: TextView? = null
    @RequiresApi(33)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        //Get the image view
        mIvThumbnail = findViewById<View>(R.id.profile_pic) as ImageView

        homeNameTV = findViewById<View>(R.id.home_name) as TextView

        val receivedIntent = intent

        var user = receivedIntent.extras?.getSerializable("user") as User

        val imagePath = receivedIntent.getStringExtra("imagePath")
        val thumbnailImage = BitmapFactory.decodeFile(imagePath)
        if (thumbnailImage != null) {
            mIvThumbnail!!.setImageBitmap(thumbnailImage)
        }
        homeNameTV!!.text = ("Welcome " + user.fullName)

        var bmr: Double? = calculateBMR(receivedIntent)


    }

    private fun calculateBMR(i: Intent): Double {

        //For men: 66.47 + (6.24 × weight in pounds) + (12.7 × height in inches) − (6.75 × age in years).
        if(true == false ){

        }
        //For women: BMR = 65.51 + (4.35 * weight in pounds) + (4.7 * height in inches) - (4.7 * age in years)

        return 10.00
    }
}
