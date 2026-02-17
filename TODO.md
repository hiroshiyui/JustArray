# TODO

## Input & State Machine

- [ ] Handle physical keyboard events (`onKeyDown`/`onKeyUp` in `JustArrayIME`)
- [ ] Wire `onNumberKey()` — number row keys (1–0) for direct candidate selection are defined in `InputStateManager` but never called from UI
- [ ] Add audio feedback (`SoundPool` or `AudioManager.playSoundEffect`) — `settings_sound` preference exists but is not wired
- [ ] Wire `vibrationEnabled` preference to `HapticHelper` — currently always vibrates regardless of setting
- [ ] Handle "no candidates" case in `onSpaceKey()` — currently silently ignored (should beep or show indicator)
- [ ] Handle `EditorInfo.imeOptions` (e.g. `IME_ACTION_SEARCH`, `IME_ACTION_SEND`) to change Enter key behavior/label
- [ ] Support Shift key for uppercase English mode input
- [ ] Respect `EditorInfo.inputType` (e.g. `TYPE_CLASS_NUMBER` should show number keyboard)

## Keyboard UI

- [ ] Wire `showArrayLabels` user preference to `ArrayKeyboard` — currently hardcoded to `true` in `KeyboardScreen`
- [ ] Wire `keyboardHeight` user preference to scale keyboard layout
- [ ] Add key press visual feedback (pressed state / ripple animation using `keyPressedBackground` color)
- [ ] Add key press popup preview (magnified character on long press)
- [ ] Add long-press handling (e.g. key repeat for backspace, alternate characters)
- [ ] Add number row or number keyboard mode
- [ ] Add landscape keyboard layout
- [ ] Improve key touch target — consider `pointerInput` for swipe gestures and better touch handling

## Candidate UI

- [ ] Show page indicator in `CandidateBar` (e.g. "2/5")
- [ ] Add vertical candidate list mode (as alternative to horizontal scroll)
- [ ] Sort candidates by frequency from Room DB (currently Trie insertion order only)

## Dictionary

- [ ] Batch Room inserts in `DictionaryInitializer` — currently inserts all entries in a single call which may cause ANR on large dictionaries
- [ ] Add progress reporting during dictionary initialization (show `dict_loading` string)
- [ ] Handle dictionary load failure gracefully in UI (show `dict_not_found` string)
- [ ] Run `incrementFrequency()` on a background thread — currently called synchronously on main thread
- [ ] Replace Java `Serializable` Trie serialization with a more efficient binary format (protobuf or custom)
- [ ] Add dictionary version tracking to detect when .cin files are updated

## Settings & Preferences

- [ ] Wire `theme` preference (system/light/dark) to `JustArrayTheme` — currently always follows system
- [ ] Wire `shortCodeEnabled` / `specialCodeEnabled` preferences to `DictionaryRepository.useShortCodes` / `useSpecialCodes`
- [ ] Implement "Open Preferences" button navigation in `MainActivity` — currently a no-op
- [ ] Add "About" section with version info and credits
- [ ] Add dictionary management UI (reimport, clear frequency data)

## Architecture & Quality

- [ ] Add unit tests for `InputStateManager` state transitions
- [ ] Add unit tests for `ArrayTrie` (insert, lookup, prefixLookup)
- [ ] Add unit tests for `CinParser`
- [ ] Add instrumented tests for `DictionaryDao`
- [ ] Add UI tests for keyboard rendering
- [ ] Add ProGuard/R8 rules for release builds
- [ ] Consider migrating from `AppContainer` to Hilt for DI
- [ ] Add Timber or structured logging for debugging

## Polish

- [ ] Design proper app icon (currently using Android default)
- [ ] Add onboarding / first-launch tutorial
- [ ] Add IME switching key (globe icon) for multi-IME users
- [ ] Support clipboard paste preview
- [ ] Add user dictionary / custom phrases
- [ ] Accessibility: TalkBack content descriptions for keys
