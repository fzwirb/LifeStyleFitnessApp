package com.example.fitnessapp

import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity(), View.OnClickListener, AdapterView.OnItemSelectedListener {

    //Variables

    //In Kotlin, the type system distinguishes between references that can hold null
    // (nullable references) and those that cannot (non-null references)
    // by using the question mark (?).

    //Ui elements
    private var mainButtonSubmit: Button? = null
    private var mainButtonCamera: Button? = null
    private var mainThumbnailImage: Bitmap? = null
    private var mainActivitySpinner: Spinner? = null
    private var mainEtName: EditText? = null;
    private var mainEtAge: EditText? = null;
    private var mainEtWeight: EditText? = null;
    private var mainEtHeight: EditText? = null;
    private var mainEtCountry: EditText? = null;
    private var mainEtCity: EditText? = null;
    private var mainEtSex: RadioGroup? = null;




    //Variables
    private var fullName: String? = null
    private var firstName: String? = null
    private var lastName: String? = null
    private var age: Int? = null
    private var weight: Int? = null
    private var country: String? = null
    private var city: String? = null
    private var activityLvl: Int? = null
    private var sex: String? = null

    var mIvThumbnail: ImageView? = null

    //Values for activity spinner
    var act_vals = arrayOf<String>("1 (Never Active)", "2", "3", "4", "5", "6", "7", "8", "9", "10 (Always Active)");


    private var mDisplayIntent: Intent? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //Get spinner
        mainActivitySpinner = findViewById<View>(R.id.activity_spinner) as Spinner

        mainActivitySpinner!!.onItemSelectedListener = this

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
        mainActivitySpinner!!.adapter = ad

        //Get the buttons
        mainButtonSubmit = findViewById<View>(R.id.button_submit) as Button
        mainButtonCamera = findViewById<View>(R.id.pic_button) as Button

        //Say that this class itself contains the listener.
        mainButtonSubmit!!.setOnClickListener(this)
        mainButtonCamera!!.setOnClickListener(this)

        //Create the intent but don't start the activity yet
        mDisplayIntent = Intent(this, HomeActivity::class.java)
    }

    override fun onClick(p0: View?) {
        if (p0 != null) {
            when (p0.id) {
                R.id.button_submit -> {


                    mainEtName = findViewById<View>(R.id.full_name) as EditText
                    fullName = mainEtName!!.text.toString()
                    checkIfBlank(fullName)


                    //start the new activity
                    startActivity(mDisplayIntent)
                }
                R.id.pic_button -> {

                    //            The button press should open a camera
                    val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                    try {
                        cameraLauncher.launch(cameraIntent)
                    }
                    catch (ex: ActivityNotFoundException) {
                        //Do something here
                    }
                }
            }
        }
    }

    private fun checkIfBlank(s: String) {
        if(s?.is())

    }

    private var cameraLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val extras = result.data!!.extras
            mainThumbnailImage = extras!!["data"] as Bitmap?

            //Open a file and write to it
            if (isExternalStorageWritable) {
                val filePathString = saveImage(mainThumbnailImage)
                mDisplayIntent!!.putExtra("imagePath", filePathString)
            } else {
                Toast.makeText(this, "External storage not writable.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun saveImage(finalBitmap: Bitmap?): String {
        val root = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val myDir = File("$root/saved_images")
        myDir.mkdirs()
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val fname = "Thumbnail_$timeStamp.jpg"
        val file = File(myDir, fname)
        if (file.exists()) file.delete()
        try {
            val out = FileOutputStream(file)
            finalBitmap!!.compress(Bitmap.CompressFormat.JPEG, 90, out)
            out.flush()
            out.close()
            Toast.makeText(this, "file saved!", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        mIvThumbnail = findViewById<View>(R.id.profile_pic) as ImageView
        val imagePath = file.absolutePath
        val thumbnailImage = BitmapFactory.decodeFile(imagePath)
        if (thumbnailImage != null) {
            mIvThumbnail!!.setImageBitmap(thumbnailImage)
        }

        return file.absolutePath
    }

    private val isExternalStorageWritable: Boolean
        get() {
            val state = Environment.getExternalStorageState()
            return Environment.MEDIA_MOUNTED == state
        }

    override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
            // make toastof name of course
            // which is selected in spinner
            Toast.makeText(applicationContext, act_vals[p2], Toast.LENGTH_LONG).show()
    }

    override fun onNothingSelected(p0: AdapterView<*>?) {

    }

}