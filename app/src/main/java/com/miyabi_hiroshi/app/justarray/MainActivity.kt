package com.miyabi_hiroshi.app.justarray

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.inputmethod.InputMethodManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.miyabi_hiroshi.app.justarray.ui.settings.SettingsScreen
import com.miyabi_hiroshi.app.justarray.ui.theme.JustArrayTheme
import com.miyabi_hiroshi.app.justarray.util.AppContainer

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            JustArrayTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    MainContent(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
private fun MainContent(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val appContainer = AppContainer.getInstance(context.applicationContext)

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Top,
    ) {
        // Setup wizard section
        Text(
            text = stringResource(R.string.setup_title),
            style = MaterialTheme.typography.headlineMedium,
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Step 1: Enable IME
        Text(
            text = stringResource(R.string.setup_step1_enable),
            style = MaterialTheme.typography.titleMedium,
        )
        Text(
            text = stringResource(R.string.setup_step1_desc),
            style = MaterialTheme.typography.bodyMedium,
        )
        Button(
            onClick = {
                context.startActivity(Intent(Settings.ACTION_INPUT_METHOD_SETTINGS))
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
        ) {
            Text(stringResource(R.string.setup_open_settings))
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Step 2: Select IME
        Text(
            text = stringResource(R.string.setup_step2_select),
            style = MaterialTheme.typography.titleMedium,
        )
        Text(
            text = stringResource(R.string.setup_step2_desc),
            style = MaterialTheme.typography.bodyMedium,
        )
        Button(
            onClick = {
                val imm = context.getSystemService(InputMethodManager::class.java)
                imm?.showInputMethodPicker()
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
        ) {
            Text(stringResource(R.string.setup_select_ime))
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Settings button
        OutlinedButton(
            onClick = {
                // Navigate to settings (same activity, different content)
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
        ) {
            Text(stringResource(R.string.setup_open_preferences))
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Inline settings
        SettingsScreen(userPreferences = appContainer.userPreferences)
    }
}
