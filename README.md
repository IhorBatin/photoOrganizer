# Photo Organizer - [WIP]

An Android application for managing and organizing photos with a options for privacy and security.

## 🚀 Features

- **Photo Management**: Browse and organize images.
- **Folder Protection**: Secure specific folders using **Biometric Authentication** (Fingerprint/Face) or **Passwords**.
- **Privacy Focused**: Uses the modern Android Photo Picker and Scoped Storage to minimize permission requirements.

## 🛠 Tech Stack

- **Language**: [Kotlin](https://kotlinlang.org/) (2.2.10)
- **Build System**: [Gradle](https://gradle.org/) (9.5.1) with Version Catalog (TOML)
- **Architecture**: MVVM (Model-View-ViewModel)
- **Frameworks & Libraries**:
  - [Jetpack Lifecycle](https://developer.android.com/topic/libraries/architecture/lifecycle): ViewModel & LiveData
  - [ViewBinding & DataBinding](https://developer.android.com/topic/libraries/data-binding)
  - [Glide](https://github.com/bumptech/glide): Image loading and caching
  - [Timber](https://github.com/JakeWharton/timber): Logging
  - [Biometric](https://developer.android.com/training/sign-in/biometric-auth): Security integration

## 📦 Project Setup

This project uses the latest Android development toolchain:
- **Target SDK**: 36 (Android 16)
- **AGP**: 9.2.1
- **JDK**: 17

To get started:
1. Clone the repository.
2. Open in **Android Studio (Panda or newer)**.
3. Sync Gradle and run the `:app` module.

## 🔒 Security

Photo Organizer respects your privacy. It implements **Scoped Storage**, ensuring it only accesses files you explicitly allow or those within its private directory. Protected folders are encrypted at the app logic level via user-defined credentials.

---
*Developed as a modern utility for Android users.*
