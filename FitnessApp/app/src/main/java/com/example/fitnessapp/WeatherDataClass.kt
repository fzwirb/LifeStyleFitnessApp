package com.example.fitnessapp

data class WeatherDataClass(val coord: Coord , val weather: List<Weather> , val main: Main) {

    data class Coord(val lat : Double, val lon : Double)
    data class Weather(val main : String, val description : String)
    data class Main(val temp : Double, val humidity : Double)

}
