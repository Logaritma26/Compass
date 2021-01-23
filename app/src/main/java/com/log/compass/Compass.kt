package com.log.compass

import android.app.Activity
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.widget.Toast
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.log.compass.viewmodel.ViewModel
import kotlin.math.roundToInt

class Compass(private val activity: Activity, private val viewModel: ViewModel) :
    SensorEventListener {

    private val fusedLocationClient: FusedLocationProviderClient
    private val sensorManager: SensorManager =
        activity.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val sensorAccelerometer: Sensor
    private val sensorMagneticField: Sensor

    private val gravity = FloatArray(3)
    private val geoMagnetic = FloatArray(3)

    private var azimuth: Float = 0.0f
    private var currentDegree: Float = 0.0f

    private var lastTime: Long = 0


    init {
        sensorAccelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        sensorMagneticField = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(activity)
    }

    fun registerListeners() {
        sensorManager.registerListener(this, sensorMagneticField, SensorManager.SENSOR_DELAY_GAME)
        sensorManager.registerListener(this, sensorAccelerometer, SensorManager.SENSOR_DELAY_GAME)
    }

    fun unregisterListeners() {
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {

        val coefficient = 0.97f
        synchronized(this) {

            if (event?.sensor?.type == Sensor.TYPE_ACCELEROMETER && event.accuracy > 1) {
                gravity[0] = coefficient * gravity[0] + (1 - coefficient) * event.values[0]
                gravity[1] = coefficient * gravity[1] + (1 - coefficient) * event.values[1]
                gravity[2] = coefficient * gravity[2] + (1 - coefficient) * event.values[2]
            }

            if (event?.sensor?.type == Sensor.TYPE_MAGNETIC_FIELD && event.accuracy > 1) {
                geoMagnetic[0] = coefficient * geoMagnetic[0] + (1 - coefficient) * event.values[0]
                geoMagnetic[1] = coefficient * geoMagnetic[1] + (1 - coefficient) * event.values[1]
                geoMagnetic[2] = coefficient * geoMagnetic[2] + (1 - coefficient) * event.values[2]
            }


            val i = FloatArray(9)
            val r = FloatArray(9)

            val success = SensorManager.getRotationMatrix(r, i, gravity, geoMagnetic)

            if (success && System.currentTimeMillis() - lastTime > 100) {

                val orientation = FloatArray(3)
                SensorManager.getOrientation(r, orientation)

                azimuth = Math.toDegrees(orientation[0].toDouble()).toFloat() // orientation
                azimuth = (azimuth + 360) % 360

                azimuth = azimuth.roundToInt().toFloat()

                currentDegree = -azimuth
                lastTime = System.currentTimeMillis()

                viewModel.updateDegree(-currentDegree.toInt())
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        if (sensor == sensorAccelerometer) {
            if (accuracy <= 1) {
                Toast.makeText(
                    activity,
                    "Your sensor accuracy is too low, please calibrate !",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        if (sensor == sensorMagneticField) {
            if (accuracy <= 1) {
                Toast.makeText(
                    activity,
                    "Your sensor accuracy is too low, please calibrate !",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

}