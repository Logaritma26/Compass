package com.log.compass.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase


@Database(entities = [Destination::class], version = 1, exportSchema = true)
abstract class DestinationDatabase : RoomDatabase() {

    abstract fun DestinationDao() : DestinationDao

    companion object  {

        @Volatile
        private var instance: DestinationDatabase? = null

        private val lock = Any()

        operator fun invoke(context: Context) = instance ?: synchronized(lock) {
            instance ?: startInstance(context).also {
                instance = it
            }
        }

        private fun startInstance(context: Context) =
            Room.databaseBuilder(context.applicationContext,
                DestinationDatabase::class.java,
                "destinationdatabase").build()

    }

}