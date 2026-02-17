# JustArray (就是行列)

An Android Input Method Editor (IME) for the Array 30 Input Method (行列30輸入法).

## Features

- Array 30 key layout with QWERTY mapping
- Short code (簡碼) and special code (特別碼) support, toggleable in settings
- Candidate selection with number keys and on-screen page navigation (⬅/➡)
- Auto-select when only one candidate matches
- Frequency-based candidate sorting (learns from usage)
- Horizontal or vertical candidate list (configurable)
- Pre-edit buffer for continuous phrase input
- Symbol input with categorized panels
- English/Chinese mode toggle with word prediction (disabled in password fields)
- Context-aware Enter key (搜尋/傳送/完成 etc. based on editor action)
- Physical keyboard support
- Dynamic Material You theming (wallpaper-derived colors on Android 12+)
- Customizable keyboard vibration and Array label visibility

## Setup

1. Download `.cin` dictionary files from [gontera/array30](https://github.com/gontera/array30)
2. Place the following files in `app/src/main/assets/`:
   - `array30-OpenVanilla-big-v2023-1.0-20230211.cin` (main dictionary)
   - `array-shortcode-20210725.cin` (short codes)
   - `array-special-201509.cin` (special codes)
3. Build and install the app
4. Enable "JustArray" (就是行列) in Settings → Language & Input → On-screen keyboard

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
