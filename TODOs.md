# TODOs

## Input & State Machine

- [ ] Support Shift key for uppercase English mode input

## Keyboard UI

- [ ] Add landscape keyboard layout
- [ ] Improve key touch target — consider `pointerInput` for swipe gestures and better touch handling

## Dictionary

- [x] Batch Room inserts in `DictionaryInitializer` — chunked(500) with runInTransaction
- [x] Add progress reporting during dictionary initialization (show `dict_loading` string)
- [x] Handle dictionary load failure gracefully in UI (show `dict_not_found` string)
- [x] Replace Java `Serializable` Trie serialization with custom binary format (magic bytes + DFS)
- [x] Add dictionary version tracking to detect when .cin files are updated (SHA-256 fingerprint)
