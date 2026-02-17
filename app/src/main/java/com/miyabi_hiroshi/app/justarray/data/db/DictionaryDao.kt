package com.miyabi_hiroshi.app.justarray.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface DictionaryDao {
    // Main dictionary
    @Query("SELECT * FROM dictionary WHERE code = :code ORDER BY frequency DESC")
    fun lookupExact(code: String): List<DictionaryEntry>

    @Query("SELECT * FROM dictionary WHERE code LIKE :prefix || '%' ORDER BY frequency DESC")
    fun lookupPrefix(prefix: String): List<DictionaryEntry>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertDictionaryEntries(entries: List<DictionaryEntry>)

    @Query("UPDATE dictionary SET frequency = frequency + 1 WHERE code = :code AND character = :character")
    fun incrementFrequency(code: String, character: String)

    // Short codes
    @Query("SELECT * FROM short_codes WHERE code = :code ORDER BY priority ASC")
    fun lookupShortCode(code: String): List<ShortCodeEntry>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertShortCodeEntries(entries: List<ShortCodeEntry>)

    // Special codes
    @Query("SELECT * FROM special_codes WHERE code = :code")
    fun lookupSpecialCode(code: String): List<SpecialCodeEntry>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertSpecialCodeEntries(entries: List<SpecialCodeEntry>)

    // Utility
    @Query("SELECT COUNT(*) FROM dictionary")
    fun getDictionaryCount(): Int

    @Query("SELECT COUNT(*) FROM short_codes")
    fun getShortCodeCount(): Int

    @Query("SELECT COUNT(*) FROM special_codes")
    fun getSpecialCodeCount(): Int
}
