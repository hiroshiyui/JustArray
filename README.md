# JustArray (就是行列)

An Android Input Method Editor (IME) for the Array 30 Input Method (行列30輸入法), built with Jetpack Compose and Material 3.

## Features

- Array 30 key layout with QWERTY mapping
- Short code (簡碼) support
- Special code (特別碼) support
- Candidate selection with number keys
- Symbol input with categorized panels
- English/Chinese mode toggle
- Customizable keyboard height, vibration, and theme

## Setup

1. Download `.cin` dictionary files from [gontera/array30](https://github.com/gontera/array30)
2. Place the following files in `app/src/main/assets/`:
   - `array30-OpenVanilla-big-v2023-1.0-20230211.cin` (main dictionary)
   - `array-shortcode-20210725.cin` (short codes)
   - `array-special-201509.cin` (special codes)
3. Build and install the app
4. Enable "JustArray" (就是行列) in Settings → Language & Input → On-screen keyboard

## Acknowledgements

- **Array Input Method** was created by **廖明德 (Liao Ming-De)**
- Dictionary data is from the [gontera/array30](https://github.com/gontera/array30) repository, which provides OpenVanilla `.cin` format dictionary files authorized for free distribution for third-party IME developers
