package com.example.composelearning.animation.book

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.composelearning.R

@Composable
@Preview(showSystemUi = true)
fun ShowBookCoverView() {
    MaterialTheme {
        BookComposeView()
    }
}

@Composable
fun BookComposeView() {

    var scrollPosition by remember {
        mutableStateOf(0f)
    }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {

        Column {
            Box(modifier = Modifier.wrapContentSize(), contentAlignment = Alignment.Center) {
                val modifier = Modifier.size(width = 150.dp, height = 200.dp)
                BookCoverView(modifier)
//        BookLeftView(modifier)
//        BookRightView(modifier)
            }
            Spacer(modifier = Modifier.height(15.dp))

            Slider(value = scrollPosition, onValueChange = {scrollPosition = it},
                valueRange = 0f..360f, modifier = Modifier.fillMaxSize())

        }


    }

}

@Composable
fun BookCoverView(modifier: Modifier) {
   Card (modifier = modifier) {
       Image(
           modifier = Modifier.shadow(elevation = 8.dp, shape = RoundedCornerShape(8.dp)),
           painter = painterResource(id = R.drawable.harry_potter_book),
           contentScale = ContentScale.FillBounds,
           contentDescription = "book cover image")
   }
}

@Composable
fun BookLeftView(modifier: Modifier) {
    Card (modifier = modifier) {
        Image(
            modifier = Modifier.shadow(elevation = 8.dp, shape = RoundedCornerShape(8.dp)),
            painter = painterResource(id = R.drawable.harry_potter_book),
            contentDescription = "book cover image")
    }
}

@Composable
fun BookRightView(modifier: Modifier) {
    Card (modifier = modifier) {
        Image(
            modifier = Modifier.shadow(elevation = 8.dp, shape = RoundedCornerShape(8.dp)),
            painter = painterResource(id = R.drawable.harry_potter_book),
            contentDescription = "book cover image")
    }
}