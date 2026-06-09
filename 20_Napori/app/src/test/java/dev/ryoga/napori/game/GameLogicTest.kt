package dev.ryoga.napori.game

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GameLogicTest {
    @Test
    fun judgeSpin_returnsExpectedJudgements() {
        assertEquals(SpinJudgement.Stopped, GameLogic.judgeSpin(0f))
        assertEquals(SpinJudgement.Good, GameLogic.judgeSpin(60f))
        assertEquals(SpinJudgement.Great, GameLogic.judgeSpin(180f))
        assertEquals(SpinJudgement.Overspin, GameLogic.judgeSpin(500f))
    }

    @Test
    fun judgeTilt_returnsExpectedJudgements() {
        assertEquals(TiltJudgement.Stable, GameLogic.judgeTilt(0f))
        assertEquals(TiltJudgement.Risky, GameLogic.judgeTilt(15f))
        assertEquals(TiltJudgement.ToppingsSlip, GameLogic.judgeTilt(30f))
        assertEquals(TiltJudgement.Miss, GameLogic.judgeTilt(45f))
    }

    @Test
    fun updateScore_addsScoreAndComboWhileSpinning() {
        val updated = GameLogic.updateScore(
            currentScore = GameScore(),
            spinSpeedDegPerSec = 180f,
            deltaSeconds = 1f,
        )

        assertEquals(24, updated.score)
        assertEquals(1, updated.combo)
        assertEquals(1, updated.maxCombo)
        assertEquals(180f, updated.maxSpinSpeed)
    }

    @Test
    fun updateScore_resetsComboWhenSpinStops() {
        val updated = GameLogic.updateScore(
            currentScore = GameScore(score = 40, combo = 3, maxCombo = 3),
            spinSpeedDegPerSec = 40f,
            deltaSeconds = 1f,
        )

        assertEquals(40, updated.score)
        assertEquals(0, updated.combo)
        assertEquals(3, updated.maxCombo)
    }

    @Test
    fun updateScore_keepsComboWhenDeltaIsZero() {
        val updated = GameLogic.updateScore(
            currentScore = GameScore(score = 40, combo = 3, maxCombo = 3),
            spinSpeedDegPerSec = 80f,
            deltaSeconds = 0f,
        )

        assertEquals(40, updated.score)
        assertEquals(3, updated.combo)
        assertEquals(3, updated.maxCombo)
    }

    @Test
    fun updateScore_tracksMaxSpinSpeed() {
        val updated = GameLogic.updateScore(
            currentScore = GameScore(maxSpinSpeed = 120f),
            spinSpeedDegPerSec = 240f,
            deltaSeconds = 0.5f,
        )

        assertTrue(updated.maxSpinSpeed >= 240f)
    }
}
