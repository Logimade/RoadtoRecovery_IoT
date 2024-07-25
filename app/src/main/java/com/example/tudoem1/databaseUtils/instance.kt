package com.example.tudoem1.databaseUtils

import android.content.Context
import androidx.room.*
import androidx.room.migration.AutoMigrationSpec

@TypeConverters(value = [Converters::class])
@Database(
    entities = [MeasureStructure::class, MetricStructure::class],
    version = 2,
    autoMigrations = [
        AutoMigration(
            from = 1, to = 2,
            spec = DatabasePrototype.MigrateOldToNew::class
        )
    ]
)

abstract class DatabasePrototype : RoomDatabase() {
    abstract fun daoNetworkMethods(): NetworkMethods

    // define class to migrate database
    class MigrateOldToNew : AutoMigrationSpec

    companion object {
        @Volatile
        private var INSTANCE: DatabasePrototype? = null

        fun getDatabase(context: Context): DatabasePrototype {
            // if the INSTANCE is not null, then return it,
            // if it is, then create the database
            if (INSTANCE == null) {
                synchronized(this) {
                    // Pass the database to the INSTANCE
                    INSTANCE = buildDatabase(context)
                }
            }
            // Return database.
            return INSTANCE!!
        }

        private fun buildDatabase(context: Context): DatabasePrototype {
            return Room.databaseBuilder(
                context.applicationContext,
                DatabasePrototype::class.java,
                "files.db"
            ).fallbackToDestructiveMigration().allowMainThreadQueries().build()
        }
    }
}