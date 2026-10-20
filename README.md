# FixitBhai

[![Android CI](https://github.com/ShashankS1011/FixitBhai/actions/workflows/android.yml/badge.svg)](https://github.com/ShashankS1011/FixitBhai/actions/workflows/android.yml)
[![CodeQL Analysis](https://github.com/ShashankS1011/FixitBhai/actions/workflows/codeql.yml/badge.svg)](https://github.com/ShashankS1011/FixitBhai/actions/workflows/codeql.yml)
[![License: MIT](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)
[![Android SDK](https://img.shields.io/badge/Min%20SDK-24-brightgreen.svg)](https://developer.android.com)

**FixitBhai** is an enterprise-grade Android application designed to automatically scan, organize, and manage local home service technicians (plumbers, electricians, AC repair experts, mechanics, carpenters, etc.) directly from your device phonebook and shared cloud directory.

Never lose the contact number of a verified local technician again or confuse them with personal contacts!

---

## Key Features

- **Smart Phonebook Scanner:** Automatically discovers repair and maintenance contacts from your phonebook using intelligent keyword matching while keeping your personal contacts 100% private and local.
- **Interactive Onboarding Walkthrough:** A sleek Material 3 carousel guiding new users step-by-step through local contact scanning, city filtering, cloud moderation, and quick actions.
- **Secure Cloud Publication & Admin Moderation Queue:** Regular users can submit cloud publication and deletion requests, which are securely reviewed and approved/rejected via an in-app Admin Moderation Panel (complete with requester name and email tracking).
- **SQLite Offline Cloud Directory Caching:** Cloud directory contacts fetched from Firestore are cached locally in SQLite for instant offline browsing and resilience against poor connectivity.
- **Modern High-Contrast Dark UI:** Sleek, high-contrast dark design system built with Material 3 components, avatar initial circles, touch ripple feedback, and custom status pills.
- **Dynamic Category & City Filtering:** Instant filtering by service trade and target city directories with stable alphabetical name sorting.
- **Blacklist / Do Not Call Warning:** Mark bad service providers or unreliable technicians with a visible warning banner to prevent accidental re-hiring.
- **Crashlytics Production Analytics:** Integrated Firebase Crashlytics with an Admin-only Crash Logs and Force Test Crash tool for robust error tracking.

---

## Tech Stack & Architecture

- **Language:** Java & Kotlin
- **Min SDK:** 24 (Android 7.0)
- **Target SDK:** 37
- **UI Framework:** Android Material Components (Material 3), RecyclerView, CoordinatorLayout, ConstraintLayout, ViewPager2
- **Database & Cloud:** SQLite (`DatabaseHelper` with `cloud_cache`), Firebase Authentication, Firebase Firestore, Firebase Crashlytics
- **Security & Analysis:** GitHub CodeQL, Dependabot Security Analysis, ProGuard/R8
- **Testing:** JUnit unit tests (25+ tests), Espresso UI Instrumented Test Harness
- **CI/CD:** GitHub Actions (Automated Build, Test, CodeQL Analysis, Release APK)

---

## Downloads & Releases

Download the latest compiled application package directly:

- **[Download FixitBhai APK](https://github.com/ShashankS1011/FixitBhai/releases/latest/download/app-debug.apk)**
- **[View All Releases](https://github.com/ShashankS1011/FixitBhai/releases)**

Alternatively, build the application directly from source:
```bash
./gradlew assembleDebug
```
The generated APK will be located at `app/build/outputs/apk/debug/app-debug.apk`.

---

## Security & Vulnerability Reporting

Security and user privacy are fundamental to FixitBhai:
- **No Unused Data Access:** Contact scanning is strictly performed locally on-device. Local contacts are never auto-uploaded to public shared directories.
- **Vulnerability Reporting:** If you discover a security issue, please review our [Security Policy](.github/SECURITY.md) and report it privately through [GitHub Private Vulnerability Reporting](https://github.com/ShashankS1011/FixitBhai/security/advisories/new).

---

## Building From Source

### Prerequisites
- Android Studio Ladybug (2024.2.1) or newer
- JDK 17+
- Android SDK 35/37

### Build Steps & Testing
1. Clone the repository:
   ```bash
   git clone https://github.com/ShashankS1011/FixitBhai.git
   cd FixitBhai
   ```
2. Run unit tests & build debug APK:
   ```bash
   ./gradlew testDebugUnitTest assembleDebug
   ```

---

## Contributors & Acknowledgements

Special thanks to the contributors who help improve FixitBhai:

- **[Divyanshu Singh](https://github.com/ROGU3D3viL)** ([@ROGU3D3viL](https://github.com/ROGU3D3viL)) — Identified and reported the phone authentication issue and cross-user cloud contact synchronization bug.
- **[@Butteryjam](https://github.com/Butteryjam)** — Reported city directory filtering behavior, Google Sign-In authentication error, and package update signing conflict.

---

## License

This project is licensed under the **MIT License**. See the [LICENSE](LICENSE) file for details.
