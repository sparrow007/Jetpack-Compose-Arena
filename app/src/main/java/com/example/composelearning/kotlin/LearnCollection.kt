package com.example.composelearning.kotlin

fun main() {
    val list = listOf(1, 2, 3, 4, 5)

    println(list.chunked(3))

    println(list.windowed(size = 3))


}