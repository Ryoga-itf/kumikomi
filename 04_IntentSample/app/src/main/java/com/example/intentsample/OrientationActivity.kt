package com.example.intentsample

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.Insets
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import kotlin.math.floor

class OrientationActivity : AppCompatActivity(), SensorEventListener {

    private lateinit var checkBoxOrientation: CheckBox
    private lateinit var azimuthText: TextView
    private lateinit var pitchText: TextView
    private lateinit var rollText: TextView

    private lateinit var sensorManager: SensorManager
    private var accelerationSensor: Sensor? = null
    private var magneticSensor: Sensor? = null

    private var accelerationValue = FloatArray(3)
    private var geoMagneticValue = FloatArray(3)
    private val orientationValue = FloatArray(3)
    private val inRotationMatrix = FloatArray(9)
    private val outRotationMatrix = FloatArray(9)
    private val inclinationMatrix = FloatArray(9)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_orientation)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars: Insets = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        checkBoxOrientation = findViewById(R.id.checkbox_orientation)
        azimuthText = findViewById(R.id.text_view_azimuth)
        rollText = findViewById(R.id.text_view_roll)
        pitchText = findViewById(R.id.text_view_pitch)
        val buttonOrientation: Button = findViewById(R.id.button_orientation)

        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        accelerationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        magneticSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

        buttonOrientation.setOnClickListener {
            if (!checkBoxOrientation.isChecked) {
                updateOrientation()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        accelerationSensor?.also {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }
        magneticSensor?.also {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent) {
        when (event.sensor.type) {
            Sensor.TYPE_MAGNETIC_FIELD -> {
                geoMagneticValue = event.values.clone()
            }
            Sensor.TYPE_ACCELEROMETER -> {
                accelerationValue = event.values.clone()
            }
        }

        if (checkBoxOrientation.isChecked) {
            updateOrientation()
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
    }

    private fun updateOrientation() {
        val success = SensorManager.getRotationMatrix(
            inRotationMatrix,
            inclinationMatrix,
            accelerationValue,
            geoMagneticValue
        )

        if (!success) return

        SensorManager.remapCoordinateSystem(
            inRotationMatrix,
            SensorManager.AXIS_X,
            SensorManager.AXIS_Z,
            outRotationMatrix
        )

        SensorManager.getOrientation(outRotationMatrix, orientationValue)

        val azimuth = floor(Math.toDegrees(orientationValue[0].toDouble())).toInt().toString()
        val pitch = floor(Math.toDegrees(orientationValue[1].toDouble())).toInt().toString()
        val roll = floor(Math.toDegrees(orientationValue[2].toDouble())).toInt().toString()

        azimuthText.text = azimuth
        pitchText.text = pitch
        rollText.text = roll
    }
}