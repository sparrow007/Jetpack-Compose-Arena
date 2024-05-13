package com.example.composelearning.animation.book

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.example.composelearning.R
import kotlin.math.absoluteValue
import kotlin.math.roundToInt

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
        mutableFloatStateOf(0f)
    }
    var size by remember { mutableStateOf(IntSize.Zero) }


    Surface(modifier = Modifier.fillMaxSize()) {
        Column (
            modifier = Modifier
                .fillMaxSize()
                .onGloballyPositioned {
                    size = it.size
                },
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            val offset = animateFloatAsState(targetValue =  ((150+75) * (scrollPosition.absoluteValue/180f)), label = "translation")


            Box (modifier = Modifier
                .wrapContentSize()
                .offset {
                    IntOffset(x = offset.value.toInt(), y = 0)
                }
                .wrapContentHeight(), contentAlignment = Alignment.Center) {
                BookContentView(Modifier.size(width = 150.dp, height = 200.dp))
                BookCoverView(
                    Modifier
                        .graphicsLayer {
                            transformOrigin = TransformOrigin(0f, 0f)
                            rotationY = scrollPosition
                            cameraDistance = 36f
                        }
                        .size(width = 150.dp, height = 200.dp)
                )

            }

            Spacer(modifier = Modifier.height(15.dp))

            Slider(value = scrollPosition, onValueChange = {scrollPosition = it},
                valueRange = -360f..360f) 
            Text(text = scrollPosition.toString())

        }
    }





//
//    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
//
//
//    }

}

@Composable
fun BookCoverView(modifier: Modifier) {
    Image(
        modifier = modifier
            .clip(shape = RoundedCornerShape(8.dp)),
        painter = painterResource(id = R.drawable.harry_potter_book),
        contentScale = ContentScale.FillBounds,
        contentDescription = "book cover image")
}

@Composable
fun BookContentView(modifier: Modifier) {
    Card(modifier = modifier, shape = RoundedCornerShape(8.dp)) {
        Column {
            Text(text = "Harry Potter and the Philosopher's Stone")
            Text(text = "J.K. Rowling")
            Text(text = "1997")
        }
    }
}

//@Composable
//fun BookLeftView(modifier: Modifier) {
//    Card (modifier = modifier) {
//        Image(
//            modifier = Modifier.shadow(elevation = 8.dp, shape = RoundedCornerShape(8.dp)).clip(
//                shape = RoundedCornerShape(8.dp)
//            ),
//            painter = painterResource(id = R.drawable.harry_potter_book),
//            contentDescription = "book cover image")
//    }
//}
//
//@Composable
//fun BookRightView(modifier: Modifier) {
//    Card (modifier = modifier) {
//        Image(
//            modifier = Modifier.shadow(elevation = 8.dp, shape = RoundedCornerShape(8.dp)),
//            painter = painterResource(id = R.drawable.harry_potter_book),
//            contentDescription = "book cover image")
//    }
//}