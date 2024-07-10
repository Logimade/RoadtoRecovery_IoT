package com.example.tudoem1.webservices

import com.example.tudoem1.databaseUtils.MeasureStructure

// Coordinates.kt
data class Coordinates(
    var lat: Double,
    var long: Double
)

// Measure.kt
data class Measure(
    val timestamp: String,
    val coordinates: Coordinates,
    val metrics: String
)

// PostData.kt
data class PostData(
    val acquisition: MeasureStructure,
    val measures: List<Measure>
)
