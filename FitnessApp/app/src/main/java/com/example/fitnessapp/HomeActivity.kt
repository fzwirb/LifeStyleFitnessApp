package com.example.fitnessapp

import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView

class HomeActivity : AppCompatActivity() {
    var mIvThumbnail: ImageView? = null
    var homeNameTV: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        //Get the image view
        mIvThumbnail = findViewById<View>(R.id.profile_pic) as ImageView

        homeNameTV = findViewById<View>(R.id.home_name) as TextView

        val receivedIntent = intent


        val imagePath = receivedIntent.getStringExtra("imagePath")
        val thumbnailImage = BitmapFactory.decodeFile(imagePath)
        if (thumbnailImage != null) {
            mIvThumbnail!!.setImageBitmap(thumbnailImage)
        }
        homeNameTV!!.text = ("Welcome " + receivedIntent.getStringExtra("full_name"))


    }
}