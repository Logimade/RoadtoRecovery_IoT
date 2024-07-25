package com.example.tudoem1.webservices

import androidx.room.PrimaryKey

// Coordinates.kt
data class Coordinates(
    var lat: Double,
    var long: Double
)

// Measure.kt
data class Measure(
    val timestamp: String,
    val measureId: String,
    val coordinates: Coordinates,
    val metrics: String,
    val networkType: String,                 // Add network type fields
    val isHspaDc: String?,
    val isLteCaCellInfo: String?,
    val isLteCaServiceState: String?,
    val isLteCaPhysicalChannel: String?,
    val isLteCaOrNsaNrDisplayInfo: String?
)

data class MeasureStructure(
    val id: String,
    val startDate: String,
    val endDate: String? = null,
    val coordinatesStart: Coordinates,
    val coordinatesEnd: Coordinates? = null,
)

// PostData.kt
data class PostData(
    val acquisition: MeasureStructure,
    val measures: List<Measure>
)
