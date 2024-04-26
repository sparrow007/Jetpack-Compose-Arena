package com.example.composelearning.selector

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.composelearning.animation.animationTime
import com.example.composelearning.ui.theme.fontFamily
import kotlinx.coroutines.delay

const val textAnimTime = 300
enum class Difficulty {
    Easy,
    Normal,
    Hard
}

@Composable
fun SelectorButton(
    rotationX: Float,
    mode: Difficulty,
    currentMode: Difficulty,
    callback: (rotationX: Float, scale: Float) -> Unit
) {

    val fontWe = if (mode == currentMode) FontWeight.Bold.weight else FontWeight.Normal.weight

    val colorAnimate by animateColorAsState(
        targetValue = if (mode == currentMode) Color.Black else Color.Gray,
        animationSpec = tween(durationMillis = textAnimTime), label = "color animation"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
    ) {
        Text(
            text = mode.name,
            style = TextStyle(
                fontFamily = fontFamily, fontWeight = FontWeight(fontWe), fontSize = 22.sp
            ),
            color = colorAnimate,
            modifier = Modifier.pointerInput(Unit) {
                detectTapGestures(
                    onPress = { offset ->

                        callback(rotationX, 0.8f)
                        delay(animationTime.toLong())
                        tryAwaitRelease()
                        callback(0f, 1f)
                    }
                )
            })
    }
}
