package com.miyabi_hiroshi.app.justarray.data.db

import androidx.room.Entity
import androidx.room.Index

@Entity(
    tableName = "user_candidates",
    primaryKeys = ["code", "character"],
    indices = [Index("code")]
)
data class UserCandidate(
    val code: String,
    val character: String,
    val frequency: Int = 0
)
