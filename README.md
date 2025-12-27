# 🎵 Blaze Music Player

![Android](https://img.shields.io/badge/Android-3DDC84?style=for-the-badge&logo=android&logoColor=white)
![Kotlin](https://img.shields.io/badge/Kotlin-0095D5?style=for-the-badge&logo=kotlin&logoColor=white)
![Material Design](https://img.shields.io/badge/Material%20Design-757575?style=for-the-badge&logo=material-design&logoColor=white)
![Version](https://img.shields.io/badge/Version-3.0-blue?style=for-the-badge)
![Min API](https://img.shields.io/badge/Min%20API-24-orange?style=for-the-badge)

**Blaze Music** is a stunning, modern, and feature-rich offline music player for Android. Built with passion using Kotlin, it offers a premium listening experience with a beautiful aesthetic design.

---

## ✨ Key Features

*   **🎨 Beautiful & Dynamic UI**: Experience a sleek interface with multiple themes (Cool Pink, Cool Blue, Cool Purple, Cool Green, Cool Black) that adapts to your style. Theme selection persists across app sessions with automatic app restart for immediate application.
*   **📱 Edge-to-Edge Design**: Fully immersive layout that looks great on modern Android devices, utilizing the full screen real estate.
*   **📂 Local Music Scanning**: Automatically finds and organizes all audio files from your device storage.
*   **⏯ Robust Playback Controls**:
    *   Shuffle & Repeat functionality.
    *   Seamless Mini Player at the bottom of the screen (tap to expand).
    *   Interactive notification controls with click-to-navigate support - tap the notification to jump directly to the currently playing song.
*   **❤️ Favorites System**: Mark songs as favorites with persistent storage - your favorites are saved and restored across app sessions. Browse your favorite tracks in a dedicated grid view, and shuffle play all favorites with one tap.
*   **📜 Playlist Management**: Create custom playlists with personalized names and creator info, persisted across app sessions. Add songs to playlists with integrated search, shuffle play entire playlists, remove individual songs or clear all songs at once. View playlist details with song count, creation date, and dynamic cover art. Automatic cleanup of deleted files.
*   **⏱ Sleep Timer**: Fall asleep to your music without worry; set a timer to automatically stop playback.
*   **🔍 Search**: Integrated search bar to quickly find and play specific tracks from your library.
*   **📲 Share**: Share your favorite tracks with friends directly from the app.
*   **📧 Feedback**: Send feedback via email directly from the app with topic and message fields.
*   **⚙️ Settings & About**: Access app settings and view app information including version details from the navigation drawer.

---

## 🛠️ Tech Stack

*   **Language**: Kotlin
*   **UI Toolkit**: XML Layouts with Material Design Components
*   **Image Loading**: Glide (v4.x) with KSP annotation processing
*   **Navigation**: Intent-based navigation with Navigation Drawer
*   **Data Persistence**: SharedPreferences with Gson (v2.8.6) for favorites and playlist storage
*   **Audio Playback**: MediaPlayer with foreground Service architecture
*   **Audio Management**: AudioManager with proper audio focus handling
*   **Notifications**: AndroidX Media library for playback notifications
*   **Email Integration**: JavaMail API (v1.6.4) for feedback functionality
*   **Build System**: Gradle with Kotlin DSL
*   **Min SDK**: 24 (Android 7.0 Nougat)
*   **Target SDK**: 36

---

## 🏗️ Architecture

### App Structure
*   **Service-Based Playback**: Music playback runs in a foreground service (`MusicService`) to ensure uninterrupted playback even when the app is in the background.
*   **Notification Controls**: Rich media notifications with play/pause, previous/next controls, and tap-to-navigate functionality.
*   **Data Persistence**: All user data (favorites, playlists, theme preferences) is stored using SharedPreferences with Gson serialization.
*   **Permission Handling**: Runtime permission requests for storage access (API 24-32) and media permissions (API 33+), plus notification permissions (API 33+).

### Key Components
*   **MainActivity**: Main entry point with music library display and search functionality
*   **PlayerActivity**: Full-screen player with playback controls and song details
*   **MusicService**: Background service managing MediaPlayer and notifications
*   **FavActivity**: Favorites management with grid view and shuffle play
*   **PlaylistActivity**: Playlist creation and management
*   **PlaylistDetails**: Individual playlist view with song management
*   **SettingsActivity**: Theme selection with app restart mechanism
*   **FeedbackActivity**: Email-based feedback system
*   **AboutActivity**: App information and version details

---

## 📋 Permissions

The app requires the following permissions:

*   **Storage Access** (API 24-32): `READ_EXTERNAL_STORAGE`, `WRITE_EXTERNAL_STORAGE`
*   **Media Access** (API 33+): `READ_MEDIA_AUDIO`
*   **Notifications** (API 33+): `POST_NOTIFICATIONS`

All permissions are requested at runtime with proper fallback handling.

---

## 🚀 Getting Started

To get a local copy up and running follow these simple steps.

### Prerequisites

*   [Android Studio](https://developer.android.com/studio) (Latest version recommended)
*   Java Development Kit (JDK) 11 or higher
*   Android device or emulator running Android 7.0 (API 24) or higher
*   Storage permissions granted for music scanning

### Installation

1.  **Clone the repository**
    ```sh
    git clone https://github.com/Im-diablo/Music-App-With-Kotlin.git
    ```
2.  **Open in Android Studio**
    *   Start Android Studio.
    *   Select "Open an existing Android Studio project".
    *   Navigate to the cloned directory and select it.
3.  **Build the Project**
    *   Let Gradle sync and download dependencies.
    *   Click the "Run" button (Green arrow) to deploy to your emulator or physical device.

---

---

## 🔧 Troubleshooting

### Common Issues

**No music files showing:**
- Ensure storage/media permissions are granted
- Check that audio files are in a supported format (MP3, M4A, WAV, etc.)
- Try rescanning by closing and reopening the app

**Theme not applying:**
- Accept the app restart prompt when changing themes
- Theme changes require a full app restart to take effect

**Playback issues:**
- Check that audio focus is not being taken by another app
- Ensure the audio file is not corrupted
- Try clearing app data and rescanning music library

**Notification not showing:**
- Grant notification permission (Android 13+)
- Check that notifications are enabled in system settings

### Development Notes

**Important Implementation Details:**
- Theme changes save to SharedPreferences with key "THEMES" and index "themeIndex"
- Favorites are stored in SharedPreferences with key "FAV_SONGS" using Gson serialization
- Playlists are stored in SharedPreferences with key "MusicPlaylist" using Gson serialization
- Music service must be stopped before app restart during theme changes to prevent memory leaks
- The app uses `BuildConfig.VERSION_NAME` for version display (ensure `buildConfig = true` in build.gradle)

**Key Files to Review When Resuming Development:**
- `MainActivity.kt`: Entry point, permission handling, music scanning
- `PlayerActivity.kt`: Playback controls, UI updates, service binding
- `MusicService.kt`: Background playback, notification management, audio focus
- `SettingsActivity.kt`: Theme management and app restart logic
- `FavActivity.kt` / `PlaylistActivity.kt`: Data persistence patterns

---

## 📸 Screenshots

<!-- Add your screenshots here -->
<div align="center">
  <img src="path/to/screenshot1.png" width="250" />
  <img src="path/to/screenshot2.png" width="250" />
</div>

*(Note: Replace `path/to/screenshotX.png` with actual paths to your screenshot images after uploading them to your repo)*

---

## 🤝 Contributing

Contributions are what make the open source community such an amazing place to learn, inspire, and create. Any contributions you make are **greatly appreciated**.

1.  Fork the Project
2.  Create your Feature Branch (`git checkout -b feature/AmazingFeature`)
3.  Commit your Changes (`git commit -m 'Add some AmazingFeature'`)
4.  Push to the Branch (`git push origin feature/AmazingFeature`)
5.  Open a Pull Request


<div align="center">
    <b>Made with ❤️ by Blaze</b>
</div>
