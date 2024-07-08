package com.example.tudoem1.gpsUtils

sealed class Types  {
    data class DbCoordinates(var lat: Double, var long: Double)
}
