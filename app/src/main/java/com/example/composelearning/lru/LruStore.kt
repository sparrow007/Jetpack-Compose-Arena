package com.example.composelearning.lru

private const val NOT_FOUND = -1
private const val MIN_VALUE = 0
private const val MAX_VALUE = 1 shl 30

class LruStore {

    private class Node(val key: Int, var value: Int) {
        var prev: Node? = null
        var next: Node? = null
    }

    private val head = Node(0, 0)
    private val tail = Node(0, 0)
    private val map = HashMap<Int, Node>()

    init {
        head.next = tail
        tail.prev = head
    }

    fun add(key: Int, value: Int) {
        if (key !in MIN_VALUE..MAX_VALUE || value !in MIN_VALUE..MAX_VALUE) return
        val existing = map[key]
        if (existing != null) {
            existing.value = value
            unlink(existing)
            linkFirst(existing)
            return
        }
        val node = Node(key, value)
        map[key] = node
        linkFirst(node)
    }

    fun get(key: Int): Int {
        val node = map[key] ?: return NOT_FOUND
        unlink(node)
        linkFirst(node)
        return node.value
    }

    fun remove(key: Int): Int {
        val node = map.remove(key) ?: return NOT_FOUND
        unlink(node)
        return node.value
    }

    fun evict() {
        val oldest = tail.prev ?: return
        if (oldest === head) return
        map.remove(oldest.key)
        unlink(oldest)
    }

    private fun linkFirst(node: Node) {
        val first = head.next
        node.prev = head
        node.next = first
        head.next = node
        first?.prev = node
    }

    private fun unlink(node: Node) {
        node.prev?.next = node.next
        node.next?.prev = node.prev
        node.prev = null
        node.next = null
    }
}

private val TOKEN_SEPARATORS = Regex("[\\s(),;]+")

private fun String.toBoundedIntOrNull(): Int? = toIntOrNull()?.takeIf { it in MIN_VALUE..MAX_VALUE }

fun solution(A: Array<String>): Array<String> {
    val store = LruStore()
    val results = ArrayList<String>()

    for (raw in A) {
        val tokens = raw.trim().split(TOKEN_SEPARATORS).filter { it.isNotEmpty() }
        if (tokens.isEmpty()) continue

        val op = tokens[0].lowercase()
        val args = tokens.drop(1)

        when (op) {
            "add" -> if (args.size == 2) {
                val key = args[0].toBoundedIntOrNull()
                val value = args[1].toBoundedIntOrNull()
                if (key != null && value != null) store.add(key, value)
            }

            "get" -> if (args.size == 1) {
                val key = args[0].toBoundedIntOrNull()
                results.add((if (key != null) store.get(key) else NOT_FOUND).toString())
            }

            "remove" -> if (args.size == 1) {
                val key = args[0].toBoundedIntOrNull()
                results.add((if (key != null) store.remove(key) else NOT_FOUND).toString())
            }

            "evict" -> if (args.isEmpty()) store.evict()

            "exit" -> if (args.isEmpty()) return results.toTypedArray()
        }
    }

    return results.toTypedArray()
}
