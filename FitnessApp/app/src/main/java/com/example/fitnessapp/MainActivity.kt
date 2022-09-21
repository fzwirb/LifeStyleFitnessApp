package com.example.fitnessapp

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import com.google.gson.Gson
import com.example.fitnessapp.NetworkUtilities.buildURLFromString
import com.example.fitnessapp.NetworkUtilities.getDataFromURL
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.io.Serializable
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt

/**
 * Activity that accepts or updates user information
 */

class MainActivity : AppCompatActivity(), View.OnClickListener, AdapterView.OnItemSelectedListener {
    @RequiresApi(33)

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
    private var mainRgSex: RadioGroup? = null;

    //Variables
    private var fullName: String? = null
    private var firstName: String? = null
    private var lastName: String? = null
    private var age: Int? = null
    private var weight: Int? = null
    private var height: Int? = null
    private var country: String? = null
    private var city: String? = null
    private var activityLvl: Int? = null
    private var sex: String? = null
    private var receivedIntent: Intent? = null
    private var user: User? = null
    private var imagePath: String? = null

    var mIvThumbnail: ImageView? = null

    //Values for activity spinner
    private var act_vals = arrayOf<String>("Sedentary", "Lightly active", "Moderately active", "Very active", "Extra active")

    private var mDisplayIntent: Intent? = null
    /**
     * Functions executes on creation of the activity
     */
    @RequiresApi(33)
    @SuppressLint("ResourceType")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // get various elements from layout
        mIvThumbnail = findViewById<View>(R.id.profile_pic) as ImageView
        mainEtName = findViewById<View>(R.id.full_name) as EditText
        mainEtAge = findViewById<View>(R.id.age) as EditText
        mainEtWeight = findViewById<View>(R.id.weight) as EditText
        mainEtHeight = findViewById<View>(R.id.height) as EditText
        mainEtCountry = findViewById<View>(R.id.country) as EditText
        mainEtCity = findViewById<View>(R.id.city) as EditText
        mainActivitySpinner = findViewById<View>(R.id.activity_spinner) as Spinner
        mainRgSex = findViewById<View>(R.id.sex) as RadioGroup

        // if the received intent has user object, set user to be the received user object
        if (intent!!.hasExtra("user")) {
            Log.d("Intent", "HAS USER")
            receivedIntent = intent
            user = receivedIntent?.extras?.getSerializable("user") as User
        }

        //Get spinner and set listener
        mainActivitySpinner = findViewById<View>(R.id.activity_spinner) as Spinner
        mainActivitySpinner!!.onItemSelectedListener = this

        // Create the instance of ArrayAdapter
        val ad: ArrayAdapter<*> = ArrayAdapter<Any?>(
            this,
            android.R.layout.simple_spinner_item,
            act_vals
        )

        // set simple layout resource file
        // for each item of spinner
        ad.setDropDownViewResource(
            android.R.layout.simple_spinner_dropdown_item
        )

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

        // if intent contains a user object, fill in the text fields with user data.
        if (user?.fullName != null) {
            mainButtonSubmit!!.text = "Update"
            mainEtName!!.setText(user!!.fullName)
            mainEtAge!!.setText(user!!.age.toString())
            mainEtHeight!!.setText(user!!.height.toString())
            mainEtWeight!!.setText(user!!.weight.toString())
            mainEtCity?.setText(user!!.city)
            mainEtCountry?.setText(user!!.country)
            Log.d("SEX", user!!.sex.toString())
            if (user!!.sex == "Male") {
                Log.d("SEX","MALE")
                mainRgSex?.check(R.id.radioButton)
            } else if(user!!.sex == "Female") {
                Log.d("SEX","FEMALE")
                mainRgSex?.check(R.id.radioButton2)
            }
            mainActivitySpinner!!.setSelection(user!!.activityLvl!!)
            imagePath = receivedIntent?.getStringExtra("imagePath")
            val thumbnailImage = BitmapFactory.decodeFile(imagePath)
            if (thumbnailImage != null) {
                mIvThumbnail!!.setImageBitmap(thumbnailImage)
            }
        }
    }

    /**
     * On activity click, if the click is on the createUserView, validate the form. If validated, start HomeActivity
     * If pic button is pressed, launch camera
     */
    override fun onClick(createUserView: View?) {
        if (createUserView != null) {
            when (createUserView.id) {
                R.id.button_submit -> {
                    var validated = validateForm()
                    if(validated) {
                        //send data to the new home activity
                        user = User( fullName, height, weight, age, activityLvl, country, city, sex)
                        mDisplayIntent!!.putExtra("imagePath", imagePath)
                        mDisplayIntent!!.putExtra("user", user)
                        mDisplayIntent!!.putExtra("full_name", fullName)
                        mDisplayIntent!!.putExtra("the_city", city)
                        mDisplayIntent!!.putExtra("the_country", country)
                        startActivity(mDisplayIntent)
                    }
                    else{
                        return
                    }
                }
                R.id.pic_button -> {
                    val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                    try {
                        cameraLauncher.launch(cameraIntent)
                    }
                    catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }
    }

    /**
     * Validates whether the user input is acceptable. Returns boolean
     */
    private fun validateForm(): Boolean {
        //list to store all string to validate at once
        val l = arrayOf<String>()
        val list: MutableList<String> = l.toMutableList()

        //name
        mainEtName = findViewById<View>(R.id.full_name) as EditText
        fullName = mainEtName!!.text.toString()

        if(TextUtils.isEmpty(fullName)){
            Toast.makeText(this@MainActivity, "Name was left blank!", Toast.LENGTH_SHORT).show()
            return false
        }

        //Age
        mainEtAge = findViewById<View>(R.id.age) as EditText
        Log.d("age", mainEtAge!!.text.toString())
        if(mainEtAge!!.text.toString().isNullOrEmpty()){
            Toast.makeText(this@MainActivity, "Age was left blank!", Toast.LENGTH_SHORT).show()
            return false
        }
        age = Integer.parseInt(mainEtAge!!.text.toString())
        list.add(age.toString())

        //Weight
        mainEtWeight = findViewById<View>(R.id.weight) as EditText
        if(mainEtWeight!!.text.toString().isNullOrEmpty()){
            Toast.makeText(this@MainActivity, "Weight was left blank!", Toast.LENGTH_SHORT).show()
            return false
        }
        weight = Integer.parseInt(mainEtWeight!!.text.toString())
        list.add(weight.toString())

        //Height
        mainEtHeight = findViewById<View>(R.id.height) as EditText
        if(mainEtHeight!!.text.toString().isNullOrEmpty()){
            Toast.makeText(this@MainActivity, "Height was left blank!", Toast.LENGTH_SHORT).show()
            return false
        }
        height = Integer.parseInt(mainEtHeight!!.text.toString())
        list.add(height.toString())

        //Location
        mainEtCountry = findViewById<View>(R.id.country) as EditText
        country = mainEtCountry!!.text.toString()
        if(country.isNullOrEmpty()){
            Toast.makeText(this@MainActivity, "Country was left blank!", Toast.LENGTH_SHORT).show()
            return false
        }
        list.add(country!!)
        mainEtCity = findViewById<View>(R.id.city) as EditText
        city = mainEtCity!!.text.toString()
        if(city.isNullOrEmpty()){
            Toast.makeText(this@MainActivity, "City was left blank!", Toast.LENGTH_SHORT).show()
            return false
        }
        list.add(city!!)

        //activity lvl
        mainActivitySpinner = findViewById<View>(R.id.activity_spinner) as Spinner
        activityLvl = mainActivitySpinner!!.getSelectedItemPosition()
        Log.d("ACTIVITY", activityLvl.toString())

        //Sex
        mainRgSex = findViewById<View>(R.id.sex) as RadioGroup
        var radioButtonID: Int? = mainRgSex!!.checkedRadioButtonId
        Log.d("RADIO BUTTON ID", radioButtonID.toString())
        var selectedButton: RadioButton? = radioButtonID?.let { findViewById(it) }
        sex = selectedButton!!.text.toString()
        Log.d("USER SEX: ", sex!!)

        //check for image
        if(mIvThumbnail == null){
            Toast.makeText(this@MainActivity, "Please select or upload an image!", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

    /**
     * Handle camera launch
     */
    private var cameraLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val extras = result.data!!.extras
            mainThumbnailImage = extras!!["data"] as Bitmap?

            //Open a file and write to it
            if (isExternalStorageWritable) {
                imagePath = saveImage(mainThumbnailImage)
                mDisplayIntent!!.putExtra("imagePath", imagePath)
            } else {
                Toast.makeText(this, "External storage not writable.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * Save image from camera
     */
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
        imagePath = file.absolutePath
        val thumbnailImage = BitmapFactory.decodeFile(imagePath)
        if (thumbnailImage != null) {
            mIvThumbnail!!.setImageBitmap(thumbnailImage)
        }
        return file.absolutePath
    }
    // Boolean representing whether storage is writeable
    private val isExternalStorageWritable: Boolean
        get() {
            val state = Environment.getExternalStorageState()
            return Environment.MEDIA_MOUNTED == state
        }
    override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
            // make toast of name which is selected in spinner
            Toast.makeText(applicationContext, act_vals[p2], Toast.LENGTH_LONG).show()
    }
    override fun onNothingSelected(p0: AdapterView<*>?) {

    }
}
/**
 * Class representing user data
 */
data class User(
    val fullName: String?,
    val height: Int?,
    val weight: Int?,
    val age: Int?,
    val activityLvl: Int?,
    val country: String?,
    val city: String?,
    val sex: String?,
) : Serializable {}