package com.miyabi_hiroshi.app.justarray

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.inputmethod.InputMethodManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.miyabi_hiroshi.app.justarray.data.prefs.UserPreferences
import com.miyabi_hiroshi.app.justarray.ui.settings.SettingsScreen
import com.miyabi_hiroshi.app.justarray.ui.settings.UserDictionaryScreen
import com.miyabi_hiroshi.app.justarray.ui.theme.JustArrayTheme
import com.miyabi_hiroshi.app.justarray.util.AppContainer

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val appContainer = AppContainer.getInstance(applicationContext)
        setContent {
            val theme by appContainer.userPreferences.theme
                .collectAsState(initial = UserPreferences.THEME_SYSTEM)
            val darkTheme = when (theme) {
                UserPreferences.THEME_LIGHT -> false
                UserPreferences.THEME_DARK -> true
                else -> isSystemInDarkTheme()
            }
            JustArrayTheme(darkTheme = darkTheme) {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    MainContent(
                        appContainer = appContainer,
                        modifier = Modifier.padding(innerPadding),
                    )
                }
            }
        }
    }
}

@Composable
private fun MainContent(
    appContainer: AppContainer,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    var showUserDictionary by remember { mutableStateOf(false) }

    if (showUserDictionary) {
        UserDictionaryScreen(
            dictionaryRepository = appContainer.dictionaryRepository,
            onBack = { showUserDictionary = false },
            modifier = modifier,
        )
        return
    }

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

        Spacer(modifier = Modifier.height(24.dp))

        // Inline settings
        SettingsScreen(
            userPreferences = appContainer.userPreferences,
            dictionaryRepository = appContainer.dictionaryRepository,
            database = appContainer.database,
            onOpenUserDictionary = { showUserDictionary = true },
        )
    }
}
