package com.example.composelearning.animation

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.calculateTargetValue
import androidx.compose.animation.splineBasedDecay
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.horizontalDrag
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun ShowCardInStack() {

    val listOfCard = arrayListOf(
        Color.Red,
        Color.Blue,
        Color.Green,
        Color.Yellow,
        Color.Magenta,
        Color.Cyan
    )

    Box(modifier = Modifier.wrapContentSize(), contentAlignment = Alignment.Center) {
        listOfCard.reversed().forEachIndexed { index, color ->
            CardWithColors(
                modifier = Modifier
                    .offset {
                        IntOffset(
                            x = 0,
                            y = index * 20
                        )
                    },
                color = color
            )
        }
    }

}

@Composable
fun CardWithColors(modifier: Modifier,color: Color) {
    Card(
        modifier = modifier
            .size(200.dp)
            .swipeToDissmis {

            },
        colors =   CardDefaults.cardColors(
            containerColor = color, //Card background color
            //contentColor = Color.White  //Card content color,e.g.text
        ),
        elevation = CardDefaults.elevatedCardElevation(
            defaultElevation = 8.dp
        )
    ) {

    }
}

private fun Modifier.swipeToDissmis(
    onDissmiss: () -> Unit
): Modifier = composed {
    val offset = remember {
       Animatable(0f)
    }

    pointerInput(this) {
        this.density
        val decay = splineBasedDecay<Float>(this)
        coroutineScope {
            while(true) {

                val pointerId = awaitPointerEventScope {awaitFirstDown().id }
                offset.stop() // now user points down the animation we will stop the ongoing animation
                //Prepare for the velocity track and fling
                val velocityTracker = VelocityTracker()
                awaitPointerEventScope {
                    horizontalDrag(pointerId) {
                        val dragOffset = offset.value + it.position.x
                        launch {
                            offset.snapTo(dragOffset)
                        }
                        velocityTracker.addPosition(it.uptimeMillis, it.position)
                        it.consume()
                        //Three things we did in this block
                        /***
                         * 1. calculate the drag of horizontal of the user
                         * 2. take the ui to user finger position and snapTo
                         * 3. calculate the velocity of user's drag
                         */
                    }
                }

                val velocity  = velocityTracker.calculateVelocity().x
                val targetOffset = decay.calculateTargetValue(offset.value, velocity)

                //Now we checks or add the boundaries for the animation
                offset.updateBounds(
                    -size.width.toFloat(),
                    size.width.toFloat()
                )

                if (targetOffset <= size.width.toFloat()) {
                    offset.animateTo(targetValue = 0f, initialVelocity = velocity)

                } else {
                    //Now it can go out of boundries and we
                    offset.animateDecay(velocity, decay)
                    onDissmiss()
                }
            }
        }

    }.offset {
        IntOffset(offset.value.toInt(), 0)
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