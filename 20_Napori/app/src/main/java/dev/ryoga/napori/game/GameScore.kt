package dev.ryoga.napori.game

data class GameScore(
    val score: Int = 0,
    val combo: Int = 0,
    val maxCombo: Int = 0,
    val maxSpinSpeed: Float = 0f,
    val scoreCarry: Float = 0f,
    val comboChargeSeconds: Float = 0f,
)
