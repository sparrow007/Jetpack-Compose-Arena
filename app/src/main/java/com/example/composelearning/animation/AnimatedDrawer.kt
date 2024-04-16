package com.example.composelearning.animation

import android.util.Log
import androidx.compose.animation.core.calculateTargetValue
import androidx.compose.animation.rememberSplineBasedDecay
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun BasicDrawerDesign() {
    var initialTranslationX = 100f
    val draggableState = rememberDraggableState {dragAmount ->
        // Handle drag amount, but for now we are not implementing it yet
        print("when the draggable state is on delta")
    }
    val decay = rememberSplineBasedDecay<Float>()
    Surface(modifier = Modifier
        .fillMaxSize()
        .draggable(draggableState, Orientation.Horizontal, onDragStopped = { velocity: Float ->
            Log.e("Fling Animation", "Print the velocity = $velocity")
            val targetOffsetX = decay.calculateTargetValue(initialValue = initialTranslationX, initialVelocity = velocity)
        })
    ) {

    }
}
@Preview
@Composable
fun ShowDraggablePreview() {
    MaterialTheme {
        BasicDrawerDesign()
    }
}