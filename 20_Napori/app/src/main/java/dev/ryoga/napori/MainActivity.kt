package dev.ryoga.napori

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import dev.ryoga.napori.game.GameLogic
import dev.ryoga.napori.game.GameScore
import dev.ryoga.napori.game.GameState
import dev.ryoga.napori.sensor.GyroSensor
import dev.ryoga.napori.sensor.MotionSensorState
import dev.ryoga.napori.ui.NaporiApp
import dev.ryoga.napori.ui.theme.NaporiTheme

class MainActivity : ComponentActivity() {
    private lateinit var gyroSensor: GyroSensor

    private var sensorState by mutableStateOf(MotionSensorState())
    private var gameScore by mutableStateOf(GameScore())
    private var gameState by mutableStateOf(GameState.Title)
    private var timeRemainingSeconds by mutableIntStateOf(GAME_DURATION_SECONDS)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        gyroSensor = GyroSensor(applicationContext) { motionState, deltaSeconds ->
            sensorState = motionState
            if (gameState == GameState.Playing) {
                gameScore = GameLogic.updateScore(
                    currentScore = gameScore,
                    spinSpeedDegPerSec = motionState.spinSpeedDegPerSec,
                    deltaSeconds = deltaSeconds,
                )
            }
        }

        enableEdgeToEdge()
        setContent {
            NaporiTheme {
                NaporiApp(
                    gameState = gameState,
                    sensorState = sensorState,
                    gameScore = gameScore,
                    timeRemainingSeconds = timeRemainingSeconds,
                    hasGyroscope = gyroSensor.hasGyroscope,
                    hasTiltSensor = gyroSensor.hasRotationVector,
                    onStartGame = ::startGame,
                    onRetryGame = ::startGame,
                    onTimeRemainingChanged = { remaining ->
                        timeRemainingSeconds = remaining
                    },
                    onGameFinished = {
                        gameState = GameState.Result
                    },
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        gyroSensor.start()
    }

    override fun onPause() {
        gyroSensor.stop()
        super.onPause()
    }

    private fun startGame() {
        gyroSensor.reset()
        sensorState = MotionSensorState()
        gameScore = GameScore()
        timeRemainingSeconds = GAME_DURATION_SECONDS
        gameState = GameState.Playing
    }

    companion object {
        private const val GAME_DURATION_SECONDS = 30
    }
}
