package com.example.composelearning.animation.book

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.example.composelearning.R
import com.example.composelearning.ui.theme.fontFamily
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue
import kotlin.math.ceil
import kotlin.math.roundToInt


val width = 150
val height = 200

@Composable
@Preview(showSystemUi = true)
fun ShowBookCoverView() {
    MaterialTheme {
        BookHeaderLayout()
    }
}

@Composable
fun ShowCarouselLayoutView() {
    var coloList by remember {
        mutableStateOf(listOf(Color.Red, Color.Green, Color.Blue, Color.Yellow, Color.Magenta))
    }
    val middleElement = ceil(((coloList.size - 1) / 2f)).toInt()
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        coloList.forEachIndexed { index, color ->
            key(color) {
                //Show the cards with different scale
                val scale = 1f - (0.1f * (index - middleElement).absoluteValue)

                Card(modifier = Modifier
                    .offset {
                        val xOffset = ((index - middleElement) * 100).dp
                            .toPx()
                            .toInt()
                        IntOffset(xOffset, 0)
                    }
                    .graphicsLayer {
                        this.rotationZ
                        scaleX = scale
                        scaleY = scale
                    }
                    .zIndex(scale)
                    .size(200.dp, 200.dp)
                    .padding(10.dp)
                    .shadow(10.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = color, //Card background color
                        //contentColor = Color.White  //Card content color,e.g.text
                    ),
                    elevation = CardDefaults.elevatedCardElevation(
                        defaultElevation = 8.dp
                    )
                ) {
                    Text(
                        text = "Card ${index + 1}",
                        style = TextStyle(fontWeight = FontWeight.Bold, fontSize = 20.sp)
                    )
                }
            }
        }
    }

}

@Composable
fun BookHeaderLayout() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()

    ) {
        Text(
            text = "Book Show",
            style = TextStyle(fontWeight = FontWeight.Bold, fontSize = 28.sp),
            modifier = Modifier.padding(vertical = 50.dp, horizontal = 20.dp)
        )
        BookComposeView()
    }

}

@Composable
fun BookComposeView() {

    var scrollPosition by remember {
        mutableFloatStateOf(0f)
    }
    var size by remember { mutableStateOf(IntSize.Zero) }
    val density = LocalDensity.current

    var checked by remember {
        mutableStateOf(false)
    }
    val rotationYAnim = remember {
        Animatable(0f)
    }

    val xOffsetAnim = remember {
       Animatable(0f)
    }

    val coroutineScope = rememberCoroutineScope()

    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .onGloballyPositioned {
                    size = it.size
                },
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            if (checked) {
                ShowBookBodyView(rotationYAnim.value, xOffsetAnim.value)

            } else {

                val offset = animateFloatAsState(
                    targetValue = ((width / 2) * (scrollPosition.absoluteValue / 180f)),
                    label = "translation"
                )

                ShowBookBodyView(scrollPosition, offset.value)
            }


            Spacer(modifier = Modifier.height(15.dp))

            Slider(
                value = scrollPosition, onValueChange = { scrollPosition = it },
                valueRange = 0f..180f,
                modifier = Modifier.padding(horizontal = 20.dp)
            )
            Spacer(modifier = Modifier.height(15.dp))


            Switch(
                checked = checked,
                onCheckedChange = {

                    if (it) {
                        checked = true
                        coroutineScope.launch {
                            launch {
                                rotationYAnim.animateTo(
                                    targetValue = 180f, animationSpec = keyframes {
                                        durationMillis = 1000
                                        0f at 0 with LinearEasing
                                        180f at 1000

                                    }
                                )
                            }

                            launch {
                                xOffsetAnim.animateTo(
                                    targetValue = ((width / 2f)),
                                    animationSpec = keyframes {
                                        durationMillis = 1000
                                        0f at 0 with LinearEasing
                                        ((width / 2f)) at 1000
                                    }
                                )
                            }
                        }
                    } else {
                        coroutineScope.launch {
                          val list = listOf(
                              launch {
                                  rotationYAnim.animateTo(
                                      targetValue = 0f, animationSpec = keyframes {
                                          durationMillis = 1000
                                          180f at 0 with LinearEasing
                                          0f at 1000
                                      }
                                  )
                              },
                                      launch {
                                  xOffsetAnim.animateTo(
                                      targetValue = 0f,
                                      animationSpec = keyframes {
                                          durationMillis = 1000
                                          ((width / 2f)) at 0 with LinearEasing
                                          0f at 1000
                                      }
                                  )
                              }
                          )
                            list.joinAll()
                            checked = false
                        }
                    }
                },
            )

        }
    }

}

@Composable
fun ShowBookBodyView(scrollPosition: Float, offset: Float) {
    Box(modifier = Modifier
        .wrapContentSize()
        .offset {
            IntOffset(
                x = offset.dp
                    .toPx()
                    .toInt(), y = 0
            )
        }
        .wrapContentHeight(), contentAlignment = Alignment.Center) {
        BookContentView(Modifier.size(width = width.dp, height = height.dp))


        if (scrollPosition.absoluteValue >= 90f) {
            BookAuthorView(
                Modifier
                    .graphicsLayer {
                        transformOrigin = TransformOrigin(0f, 0f)
                        rotationY = -scrollPosition
                        cameraDistance = 36f
                    }
                    .size(width = width.dp, height = height.dp)
            )

        } else {
            BookCoverView(
                Modifier
                    .graphicsLayer {
                        transformOrigin = TransformOrigin(0f, 0f)
                        rotationY = -scrollPosition
                        cameraDistance = 36f
                    }
                    .size(width = width.dp, height = height.dp)
            )
        }

    }
}

@Composable
fun BookCoverView(modifier: Modifier) {
    Image(
        modifier = modifier
            .clip(shape = RoundedCornerShape(8.dp)),
        painter = painterResource(id = R.drawable.harry_potter_book),
        contentScale = ContentScale.FillBounds,
        contentDescription = "book cover image"
    )
}

@Composable
fun BookContentView(modifier: Modifier) {
    Card(
        modifier = modifier, shape = RoundedCornerShape(8.dp), colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 5.dp)
    ) {
        Column(modifier = Modifier.padding(5.dp)) {
            Spacer(modifier = Modifier.height(5.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(), contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Description",
                    style = TextStyle(
                        fontWeight = FontWeight.Bold,
                        fontFamily = fontFamily,
                        fontSize = 18.sp
                    )
                )
            }
            Spacer(modifier = Modifier.height(3.dp))

            Text(
                text = "Joanne Rowling born 31 July 1965, known by her pen name J. K. Rowling, is a British author and philanthropist. She wrote Harry Potter, a seven-volume fantasy series published from 1997 to 2007",
                style = TextStyle(
                    fontWeight = FontWeight.Normal,
                    fontSize = 11.sp,
                    fontFamily = fontFamily,
                    textAlign = TextAlign.Justify
                )
            )

        }
    }
}

@Composable
fun BookAuthorView(modifier: Modifier) {
    Card(
        modifier = modifier, shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 5.dp)

    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    rotationY = 180f
                },
            contentAlignment = Alignment.Center
        ) {

            Column {
                Image(
                    modifier = Modifier
                        .size(70.dp)
                        .clip(shape = CircleShape),
                    painter = painterResource(id = R.drawable.jk_rowling),
                    contentScale = ContentScale.FillBounds,
                    contentDescription = "book cover image"
                )

                Spacer(modifier = Modifier.height(50.dp))

                Text(
                    text = "J.K. Rowling",
                    style = TextStyle(fontWeight = FontWeight.Bold, fontSize = 15.sp)
                )
            }
        }


    }
}