package com.miyabi_hiroshi.app.justarray.data.db

import androidx.room.Entity
import androidx.room.Index

@Entity(
    tableName = "special_codes",
    primaryKeys = ["code", "character"],
    indices = [Index("code")]
)
data class SpecialCodeEntry(
    val code: String,
    val character: String,
    val description: String = ""
)
