package com.example.tudoem1.databaseUtils

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Relation
import com.example.tudoem1.webservices.Coordinates
import java.util.UUID


@Entity
data class MeasureStructure(
    @PrimaryKey val id: UUID,
    val startDate: String,
    val endDate: String? = null,
    val coordinatesStart: Coordinates,
    val coordinatesEnd: Coordinates? = null,
    val uploaded: Boolean = false
)

@Entity
data class MetricStructure(
    @PrimaryKey val timeStamp: String,
    val measureId: UUID,
    val coordinates: Coordinates,
    val metrics: String

)

@Entity
data class MeasureWithMetrics(
    @Embedded val video: MeasureStructure,
    @Relation(
        parentColumn = "id",
        entityColumn = "measureId",
    )
    val frames: List<MetricStructure>
)