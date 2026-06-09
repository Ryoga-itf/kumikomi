package dev.ryoga.napori.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import dev.ryoga.napori.game.GameScore

@Composable
fun ResultScreen(
    gameScore: GameScore,
    onRetryGame: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = "Result",
            style = MaterialTheme.typography.displayMedium,
            fontWeight = FontWeight.Black,
            color = Color(0xFF40220F),
        )
        Text(
            text = resultRank(gameScore),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Black,
            color = Color(0xFFC83D20),
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(30.dp))
        ResultMetrics(gameScore = gameScore)
        Spacer(modifier = Modifier.height(34.dp))

        Button(
            onClick = onRetryGame,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFC83D20),
                contentColor = Color.White,
            ),
        ) {
            Text(
                text = "Retry",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

@Composable
private fun ResultMetrics(
    gameScore: GameScore,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        ResultMetric(label = "final score", value = gameScore.score.toString())
        ResultMetric(label = "max combo", value = gameScore.maxCombo.toString())
        ResultMetric(label = "max speed", value = "${gameScore.maxSpinSpeed.toInt()} deg/s")
    }
}

@Composable
private fun ResultMetric(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Color.White.copy(alpha = 0.82f))
            .padding(horizontal = 18.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = Color(0xFF856143),
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Black,
            color = Color(0xFF40220F),
        )
    }
}

private fun resultRank(gameScore: GameScore): String = when {
    gameScore.score >= 900 -> "Pizzaiolo!"
    gameScore.score >= 500 -> "Great!"
    gameScore.score >= 180 -> "Good!"
    else -> "Spin more！"
}
