package dev.ryoga.napori.game

enum class SpinJudgement(
    val label: String,
    val scoreRatePerSecond: Float,
) {
    Stopped("Spin more！", 0f),
    Good("Good!", 12f),
    Great("Great!", 24f),
    Overspin("Too fast！", 0f),
}
