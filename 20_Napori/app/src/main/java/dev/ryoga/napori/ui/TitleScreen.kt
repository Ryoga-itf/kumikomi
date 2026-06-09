package dev.ryoga.napori.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import dev.ryoga.napori.sensor.MotionSensorState
import dev.ryoga.napori.ui.components.PizzaView

@Composable
fun TitleScreen(
    sensorState: MotionSensorState,
    hasGyroscope: Boolean,
    onStartGame: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = "Napori",
            style = MaterialTheme.typography.displayLarge,
            fontWeight = FontWeight.Black,
            color = Color(0xFF40220F),
        )

        Spacer(modifier = Modifier.height(36.dp))
        PizzaView(rotationAngleDeg = sensorState.rotationAngleDeg)
        Spacer(modifier = Modifier.height(36.dp))

        Button(
            onClick = onStartGame,
            enabled = hasGyroscope,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFC83D20),
                contentColor = Color.White,
            ),
        ) {
            Text(
                text = "Start",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
        }

        Spacer(modifier = Modifier.height(18.dp))
        Text(
            text = if (hasGyroscope) {
                "スマホをピザのように回せ"
            } else {
                "この端末ではジャイロスコープを利用できません。"
            },
            modifier = Modifier.fillMaxWidth(),
            style = MaterialTheme.typography.bodySmall,
            color = Color(0xFF856143),
            textAlign = TextAlign.Center,
        )
    }
}
