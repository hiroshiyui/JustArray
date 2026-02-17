# JustArray (就是行列)

An Android Input Method Editor (IME) for the Array 30 Input Method (行列30輸入法), built with Jetpack Compose.

Requires Android 7.0 (API 24) or later.

## Features

### Input

- Array 30 key layout with QWERTY mapping
- Short code (簡碼) and special code (特別碼) support, toggleable in settings
- Candidate selection with number keys and on-screen page navigation
- Auto-select when only one candidate matches
- Frequency-based candidate sorting (learns from usage)
- Pre-edit buffer for continuous phrase input
- User dictionary for custom code-phrase pairs
- Symbol input with categorized panels
- English/Chinese mode toggle with word prediction
- Shift key for uppercase in English mode (one-shot and caps lock)
- Long-press keys for accented character alternates
- Swipe up on keys for quick uppercase input
- Number/phone keypad for numeric input fields
- Context-aware Enter key (搜尋/傳送/完成 etc. based on editor action)
- Clipboard paste preview in candidate bar
- Physical keyboard support
- IME switching key for multi-IME users

### Appearance

- Light, dark, or system-following theme
- Dynamic Material You theming (wallpaper-derived colors on Android 12+)
- Split keyboard layout in landscape orientation
- Adjustable keyboard height
- Toggleable Array label overlay on keys

### Accessibility

- TalkBack content descriptions for keys, candidates, and controls

### Settings

- Dictionary reimport and user candidate management
- Short code, special code, and user candidate toggles
- Vibration feedback toggle

## Setup

1. Download `.cin` dictionary files from [gontera/array30](https://github.com/gontera/array30)
2. Place the following files in `app/src/main/assets/`:
   - `array30-OpenVanilla-big-v2023-1.0-20230211.cin` (main dictionary)
   - `array-shortcode-20210725.cin` (short codes)
   - `array-special-201509.cin` (special codes)
3. Build and install the app
4. Enable "JustArray" (就是行列) in Settings → Language & Input → On-screen keyboard

## Build

```sh
./gradlew assembleDebug
```

## License

This project is licensed under the [GNU General Public License v3.0](LICENSE).

The `.cin` dictionary files in `app/src/main/assets/` are licensed separately under the
[Array Input Method Public License](https://array.com.tw/company/array_license.pdf).
These files are sourced from the [gontera/array30](https://github.com/gontera/array30)
repository (tag `v2023-1.0-20230211`).

## Acknowledgements

- **Array Input Method** was created by **廖明德 (Liao Ming-De)**
- Dictionary data is from the [gontera/array30](https://github.com/gontera/array30) repository, which provides OpenVanilla `.cin` format dictionary files under the Array Input Method Public License
- English word list (`english_words.txt`) is from [first20hours/google-10000-english](https://github.com/first20hours/google-10000-english) (public domain), derived from Google's Trillion Word Corpus
