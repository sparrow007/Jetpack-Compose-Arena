package com.example.composelearning.animation

import android.util.Log
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.calculateTargetValue
import androidx.compose.animation.rememberSplineBasedDecay
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

@Composable
fun BasicDrawerDesign() {
    var initialTranslationX = 300f
    var drawerWidth = with(LocalDensity.current) {
        300.dp.toPx()
    }
    val translationX = remember {
        Animatable(0f)
    }
    val coroutineScope = rememberCoroutineScope()
    val draggableState = rememberDraggableState {dragAmount ->
        // Handle drag amount, but for now we are not implementing it yet
        coroutineScope.launch {
            translationX.animateTo(dragAmount + translationX.value)
        }
    }

    val translationXValue = remember {
        derivedStateOf { translationX.value }
    }

    val decay = rememberSplineBasedDecay<Float>()
    translationX.updateBounds(lowerBound = 0f, upperBound = drawerWidth)
    Surface(modifier = Modifier
        .fillMaxSize()
    ) {

        Box(modifier = Modifier
            .background(Color.Green)
            .draggable(draggableState, Orientation.Horizontal, onDragStopped = { velocity: Float ->
                val targetOffsetX = decay.calculateTargetValue(
                    initialValue = translationX.value,
                    initialVelocity = velocity
                )

                val actualTargetX = if (targetOffsetX > drawerWidth * 0.5) {
                    drawerWidth
                } else {
                    0f
                }
                val targetDifference = (actualTargetX - targetOffsetX)
                coroutineScope.launch {
                    translationX.animateDecay(
                        initialVelocity = velocity,
                        animationSpec = decay
                    )
                }

            }),
            ) {
            Button(onClick = { /*TODO*/ }, modifier = Modifier
                .size(100.dp)
                .background(Color.Red)
                .graphicsLayer {
                    this.translationX = translationXValue.value
                },
                shape = RectangleShape

            ) {
                Text(text = "This is cool")
            }
        }


    }
}

@Preview
@Composable
fun ShowDraggablePreview() {
    MaterialTheme {
        BasicDrawerDesign()
    }
}