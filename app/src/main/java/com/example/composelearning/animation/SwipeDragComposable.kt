package com.example.composelearning.animation

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.calculateTargetValue
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.splineBasedDecay
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.gestures.verticalDrag
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue
import kotlin.math.min
import kotlin.math.roundToInt

@Composable
fun ShowCardInStack() {
    val listOfCard = remember {
        mutableStateOf(
            arrayListOf(
                Color.Red,
                Color.Blue,
                Color.Green,
                Color.Yellow
            ).reversed()
        )
    }

    Box(modifier = Modifier
        .fillMaxSize()
        .padding(bottom = 20.dp), contentAlignment = Alignment.BottomCenter) {
        listOfCard.value.forEachIndexed { index, color ->
            key(color) {
                //currently using the formula for constant list but we can use linear conversion formula for dynamic list
                //
                val predictiveScale = 1f - (5) * 0.05f
                val animateScale = animateFloatAsState(targetValue = 1f - (listOfCard.value.size - index) * 0.09f,
                    label = "scale animation"
                )
                CardWithColors(
                    modifier = Modifier
                        .swipeToDismissXAxis(predictiveScale) {
                            //Update the list for swipe values
                            listOfCard.value = listOf(color) + (listOfCard.value - color)
                        }
                        .offset {
                            IntOffset(0, (listOfCard.value.size - index) * -12.dp.roundToPx())
                        }
                        .graphicsLayer {
                            scaleX = animateScale.value
                            scaleY = animateScale.value
                        },
                    color = color
                )
            }
        }
    }

}

@Composable
fun CardWithColors(modifier: Modifier,color: Color) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(300.dp)
            .padding(40.dp),
        colors =   CardDefaults.cardColors(
            containerColor = color, //Card background color
            //contentColor = Color.White  //Card content color,e.g.text
        ),
        elevation = CardDefaults.elevatedCardElevation(
            defaultElevation = 8.dp
        )
    ) {
        Text(text = "This is Me")
    }
}

private fun Modifier.swipeToDismissXAxis(
    scale: Float,
    onDissmiss: () -> Unit
): Modifier = composed {
    var offsetY = remember {
        Animatable(0f)
    }
    val animationDuration = 700
    var rotationAnim = remember {
        Animatable(0f)
    }
    val scaleAnim = remember {
        Animatable(1f)
    }
    var clearHuddle by remember {
        mutableStateOf(false)
    }

    pointerInput(Unit) {
        val splineDecay = splineBasedDecay<Float>(this)
        coroutineScope {
            while (true) {
                offsetY.stop()

                val velocityTracker = VelocityTracker()
                awaitPointerEventScope {
                    verticalDrag(this.awaitFirstDown().id) { change ->
                        val targetOffset = offsetY.value + change.positionChange().y
                        launch {
                            offsetY.snapTo(targetOffset)
                            rotationAnim.snapTo(0f)
                        }
                        velocityTracker.addPosition(change.uptimeMillis, change.position)
                        if (change.positionChange() != Offset.Zero) change.consume()

                    }
                    val velocity = velocityTracker.calculateVelocity().y
                    val targetOffsetY = splineDecay.calculateTargetValue(offsetY.value, velocity)
                    val flingDistance = size.height * 2f

                    val rotationToFling = min(
                        180f * (targetOffsetY.absoluteValue / size.height).roundToInt(),
                        180f * 1
                    )

                    val rotationOvershoot = rotationToFling + 12
                    val easeInOutEasing = CubicBezierEasing(0.42f, 0.0f, 0.58f, 1.0f)
                    launch {
                      //  scaleAnim.snapTo(scale)
                        val animJobs = listOf(
                            launch {
                                rotationAnim.animateTo(
                                    targetValue = 360f,
                                    //initialVelocity = velocity,
                                    animationSpec = keyframes {
                                        durationMillis = animationDuration
                                        0f at 0
                                        180f at (animationDuration / 2 - 50) with LinearEasing
                                        300f at (animationDuration - animationDuration / 3) with LinearEasing
                                        360f at animationDuration
                                    }
                                ) {

                                }

                                //  rotationAnim.snapTo(0f)
                            },
                            launch {
                                offsetY.animateTo(
                                    targetValue = 0f,
                                    animationSpec = keyframes {
                                        durationMillis = animationDuration
                                        -flingDistance at (animationDuration / 2) with LinearEasing
                                        -(size.height + 12f) at (animationDuration - animationDuration / 3) with LinearEasing
                                        0f at animationDuration
                                    }
                                ) {
                                    if (value <= size.height * 2 && !clearHuddle) {
                                        onDissmiss.invoke()
                                        clearHuddle = true

                                    }
                                }

                            }

                        )
                        animJobs.joinAll()
                        //scaleAnim.animateTo(1f)
                        clearHuddle = false
                    }
                }
            }
        }
    }
        .offset {
            IntOffset(0, offsetY.value.toInt())
        }
        .graphicsLayer {
            // transformOrigin = TransformOrigin.Center
            //   this.transformOrigin = TransformOrigin(0f, 0f)
            rotationX = rotationAnim.value
            scaleX =scaleAnim.value
            scaleY = scaleAnim.value

        }

}

private fun Modifier.swipeToDissmis(
    onDissmiss: () -> Unit
): Modifier = composed {
    val offset = remember {
       Animatable(0f)
    }
    val rotationAnim = remember {
        Animatable(0f)
    }

    var clearHuddle by remember {
        mutableStateOf(false)
    }

    pointerInput(this) {
        val decay = splineBasedDecay<Float>(this)
        coroutineScope {
            while (true) {
                val pointerId = awaitPointerEventScope { awaitFirstDown().id }
                offset.stop() // now user points down the animation we will stop the ongoing animation
                rotationAnim.stop()
                //Prepare for the velocity track and fling
                val velocityTracker = VelocityTracker()
                awaitPointerEventScope {
                    verticalDrag(pointerId) { change ->
                        val dragOffset = offset.value + change.positionChange().y
                        launch {
                            offset.snapTo(dragOffset)
                            rotationAnim.snapTo(1f)
                        }
                        velocityTracker.addPosition(change.uptimeMillis, change.position)
                        if (change.positionChange() != Offset.Zero) change.consume()
                        //Three things we did in this block
                        /***
                         * 1. calculate the drag of horizontal of the user
                         * 2. take the ui to user finger position and snapTo
                         * 3. calculate the velocity of user's drag
                         */
                    }
                }

                val velocity = velocityTracker.calculateVelocity().y
                val targetOffset = decay.calculateTargetValue(offset.value, velocity)
                val maxDistanceTravel = (size.height * 3f)
                val distanceFling =
                    Math.min(maxDistanceTravel, targetOffset.absoluteValue + size.height.toFloat())
                val animationDuration = 500
                val easeInOutEasing = CubicBezierEasing(0.42f, 0.0f, 0.58f, 1.0f)

                val rotationFling = 360f * 3
                val overShoot = rotationFling + 12f

                val animJobs = listOf(
                    launch {
                        rotationAnim.animateTo(
                            targetValue = rotationFling,
                            initialVelocity = velocity,
                            animationSpec = keyframes {
                                durationMillis = animationDuration
                                0f at 0 with easeInOutEasing
                                overShoot at animationDuration - 50 with LinearOutSlowInEasing
                                rotationFling at animationDuration
                            }
                        )
                        rotationAnim.snapTo(0f)
                    },
                    launch {
                        offset.animateTo(
                            targetValue = 0f,
                            animationSpec = keyframes {
                                durationMillis = animationDuration
                                -distanceFling at (animationDuration / 2) with LinearEasing
                                // 40f at animationDuration - 80
                            }
                        ) {
                            if (value <= -size.height * 2 && !clearHuddle) {
                                onDissmiss()
                                clearHuddle = true
                            }
                        }
                    }

                )

                animJobs.joinAll()
                clearHuddle = false
            }
        }

    }
        .offset {
            IntOffset(0, offset.value.toInt())
        }
        .graphicsLayer {
            transformOrigin = TransformOrigin.Center
            rotationZ = rotationAnim.value
        }

}
@Composable
@Preview
fun ShowSwipePreview() {
    MaterialTheme {
       Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
           ShowCardInStack()
       }
    }
}



@Composable
fun SwipeDragComposable() {
    var offsetX = remember { mutableStateOf(0f) }
    Text(
        text = "Swipe me here",
        modifier = Modifier
            .offset { IntOffset(offsetX.value.roundToInt(), 0) }
            .draggable(
                orientation = Orientation.Horizontal,
                state = rememberDraggableState {
                    offsetX.value += it
                }
            )
    )
}

@Composable
fun ShowTextDragGesture() {

    var offsetX = remember {
        mutableStateOf(0f)
    }

    var offsetY = remember {
        mutableStateOf(0f)
    }

    Text(
        text= "Swipe me here",
        modifier = Modifier
            .offset {
                IntOffset(offsetX.value.roundToInt(), offsetY.value.roundToInt())
            }
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    offsetX.value += dragAmount.x
                    offsetY.value += dragAmount.y
                }
            }
    )
}