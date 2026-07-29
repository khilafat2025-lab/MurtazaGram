# MurtazaGram

An enhanced Telegram client for Android with power features, built on the Telegram Bot API.

## Features

- **Ghost Mode** - Read messages without sending read receipts
- **Auto Translation** - Automatically translate incoming messages
- **Anti-Delete** - Preserve original message text even after sender deletes
- **Fast Downloads** - 16 concurrent connections for faster file downloads
- **Pin up to 20 messages** per chat (vs Telegram's default limit)
- **Forward from restricted chats** - Bypass no-forward restrictions
- **Copy from restricted chats** - Copy text even when restricted
- **User ID on profile** - Always visible, one-tap copy
- **Filter tabs** - All / Chats / Groups / Channels / Bots
- **Original quality downloads** - Download media in original quality

## Tech Stack

- **Language:** Java
- **API:** Telegram Bot API (https://api.telegram.org)
- **Min SDK:** 21 (Android 5.0 Lollipop)
- **Target SDK:** 34 (Android 14)
- **Architecture:** Activity-based with ListView adapters

## Building

### Prerequisites
- Android Studio or Gradle 7.4+
- Android SDK 34
- Java 8+

### Build from source
```bash
git clone https://github.com/khilafat2025-lab/MurtazaGram.git
cd MurtazaGram
./gradlew assembleDebug
```

The APK will be at `app/build/outputs/apk/debug/app-debug.apk`

## Usage

1. Get a Bot Token from [@BotFather](https://t.me/BotFather) on Telegram
2. Install MurtazaGram APK
3. Enter your Bot Token on the login screen
4. Start chatting with all power features enabled

## Project Structure

```
app/src/main/
├── java/com/murtaza/gram/
│   ├── api/          # TelegramApiClient - Bot API wrapper
│   ├── model/        # Chat, Message, User data models
│   ├── adapter/      # ChatAdapter, MessageAdapter
│   ├── ui/           # Activities (Splash, Login, Main, Chat, Settings, Profile)
│   └── util/         # MurtazaGramConfig, MurtazaGramUtils
├── res/              # Layouts, drawables, values, icons
└── AndroidManifest.xml
```

## Disclaimer

MurtazaGram is not affiliated with Telegram. It uses the official Telegram Bot API.
Use responsibly and in accordance with Telegram's Terms of Service.

## License

MIT License