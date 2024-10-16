package com.example.composelearning.customlayout

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Layout

@Composable
fun RotationCarouselCustomLayout(
    numberOfItems: Int,
    itemFraction: Float = .25f,
    modifier: Modifier = Modifier,
    content: @Composable (index: Int) -> Unit
) {

    Layout(modifier = modifier, content = {
        repeat(numberOfItems) { index ->
            content(index)
        }
    }) { measurables, constraints ->
        //TODO

        layout(constraints.maxWidth, constraints.maxHeight) {
            //TODO
        }
    }

}