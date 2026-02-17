package com.miyabi_hiroshi.app.justarray.data.db

import androidx.room.Entity
import androidx.room.Index

@Entity(
    tableName = "short_codes",
    primaryKeys = ["code", "character"],
    indices = [Index("code")]
)
data class ShortCodeEntry(
    val code: String,
    val character: String,
    val priority: Int = 0
)
