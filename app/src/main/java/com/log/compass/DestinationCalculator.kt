package com.log.compass

import androidx.lifecycle.LifecycleOwner
import com.log.compass.viewmodel.ViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.*

class DestinationCalculator(val viewModel: ViewModel, private val lifecycleOwner: LifecycleOwner) {

    init {
        observeSelfLocation()
    }

    private fun observeSelfLocation() {
        viewModel.location.observe(lifecycleOwner, {
            calculateDistance(it[0], it[1])
            destinationCompass(it[0], it[1])
        })
    }

    private fun calculateDistance(latitude: Double, longitude: Double){
        CoroutineScope(Dispatchers.Main).launch {
            val destination = viewModel.getDestination()
            if (destination != null){
                val lat1 = Math.toRadians(latitude)
                val lon1 = Math.toRadians(longitude)
                val lat2 = Math.toRadians(destination.latitude.toDouble())
                val lon2 = Math.toRadians(destination.longitude.toDouble())

                // Haversine formula
                val difLon = lon2 - lon1
                val difLat = lat2 - lat1

                val a = (sin(difLat / 2).pow(2.0) + (cos(lat1) * cos(lat2) * sin(difLon / 2).pow(2.0)))
                val c = 2 * asin(sqrt(a))

                // Radius of earth in kilometers
                val r = 6371.0
                val res = ((c * r)*1000).toInt()

                viewModel.updateDistance(res)
            }

        }
    }

    private fun destinationCompass(ownLatitude: Double, ownLongitude: Double) {
        CoroutineScope(Dispatchers.Main).launch {
            val destination = viewModel.getDestination()
            val compassDegree = viewModel.getCompassDegree()
            var destinationDegree: Int = -1

            if (destination != null && compassDegree != null){
                val lat1 = Math.toRadians(ownLatitude)
                val lon1 = Math.toRadians(ownLongitude)
                val lat2 = Math.toRadians(destination.latitude.toDouble())
                val lon2 = Math.toRadians(destination.longitude.toDouble())

                var xVal = lat1 - lat2
                var yVal = lon1 - lon2


                val xSign = xVal < 0
                val ySign = yVal < 0

                xVal = abs(xVal)
                yVal = abs(yVal)

                when {
                    xSign && ySign -> {
                        val degree = tangentDegree(xVal, yVal)
                        destinationDegree = ((compassDegree - degree + 360) % 360).toInt()
                    }
                    !xSign && ySign -> {
                        val degree = 180 - tangentDegree(xVal, yVal)
                        destinationDegree = ((compassDegree - degree + 360) % 360).toInt()
                    }
                    !xSign && !ySign -> {
                        val degree = 180 + tangentDegree(xVal, yVal)
                        destinationDegree = ((compassDegree  - degree + 360) % 360).toInt()
                    }
                    xSign && !ySign -> {
                        val degree = 360 - tangentDegree(xVal, yVal)
                        destinationDegree = ((compassDegree - degree + 360) % 360).toInt()
                    }
                }

                viewModel.updateDestinationDegree(destinationDegree)
            }
        }
    }

    
    // get the angle value of triangle then inverse tangent to get the degree of triangle
    private fun tangentDegree(xVal: Double, yVal: Double) : Double{
        val angleValue = yVal/xVal
        val radian = atan(angleValue)
        return Math.toDegrees(radian)
    }

}