package com.example.composelearning.animation

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateIntOffsetAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.layout
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp

@Composable
fun MoveViewByOffset() {

    var shouldMoveBox by remember { mutableStateOf(false) }

    val offset = animateIntOffsetAsState(
        targetValue = if (shouldMoveBox) IntOffset(150, 150) else IntOffset.Zero, label = ""
    )

    Box (modifier = Modifier.fillMaxSize()){
        Box(
            modifier = Modifier
                .offset {
                    offset.value
                }
                .background(Color.Green)
                .size(100.dp)
                .clickable {
                    shouldMoveBox = !shouldMoveBox
                }

        )
    }

}


@Composable
fun MoveLayoutView() {
    var shouldMoveBox by remember { mutableStateOf(false) }

    val offset = animateIntOffsetAsState(
        targetValue = if (shouldMoveBox) IntOffset(150, 150) else IntOffset.Zero, label = ""
    )

    Box(modifier = Modifier.fillMaxSize()) {
        Column {

            Spacer(modifier = Modifier.size(10.dp))

            Box(
                modifier = Modifier
                    .background(Color.Blue)
                    .size(100.dp)
                    .clickable {
                        shouldMoveBox = !shouldMoveBox
                    }

            )
            Spacer(modifier = Modifier.size(10.dp))

            Box(
                modifier = Modifier
                    .layout { measurable, contraints ->
                        val placeable = measurable.measure(contraints)
                        layout(
                            placeable.width + offset.value.x,
                            placeable.height + offset.value.y,
                        ) {
                            placeable.place(offset.value)
                        }
                    }
                    .background(Color.Green)
                    .size(100.dp)
                    .clickable {
                        shouldMoveBox = !shouldMoveBox
                    }

            )


            Spacer(modifier = Modifier.size(10.dp))

            Box(
                modifier = Modifier
                    .background(Color.Red)
                    .size(100.dp)
                    .clickable {
                        shouldMoveBox = !shouldMoveBox
                    }

            )

        }
    }

}

@Composable
fun AnimateShadow() {

    val mutableSource = remember {
        MutableInteractionSource()
    }

    val pressedState = mutableSource.collectIsPressedAsState()

    val animateShadowState = animateDpAsState(
        targetValue = if (pressedState.value) 150.dp else 8.dp,
        label = "animating shadow")

    Box(
        modifier = Modifier
            .size(100.dp)
            .graphicsLayer {
                shadowElevation = animateShadowState.value.toPx()
            }
            .background(Color.Green)
            .clickable(interactionSource = mutableSource, indication = null) {
                // do nothing
            }

    )

}

//Will start the test animation for compose



@Preview(showSystemUi = true)
@Composable
private fun ShowAnimationPreview() {
    MaterialTheme {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            AnimateShadow()
        }

    }
}


