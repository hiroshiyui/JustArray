package com.miyabi_hiroshi.app.justarray.ime

import org.junit.Assert.*
import org.junit.Test

class KeyCodeTest {

    @Test
    fun `top row labels q through p`() {
        assertEquals("1↑", KeyCode.getArrayLabel('q'))
        assertEquals("2↑", KeyCode.getArrayLabel('w'))
        assertEquals("3↑", KeyCode.getArrayLabel('e'))
        assertEquals("4↑", KeyCode.getArrayLabel('r'))
        assertEquals("5↑", KeyCode.getArrayLabel('t'))
        assertEquals("6↑", KeyCode.getArrayLabel('y'))
        assertEquals("7↑", KeyCode.getArrayLabel('u'))
        assertEquals("8↑", KeyCode.getArrayLabel('i'))
        assertEquals("9↑", KeyCode.getArrayLabel('o'))
        assertEquals("0↑", KeyCode.getArrayLabel('p'))
    }

    @Test
    fun `mid row labels a through semicolon`() {
        assertEquals("1-", KeyCode.getArrayLabel('a'))
        assertEquals("2-", KeyCode.getArrayLabel('s'))
        assertEquals("3-", KeyCode.getArrayLabel('d'))
        assertEquals("4-", KeyCode.getArrayLabel('f'))
        assertEquals("5-", KeyCode.getArrayLabel('g'))
        assertEquals("6-", KeyCode.getArrayLabel('h'))
        assertEquals("7-", KeyCode.getArrayLabel('j'))
        assertEquals("8-", KeyCode.getArrayLabel('k'))
        assertEquals("9-", KeyCode.getArrayLabel('l'))
        assertEquals("0-", KeyCode.getArrayLabel(';'))
    }

    @Test
    fun `bottom row labels z through slash`() {
        assertEquals("1↓", KeyCode.getArrayLabel('z'))
        assertEquals("2↓", KeyCode.getArrayLabel('x'))
        assertEquals("3↓", KeyCode.getArrayLabel('c'))
        assertEquals("4↓", KeyCode.getArrayLabel('v'))
        assertEquals("5↓", KeyCode.getArrayLabel('b'))
        assertEquals("6↓", KeyCode.getArrayLabel('n'))
        assertEquals("7↓", KeyCode.getArrayLabel('m'))
        assertEquals("8↓", KeyCode.getArrayLabel(','))
        assertEquals("9↓", KeyCode.getArrayLabel('.'))
        assertEquals("0↓", KeyCode.getArrayLabel('/'))
    }

    @Test
    fun `case insensitive lookup`() {
        assertEquals("1↑", KeyCode.getArrayLabel('Q'))
        assertEquals("1↑", KeyCode.getArrayLabel('q'))
        assertEquals("1-", KeyCode.getArrayLabel('A'))
        assertEquals("1-", KeyCode.getArrayLabel('a'))
    }

    @Test
    fun `unknown char returns char itself`() {
        assertEquals("!", KeyCode.getArrayLabel('!'))
        assertEquals("@", KeyCode.getArrayLabel('@'))
        assertEquals("1", KeyCode.getArrayLabel('1'))
    }

    @Test
    fun `toArrayLabels converts sequence`() {
        assertEquals("1↑ 1- 1↓", KeyCode.toArrayLabels("qaz"))
        assertEquals("1- 2- 3- 4-", KeyCode.toArrayLabels("asdf"))
    }

    @Test
    fun `getAllKeys returns 30 keys`() {
        assertEquals(30, KeyCode.getAllKeys().size)
    }

    @Test
    fun `getRow returns 10 keys per row`() {
        assertEquals(10, KeyCode.getRow(0).size) // top
        assertEquals(10, KeyCode.getRow(1).size) // mid
        assertEquals(10, KeyCode.getRow(2).size) // bottom
    }
}
