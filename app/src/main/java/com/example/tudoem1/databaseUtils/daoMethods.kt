package com.example.tudoem1.databaseUtils

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.tudoem1.webservices.Coordinates
import java.util.UUID

@Dao
interface NetworkMethods {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMeasure(measure: MeasureStructure)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMetric(metric: MetricStructure)

//    @Transaction
//    @Query("SELECT * FROM MeasureStructure")
//    suspend fun getAllMeasures(): List<MeasureStructure>

    @Transaction
    @Query("SELECT * FROM MeasureStructure WHERE uploaded=0")
    suspend fun getMeasuresToUpload(): List<MeasureWithMetrics>

    @Query("UPDATE MeasureStructure SET endDate=:endMeasure, coordinatesEnd=:coordinatesStopped WHERE id=:key")
    suspend fun updateMeasure(key:UUID, endMeasure:String, coordinatesStopped: Coordinates)

}