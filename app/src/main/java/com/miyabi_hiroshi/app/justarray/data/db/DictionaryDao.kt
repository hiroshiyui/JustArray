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

    // User candidates
    @Query("SELECT * FROM user_candidates WHERE code = :code ORDER BY frequency DESC")
    fun lookupUserCandidates(code: String): List<UserCandidate>

    @Query("INSERT OR REPLACE INTO user_candidates (code, character, frequency) VALUES (:code, :character, COALESCE((SELECT frequency FROM user_candidates WHERE code = :code AND character = :character), 0) + 1)")
    fun incrementUserFrequency(code: String, character: String)

    @Query("DELETE FROM user_candidates")
    fun clearUserCandidates()

    @Query("DELETE FROM dictionary")
    fun clearDictionary()

    @Query("DELETE FROM short_codes")
    fun clearShortCodes()

    @Query("DELETE FROM special_codes")
    fun clearSpecialCodes()

    // User phrases
    @Query("SELECT * FROM user_phrases WHERE code = :code")
    fun lookupUserPhrases(code: String): List<UserPhrase>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertUserPhrase(userPhrase: UserPhrase)

    @Query("DELETE FROM user_phrases WHERE code = :code AND phrase = :phrase")
    fun deleteUserPhrase(code: String, phrase: String)

    @Query("SELECT * FROM user_phrases ORDER BY code")
    fun getAllUserPhrases(): List<UserPhrase>

    // Utility
    @Query("SELECT COUNT(*) FROM dictionary")
    fun getDictionaryCount(): Int

    @Query("SELECT COUNT(*) FROM short_codes")
    fun getShortCodeCount(): Int

    @Query("SELECT COUNT(*) FROM special_codes")
    fun getSpecialCodeCount(): Int
}
