package com.miyabi_hiroshi.app.justarray.ui.keyboard

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.unit.dp

val LocalKeyboardHeightScale = compositionLocalOf { 1.0f }

data class KeyDefinition(
    val qwertyChar: Char,
    val arrayLabel: String,
    val displayChar: String,
    val widthWeight: Float = 1f,
)

object KeyboardLayout {
    val KEY_HEIGHT = 48.dp
    val KEY_SPACING = 4.dp
    val KEYBOARD_PADDING = 6.dp
    val FUNCTION_KEY_HEIGHT = 44.dp

    // Landscape split keyboard
    val LANDSCAPE_KEY_HEIGHT = 38.dp
    val LANDSCAPE_FUNCTION_KEY_HEIGHT = 36.dp
    val LANDSCAPE_CENTER_GAP = 20.dp
    const val SPLIT_INDEX = 5  // split each row at index 5

    val NUMBER_ROW = listOf(
        KeyDefinition('1', "", "1"),
        KeyDefinition('2', "", "2"),
        KeyDefinition('3', "", "3"),
        KeyDefinition('4', "", "4"),
        KeyDefinition('5', "", "5"),
        KeyDefinition('6', "", "6"),
        KeyDefinition('7', "", "7"),
        KeyDefinition('8', "", "8"),
        KeyDefinition('9', "", "9"),
        KeyDefinition('0', "", "0"),
    )

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

    val KEY_ALTERNATES: Map<Char, List<String>> = mapOf(
        'a' to listOf("à", "á", "â", "ã", "ä", "å", "æ"),
        'e' to listOf("è", "é", "ê", "ë"),
        'i' to listOf("ì", "í", "î", "ï"),
        'o' to listOf("ò", "ó", "ô", "õ", "ö", "ø"),
        'u' to listOf("ù", "ú", "û", "ü"),
        'n' to listOf("ñ"),
        'c' to listOf("ç"),
        's' to listOf("ß"),
        'y' to listOf("ý", "ÿ"),
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
