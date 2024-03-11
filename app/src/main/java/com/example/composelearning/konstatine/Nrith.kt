package com.example.composelearning.konstatine


import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseInQuint
import androidx.compose.animation.core.EaseOutQuint
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.BaselineShift
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextGeometricTransform
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import com.example.composelearning.R
import kotlinx.coroutines.launch

private val baloo = FontFamily(
    Font(R.font.baloo)
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Nrith("No Running In The Halls!")
                    }
                }
            }
        }
    }
}

@Composable
fun Nrith(text: String) {
    val scaleMin = 0.6f
    val scaleMax = 1f

    val skewXNeutral = 0f
    val skewXBraking = 0.5f
    val skewXAccelerating = 1f
    val skewXJumpingBack = -0.3f

    val yNeutral = 0f
    val yHighestPoint = 1f

    BoxWithConstraints {
        val density = LocalDensity.current

        val translationX = remember { Animatable(with(density) { IntOffset(x = -maxWidth.toPx().toInt(), y = 0) }, IntOffset.VectorConverter) }
        val scaleX = remember { Animatable(scaleMax) }
        val skewX = remember { Animatable(skewXNeutral) }
        val y = remember { Animatable(yNeutral) }

        val colorProgress = (scaleMax - scaleX.value) / (scaleMax - scaleMin)
        val color = lerp(LocalContentColor.current, Color(0xFFEF5350), colorProgress)

        val animInDuration = 1500
        val halfAnimInDuration = animInDuration / 2
        LaunchedEffect(Unit) {
            // Move in
            launch {
                launch {
                    launch {
                        translationX.animateTo(
                            targetValue = IntOffset.Zero,
                            animationSpec = tween(animInDuration, halfAnimInDuration, EaseOutQuint)
                        )
                    }

                    launch {
                        skewX.animateTo(
                            targetValue = skewXBraking,
                            animationSpec = tween(animInDuration, halfAnimInDuration, EaseOutQuint)
                        )
                    }

                    scaleX.animateTo(
                        targetValue = scaleMin,
                        animationSpec = tween(animInDuration, halfAnimInDuration, EaseOutQuint)
                    )
                }
            }.join()

            // Back to neutral
            launch {
                launch {
                    y.animateTo(yHighestPoint, animationSpec = spring(stiffness = Spring.StiffnessHigh))
                    y.animateTo(yNeutral, animationSpec = spring(stiffness = Spring.StiffnessHigh))
                }

                launch {
                    skewX.animateTo(
                        targetValue = skewXJumpingBack,
                        animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy)
                    )
                    skewX.animateTo(
                        targetValue = skewXNeutral,
                        animationSpec = spring(dampingRatio = Spring.DampingRatioHighBouncy)
                    )
                }

                scaleX.animateTo(
                    targetValue = scaleMax,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioNoBouncy,
                        stiffness = Spring.StiffnessMedium
                    )
                )
            }.join()

            // Move out
            launch {
                launch {
                    translationX.animateTo(with(density) {
                        IntOffset(
                            x = maxWidth.toPx().toInt(),
                            y = 0
                        )
                    }, animationSpec = tween(1500, 500, EaseInQuint))
                }
                launch {
                    skewX.animateTo(
                        targetValue = skewXAccelerating,
                        animationSpec = tween(2000, 0, EaseInQuint)
                    )
                }
            }
        }

        val annotatedText = buildAnnotatedString {
            withStyle(
                SpanStyle(
                    textGeometricTransform = TextGeometricTransform(
                        scaleX = scaleX.value,
                        skewX = skewX.value
                    ),
                    baselineShift = BaselineShift(y.value)
                )
            ) {
                append(text)
            }
        }

        Box(
            modifier = Modifier.offset { translationX.value },
            contentAlignment = Alignment.CenterEnd
        ) {
            Text(
                text = annotatedText,
                fontSize = 48.sp,
                textAlign = TextAlign.Center,
                fontFamily = baloo,
                color = color
            )

            // Fixme: How to keep the width of the wrapping box when its content shrinks?
            Text(
                text = text,
                fontSize = 48.sp,
                textAlign = TextAlign.Center,
                color = Color.Transparent,
                fontFamily = baloo
            )
        }
    }
}