package com.example.composelearning

/**
 * Generates every valid arrangement of n pairs of parentheses.
 *
 * @param n number of parentheses pairs
 * @return the arrangements, already sorted in ascending ASCII order
 *
 * The result needs no explicit sort: '(' (ASCII 40) sorts before ')' (ASCII 41)
 * and the generator always tries '(' before ')', so the depth-first walk emits
 * the strings in lexicographic order.
 *
 * Time:  O(Catalan(n) * n) — optimal, one unit of work per emitted character.
 * Space: O(n) working memory plus the output array itself.
 */
fun solution(n: Int): Array<String> {
    if (n <= 0) return arrayOf("")

    val result = ArrayList<String>(catalan(n))
    val buffer = CharArray(2 * n)
    build(buffer, 0, 0, 0, n, result)
    return result.toTypedArray()
}

/**
 * Backtracking core.
 *
 * @param pos   next index to fill in [buffer]
 * @param open  '(' used so far
 * @param close ')' used so far
 *
 * Two rules keep every partial string valid:
 *  1. an '(' may be added while open < n
 *  2. a ')' may be added only while close < open, so it always has a partner
 */
private fun build(
    buffer: CharArray,
    pos: Int,
    open: Int,
    close: Int,
    n: Int,
    result: MutableList<String>
) {
    if (pos == buffer.size) {
        result.add(String(buffer))
        return
    }
    if (open < n) {
        buffer[pos] = '('
        build(buffer, pos + 1, open + 1, close, n, result)
    }
    if (close < open) {
        buffer[pos] = ')'
        build(buffer, pos + 1, open, close + 1, n, result)
    }
}

/** Catalan(n) = number of valid arrangements, used to pre-size the output list. */
private fun catalan(n: Int): Int {
    var value = 1L
    for (i in 0 until n) {
        value = value * 2 * (2 * i + 1) / (i + 2)
    }
    return value.toInt()
}

/**
 * Iterative variant with an explicit stack — same output, same order, no JVM
 * call stack usage. Kept for reference; [solution] is the one to use.
 */
fun solutionIterative(n: Int): Array<String> {
    if (n <= 0) return arrayOf("")

    val result = ArrayList<String>(catalan(n))
    val buffer = CharArray(2 * n)

    // One parallel slot per frame.
    // branch: 0 = try '(' next, 1 = try ')' next, 2 = frame exhausted
    val posStack = IntArray(2 * n + 1)
    val openStack = IntArray(2 * n + 1)
    val closeStack = IntArray(2 * n + 1)
    val branchStack = IntArray(2 * n + 1)

    var top = 0
    posStack[0] = 0; openStack[0] = 0; closeStack[0] = 0; branchStack[0] = 0

    while (top >= 0) {
        val pos = posStack[top]
        val open = openStack[top]
        val close = closeStack[top]

        if (pos == buffer.size) {
            result.add(String(buffer))
            top--
            continue
        }

        when (branchStack[top]++) {
            0 -> if (open < n) {
                buffer[pos] = '('
                top++
                posStack[top] = pos + 1
                openStack[top] = open + 1
                closeStack[top] = close
                branchStack[top] = 0
            }
            1 -> if (close < open) {
                buffer[pos] = ')'
                top++
                posStack[top] = pos + 1
                openStack[top] = open
                closeStack[top] = close + 1
                branchStack[top] = 0
            }
            else -> top--
        }
    }
    return result.toTypedArray()
}

fun main() {
    println(solution(3).joinToString(" ")) // ((())) (()()) (())() ()(()) ()()()
    println(solution(1).joinToString(" ")) // ()
    println(solution(0).joinToString(" ")) // (empty string)
    println(solution(4).size)              // 14
    check(solution(3).contentEquals(solutionIterative(3)))
}
