package com.example.composelearning.animation

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.composelearning.R

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun ChoiceChipGroup(
    choiceGroups: List<Pair<List<String>, Int?>>,
    onChoiceClick: (Int, Int) -> Unit,
    onClearClick: () -> Unit,
) {
    val padding = 16.dp
    var height by remember { mutableStateOf(0.dp) }
    val density = LocalDensity.current

    LazyRow(
        modifier = Modifier.onGloballyPositioned {
            if (height == 0.dp) height = with(density) { it.size.height.toDp() }
        },
        contentPadding = PaddingValues(padding)
    ) {
        if (height > 0.dp) {
            item {
                AnimatedVisibility(
                    modifier = Modifier.height(height - padding.times(2)),
                    visible = choiceGroups.any { it.second != null },
                    enter = expandHorizontally(expandFrom = Alignment.CenterHorizontally) + fadeIn() + scaleIn(
                        initialScale = .8f
                    ),
                    exit = shrinkHorizontally(shrinkTowards = Alignment.CenterHorizontally) + fadeOut() + scaleOut(
                        targetScale = .8f
                    ),
                ) {
                    ClearButton(onClearClick)
                }
            }
        }

        itemsIndexed(choiceGroups) { index, (items, selection) ->
            ChoiceChip(
                choiceChipIndex = index,
                items = items,
                selection = selection,
                onChoiceClick = onChoiceClick
            )
        }
    }
}

@Composable
private fun ClearButton(
    onClearClick: () -> Unit
) {
    Box(
        modifier = Modifier.padding(end = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .aspectRatio(1f, true)
                .border(BorderStroke(1.dp, MaterialTheme.colorScheme.primary), CircleShape)
                .clickable { onClearClick() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = R.drawable.baseline_clear_24),
                contentDescription = "Clear"
            )
        }
    }
}

@Composable
private fun ChoiceChip(
    choiceChipIndex: Int,
    items: List<String>,
    selection: Int? = null,
    onChoiceClick: (Int, Int) -> Unit
) {
    val background by animateColorAsState(targetValue = if (selection == null) Color.Transparent else MaterialTheme.colorScheme.primary)

    Surface(
        modifier = Modifier.padding(end = 8.dp),
        shape = RoundedCornerShape(50),
        color = Color.Transparent,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
    ) {
        Row(
            modifier = Modifier.height(IntrinsicSize.Min)
        ) {
            items.forEachIndexed { index, label ->
                AnimatedVisibility(selection == null || index == selection) {
                    val isSelected = index == selection
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(if (isSelected) background else Color.Transparent)
                            .clickable {
                                onChoiceClick(choiceChipIndex, index)
                            },
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            text = label,
                            textAlign = TextAlign.Center,
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Normal
                        )
                    }
                }

                AnimatedVisibility(selection == null && index < items.size - 1) {
                    Box(
                        modifier = Modifier
                            .padding(vertical = 10.dp)
                            .fillMaxHeight()
                            .width(1.dp)
                            .background(MaterialTheme.colorScheme.primary)
                    )
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.N)
@Preview(showSystemUi = false)
@Composable
fun ChoiceGroupsPreview() {
    val choiceGroups = remember {
        mutableStateListOf<Pair<List<String>, Int?>>(
            listOf("Cats", "Dogs") to null,
            listOf("Flutter", "Compose", "XML") to null,
            listOf("Some flag") to null,
        )
    }

    ChoiceChipGroup(
        choiceGroups = choiceGroups,
        onChoiceClick = { choiceChipIndex, index ->
            choiceGroups[choiceChipIndex] = choiceGroups[choiceChipIndex].let { current ->
                current.first to if (current.second == index) null else index
            }
        },
        onClearClick = {
            choiceGroups.replaceAll { (items, _) ->
                items to null
            }
        }
    )
}