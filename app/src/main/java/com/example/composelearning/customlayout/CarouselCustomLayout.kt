package com.example.composelearning.customlayout

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

//Create carousel to learn custom animation and layout in the compose

@Composable
fun CarouselCustomLayout(
    numberOfItems: Int,
    itemFraction: Float = .25f,
    modifier: Modifier = Modifier,
    content: @Composable (index: Int) -> Unit
) {


}