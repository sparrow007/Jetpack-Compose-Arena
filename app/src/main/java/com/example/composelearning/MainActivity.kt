package com.example.composelearning

import android.os.Build
import android.os.Bundle
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import com.example.composelearning.animation.AnimateOneChip
import com.example.composelearning.animation.CircularCarousel
import com.example.composelearning.animation.ShowCardInStack
import com.example.composelearning.animation.ShowSliderToExperiment
import com.example.composelearning.animation.SwipeableCards
import com.example.composelearning.animation.book.BookComposeView
import com.example.composelearning.animation.book.ShowCarouselLayoutView
import com.example.composelearning.animation.colorswaft.ColorSwitchLayoutPreview
import com.example.composelearning.selector.SelectorDiffcultyLayout
import com.example.composelearning.ui.theme.ComposeLearningTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                val colors = listOf(
                    Color.Blue,
                    Color.Red,
                    Color.Green,
                    Color.Magenta,
                    Color.Cyan,
                )
                // A surface container using the 'background' color from the theme
                Surface(modifier = Modifier.fillMaxSize()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        ColorSwitchLayoutPreview()
                    }
                }
            }
        }
        WindowCompat.setDecorFitsSystemWindows(window, false)
        actionBar?.hide()
        window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

//More changes to notice

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    ComposeLearningTheme {
        Greeting("Android")
    }
}