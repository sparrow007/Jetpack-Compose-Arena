package com.example.composelearning.animation.carousel.cardstack

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@Composable
@Preview
fun AdsCardShow() {
    val rotationAnimation = remember {
        Animatable(-10f)
    }
    val scope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .onLayoutTouch {
                if (it) {
                    scope.launch {
                        rotationAnimation.animateTo(
                            10f, spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessLow
                            )
                        )
                    }
                } else {
                    scope.launch {
                        rotationAnimation.animateTo(
                            -10f, spring(
                                dampingRatio = Spring.DampingRatioNoBouncy,
                                stiffness = Spring.StiffnessLow
                            )
                        )
                    }
                    Log.e("TAG", "AdsCardShow: Released")
                }
            }, contentAlignment = Alignment.Center
    ) {

        val elements = 4

        for (i in 0 until elements) {
            Card(
                modifier = Modifier
                    .graphicsLayer {
                        cameraDistance = 12 * density
                        transformOrigin = TransformOrigin(1f, 1f)
                        rotationZ = ((elements - (i + 1)) * (rotationAnimation.value)).toFloat()
                    }
                    .offset {
                        val position = (elements - (i + 1)) * 20
                        IntOffset(
                            ((elements - (i + 1)) * 30),
                            -position
                        )
                    },
                shape = RoundedCornerShape(12.dp),
            ) {
                Box(
                    modifier = Modifier
                        .width(120.dp)
                        .height(170.dp)
                        .background(Color.White)
                        .border(1.dp, Color.Gray, RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Column {

                        BoxWithColor()
                        BoxWithColor()
                        BoxWithColor()

                        Spacer(modifier = Modifier.height(10.dp))

                        BoxWithColor()
                        BoxWithColor()
                        BoxWithColor()

                        Spacer(modifier = Modifier.height(10.dp))

                        BoxWithColor()
                        BoxWithColor()
                        BoxWithColor()


                    }

                }
            }
        }
    }
}

@Composable
fun BoxWithColor() {
    Box(
        modifier = Modifier
            .padding(
                start = 10.dp,
                end = 10.dp, top = 5.dp
            )
            .fillMaxWidth()
            .height(5.dp)
            .background(getRandomColor())
    )
}

@SuppressLint("UnnecessaryComposedModifier", "ReturnFromAwaitPointerEventScope")
fun Modifier.onLayoutTouch(isPressed: (Boolean) -> Unit) = composed {
    val scope = rememberCoroutineScope()
    pointerInput(null) {
        scope.launch {
            awaitPointerEventScope {
                while (true) {
                    val pointerEvent = this.awaitPointerEvent()

                    when (pointerEvent.type) {
                        PointerEventType.Press -> {
                            // User has pressed the button
                            isPressed(true)
                            Log.e("TAG", "onLayoutTouch: Press")
                        }

                        PointerEventType.Release -> {
                            // User is no longer pressing the button
                            isPressed(false)
                            Log.e("TAG", "onLayoutTouch: Release")
                        }

                        else -> {
                            // Handle other
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun getRandomColor(): Color {
    val colors = listOf(
        Color(0xFFE57373),
        Color(0xFF81C784),
        Color(0xFF64B5F6),
        Color(0xFFFFD54F),
        Color(0xFF9575CD),
        Color(0xFF4DB6AC),
        Color(0xFFA1887F),
        Color(0xFF90A4AE),
        Color(0xFFE57373),
        Color(0xFF81C784),
        Color(0xFF64B5F6),
        Color(0xFFFFD54F),
        Color(0xFF9575CD),
        Color(0xFF4DB6AC),
        Color(0xFFA1887F),
        Color(0xFF90A4AE),
    )
    return colors.random()
}