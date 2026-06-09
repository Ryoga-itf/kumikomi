package dev.ryoga.napori.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.ryoga.napori.game.GameScore
import dev.ryoga.napori.game.GameState
import dev.ryoga.napori.sensor.MotionSensorState
import dev.ryoga.napori.ui.theme.NaporiTheme
import kotlinx.coroutines.delay

@Composable
fun NaporiApp(
    gameState: GameState,
    sensorState: MotionSensorState,
    gameScore: GameScore,
    timeRemainingSeconds: Int,
    hasGyroscope: Boolean,
    hasTiltSensor: Boolean,
    onStartGame: () -> Unit,
    onRetryGame: () -> Unit,
    onTimeRemainingChanged: (Int) -> Unit,
    onGameFinished: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LaunchedEffect(gameState) {
        if (gameState != GameState.Playing) return@LaunchedEffect

        var remaining = timeRemainingSeconds
        while (remaining > 0) {
            delay(1_000)
            remaining -= 1
            onTimeRemainingChanged(remaining)
        }
        onGameFinished()
    }

    Surface(modifier = modifier.fillMaxSize(), color = Color(0xFFFFFBF2)) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFFFFF5DE),
                            Color(0xFFFFFBF2),
                            Color(0xFFF8E2BE),
                        ),
                    ),
                )
                .safeDrawingPadding()
                .padding(horizontal = 24.dp, vertical = 28.dp),
        ) {
            when (gameState) {
                GameState.Title -> TitleScreen(
                    sensorState = sensorState,
                    hasGyroscope = hasGyroscope,
                    onStartGame = onStartGame,
                )

                GameState.Playing -> GameScreen(
                    sensorState = sensorState,
                    gameScore = gameScore,
                    timeRemainingSeconds = timeRemainingSeconds,
                    hasGyroscope = hasGyroscope,
                    hasTiltSensor = hasTiltSensor,
                )

                GameState.Result -> ResultScreen(
                    gameScore = gameScore,
                    onRetryGame = onRetryGame,
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun NaporiAppPreview() {
    NaporiTheme {
        NaporiApp(
            gameState = GameState.Playing,
            sensorState = MotionSensorState(
                rotationAngleDeg = 28f,
                spinSpeedDegPerSec = 142f,
            ),
            gameScore = GameScore(
                score = 128,
                combo = 4,
                maxCombo = 6,
                maxSpinSpeed = 214f,
            ),
            timeRemainingSeconds = 24,
            hasGyroscope = true,
            hasTiltSensor = true,
            onStartGame = {},
            onRetryGame = {},
            onTimeRemainingChanged = {},
            onGameFinished = {},
        )
    }
}
