package dev.ryoga.napori.game

import kotlin.math.floor
import kotlin.math.max

object GameLogic {
    fun judgeSpin(spinSpeedDegPerSec: Float): SpinJudgement = when {
        spinSpeedDegPerSec < 60f -> SpinJudgement.Stopped
        spinSpeedDegPerSec < 180f -> SpinJudgement.Good
        spinSpeedDegPerSec < 500f -> SpinJudgement.Great
        else -> SpinJudgement.Overspin
    }

    fun judgeTilt(tiltDeg: Float): TiltJudgement = when {
        tiltDeg < 15f -> TiltJudgement.Stable
        tiltDeg < 30f -> TiltJudgement.Risky
        tiltDeg < 45f -> TiltJudgement.ToppingsSlip
        else -> TiltJudgement.Miss
    }

    fun updateScore(
        currentScore: GameScore,
        spinSpeedDegPerSec: Float,
        deltaSeconds: Float,
    ): GameScore {
        val maxSpinSpeed = max(currentScore.maxSpinSpeed, spinSpeedDegPerSec)
        val judgement = judgeSpin(spinSpeedDegPerSec)
        if (deltaSeconds <= 0f) {
            return currentScore.copy(maxSpinSpeed = maxSpinSpeed)
        }
        if (judgement.scoreRatePerSecond <= 0f) {
            return currentScore.copy(
                combo = 0,
                maxSpinSpeed = maxSpinSpeed,
                comboChargeSeconds = 0f,
            )
        }

        val comboMultiplier = 1f + currentScore.combo * 0.05f
        val scoreWithCarry = currentScore.scoreCarry +
            judgement.scoreRatePerSecond * comboMultiplier * deltaSeconds
        val scoreToAdd = floor(scoreWithCarry).toInt()
        val comboCharge = currentScore.comboChargeSeconds + deltaSeconds
        val comboToAdd = floor(comboCharge).toInt()
        val nextCombo = currentScore.combo + comboToAdd

        return currentScore.copy(
            score = currentScore.score + scoreToAdd,
            combo = nextCombo,
            maxCombo = max(currentScore.maxCombo, nextCombo),
            maxSpinSpeed = maxSpinSpeed,
            scoreCarry = scoreWithCarry - scoreToAdd,
            comboChargeSeconds = comboCharge - comboToAdd,
        )
    }
}
