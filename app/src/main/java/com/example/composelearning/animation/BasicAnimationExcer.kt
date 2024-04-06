package com.example.composelearning.animation

import androidx.compose.animation.core.animateIntOffsetAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp

@Composable
fun MoveViewByOffset() {

    var shouldMoveBox by remember { mutableStateOf(false) }

    val offset = animateIntOffsetAsState(
        targetValue = if (shouldMoveBox) IntOffset(150, 50) else IntOffset.Zero, label = ""
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
        targetValue = if (shouldMoveBox) IntOffset(150, 50) else IntOffset.Zero, label = ""
    )

    Box(modifier = Modifier.fillMaxSize()) {
        Column {
            Spacer(modifier = Modifier.size(10.dp))
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
                    .background(Color.Red)
                    .size(100.dp)
                    .clickable {
                        shouldMoveBox = !shouldMoveBox
                    }

            )

        }
    }

}


@Preview(showSystemUi = true)
@Composable
private fun ShowAnimationPreview() {
    MaterialTheme {
        MoveLayoutView()
    }
}


