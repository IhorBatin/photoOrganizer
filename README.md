# Photo Organizer

A private, secure photo management app for Android — take and organize photos that stay fully isolated from your device's shared gallery and other apps.

## 🚀 Features

- **Private Photo Capture**: Take photos directly in-app — they're saved straight into private storage and never touch the shared gallery or become accessible to other apps.
- **Locked Folder Structure**: Organize photos into folders, and lock any folder individually with **Biometric Authentication** (Fingerprint/Face) or a **password**.
- **Privacy by Design**: Built on the modern Android Photo Picker and Scoped Storage, minimizing permissions and keeping photos contained to the app's private space.

## 🔒 How It Works

Locked folders are protected at the app logic level — each folder's contents are only accessible after a successful biometric or password check via Android's Biometric API. Combined with Scoped Storage, this means locked photos aren't just hidden from the UI — they're not exposed to other apps on the device at all, unlike gallery-based "hide" features that simply rename or move files.

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

## 📸 Screenshots
| | | |
|---|---|---|
| <img width="427" height="952" alt="Screenshot 1" src="https://github.com/user-attachments/assets/f7ab192a-9842-4803-86eb-c0f77756bcc3" /> | <img width="427" height="952" alt="Screenshot 2" src="https://github.com/user-attachments/assets/609da1d2-8e17-4a5d-96e6-46e2b5c9461a" /> | <img width="427" height="952" alt="Screenshot 3" src="https://github.com/user-attachments/assets/36226c8c-c3d1-426a-b9d9-792e61982e3b" /> |

## 📦 Project Setup

- **Target SDK**: 36 (Android 16)
- **AGP**: 9.2.1
- **JDK**: 17

To get started:

1. Clone the repository.
2. Open in Android Studio.
3. Sync Gradle and run the `:app` module.

## 📄 License

This project is licensed under the [MIT License](LICENSE).

---

*A personal project built to explore secure, private photo storage on Android.*
