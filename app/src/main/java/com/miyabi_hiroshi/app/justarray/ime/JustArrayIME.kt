package com.miyabi_hiroshi.app.justarray.ime

import android.content.ClipboardManager
import android.inputmethodservice.InputMethodService
import android.os.Build
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ComposeView
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import androidx.compose.foundation.isSystemInDarkTheme
import com.miyabi_hiroshi.app.justarray.data.dictionary.DictLoadState
import com.miyabi_hiroshi.app.justarray.data.prefs.UserPreferences
import com.miyabi_hiroshi.app.justarray.ui.keyboard.KeyboardScreen
import com.miyabi_hiroshi.app.justarray.ui.keyboard.LocalKeyboardHeightScale
import com.miyabi_hiroshi.app.justarray.ui.theme.JustArrayTheme
import com.miyabi_hiroshi.app.justarray.util.AppContainer
import com.miyabi_hiroshi.app.justarray.util.HapticHelper

class JustArrayIME : InputMethodService() {

    private val lifecycleOwner = ImeLifecycleOwner()
    private val scope = MainScope()
    private lateinit var inputStateManager: InputStateManager
    private lateinit var hapticHelper: HapticHelper
    private lateinit var appContainer: AppContainer
    private var vibrationEnabled = true
    private val clipboardText = MutableStateFlow<String?>(null)

    override fun onCreate() {
        super.onCreate()
        lifecycleOwner.onCreate()
        hapticHelper = HapticHelper(this)

        appContainer = AppContainer.getInstance(applicationContext)
        inputStateManager = InputStateManager(
            dictionaryRepository = appContainer.dictionaryRepository,
            onCommitText = { text ->
                if (text == "\b") {
                    currentInputConnection?.deleteSurroundingText(1, 0)
                } else {
                    currentInputConnection?.commitText(text, 1)
                }
            },
            onSetComposingText = { text ->
                currentInputConnection?.setComposingText(text, 1)
            },
            onFinishComposing = {
                currentInputConnection?.finishComposingText()
            },
            onPerformEditorAction = { action ->
                currentInputConnection?.performEditorAction(action)
            }
        )

        scope.launch {
            appContainer.userPreferences.vibrationEnabled.collectLatest { enabled ->
                vibrationEnabled = enabled
            }
        }
    }

    private fun refreshClipboard() {
        val cm = getSystemService(ClipboardManager::class.java)
        val clip = cm?.primaryClip
        clipboardText.value = if (clip != null && clip.itemCount > 0) {
            clip.getItemAt(0).coerceToText(this)?.toString()?.takeIf { it.isNotBlank() }
        } else null
    }

    private fun switchIme() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            switchToNextInputMethod(false)
        } else {
            val imm = getSystemService(InputMethodManager::class.java)
            @Suppress("DEPRECATION")
            window?.window?.let { w ->
                imm?.switchToNextInputMethod(w.attributes.token, false)
            }
        }
    }

    override fun onCreateInputView(): View {
        // Set lifecycle owners on the IME window's decor view so Compose's
        // WindowRecomposer can find them when traversing the view tree.
        window?.window?.decorView?.let { decorView ->
            decorView.setViewTreeLifecycleOwner(lifecycleOwner)
            decorView.setViewTreeSavedStateRegistryOwner(lifecycleOwner)
        }

        val composeView = ComposeView(this).apply {
            setViewTreeLifecycleOwner(lifecycleOwner)
            setViewTreeSavedStateRegistryOwner(lifecycleOwner)
            setContent {
                val userPreferences = appContainer.userPreferences
                val vibrationEnabled by userPreferences.vibrationEnabled
                    .collectAsState(initial = true)
                val showArrayLabels by userPreferences.showArrayLabels
                    .collectAsState(initial = true)
                val keyboardHeight by userPreferences.keyboardHeight
                    .collectAsState(initial = 1.0f)
                val shortCodeEnabled by userPreferences.shortCodeEnabled
                    .collectAsState(initial = true)
                val specialCodeEnabled by userPreferences.specialCodeEnabled
                    .collectAsState(initial = true)
                val userCandidatesEnabled by userPreferences.userCandidatesEnabled
                    .collectAsState(initial = true)
                val theme by userPreferences.theme
                    .collectAsState(initial = UserPreferences.THEME_SYSTEM)
                val darkTheme = when (theme) {
                    UserPreferences.THEME_LIGHT -> false
                    UserPreferences.THEME_DARK -> true
                    else -> isSystemInDarkTheme()
                }

                LaunchedEffect(shortCodeEnabled) {
                    appContainer.dictionaryRepository.useShortCodes = shortCodeEnabled
                }
                LaunchedEffect(specialCodeEnabled) {
                    appContainer.dictionaryRepository.useSpecialCodes = specialCodeEnabled
                }
                LaunchedEffect(userCandidatesEnabled) {
                    appContainer.dictionaryRepository.useUserCandidates = userCandidatesEnabled
                }

                val clipboard by clipboardText.collectAsState()
                val dictLoadState by appContainer.dictionaryRepository.loadState
                    .collectAsState()

                CompositionLocalProvider(LocalKeyboardHeightScale provides keyboardHeight) {
                    JustArrayTheme(darkTheme = darkTheme) {
                        KeyboardScreen(
                            inputStateManager = inputStateManager,
                            showArrayLabels = showArrayLabels,
                            dictLoadState = dictLoadState,
                            clipboardText = clipboard,
                            onKeyPress = { if (vibrationEnabled) hapticHelper.vibrate() },
                            onSwitchIme = { switchIme() },
                            onClipboardPaste = { text ->
                                currentInputConnection?.commitText(text, 1)
                            },
                        )
                    }
                }
            }
        }
        lifecycleOwner.onResume()
        return composeView
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (event == null) return super.onKeyDown(keyCode, event)

        val consumed = when (keyCode) {
            KeyEvent.KEYCODE_SPACE -> {
                inputStateManager.onSpaceKey()
                true
            }
            KeyEvent.KEYCODE_DEL -> {
                inputStateManager.onBackspaceKey()
                true
            }
            KeyEvent.KEYCODE_ENTER -> {
                inputStateManager.onEnterKey()
                true
            }
            KeyEvent.KEYCODE_ESCAPE -> {
                inputStateManager.reset()
                true
            }
            KeyEvent.KEYCODE_SHIFT_LEFT, KeyEvent.KEYCODE_SHIFT_RIGHT -> {
                inputStateManager.onShiftKey()
                true
            }
            in KeyEvent.KEYCODE_0..KeyEvent.KEYCODE_9 -> {
                val digit = keyCode - KeyEvent.KEYCODE_0
                inputStateManager.onNumberKey(digit)
                true
            }
            else -> {
                val unicodeChar = event.unicodeChar
                if (unicodeChar != 0) {
                    val char = unicodeChar.toChar().lowercaseChar()
                    if (KeyCode.getArrayKey(char) != null) {
                        inputStateManager.onArrayKey(char)
                        true
                    } else {
                        false
                    }
                } else {
                    false
                }
            }
        }

        if (consumed) {
            if (vibrationEnabled) hapticHelper.vibrate()
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onStartInput(info: EditorInfo?, restarting: Boolean) {
        super.onStartInput(info, restarting)
        if (!restarting) {
            inputStateManager.reset()
        }
        inputStateManager.updateInputTypeClass(info?.inputType ?: 0)
        inputStateManager.updateImeOptions(info?.imeOptions ?: 0)
    }

    override fun onFinishInput() {
        super.onFinishInput()
        inputStateManager.reset()
    }

    override fun onWindowShown() {
        super.onWindowShown()
        lifecycleOwner.onResume()
        refreshClipboard()
    }

    override fun onWindowHidden() {
        super.onWindowHidden()
        lifecycleOwner.onPause()
    }

    override fun onDestroy() {
        scope.cancel()
        lifecycleOwner.onDestroy()
        super.onDestroy()
    }
}
