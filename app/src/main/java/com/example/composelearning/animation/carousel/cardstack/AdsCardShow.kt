package com.example.composelearning.animation.carousel.cardstack

import android.annotation.SuppressLint
import android.util.Log
import android.view.MotionEvent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.pointerInteropFilter
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
                        .border(1.dp, Color.Gray, RoundedCornerShape(12.dp))
                ) {
                }
            }
        }
    }
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


@SuppressLint("ModifierFactoryUnreferencedReceiver")
@OptIn(ExperimentalComposeUiApi::class)
fun Modifier.cardLongClick(position: Int, totalSize: Int) = composed {
    val xPosition = remember {
        (totalSize - (position + 1)) * 30
    }
    val yPosition = remember {
        (totalSize - (position + 1)) * 20
    }

    var rotationAnimation = remember {
        Animatable(0f)
    }
    var longPressActive by remember { mutableStateOf(false) }


    val scope = rememberCoroutineScope()
    pointerInput(Unit) {
        detectTapGestures(
            onPress = {
                tryAwaitRelease()
                scope.launch {
                    val rotation = ((totalSize - (position + 1)) * -10).toFloat()
                    rotationAnimation.animateTo(
                        rotation, spring(
                            dampingRatio = Spring.DampingRatioNoBouncy,
                            stiffness = Spring.StiffnessMedium
                        )
                    )
                }
                longPressActive = false
            },
            onLongPress = {
                scope.launch {
                    val rotation = ((totalSize - (position + 1)) * 10).toFloat()
                    rotationAnimation.animateTo(
                        rotation, spring(
                            dampingRatio = Spring.DampingRatioNoBouncy,
                            stiffness = Spring.StiffnessMedium
                        )
                    )
                }
                longPressActive = true
            }
        )
    }
        .pointerInteropFilter {
            when (it.action) {
                MotionEvent.ACTION_DOWN -> {
                    // User has pressed the button
                    scope.launch {
                        val rotation = ((totalSize - (position + 1)) * 10).toFloat()
                        rotationAnimation.animateTo(
                            rotation, spring(
                                dampingRatio = Spring.DampingRatioNoBouncy,
                                stiffness = Spring.StiffnessMedium
                            )
                        )
                    }

                }

                MotionEvent.ACTION_UP -> {
                    // User is no longer pressing the button
                    scope.launch {
                        val rotation = ((totalSize - (position + 1)) * -10).toFloat()
                        rotationAnimation.animateTo(
                            rotation, spring(
                                dampingRatio = Spring.DampingRatioNoBouncy,
                                stiffness = Spring.StiffnessMedium
                            )
                        )
                    }
                }

                else -> false
            }
            true
        }
        .offset {
            IntOffset(
                xPosition,
                -yPosition
            )
        }
        .graphicsLayer {
            cameraDistance = 12 * density
            transformOrigin = TransformOrigin(1f, 1f)
            rotationZ = rotationAnimation.value
        }
}


//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun CardsView(modifier: Modifier = Modifier) {
//    Card(
//        modifier = modifier
//            .width(120.dp)
//            .height(170.dp)
//            .border(1.dp, Color.Gray, RoundedCornerShape(12.dp)),
//        onClick = { /*TODO*/ }) {
//
//
//    }
//}
