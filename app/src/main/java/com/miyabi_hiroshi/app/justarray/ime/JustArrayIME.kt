package com.miyabi_hiroshi.app.justarray.ime

import android.inputmethodservice.InputMethodService
import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.compose.ui.platform.ComposeView
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.miyabi_hiroshi.app.justarray.ui.keyboard.KeyboardScreen
import com.miyabi_hiroshi.app.justarray.ui.theme.JustArrayTheme
import com.miyabi_hiroshi.app.justarray.util.AppContainer
import com.miyabi_hiroshi.app.justarray.util.HapticHelper

class JustArrayIME : InputMethodService() {

    private val lifecycleOwner = ImeLifecycleOwner()
    private lateinit var inputStateManager: InputStateManager
    private lateinit var hapticHelper: HapticHelper

    override fun onCreate() {
        super.onCreate()
        lifecycleOwner.onCreate()
        hapticHelper = HapticHelper(this)

        val appContainer = AppContainer.getInstance(applicationContext)
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
            }
        )
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
                JustArrayTheme {
                    KeyboardScreen(
                        inputStateManager = inputStateManager,
                        onKeyPress = { hapticHelper.vibrate() }
                    )
                }
            }
        }
        lifecycleOwner.onResume()
        return composeView
    }

    override fun onStartInput(info: EditorInfo?, restarting: Boolean) {
        super.onStartInput(info, restarting)
        if (!restarting) {
            inputStateManager.reset()
        }
    }

    override fun onFinishInput() {
        super.onFinishInput()
        inputStateManager.reset()
    }

    override fun onWindowShown() {
        super.onWindowShown()
        lifecycleOwner.onResume()
    }

    override fun onWindowHidden() {
        super.onWindowHidden()
        lifecycleOwner.onPause()
    }

    override fun onDestroy() {
        lifecycleOwner.onDestroy()
        super.onDestroy()
    }
}
