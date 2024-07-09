package com.example.tudoem1.databaseUtils

import androidx.room.TypeConverter
import com.example.tudoem1.webservices.Coordinates
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class Converters {
    @TypeConverter
    fun fromCoordinates(coordinates: Coordinates?): String? {
        return Gson().toJson(coordinates)
    }

    @TypeConverter
    fun toCoordinates(coordinatesString: String?): Coordinates? {
        return Gson().fromJson(coordinatesString, object : TypeToken<Coordinates?>() {}.type)
    }
}