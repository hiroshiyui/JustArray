package com.miyabi_hiroshi.app.justarray.data.dictionary

import java.io.Serializable

class TrieNode : Serializable {
    val children: HashMap<Char, TrieNode> = HashMap()
    val values: MutableList<String> = mutableListOf()
}

class ArrayTrie : Serializable {
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

    fun prefixLookup(prefix: String): List<String> {
        var node = root
        for (ch in prefix) {
            node = node.children[ch] ?: return emptyList()
        }
        return collectAll(node)
    }

    private fun collectAll(node: TrieNode): List<String> {
        val result = mutableListOf<String>()
        result.addAll(node.values)
        for (child in node.children.values) {
            result.addAll(collectAll(child))
        }
        return result
    }

    val isEmpty: Boolean get() = root.children.isEmpty()
}
