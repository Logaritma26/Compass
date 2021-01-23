package com.log.compass.viewmodel

import android.app.Application
import androidx.lifecycle.MutableLiveData
import com.log.compass.database.Destination
import com.log.compass.database.DestinationDatabase
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlin.math.*

class ViewModel(application: Application) : AbstractVM(application) {


    private val destinationDao = DestinationDatabase(getApplication()).DestinationDao()


    // compass degree
    val currentDegree: MutableLiveData<Array<Int>> = MutableLiveData<Array<Int>>(arrayOf(0, 0))

    // self location
    val location: MutableLiveData<Array<Double>> = MutableLiveData<Array<Double>>()

    // distance between current location and destination
    val distance: MutableLiveData<Int> = MutableLiveData()

    // degree of destination / destination indicator degree
    val destinationDegree: MutableLiveData<Array<Int>> = MutableLiveData(arrayOf(0, 0))


    // distance functions
    fun updateDistance(newDistance: Int) {
        distance.value = newDistance
    }


    // degree functions
    fun updateDegree(newDegree: Int) {
        val oldDegree = currentDegree.value?.get(0)
        if (oldDegree != null) {
            val newArray: Array<Int> = arrayOf(newDegree, oldDegree)
            currentDegree.value = newArray
        } else {
            val newArray: Array<Int> = arrayOf(newDegree, 0)
            currentDegree.value = newArray
        }
        updateWithCompass()
    }

    fun getCompassDegree(): Int? = currentDegree.value?.get(0)


    // location functions
    fun updateLocation(newLocation: Array<Double>) {
        location.value = newLocation
    }


    // live - destination functions
    suspend fun getDestination(): Destination? = async { return@async destinationDao.getDestination() }.await()

    private suspend fun deleteDestiantion() = async {
        destinationDao.deleteDestination()
    }.await()

    fun setDestination(destination: Destination) = launch {
        deleteDestiantion()
        destinationDao.insert(destination)
    }


    // destinationDegree functions
    fun updateDestinationDegree(newDegree: Int) {
        val oldDegree = destinationDegree.value?.get(0)
        if (oldDegree != null) {
            val newArray: Array<Int> = arrayOf(newDegree, oldDegree)
            destinationDegree.value = newArray
        } else {
            val newArray: Array<Int> = arrayOf(newDegree, 0)
            destinationDegree.value = newArray
        }
    }

    private fun updateWithCompass() {
        val compass = currentDegree.value
        val destDegree = destinationDegree.value?.get(0)
        destDegree?.let {
            var dif: Int
            compass?.let {
                dif = it[0] - it[1]
                updateDestinationDegree(destDegree + dif)
            }
        }
    }


}