package dev.ryoga.napori.sensor

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlin.math.PI
import kotlin.math.acos
import kotlin.math.abs

class GyroSensor(
    context: Context,
    private val onMotionChanged: (MotionSensorState, deltaSeconds: Float) -> Unit,
) : SensorEventListener {
    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
    private val rotationVector = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
    private val rotationMatrix = FloatArray(9)

    private var currentState = MotionSensorState()
    private var lastGyroTimestampNanos = 0L
    private var rotationAngleDeg = 0f
    private var smoothedSpinSpeedDegPerSec = 0f

    val hasGyroscope: Boolean = gyroscope != null
    val hasRotationVector: Boolean = rotationVector != null

    fun reset() {
        currentState = MotionSensorState()
        lastGyroTimestampNanos = 0L
        rotationAngleDeg = 0f
        smoothedSpinSpeedDegPerSec = 0f
        onMotionChanged(currentState, 0f)
    }

    fun start() {
        lastGyroTimestampNanos = 0L
        gyroscope?.also { gyro ->
            sensorManager.registerListener(
                this,
                gyro,
                SensorManager.SENSOR_DELAY_GAME,
            )
        }
        rotationVector?.also { sensor ->
            sensorManager.registerListener(
                this,
                sensor,
                SensorManager.SENSOR_DELAY_GAME,
            )
        }
    }

    fun stop() {
        sensorManager.unregisterListener(this)
        lastGyroTimestampNanos = 0L
    }

    override fun onSensorChanged(event: SensorEvent) {
        when (event.sensor.type) {
            Sensor.TYPE_GYROSCOPE -> handleGyroscope(event)
            Sensor.TYPE_ROTATION_VECTOR -> handleRotationVector(event)
        }
    }

    private fun handleGyroscope(event: SensorEvent) {
        val angularVelocityZRadPerSec = event.values[2]
        val rawSpinSpeedDegPerSec = abs(angularVelocityZRadPerSec.toDegreesPerSecond())
        val deltaSeconds = if (lastGyroTimestampNanos == 0L) {
            0f
        } else {
            (event.timestamp - lastGyroTimestampNanos) / NANOS_PER_SECOND
        }

        if (deltaSeconds > 0f) {
            val deltaAngleDeg = angularVelocityZRadPerSec.toDegreesPerSecond() * deltaSeconds
            rotationAngleDeg = (rotationAngleDeg + deltaAngleDeg).wrapDegrees()
            smoothedSpinSpeedDegPerSec = smoothSpeed(rawSpinSpeedDegPerSec, deltaSeconds)
        } else {
            smoothedSpinSpeedDegPerSec = rawSpinSpeedDegPerSec
        }

        lastGyroTimestampNanos = event.timestamp
        currentState = currentState.copy(
            rotationAngleDeg = rotationAngleDeg,
            spinSpeedDegPerSec = smoothedSpinSpeedDegPerSec,
        )
        onMotionChanged(
            currentState,
            deltaSeconds,
        )
    }

    private fun handleRotationVector(event: SensorEvent) {
        SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
        val screenNormalZ = rotationMatrix[8].coerceIn(-1f, 1f)
        val tiltDeg = acos(screenNormalZ) * 180f / PI.toFloat()

        currentState = currentState.copy(tiltDeg = tiltDeg)
        onMotionChanged(currentState, 0f)
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit

    private fun Float.toDegreesPerSecond(): Float = this * 180f / PI.toFloat()

    private fun smoothSpeed(rawSpinSpeedDegPerSec: Float, deltaSeconds: Float): Float {
        val alpha = deltaSeconds / (SPEED_SMOOTHING_SECONDS + deltaSeconds)
        return smoothedSpinSpeedDegPerSec +
            (rawSpinSpeedDegPerSec - smoothedSpinSpeedDegPerSec) * alpha
    }

    private fun Float.wrapDegrees(): Float {
        val wrapped = this % 360f
        return if (wrapped < 0f) wrapped + 360f else wrapped
    }

    companion object {
        private const val NANOS_PER_SECOND = 1_000_000_000f
        private const val SPEED_SMOOTHING_SECONDS = 0.12f
    }
}
