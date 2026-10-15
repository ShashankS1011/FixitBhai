# FixitBhai 🛠️

[![Android CI](https://github.com/ShashankS1011/FixitBhai/actions/workflows/android.yml/badge.svg)](https://github.com/ShashankS1011/FixitBhai/actions/workflows/android.yml)
[![CodeQL Analysis](https://github.com/ShashankS1011/FixitBhai/actions/workflows/codeql.yml/badge.svg)](https://github.com/ShashankS1011/FixitBhai/actions/workflows/codeql.yml)
[![License: MIT](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)
[![Android SDK](https://img.shields.io/badge/Min%20SDK-24-brightgreen.svg)](https://developer.android.com)

**FixitBhai** is a modern Android application designed to automatically scan, organize, and manage local home service technicians (plumbers, electricians, AC repair experts, mechanics, carpenters, etc.) directly from your phonebook and cloud directory.

Never lose the contact number of a verified local technician again or confuse them with personal contacts!

---

## 🌟 Key Features

- **📱 Smart Phonebook Scanner:** Automatically discovers repair and maintenance contacts from your phonebook using intelligent keyword matching while filtering out personal family contacts.
- **☁️ Cloud Backup & Sync:** Seamlessly backs up service contacts to your personal Firebase Cloud Firestore directory for cross-device access.
- **🎨 Modern Slate Dark UI:** Sleek, high-contrast dark design system built with Material 3 components, avatar initial circles, and custom status pills.
- **🏷️ Dynamic Category Filtering:** Instant filtering by service trade (AC Repair, RO Service, Electrician, Plumber, Mechanic) and city via horizontal filter chips.
- **🚫 Blacklist / Do Not Call Warning:** Mark bad service providers or unreliable technicians with a visible warning banner to prevent accidental re-hiring.
- **📍 City Selection:** Filter local experts by specific target city directories.
- **📲 Quick Actions:** One-tap calling, contact sharing, and instant vCard QR code generation for camera scanning.

---

## 🛠️ Tech Stack & Architecture

- **Language:** Java & Kotlin
- **Min SDK:** 24 (Android 7.0)
- **Target SDK:** 37
- **UI Framework:** Android Material Components (Material 3), RecyclerView, CoordinatorLayout, ConstraintLayout
- **Database & Cloud:** SQLite (`DatabaseHelper`), Firebase Authentication (Phone Auth), Firebase Firestore
- **Security & Analysis:** GitHub CodeQL, Dependabot Security Analysis, ProGuard/R8
- **CI/CD:** GitHub Actions (Automated Build, Test, CodeQL Analysis, Release APK)

---

## 📦 Downloads & Releases

You can download the latest compiled APK directly from GitHub Releases:

- 🚀 **[Download Latest FixitBhai APK](https://github.com/ShashankS1011/FixitBhai/releases/latest)**

Alternatively, build the application directly from source:
```bash
./gradlew assembleDebug
```
The generated APK will be located at `app/build/outputs/apk/debug/app-debug.apk`.

---

## 🔒 Security & Vulnerability Reporting

Security and user privacy are fundamental to FixitBhai:
- **No Unused Data Access:** Contact scanning is strictly performed locally on-device.
- **Vulnerability Reporting:** If you discover a security issue, please review our [Security Policy](.github/SECURITY.md) and report it privately through [GitHub Private Vulnerability Reporting](https://github.com/ShashankS1011/FixitBhai/security/advisories/new).

---

## 🏗️ Building From Source

### Prerequisites
- Android Studio Ladybug (2024.2.1) or newer
- JDK 17+
- Android SDK 35/37

### Build Steps
1. Clone the repository:
   ```bash
   git clone https://github.com/ShashankS1011/FixitBhai.git
   cd FixitBhai
   ```
2. Build debug APK & run unit tests:
   ```bash
   ./gradlew testDebugUnitTest assembleDebug
   ```

---

## 📄 License

This project is licensed under the **MIT License**. See the [LICENSE](LICENSE) file for details.
