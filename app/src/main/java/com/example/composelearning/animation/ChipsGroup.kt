package com.example.composelearning.animation

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp


@Composable
fun Chips(chipText: String) {
    Row (
        modifier = Modifier
            .wrapContentSize()
            .border(
                width = 1.dp,
                brush = Brush.horizontalGradient(colors = listOf(Color.Green, Color.Blue)),
                shape = RoundedCornerShape(20.dp)
            )
            .padding(start = 10.dp, end = 10.dp, top = 5.dp, bottom = 5.dp)
    ) {
        Image(imageVector = Icons.Filled.Close, contentDescription = "closing")
        Spacer(modifier = Modifier.width(10.dp))
        Text(text = chipText)
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

@Preview(showSystemUi = true)
@Composable
fun ShowPreviewChips() {
    MaterialTheme {
        ListOfChips()
    }
}