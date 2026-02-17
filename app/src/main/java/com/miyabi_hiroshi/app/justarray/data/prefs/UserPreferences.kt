package com.miyabi_hiroshi.app.justarray.data.prefs

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "user_preferences")

class UserPreferences(private val context: Context) {

    companion object {
        val KEY_KEYBOARD_HEIGHT = floatPreferencesKey("keyboard_height")
        val KEY_VIBRATION = booleanPreferencesKey("vibration_enabled")
        val KEY_SOUND = booleanPreferencesKey("sound_enabled")
        val KEY_THEME = stringPreferencesKey("theme")
        val KEY_SHOW_ARRAY_LABELS = booleanPreferencesKey("show_array_labels")
        val KEY_SHORT_CODE = booleanPreferencesKey("short_code_enabled")
        val KEY_SPECIAL_CODE = booleanPreferencesKey("special_code_enabled")
        val KEY_USER_CANDIDATES = booleanPreferencesKey("user_candidates_enabled")
        val KEY_SHOW_REVERSE_CODES = booleanPreferencesKey("show_reverse_codes")

        const val THEME_SYSTEM = "system"
        const val THEME_LIGHT = "light"
        const val THEME_DARK = "dark"
    }

    val keyboardHeight: Flow<Float> = context.dataStore.data.map { prefs ->
        prefs[KEY_KEYBOARD_HEIGHT] ?: 1.0f
    }

    val vibrationEnabled: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_VIBRATION] ?: true
    }

    val soundEnabled: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_SOUND] ?: false
    }

    val theme: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[KEY_THEME] ?: THEME_SYSTEM
    }

    val showArrayLabels: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_SHOW_ARRAY_LABELS] ?: true
    }

    val shortCodeEnabled: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_SHORT_CODE] ?: true
    }

    val specialCodeEnabled: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_SPECIAL_CODE] ?: true
    }

    val userCandidatesEnabled: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_USER_CANDIDATES] ?: true
    }

    val showReverseCodes: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_SHOW_REVERSE_CODES] ?: false
    }

    suspend fun setKeyboardHeight(height: Float) {
        context.dataStore.edit { it[KEY_KEYBOARD_HEIGHT] = height }
    }

    suspend fun setVibrationEnabled(enabled: Boolean) {
        context.dataStore.edit { it[KEY_VIBRATION] = enabled }
    }

    suspend fun setSoundEnabled(enabled: Boolean) {
        context.dataStore.edit { it[KEY_SOUND] = enabled }
    }

    suspend fun setTheme(theme: String) {
        context.dataStore.edit { it[KEY_THEME] = theme }
    }

    suspend fun setShowArrayLabels(show: Boolean) {
        context.dataStore.edit { it[KEY_SHOW_ARRAY_LABELS] = show }
    }

    suspend fun setShortCodeEnabled(enabled: Boolean) {
        context.dataStore.edit { it[KEY_SHORT_CODE] = enabled }
    }

    suspend fun setSpecialCodeEnabled(enabled: Boolean) {
        context.dataStore.edit { it[KEY_SPECIAL_CODE] = enabled }
    }


    suspend fun setUserCandidatesEnabled(enabled: Boolean) {
        context.dataStore.edit { it[KEY_USER_CANDIDATES] = enabled }
    }

    suspend fun setShowReverseCodes(show: Boolean) {
        context.dataStore.edit { it[KEY_SHOW_REVERSE_CODES] = show }
    }
}
