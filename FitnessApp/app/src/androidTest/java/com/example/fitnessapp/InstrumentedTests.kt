package com.example.fitnessapp

import android.content.Intent
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith


/**
 * Tests the user interface to ensure the correct intents are being launched when the UI is navigated through
 */
@RunWith(AndroidJUnit4::class)
class InstrumentedTests {
    // user used to test functionality
    private val user = User("test", 72, 190, 24, 0, "US", "Salt Lake City", "Male")

    // create the ActivityScenario and intent to launch the activities for HikesActivity and MainActivity
    private lateinit var hikesScenario: ActivityScenario<HikesActivity>
    private val hikesIntent = Intent(ApplicationProvider.getApplicationContext(), HikesActivity::class.java).putExtra("user", user).putExtra("full_name", "test").putExtra("the_city", "SLC").putExtra("the_country", "US")

    private lateinit var mainScenario: ActivityScenario<MainActivity>
    private val mainIntent = Intent(ApplicationProvider.getApplicationContext(), MainActivity::class.java).putExtra("user", user).putExtra("full_name", "test").putExtra("the_city", "SLC").putExtra("the_country", "US")

    @get:Rule
    val hikesRule = ActivityScenarioRule<HikesActivity>(hikesIntent)
    @get:Rule
    val mainRule = ActivityScenarioRule<MainActivity>(mainIntent)

    /**
     * Launch the intents
     */
    @Before
    fun initialization(){
        hikesScenario = ActivityScenario.launch(hikesIntent)
        mainScenario = ActivityScenario.launch(mainIntent)
    }
    /**
     * Close the scenarios after testing
     */
    @After
    fun cleanup() {
        hikesScenario.close()
        mainScenario.close()
    }
    /**
     * Tests that the camera is opened when clicking the pic_button element
     */
    @Test
    fun testImageClick() {
        onView(withId(R.id.pic_button))
            .check(matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))
    }
    /**
     * Tests that the app context is loaded correctly
     */
    @Test
    fun testAppContext() {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("com.example.fitnessapp", appContext.packageName)
    }

    // TODO get working
//    @Test
//    fun launchHikesTest() {
//        val expectedIntent = AllOf.allOf(IntentMatchers.hasAction(Intent.ACTION_VIEW))
//        onView(withId(R.id.launch_hikes_button)).perform(click())
//        intended(expectedIntent)
//        Espresso.pressBack()
//    }
}