package com.miyabi_hiroshi.app.justarray.ime

/**
 * Array 30 key mapping.
 * Each QWERTY key maps to an Array label shown in the key corners.
 * The 30 Array keys use positions 1↑ through 0↓ across 3 rows.
 */
object KeyCode {
    // Row labels: ↑ = top, - = middle, ↓ = bottom
    data class ArrayKey(
        val qwerty: Char,
        val arrayLabel: String,
        val row: Int,   // 0=top, 1=mid, 2=bottom
        val col: Int    // 0-9
    )

    // Top row: q w e r t y u i o p
    // Mid row: a s d f g h j k l ;
    // Bot row: z x c v b n m , . /

    private val keyMap: Map<Char, ArrayKey> = buildMap {
        val topRow = "qwertyuiop"
        val midRow = "asdfghjkl;"
        val botRow = "zxcvbnm,./"
        val labels = arrayOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "0")

        topRow.forEachIndexed { i, c ->
            put(c, ArrayKey(c, "${labels[i]}↑", 0, i))
        }
        midRow.forEachIndexed { i, c ->
            put(c, ArrayKey(c, "${labels[i]}-", 1, i))
        }
        botRow.forEachIndexed { i, c ->
            put(c, ArrayKey(c, "${labels[i]}↓", 2, i))
        }
    }

    fun getArrayLabel(qwertyChar: Char): String {
        return keyMap[qwertyChar.lowercaseChar()]?.arrayLabel ?: qwertyChar.toString()
    }

    fun getArrayKey(qwertyChar: Char): ArrayKey? {
        return keyMap[qwertyChar.lowercaseChar()]
    }

    fun getAllKeys(): List<ArrayKey> = keyMap.values.sortedWith(compareBy({ it.row }, { it.col }))

    fun getRow(row: Int): List<ArrayKey> = getAllKeys().filter { it.row == row }

    /**
     * Converts a sequence of QWERTY chars to Array label string for display.
     * e.g., "asdf" → "1- 2- 3- 4-"
     */
    fun toArrayLabels(qwertySequence: String): String {
        return qwertySequence.map { getArrayLabel(it) }.joinToString(" ")
    }
}
