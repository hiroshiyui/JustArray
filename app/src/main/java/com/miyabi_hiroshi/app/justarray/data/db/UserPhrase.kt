package com.miyabi_hiroshi.app.justarray.data.db

import androidx.room.Entity
import androidx.room.Index

@Entity(
    tableName = "user_phrases",
    primaryKeys = ["code", "phrase"],
    indices = [Index("code")]
)
data class UserPhrase(
    val code: String,
    val phrase: String,
)
