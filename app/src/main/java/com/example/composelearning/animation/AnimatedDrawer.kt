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
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun BasicDrawerDesign() {
    var initialTranslationX = 300f
    var drawerWidth = 300f
    val draggableState = rememberDraggableState {dragAmount ->
        // Handle drag amount, but for now we are not implementing it yet
        print("when the draggable state is on delta")
    }
    val translationX = remember {
        Animatable(0f)
    }
    val decay = rememberSplineBasedDecay<Float>()
    translationX.updateBounds(lowerBound = 0f, upperBound = drawerWidth)
    Surface(modifier = Modifier
        .fillMaxSize()
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

            Log.e(
                "Fling Animation",
                "Print the targetOffest = $targetOffsetX, and velocity = $velocity, and difference = $targetDifference"
            )

        })
    ) {

        Box(modifier = Modifier
            .size(200.dp)
            .background(Color.Blue)
            .graphicsLayer {
                this.translationX = translationX.value
            })

    }
}

@Preview
@Composable
fun ShowDraggablePreview() {
    MaterialTheme {
        BasicDrawerDesign()
    }
}