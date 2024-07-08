package com.example.tudoem1.webservices

// Coordinates.kt
data class Coordinates(
    val lat: Double,
    val lon: Double
)

// Measure.kt
data class Measure(
    val timestamp: String,
    val coordinates: Coordinates,
    val metrics: String
)

// PostData.kt
data class PostData(
    val measures: List<Measure>
)
