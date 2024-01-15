package com.example.composelearning.animation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp


@Composable
fun Chips(chipText: String) {
    var isVisible by remember { mutableStateOf(true) }
  //  isVisible = visible
    AnimatedVisibility(
        visible = isVisible,

        ) {
        Row (
            modifier = Modifier
                .wrapContentSize()
                .border(
                    width = 1.dp,
                    brush = Brush.horizontalGradient(colors = listOf(Color.Green, Color.Blue)),
                    shape = RoundedCornerShape(20.dp)
                )
                .padding(start = 10.dp, end = 10.dp, top = 5.dp, bottom = 5.dp)
                .clickable { isVisible = !isVisible }
        ) {
            Image(imageVector = Icons.Filled.Close, contentDescription = "closing")
            Spacer(modifier = Modifier.width(10.dp))
            Text(text = chipText)
        }
    }
}

@Composable
fun AnimateOneChip() {
    var isVisible by remember { mutableStateOf(true) }

    Row {
        Image(imageVector = Icons.Filled.Close, contentDescription = "close button", modifier = Modifier.clickable {
            isVisible = !isVisible
        })

        Spacer(modifier = Modifier.width(10.dp))
        AnimatedVisibility(visible = isVisible) {
            ListOfChips()
        }
    }
}

@Composable
fun ListOfChips() {
    val listOfChips = listOf("Hollywood", "Bollywood", "Action", "Dance")
    LazyRow {
        items(listOfChips) {
            Spacer(modifier = Modifier.width(10.dp))
            Chips(chipText = it)
        }
    }
}

@Composable
fun SelectableChip(chipsText: List<String>) {
    var selectionPosition by remember { mutableStateOf(-1) }
    var isSelected by remember { mutableStateOf(true) }
    Row(
        modifier = Modifier
            .height(IntrinsicSize.Min)
            .border(
                width = 1.dp,
                brush = Brush.horizontalGradient(colors = listOf(Color.Green, Color.Blue)),
                shape = RoundedCornerShape(20.dp)
            )
            .padding(10.dp)

    ) {
        chipsText.forEachIndexed { index, s ->
            AnimatedVisibility(visible = isSelected ||  selectionPosition == index) {
                Text(
                    text = s,
                    color = if (isSelected) Color.Black else Color.Blue,
                    modifier = Modifier
                        .padding(horizontal = 10.dp)
                        .clickable {
                            isSelected = !isSelected
                            selectionPosition = index
                        }
                )
            }
            AnimatedVisibility(visible = isSelected || selectionPosition == index) {
                if (index < chipsText.size - 1 && isSelected) {
                    Box(
                        modifier = Modifier
                            .width(2.dp)
                            .fillMaxHeight()
                            .background(
                                color = Color.Green,
                                shape = RoundedCornerShape(1.dp)
                            )
                            .padding(10.dp)
                    )
                }
            }

        }
    }
}

@Preview(showSystemUi = true)
@Composable
fun ShowPreviewChips() {
    MaterialTheme {
        val listOfChips = listOf("Hollywood", "Bollywood", "Action", "Dance")

        SelectableChip(chipsText = listOfChips)
    }
}