package com.miyabi_hiroshi.app.justarray.ui.keyboard

import androidx.compose.ui.unit.dp

data class KeyDefinition(
    val qwertyChar: Char,
    val arrayLabel: String,
    val displayChar: String,
    val widthWeight: Float = 1f,
)

object KeyboardLayout {
    val KEY_HEIGHT = 48.dp
    val KEY_SPACING = 2.dp
    val KEYBOARD_PADDING = 4.dp
    val FUNCTION_KEY_HEIGHT = 44.dp

    val TOP_ROW = listOf(
        KeyDefinition('q', "1↑", "Q"),
        KeyDefinition('w', "2↑", "W"),
        KeyDefinition('e', "3↑", "E"),
        KeyDefinition('r', "4↑", "R"),
        KeyDefinition('t', "5↑", "T"),
        KeyDefinition('y', "6↑", "Y"),
        KeyDefinition('u', "7↑", "U"),
        KeyDefinition('i', "8↑", "I"),
        KeyDefinition('o', "9↑", "O"),
        KeyDefinition('p', "0↑", "P"),
    )

    val MIDDLE_ROW = listOf(
        KeyDefinition('a', "1-", "A"),
        KeyDefinition('s', "2-", "S"),
        KeyDefinition('d', "3-", "D"),
        KeyDefinition('f', "4-", "F"),
        KeyDefinition('g', "5-", "G"),
        KeyDefinition('h', "6-", "H"),
        KeyDefinition('j', "7-", "J"),
        KeyDefinition('k', "8-", "K"),
        KeyDefinition('l', "9-", "L"),
        KeyDefinition(';', "0-", ";"),
    )

    val BOTTOM_ROW = listOf(
        KeyDefinition('z', "1↓", "Z"),
        KeyDefinition('x', "2↓", "X"),
        KeyDefinition('c', "3↓", "C"),
        KeyDefinition('v', "4↓", "V"),
        KeyDefinition('b', "5↓", "B"),
        KeyDefinition('n', "6↓", "N"),
        KeyDefinition('m', "7↓", "M"),
        KeyDefinition(',', "8↓", ","),
        KeyDefinition('.', "9↓", "."),
        KeyDefinition('/', "0↓", "/"),
    )

    val SYMBOL_CATEGORIES = listOf(
        "標點" to listOf("，", "。", "、", "；", "：", "？", "！", "…", "—", "～",
            "「", "」", "『", "』", "（", "）", "【", "】", "《", "》"),
        "括號" to listOf("（", "）", "「", "」", "『", "』", "【", "】", "〔", "〕",
            "《", "》", "〈", "〉", "﹁", "﹂", "﹃", "﹄", "{", "}"),
        "符號" to listOf("＃", "＄", "％", "＆", "＊", "＋", "－", "＝", "＠", "＾",
            "｜", "＼", "／", "￥", "€", "£", "¥", "°", "§", "†"),
        "數字" to listOf("０", "１", "２", "３", "４", "５", "６", "７", "８", "９",
            "①", "②", "③", "④", "⑤", "⑥", "⑦", "⑧", "⑨", "⑩"),
    )
}
