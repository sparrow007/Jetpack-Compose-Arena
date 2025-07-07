package com.example.composelearning.kotlin

fun main() {
    val arrya = arrayOf(1, 3, 7, 3, 5, 2, 4, 4, 6)
    mergeSort(
        arrya,
        0,
        arrya.size - 1
    )
    println()
    arrya.forEach {
        print("$it ")
    }
}

fun mergeArray(array: Array<Int>, low: Int, mid: Int, high: Int) {
    val tempArray = Array(size = array.size, {
        it
    })

    var left = low
    var right = mid + 1
    var index = 0
    while (left <= mid && right <= high) {
        if (array[left] < array[right]) {
            tempArray[index] = array[left]
            left++
        } else {
            tempArray[index] = array[right]
            right++
        }

        index++
    }

    while (left <= mid) {
        tempArray[index] = array[left]
        left++
        index++
    }

    while (right <= high) {
        tempArray[index] = array[right]
        right++
        index++
    }

    for (i in low..high) {
        array[i] = tempArray[i - low]
    }
}

fun mergeSort(array: Array<Int>, low: Int, high: Int) {
    if (low >= high) return
    val mid = (low + high) / 2
    mergeSort(array, low, mid)
    mergeSort(array, mid + 1, high)
    mergeArray(array, low, mid, high)
}