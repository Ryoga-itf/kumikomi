package dev.ryoga.napori.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import dev.ryoga.napori.R

@Composable
fun PizzaView(
    rotationAngleDeg: Float,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(318.dp)
            .clip(CircleShape)
            .background(Color(0xFFFFE8B8)),
        contentAlignment = Alignment.Center,
    ) {
        Image(
            painter = painterResource(id = R.drawable.pizza),
            contentDescription = "回転するピザ",
            modifier = Modifier
                .size(292.dp)
                .graphicsLayer {
                    rotationZ = rotationAngleDeg
                },
            contentScale = ContentScale.Fit,
        )
    }
}
