package com.example.fitnessapp

import java.io.IOException
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.URL
import java.util.*
import com.google.gson.Gson

/**
 * Class with function to query weather data from the openweathermap api
 */

object NetworkUtilities {
    //https://api.openweathermap.org/data/2.5/weather?q=London,uk&APPID=1094276f365a8a0e492cb620a97577be
    private const val BASE_URL = "https://api.openweathermap.org/data/2.5/weather?q="
    private const val APPIDQUERY = "&APPID="
    private const val app_id = "1094276f365a8a0e492cb620a97577be"
    /**
     * Returns a URL when given a string representation of the url
     */
    fun buildURLFromString(location: String): URL? {
        var myURL: URL? = null
        try {
            myURL = URL(BASE_URL + location + APPIDQUERY + app_id)
        } catch (e: MalformedURLException) {
            e.printStackTrace()
        }
        return myURL;
    }
    /**
     * Returns data from a given url
     */
    @Throws(IOException::class)
    fun getDataFromURL(url: URL): String? {
        val urlConnection = url.openConnection() as HttpURLConnection
        return try {
            val inputStream = urlConnection.inputStream

            val scanner = Scanner(inputStream)
            scanner.useDelimiter("\\A")
            val hasInput = scanner.hasNext()
            if (hasInput) {
                scanner.next()
            } else {
                null
            }
        } finally {
            urlConnection.disconnect()
        }
    }
}