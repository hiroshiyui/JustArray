package com.miyabi_hiroshi.app.justarray.ui.keyboard

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

class ArrayKeyboardTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun allQwertyKeysRendered() {
        composeTestRule.setContent {
            ArrayKeyboard(
                showArrayLabels = false,
                onKeyPress = {},
            )
        }

        val expectedKeys = listOf(
            "Q", "W", "E", "R", "T", "Y", "U", "I", "O", "P",
            "A", "S", "D", "F", "G", "H", "J", "K", "L", ";",
            "Z", "X", "C", "V", "B", "N", "M", ",", ".", "/",
        )
        for (key in expectedKeys) {
            composeTestRule.onNodeWithText(key).assertIsDisplayed()
        }
    }

    @Test
    fun arrayLabelsShownWhenEnabled() {
        composeTestRule.setContent {
            ArrayKeyboard(
                showArrayLabels = true,
                onKeyPress = {},
            )
        }

        // Check a sample of array labels from each row
        composeTestRule.onNodeWithText("1↑").assertIsDisplayed()
        composeTestRule.onNodeWithText("5-").assertIsDisplayed()
        composeTestRule.onNodeWithText("0↓").assertIsDisplayed()
    }

    @Test
    fun arrayLabelsHiddenWhenDisabled() {
        composeTestRule.setContent {
            ArrayKeyboard(
                showArrayLabels = false,
                onKeyPress = {},
            )
        }

        composeTestRule.onNodeWithText("1↑").assertDoesNotExist()
        composeTestRule.onNodeWithText("5-").assertDoesNotExist()
        composeTestRule.onNodeWithText("0↓").assertDoesNotExist()
    }

    @Test
    fun keyTapFiresOnKeyPress() {
        var pressedKey: Char? = null
        composeTestRule.setContent {
            ArrayKeyboard(
                showArrayLabels = false,
                onKeyPress = { pressedKey = it },
            )
        }

        composeTestRule.onNodeWithText("A").performClick()

        assertEquals('a', pressedKey)
    }

    @Test
    fun numberRowKeysRendered() {
        composeTestRule.setContent {
            ArrayKeyboard(
                showArrayLabels = false,
                onKeyPress = {},
            )
        }

        for (digit in 0..9) {
            composeTestRule.onNodeWithText(digit.toString()).assertIsDisplayed()
        }
    }

    @Test
    fun numberKeyTapFiresOnNumberPress() {
        var pressedNumber: Int? = null
        composeTestRule.setContent {
            ArrayKeyboard(
                showArrayLabels = false,
                onKeyPress = {},
                onNumberPress = { pressedNumber = it },
            )
        }

        composeTestRule.onNodeWithText("5").performClick()

        assertEquals(5, pressedNumber)
    }
}
