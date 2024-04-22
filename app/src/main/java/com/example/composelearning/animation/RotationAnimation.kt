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
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
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
fun ViewRoatation(rotationX: Float, rotationY: Float, rotationZ: Float) {
        Box(modifier = Modifier
            .size(200.dp)
            .graphicsLayer {
                this.rotationX = rotationX
                this.rotationZ = rotationZ
                this.rotationY = rotationY
            }
            .background(Color.Blue), contentAlignment = Alignment.Center, content = {
            Text(text = "Hello Wor")
        })
}

@Composable
fun ShowSliderToExperiment() {
    var rotationX by remember { mutableStateOf(0f) }
    var rotationY by remember { mutableStateOf(0f) }
    var rotationZ by remember { mutableStateOf(0f) }

    Column {
        ViewRoatation(rotationX, rotationY, rotationZ)

        Spacer(modifier = Modifier.height(20.dp))

        Slider(value =rotationX, onValueChange = { it -> rotationX = it })
        Spacer(modifier = Modifier.height(10.dp))

        Slider(value =rotationY, onValueChange = { it -> rotationY = it })
        Spacer(modifier = Modifier.height(10.dp))

        Slider(value =rotationZ, onValueChange = { it -> rotationZ = it })
    }

    Button(onClick = {
        rotationX += 10f
        rotationY += 10f
        rotationZ += 10f
    }) {
        Text(text = "Rotate")
    }

}



@Composable
@Preview
fun ShowRotationPreview() {
    MaterialTheme {
       ShowSliderToExperiment()
    }
}