package com.example.composelearning.animation

import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.draggable
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
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
        modifier = modifier.size(200.dp),
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