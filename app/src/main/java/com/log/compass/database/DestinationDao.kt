package com.log.compass.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface DestinationDao {

    @Insert
    suspend fun insert(destination: Destination)

    @Query("SELECT * FROM Destination")
    suspend fun getDestination(): Destination?

    @Query("DELETE FROM Destination")
    suspend fun deleteDestination()

}