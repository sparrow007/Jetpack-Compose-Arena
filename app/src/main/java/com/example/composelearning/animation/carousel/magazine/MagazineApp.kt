package com.example.composelearning.animation.carousel.magazine


import android.annotation.SuppressLint
import android.util.Log
import androidx.annotation.DrawableRes
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FloatSpringSpec
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.SpringSpec
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntOffsetAsState
import androidx.compose.animation.core.calculateTargetValue
import androidx.compose.animation.core.tween
import androidx.compose.animation.splineBasedDecay
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.drag
import androidx.compose.foundation.gestures.horizontalDrag
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.MailOutline
import androidx.compose.material.icons.sharp.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.consumePositionChange
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.example.composelearning.R
import com.example.composelearning.animation.CircularCarousel
import com.example.composelearning.animation.book.width
import kotlinx.coroutines.cancel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import java.util.UUID
import kotlin.math.PI
import kotlin.math.absoluteValue
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.BottomAppBarDefaults
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import com.example.composelearning.ui.theme.fontFamily

@Preview(showSystemUi = true)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MagazineAppHome() {
    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()
    val selectedIndex = remember { mutableStateOf(0) }
    var backgroundColor by remember {
        mutableStateOf(Color.White)
    }

    var animateColor = animateColorAsState(targetValue = backgroundColor)

    Scaffold(
        containerColor = animateColor.value,
        topBar = {
            TopAppBar(
                title = {
                    Image(
                    modifier = Modifier
                        .width(100.dp)
                        .wrapContentHeight(),
                    painter = painterResource(id = R.drawable.vogue_logo), contentDescription = null
                    ) },
            )
        },
        bottomBar = {
            BottomAppBar (
                containerColor = Color.White
            ) {
               Row (modifier = Modifier
                   .fillMaxWidth()
                   .wrapContentHeight(), horizontalArrangement = Arrangement.SpaceEvenly) {
                   Icon(
                       imageVector = Icons.Filled.Home,contentDescription = "Home"
                   )

                   Icon(
                       imageVector = Icons.Outlined.MailOutline,contentDescription = "Mail"
                   )

                   Icon(
                       imageVector = Icons.Outlined.Info,contentDescription = "Home"
                   )

                   Icon(
                       imageVector = Icons.Outlined.AccountCircle,contentDescription = "Home"
                   )
               }
            }

        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                SearchBar()
                Spacer(modifier = Modifier.height(16.dp))
                TripouselView() {
                    backgroundColor = Color(it)
                }
                Spacer(modifier = Modifier.height(16.dp))
                AllMagazineList()
            }

        }
    }
}


@Composable
fun SearchBar(

) {
    Box(modifier = Modifier
        .padding(horizontal = 10.dp)
        .fillMaxWidth()
        .height(50.dp)
        .padding(start = 5.dp)
        .background(Color.Gray, shape = RoundedCornerShape(18.dp)), contentAlignment = Alignment.CenterStart) {
        Icon(
            modifier = Modifier.padding(start = 10.dp),
            imageVector = Icons.Sharp.Search ,contentDescription = "Home", tint = Color.White
        )
    }
}

@Composable
fun AllMagazineList() {
    val magazineList = remember {
        mutableStateOf(
            listOf(
                R.drawable.alia_vog,
                R.drawable.hariy_vog,
                R.drawable.tom_vog,
                R.drawable.amrit_vog
            )
        )
    }

    Column {
        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth().wrapContentHeight(),
            verticalAlignment = Alignment.CenterVertically) {
            Text(text = "All Magazine", style = TextStyle(fontFamily = fontFamily, fontWeight = FontWeight.Bold))
            IconButton(onClick = { }) {
                Icon(imageVector = Icons.Filled.ArrowForward, contentDescription = "Shopping Cart")
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        LazyRow {
            items(magazineList.value.size) { index ->
                Image(
                    painter = painterResource(id = magazineList.value[index]),
                    contentDescription = ""
                )
            }
        }

    }
}