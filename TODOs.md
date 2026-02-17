# TODOs

## Input & State Machine

- [ ] Add reverse lookup (character → code) for learning Array codes
- [ ] Add clipboard history (last N items) shown in candidate bar when Idle

## Keyboard UI

- [ ] Add visual feedback when composing buffer is full (5th key silently ignored)
- [ ] Show uppercase key labels when Shift/CapsLock is active in English mode

## Dictionary & Performance

- [x] Use lazy `Sequence` + `take(limit)` in `ArrayTrie.prefixLookup()` to avoid collecting entire subtree for common English prefixes
- [ ] Cache user candidate frequency map in memory to avoid Room DB hit on every keystroke lookup
- [ ] Track English word usage frequency for better prediction ranking

## Testing

- [ ] Add tests for `DictionaryRepository.lookup()` priority and deduplication logic (special → short → main)
- [ ] Add trie serialization round-trip test (`TrieSerializer`)
- [ ] Add multi-step state machine integration tests (compose → select → continue composing → commit)
