package com.example.fitnessapp

import android.R.attr
import android.annotation.SuppressLint
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
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.test.core.app.ActivityScenario.launch
import com.amplifyframework.AmplifyException
import com.amplifyframework.auth.AuthException
import com.amplifyframework.auth.cognito.AWSCognitoAuthPlugin
import com.amplifyframework.auth.result.AuthSignInResult
import com.amplifyframework.core.Amplify
import com.amplifyframework.core.AmplifyConfiguration
import com.amplifyframework.storage.StorageException
import com.amplifyframework.storage.result.StorageUploadFileResult
import com.amplifyframework.storage.s3.AWSS3StoragePlugin
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedWriter
import java.io.FileWriter
import android.R.attr.data
import android.content.Context
import android.net.Uri
import androidx.room.Database


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
    private var weightSpinner: Spinner? = null
    private var heightSpinner: Spinner? = null
    private var ageSpinner: Spinner? = null

    private var mainEtName: EditText? = null
    private var mainEtCountry: EditText? = null
    private var mainEtCity: EditText? = null
    private var mainRgSex: RadioGroup? = null

    //Variables
    private var fullName: String? = null
    private var age: Int? = null
    private var weight: Int? = null
    private var height: Int? = null
    private var country: String? = null
    private var city: String? = null
    private var activityLvl: Int? = null
    private var sex: String? = null
    private var imagePath: String? = null
    private var userData: UserData? = null

    var mIvThumbnail: ImageView? = null

    //Values for activity spinner
    private var act_vals = arrayOf<String>(
        "Sedentary",
        "Lightly active",
        "Moderately active",
        "Very active",
        "Extra active"
    )
    private var agesList: MutableList<Int> = ArrayList()
    private var weightsList: MutableList<Int> = ArrayList()
    private var heightList: MutableList<Int> = ArrayList()

    private var mDisplayIntent: Intent? = null

    private val appViewModel: AppViewModel by viewModels {
        AppViewModelFactory((application as FitnessApplication).repository)
    }

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
        mainEtCountry = findViewById<View>(R.id.country) as EditText
        mainEtCity = findViewById<View>(R.id.city) as EditText
        mainActivitySpinner = findViewById<View>(R.id.activity_spinner) as Spinner
        weightSpinner = findViewById<View>(R.id.weight_spinner) as Spinner
        heightSpinner = findViewById<View>(R.id.height_spinner) as Spinner
        ageSpinner = findViewById<View>(R.id.age_spinner) as Spinner

        mainRgSex = findViewById<View>(R.id.sex) as RadioGroup

        var i: Int? = 0
        var j: Int? = 50
        while (i!! < 100) {
            agesList.add(i, i)
            heightList.add(i, i)
            weightsList.add(i , j!!)
            i += 1
            j += 5
        }

        initSpinners()

        //Get the buttons
        mainButtonSubmit = findViewById<View>(R.id.button_submit) as Button
        mainButtonCamera = findViewById<View>(R.id.pic_button) as Button

        //Say that this class itself contains the listener.
        mainButtonSubmit!!.setOnClickListener(this)
        mainButtonCamera!!.setOnClickListener(this)


        //Create the intent but don't start the activity yet
        mDisplayIntent = Intent(this, HomeActivity::class.java)

        //AWS s3 STUFF

        try {
            Amplify.addPlugin(AWSCognitoAuthPlugin())
            Amplify.addPlugin(AWSS3StoragePlugin())
            Amplify.configure(applicationContext)
            Log.i("Amplify","Initialized Amplify")
            Amplify.Auth.signInWithWebUI(
                this,
                {result: AuthSignInResult -> Log.i("AuthQuickStart", result.toString())},
                {error: AuthException -> Log.e("AuthError", error.toString())}
            )
        } catch (error: AmplifyException){
            Log.e("Amplify", "Could not init Amplify", error)
        }
        lifecycleScope.launch {
            //get user will happen first
            val u = appViewModel.getUser()
            userData = u
            //once user has been retrieved from db and userData is init, then fill in ui components with data
            fillData(userData)
            }
        }

    /**
     * If a user exists we fill the fields with their latest data
     */
    @RequiresApi(33)
    private fun fillData(u: UserData?) {
        // if a user exists, fill in the text fields with user data.
        if (u?.fullName != null ) {
            if (u?.fullName.toString() == "test"){
                return
            }
            mainButtonSubmit!!.text = "Update"
            mainEtName!!.setText(u.fullName)
            ageSpinner!!.setSelection(u.age!!)
            weightSpinner!!.setSelection(u.weight!!)
            heightSpinner!!.setSelection(u.height!!)
            mainEtCity?.setText(u.city)
            mainEtCountry?.setText(u.country)
            Log.d("SEX", u.sex.toString())
            if (u.sex == "Male") {
                Log.d("SEX", "MALE")
                mainRgSex?.check(R.id.radioButton)
            } else if (u.sex == "Female") {
                Log.d("SEX", "FEMALE")
                mainRgSex?.check(R.id.radioButton2)
            }
            mainActivitySpinner!!.setSelection(u.activityLvl!!)
            imagePath = u.imagePath
            val thumbnailImage = BitmapFactory.decodeFile(imagePath)
            if (thumbnailImage != null) {
                mIvThumbnail!!.setImageBitmap(thumbnailImage)
            }
        }
    }

    /**
     * method to initialize all the spinners and add data to them
     */
    private fun initSpinners() {

        //ACTIVITY LVL
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

        //WEIGHT
        weightSpinner = findViewById<View>(R.id.weight_spinner) as Spinner
        weightSpinner!!.onItemSelectedListener = this

        // Create the instance of ArrayAdapter
        val ad_w: ArrayAdapter<*> = ArrayAdapter<Any?>(
            this,
            android.R.layout.simple_spinner_item,
            weightsList as List<Any?>
        )

        // set simple layout resource file
        // for each item of spinner
        ad_w.setDropDownViewResource(
            android.R.layout.simple_spinner_dropdown_item
        )

        // Set the ArrayAdapter (ad) data on the
        // Spinner which binds data to spinner
        weightSpinner!!.adapter = ad_w


        //HEIGHT
        heightSpinner = findViewById<View>(R.id.height_spinner) as Spinner
        heightSpinner!!.onItemSelectedListener = this

        // Create the instance of ArrayAdapter
        val ad_h: ArrayAdapter<*> = ArrayAdapter<Any?>(
            this,
            android.R.layout.simple_spinner_item,
            heightList as List<Any?>
        )

        // set simple layout resource file
        // for each item of spinner
        ad_h.setDropDownViewResource(
            android.R.layout.simple_spinner_dropdown_item
        )

        // Set the ArrayAdapter (ad) data on the
        // Spinner which binds data to spinner
        heightSpinner!!.adapter = ad_h


        //AGE
        ageSpinner = findViewById<View>(R.id.age_spinner) as Spinner
        ageSpinner!!.onItemSelectedListener = this

        // Create the instance of ArrayAdapter
        val ad_a: ArrayAdapter<*> = ArrayAdapter<Any?>(
            this,
            android.R.layout.simple_spinner_item,
            agesList as List<Any?>
        )

        // set simple layout resource file
        // for each item of spinner
        ad_a.setDropDownViewResource(
            android.R.layout.simple_spinner_dropdown_item
        )

        // Set the ArrayAdapter (ad) data on the
        // Spinner which binds data to spinner
        ageSpinner!!.adapter = ad_a

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
                        userData = UserData( fullName, height, weight, age, activityLvl, country, city, sex, imagePath)
                        appViewModel.setUser(userData)
                        uploadFile("application.db")
                        uploadFile("application.db-shm")
                        uploadFile("application.db-wal")
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
     * Uploads db files to aws every time the user updates their data
     */
    private fun uploadFile(filename: String?) {

            val authUser = Amplify.Auth.currentUser.userId
            val roomDB = File(this.getDatabasePath(filename).absolutePath)
            Log.d("MyAmplifyApp", roomDB.toString())
            Amplify.Storage.uploadFile(
                authUser + filename,
                roomDB,
                { result: StorageUploadFileResult ->
                    Log.i(
                        "MyAmplifyApp",
                        "Successfully uploaded: " + result.key
                    )
                },
                { storageFailure: StorageException? ->
                    Log.e(
                        "MyAmplifyApp",
                        "Upload failed",
                        storageFailure
                    )
                }
            )
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
        age = ageSpinner!!.getSelectedItemPosition()

        //Weight
        weight = weightSpinner!!.getSelectedItemPosition()
        if(weight.toString().isNullOrEmpty()){
            Toast.makeText(this@MainActivity, "Weight was left blank!", Toast.LENGTH_SHORT).show()
            return false
        }

        //Height
        height = heightSpinner!!.getSelectedItemPosition()

        if(height.toString().isNullOrEmpty()){
            Toast.makeText(this@MainActivity, "Height was left blank!", Toast.LENGTH_SHORT).show()
            return false
        }

        //Location
        mainEtCountry = findViewById<View>(R.id.country) as EditText
        country = mainEtCountry!!.text.toString()
        if(country.isNullOrEmpty()){
            Toast.makeText(this@MainActivity, "Country was left blank!", Toast.LENGTH_SHORT).show()
            return false
        }
        if(country!!.length == 1 || country!!.length > 2 ) {
            Toast.makeText(this@MainActivity, "Country must be a 2 character country code!", Toast.LENGTH_SHORT).show()
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
    }
    override fun onNothingSelected(p0: AdapterView<*>?) {

    }
}
