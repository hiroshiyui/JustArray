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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.miyabi_hiroshi.app.justarray.R
import com.miyabi_hiroshi.app.justarray.data.prefs.UserPreferences
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    userPreferences: UserPreferences,
    modifier: Modifier = Modifier,
) {
    val scope = rememberCoroutineScope()

    val keyboardHeight by userPreferences.keyboardHeight.collectAsState(initial = 1.0f)
    val vibration by userPreferences.vibrationEnabled.collectAsState(initial = true)
    val sound by userPreferences.soundEnabled.collectAsState(initial = false)
    val showArrayLabels by userPreferences.showArrayLabels.collectAsState(initial = true)
    val shortCode by userPreferences.shortCodeEnabled.collectAsState(initial = true)
    val specialCode by userPreferences.specialCodeEnabled.collectAsState(initial = true)

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

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

        // Show Array labels
        SettingsSwitch(
            label = stringResource(R.string.settings_show_array_labels),
            checked = showArrayLabels,
            onCheckedChange = { scope.launch { userPreferences.setShowArrayLabels(it) } },
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
