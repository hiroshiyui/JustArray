package com.miyabi_hiroshi.app.justarray.data.dictionary

import android.content.Context
import java.io.BufferedReader
import java.io.InputStreamReader

data class CinEntry(val code: String, val value: String)

class CinParser(private val context: Context) {

    fun parse(assetFileName: String): List<CinEntry> {
        return try {
            context.assets.open(assetFileName).use { inputStream ->
                BufferedReader(InputStreamReader(inputStream, "UTF-8")).use { reader ->
                    parse(reader)
                }
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    companion object {
        fun parse(reader: BufferedReader): List<CinEntry> {
            val entries = mutableListOf<CinEntry>()
            var insideCharDef = false

            reader.forEachLine { line ->
                val trimmed = line.trim()
                when {
                    trimmed == "%chardef begin" -> insideCharDef = true
                    trimmed == "%chardef end" -> insideCharDef = false
                    insideCharDef && trimmed.isNotEmpty() -> {
                        val parts = trimmed.split(Regex("\\s+"), limit = 2)
                        if (parts.size == 2) {
                            entries.add(CinEntry(parts[0], parts[1]))
                        }
                    }
                }
            }

            return entries
        }
    }
}
