package com.example.composelearning

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import com.example.composelearning.customlayout.CustomLazyLayoutScreen
import com.example.composelearning.ui.theme.ComposeLearningTheme
import viewmodel.MainViewModel

class MainActivity : ComponentActivity() {


    private val viewModel by viewModels<MainViewModel>()


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
                val state by viewModel.state.collectAsState()
                CustomLazyLayoutScreen(
                    state = state,
                    actions = viewModel
                )
                // A surface container using the 'background' color from the theme
//                Surface(modifier = Modifier.fillMaxSize()) {
//                    Box(
//                        modifier = Modifier.fillMaxSize(),
//                        contentAlignment = Alignment.Center,
//                    ) {
//                        val state by viewModel.state.collectAsState()
//
//
//                    }
//                }
            }
        }
//        WindowCompat.setDecorFitsSystemWindows(window, false)
//        actionBar?.hide()
//        window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
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