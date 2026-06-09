package dev.ryoga.napori.sensor

data class MotionSensorState(
    val rotationAngleDeg: Float = 0f,
    val spinSpeedDegPerSec: Float = 0f,
    val tiltDeg: Float = 0f,
    val verticalAcceleration: Float = 0f,
)
