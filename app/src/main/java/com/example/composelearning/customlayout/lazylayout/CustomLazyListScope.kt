package com.example.composelearning.customlayout.lazylayout

import com.example.composelearning.customlayout.ComposeItemContent
import com.example.composelearning.customlayout.LazyLayoutItemContent
import com.example.composelearning.customlayout.ListItem

interface CustomLazyListScope {
    fun items(
        item: List<ListItem>,
        composeComponent: ComposeItemContent
    )
}

class CustomLazyListScopeImpl : CustomLazyListScope {
    private val _items = mutableListOf<LazyLayoutItemContent>()
    val items: List<LazyLayoutItemContent>
        get() = _items

    override fun items(
        items: List<ListItem>,
        composeComponent: ComposeItemContent
    ) {
        items.forEach {
            _items.add(LazyLayoutItemContent(it, composeComponent))
        }
    }
}