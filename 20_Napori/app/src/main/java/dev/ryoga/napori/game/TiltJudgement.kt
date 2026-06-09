package dev.ryoga.napori.game

enum class TiltJudgement(
    val label: String,
) {
    Stable("Nice!"),
    Risky("Hey, it's tilted!"),
    ToppingsSlip("Be careful!"),
    Miss("No way!"),
}
