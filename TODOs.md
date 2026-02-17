# TODOs

## Input & State Machine

- [ ] Support Shift key for uppercase English mode input

## Keyboard UI

- [ ] Add landscape keyboard layout
- [ ] Improve key touch target — consider `pointerInput` for swipe gestures and better touch handling

## Dictionary

- [ ] Batch Room inserts in `DictionaryInitializer` — currently inserts all entries in a single call which may cause ANR on large dictionaries
- [ ] Add progress reporting during dictionary initialization (show `dict_loading` string)
- [ ] Handle dictionary load failure gracefully in UI (show `dict_not_found` string)
- [ ] Replace Java `Serializable` Trie serialization with a more efficient binary format (protobuf or custom)
- [ ] Add dictionary version tracking to detect when .cin files are updated

## Settings & Preferences

- [ ] Wire `theme` preference (system/light/dark) to `JustArrayTheme` — currently always follows system
- [ ] Add "About" section with version info and credits
- [ ] Add dictionary management UI (reimport, clear frequency data)

## Architecture & Quality

All items completed. Hilt DI migration was considered and decided against — `AppContainer` is simple (~30 lines, 3 dependencies) and Hilt would add disproportionate complexity at current scale.

## Polish

- [ ] Add IME switching key (globe icon) for multi-IME users
- [ ] Support clipboard paste preview
- [ ] Add user dictionary / custom phrases
- [ ] Accessibility: TalkBack content descriptions for keys
- [ ] Accessibility: TalkBack content descriptions for candidates
