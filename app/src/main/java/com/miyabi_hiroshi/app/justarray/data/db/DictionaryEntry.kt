package com.miyabi_hiroshi.app.justarray.data.db

import androidx.room.Entity
import androidx.room.Index

@Entity(
    tableName = "dictionary",
    primaryKeys = ["code", "character"],
    indices = [Index("code")]
)
data class DictionaryEntry(
    val code: String,
    val character: String,
    val frequency: Int = 0
)
