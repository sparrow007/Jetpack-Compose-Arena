package com.example.composelearning.lru

import org.junit.Assert.assertEquals
import org.junit.Test

class LruStoreTest {

    private fun run(vararg ops: String) = solution(arrayOf(*ops)).toList()

    @Test
    fun addGetRemoveOverwrite() {
        assertEquals(
            listOf("100", "-1", "100", "200", "200", "-1"),
            run("add 1 100", "get 1", "get 2", "remove 1", "add 1 200", "get 1", "remove 1", "get 1")
        )
    }

    @Test
    fun evictDropsLeastRecentlyAccessed() {
        assertEquals(
            listOf("10", "-1", "30"),
            run("add 1 10", "add 2 20", "add 3 30", "get 1", "evict", "get 2", "get 3")
        )
    }

    @Test
    fun overwriteRefreshesRecency() {
        assertEquals(
            listOf("-1", "99"),
            run("add 1 10", "add 2 20", "add 1 99", "evict", "get 2", "get 1")
        )
    }

    @Test
    fun removeAlsoRefreshesEvictionOrder() {
        assertEquals(
            listOf("10", "-1", "30"),
            run("add 1 10", "add 2 20", "add 3 30", "remove 1", "evict", "get 2", "get 3")
        )
    }

    @Test
    fun evictUntilEmptyAndBeyond() {
        assertEquals(
            listOf("-1", "-1", "-1"),
            run("add 1 1", "add 2 2", "evict", "evict", "evict", "evict", "get 1", "get 2", "get 3")
        )
    }

    @Test
    fun evictOnEmptyStoreIsNoOp() {
        assertEquals(listOf("7"), run("evict", "add 5 7", "get 5"))
    }

    @Test
    fun reAddAfterEvictWorks() {
        assertEquals(listOf("-1", "9"), run("add 1 1", "evict", "get 1", "add 1 9", "get 1"))
    }

    @Test
    fun unknownOperationsAreIgnored() {
        assertEquals(listOf("5"), run("frobnicate 1 2", "put 1 2", "clear", "add 1 5", "get 1"))
    }

    @Test
    fun malformedArgumentCountsAreIgnored() {
        assertEquals(
            listOf("-1", "5"),
            run("add", "add 1", "add 1 2 3", "get 1", "get 1 2", "remove", "add 1 5", "get 1")
        )
    }

    @Test
    fun nonNumericAndOutOfRangeKeys() {
        assertEquals(
            listOf("-1", "-1", "-1", "-1"),
            run("add abc 1", "get abc", "add -3 7", "get -3", "add 1073741825 1", "get 1073741825", "get 2147483648")
        )
    }

    @Test
    fun keyBoundsAreInclusive() {
        val max = (1 shl 30).toString()
        assertEquals(listOf("1", "2"), run("add 0 1", "add $max 2", "get 0", "get $max"))
    }

    @Test
    fun nonNumericValueIsIgnored() {
        assertEquals(listOf("-1"), run("add 1 abc", "get 1"))
    }

    @Test
    fun valueOutOfRangeIsIgnored() {
        val max = (1 shl 30).toString()
        assertEquals(
            listOf("-1", "-1", "-1", max),
            run("add 1 1073741825", "get 1", "add 1 -5", "get 1", "add 1 2147483648", "get 1", "add 1 $max", "get 1")
        )
    }

    @Test
    fun exitStopsProcessing() {
        assertEquals(listOf("1"), run("add 1 1", "get 1", "exit", "add 2 2", "get 2"))
    }

    @Test
    fun exitWithArgumentsIsNotAnExit() {
        assertEquals(listOf("1", "2"), run("add 1 1", "get 1", "exit now", "add 2 2", "get 2"))
    }

    @Test
    fun missingExitStillReturnsResults() {
        assertEquals(listOf("1"), run("add 1 1", "get 1"))
    }

    @Test
    fun emptyInputProducesEmptyOutput() {
        assertEquals(emptyList<String>(), solution(emptyArray()).toList())
    }

    @Test
    fun blankLinesAreSkipped() {
        assertEquals(listOf("3"), run("", "   ", "\t", "add 1 3", " get  1 "))
    }

    @Test
    fun operationNamesAreCaseInsensitive() {
        assertEquals(listOf("4", "4", "-1"), run("ADD 1 4", "Get 1", "ReMoVe 1", "EVICT", "get 1"))
    }

    @Test
    fun parenthesisAndCommaSyntax() {
        assertEquals(listOf("7", "7", "-1"), run("add(5, 7)", "get(5)", "remove(5)", "get(5)"))
    }

    @Test
    fun manyEntriesEvictInInsertionOrderWhenUntouched() {
        val ops = (1..1000).map { "add $it $it" } + List(999) { "evict" } + listOf("get 1000", "get 1")
        assertEquals(listOf("1000", "-1"), solution(ops.toTypedArray()).toList())
    }
}
