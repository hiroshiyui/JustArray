package com.miyabi_hiroshi.app.justarray.data.dictionary

class TrieNode {
    val children: HashMap<Char, TrieNode> = HashMap()
    val values: MutableList<String> = mutableListOf()
}

class ArrayTrie {
    val root = TrieNode()

    fun insert(code: String, value: String) {
        var node = root
        for (ch in code) {
            node = node.children.getOrPut(ch) { TrieNode() }
        }
        if (value !in node.values) {
            node.values.add(value)
        }
    }

    fun lookup(code: String): List<String> {
        var node = root
        for (ch in code) {
            node = node.children[ch] ?: return emptyList()
        }
        return node.values.toList()
    }

    fun prefixLookup(prefix: String): Sequence<String> {
        var node = root
        for (ch in prefix) {
            node = node.children[ch] ?: return emptySequence()
        }
        return collectAll(node)
    }

    private fun collectAll(node: TrieNode): Sequence<String> = sequence {
        yieldAll(node.values)
        for (child in node.children.values) {
            yieldAll(collectAll(child))
        }
    }

    val isEmpty: Boolean get() = root.children.isEmpty()
}
