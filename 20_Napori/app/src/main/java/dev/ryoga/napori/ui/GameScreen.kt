package dev.ryoga.napori.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import dev.ryoga.napori.game.GameLogic
import dev.ryoga.napori.game.GameScore
import dev.ryoga.napori.sensor.MotionSensorState
import dev.ryoga.napori.ui.components.PizzaView

@Composable
fun GameScreen(
    sensorState: MotionSensorState,
    gameScore: GameScore,
    timeRemainingSeconds: Int,
    hasGyroscope: Boolean,
    hasTiltSensor: Boolean,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Header(timeRemainingSeconds = timeRemainingSeconds)
        Spacer(modifier = Modifier.height(20.dp))
        ScoreRow(gameScore = gameScore)
        Spacer(modifier = Modifier.height(22.dp))
        PizzaStage(
            rotationAngleDeg = sensorState.rotationAngleDeg,
            modifier = Modifier.weight(1f),
        )
        Spacer(modifier = Modifier.height(24.dp))
        StatusPanel(
            sensorState = sensorState,
            hasGyroscope = hasGyroscope,
            hasTiltSensor = hasTiltSensor,
        )
    }
}

@Composable
private fun Header(
    timeRemainingSeconds: Int,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "Napori",
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.Black,
            color = Color(0xFF40220F),
        )
        Text(
            text = "$timeRemainingSeconds sec",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Black,
            color = Color(0xFFC83D20),
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun ScoreRow(
    gameScore: GameScore,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        ScoreMetric(
            label = "score",
            value = gameScore.score.toString(),
            modifier = Modifier.weight(1f),
        )
        ScoreMetric(
            label = "combo",
            value = "${gameScore.combo}",
            modifier = Modifier.weight(1f),
        )
        ScoreMetric(
            label = "best",
            value = "${gameScore.maxSpinSpeed.toInt()}",
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun ScoreMetric(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Color.White.copy(alpha = 0.78f))
            .padding(horizontal = 10.dp, vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = Color(0xFF856143),
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Black,
            color = Color(0xFF40220F),
            maxLines = 1,
        )
    }
}

@Composable
private fun PizzaStage(
    rotationAngleDeg: Float,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center,
    ) {
        PizzaView(rotationAngleDeg = rotationAngleDeg)
    }
}

@Composable
private fun StatusPanel(
    sensorState: MotionSensorState,
    hasGyroscope: Boolean,
    hasTiltSensor: Boolean,
    modifier: Modifier = Modifier,
) {
    val judgement = GameLogic.judgeSpin(sensorState.spinSpeedDegPerSec)
    val tiltJudgement = GameLogic.judgeTilt(sensorState.tiltDeg)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Color.White.copy(alpha = 0.84f))
            .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom,
        ) {
            Text(
                text = "spin",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF40220F),
            )
            Text(
                text = "${sensorState.spinSpeedDegPerSec.toInt()} deg/s",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Black,
                color = Color(0xFFC83D20),
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom,
        ) {
            Text(
                text = "tilt",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF40220F),
            )
            Text(
                text = if (hasTiltSensor) {
                    "${sensorState.tiltDeg.toInt()} deg"
                } else {
                    "--"
                },
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Black,
                color = Color(0xFF2E7D60),
            )
        }

        Text(
            text = if (hasGyroscope) {
                if (hasTiltSensor) {
                    "${judgement.label} / ${tiltJudgement.label}"
                } else {
                    judgement.label
                }
            } else {
                "この端末ではジャイロスコープを利用できません"
            },
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF5E3A20),
        )

        Text(
            text = "スマホをピザのように回せ",
            style = MaterialTheme.typography.bodySmall,
            color = Color(0xFF856143),
        )
    }
}
