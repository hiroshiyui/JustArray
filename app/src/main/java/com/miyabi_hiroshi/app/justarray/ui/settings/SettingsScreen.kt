package com.miyabi_hiroshi.app.justarray.ui.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.miyabi_hiroshi.app.justarray.BuildConfig
import com.miyabi_hiroshi.app.justarray.R
import com.miyabi_hiroshi.app.justarray.data.db.ArrayDatabase
import com.miyabi_hiroshi.app.justarray.data.dictionary.DictionaryRepository
import com.miyabi_hiroshi.app.justarray.data.prefs.UserPreferences
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    userPreferences: UserPreferences,
    dictionaryRepository: DictionaryRepository,
    database: ArrayDatabase,
    onOpenUserDictionary: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val keyboardHeight by userPreferences.keyboardHeight.collectAsState(initial = 1.0f)
    val vibration by userPreferences.vibrationEnabled.collectAsState(initial = true)
    val sound by userPreferences.soundEnabled.collectAsState(initial = false)
    val theme by userPreferences.theme.collectAsState(initial = UserPreferences.THEME_SYSTEM)
    val showArrayLabels by userPreferences.showArrayLabels.collectAsState(initial = true)
    val showReverseCodes by userPreferences.showReverseCodes.collectAsState(initial = false)
    val shortCode by userPreferences.shortCodeEnabled.collectAsState(initial = true)
    val specialCode by userPreferences.specialCodeEnabled.collectAsState(initial = true)
    val userCandidates by userPreferences.userCandidatesEnabled.collectAsState(initial = true)

    var showClearDialog by remember { mutableStateOf(false) }
    var showReimportDialog by remember { mutableStateOf(false) }
    var reimporting by remember { mutableStateOf(false) }

    val themeOptions = listOf(
        UserPreferences.THEME_SYSTEM to stringResource(R.string.settings_theme_system),
        UserPreferences.THEME_LIGHT to stringResource(R.string.settings_theme_light),
        UserPreferences.THEME_DARK to stringResource(R.string.settings_theme_dark),
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            text = stringResource(R.string.settings_title),
            style = MaterialTheme.typography.headlineMedium,
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Keyboard height
        Text(
            text = stringResource(R.string.settings_keyboard_height),
            style = MaterialTheme.typography.titleMedium,
        )
        Slider(
            value = keyboardHeight,
            onValueChange = { scope.launch { userPreferences.setKeyboardHeight(it) } },
            valueRange = 0.7f..1.5f,
            modifier = Modifier.fillMaxWidth(),
        )

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

        // Vibration
        SettingsSwitch(
            label = stringResource(R.string.settings_vibration),
            checked = vibration,
            onCheckedChange = { scope.launch { userPreferences.setVibrationEnabled(it) } },
        )

        // Sound
        SettingsSwitch(
            label = stringResource(R.string.settings_sound),
            checked = sound,
            onCheckedChange = { scope.launch { userPreferences.setSoundEnabled(it) } },
        )

        // Theme
        Text(
            text = stringResource(R.string.settings_theme),
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(vertical = 8.dp),
        )
        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
            themeOptions.forEachIndexed { index, (value, label) ->
                SegmentedButton(
                    selected = theme == value,
                    onClick = { scope.launch { userPreferences.setTheme(value) } },
                    shape = SegmentedButtonDefaults.itemShape(
                        index = index,
                        count = themeOptions.size,
                    ),
                ) {
                    Text(label)
                }
            }
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

        // Show Array labels
        SettingsSwitch(
            label = stringResource(R.string.settings_show_array_labels),
            checked = showArrayLabels,
            onCheckedChange = { scope.launch { userPreferences.setShowArrayLabels(it) } },
        )

        // Show reverse codes in candidates
        SettingsSwitch(
            label = stringResource(R.string.settings_show_reverse_codes),
            checked = showReverseCodes,
            onCheckedChange = { scope.launch { userPreferences.setShowReverseCodes(it) } },
        )

        // Short codes
        SettingsSwitch(
            label = stringResource(R.string.settings_short_code),
            checked = shortCode,
            onCheckedChange = { scope.launch { userPreferences.setShortCodeEnabled(it) } },
        )

        // Special codes
        SettingsSwitch(
            label = stringResource(R.string.settings_special_code),
            checked = specialCode,
            onCheckedChange = { scope.launch { userPreferences.setSpecialCodeEnabled(it) } },
        )

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

        // User preferred candidates
        SettingsSwitch(
            label = stringResource(R.string.settings_user_candidates),
            checked = userCandidates,
            onCheckedChange = { scope.launch { userPreferences.setUserCandidatesEnabled(it) } },
        )

        if (userCandidates) {
            OutlinedButton(
                onClick = { showClearDialog = true },
                modifier = Modifier.padding(start = 16.dp, top = 4.dp),
            ) {
                Text(text = stringResource(R.string.settings_clear_user_candidates))
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // User Dictionary
        OutlinedButton(
            onClick = onOpenUserDictionary,
            modifier = Modifier.padding(start = 16.dp, top = 4.dp),
        ) {
            Text(text = stringResource(R.string.settings_user_dictionary))
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Reimport Dictionary
        if (reimporting) {
            Row(
                modifier = Modifier.padding(start = 16.dp, top = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.padding(end = 8.dp),
                )
                Text(text = stringResource(R.string.settings_dict_reimporting))
            }
        } else {
            OutlinedButton(
                onClick = { showReimportDialog = true },
                modifier = Modifier.padding(start = 16.dp, top = 4.dp),
            ) {
                Text(text = stringResource(R.string.settings_dict_reimport))
            }
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

        // About
        Text(
            text = stringResource(R.string.settings_about),
            style = MaterialTheme.typography.titleMedium,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.settings_about_version, BuildConfig.VERSION_NAME),
            style = MaterialTheme.typography.bodyMedium,
        )
        Text(
            text = stringResource(R.string.settings_about_author),
            style = MaterialTheme.typography.bodyMedium,
        )
        Text(
            text = stringResource(R.string.settings_about_license),
            style = MaterialTheme.typography.bodyMedium,
        )
        Text(
            text = stringResource(R.string.settings_about_array_credit),
            style = MaterialTheme.typography.bodyMedium,
        )
    }

    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = { Text(text = stringResource(R.string.settings_clear_user_candidates)) },
            text = { Text(text = stringResource(R.string.settings_clear_user_candidates_confirm)) },
            confirmButton = {
                TextButton(onClick = {
                    dictionaryRepository.clearUserCandidates()
                    showClearDialog = false
                }) {
                    Text(text = stringResource(android.R.string.ok))
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) {
                    Text(text = stringResource(android.R.string.cancel))
                }
            },
        )
    }

    if (showReimportDialog) {
        AlertDialog(
            onDismissRequest = { showReimportDialog = false },
            title = { Text(text = stringResource(R.string.settings_dict_reimport)) },
            text = { Text(text = stringResource(R.string.settings_dict_reimport_confirm)) },
            confirmButton = {
                TextButton(onClick = {
                    showReimportDialog = false
                    reimporting = true
                    scope.launch {
                        dictionaryRepository.reimport(context, database)
                        reimporting = false
                    }
                }) {
                    Text(text = stringResource(android.R.string.ok))
                }
            },
            dismissButton = {
                TextButton(onClick = { showReimportDialog = false }) {
                    Text(text = stringResource(android.R.string.cancel))
                }
            },
        )
    }
}

@Composable
private fun SettingsSwitch(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f),
        )
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
        )
    }
}
