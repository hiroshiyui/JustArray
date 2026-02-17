package com.miyabi_hiroshi.app.justarray.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "english_word_frequencies")
data class EnglishWordFrequency(
    @PrimaryKey
    val word: String,
    val frequency: Int = 0
)
