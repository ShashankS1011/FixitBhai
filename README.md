# FixitBhai

[![Android CI](https://github.com/ShashankS1011/FixitBhai/actions/workflows/android.yml/badge.svg)](https://github.com/ShashankS1011/FixitBhai/actions/workflows/android.yml)
[![CodeQL Analysis](https://github.com/ShashankS1011/FixitBhai/actions/workflows/codeql.yml/badge.svg)](https://github.com/ShashankS1011/FixitBhai/actions/workflows/codeql.yml)
[![License: MIT](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)
[![Android SDK](https://img.shields.io/badge/Min%20SDK-24-brightgreen.svg)](https://developer.android.com)

**FixitBhai** is a high-performance, privacy-focused Android application designed to automatically discover, organize, and manage local home service experts (plumbers, electricians, AC repair technicians, mechanics, carpenters, etc.) directly from your device phonebook and shared cloud directory.

Never lose the contact number of a verified local technician again or confuse them with personal family contacts!

---

## Table of Contents
- [Key Features](#key-features)
- [Architecture & Data Flow](#architecture--data-flow)
- [Admin Moderation & Security](#admin-moderation--security)
- [Tech Stack](#tech-stack)
- [Downloads & Releases](#downloads--releases)
- [Building & Testing](#building--testing)
- [License](#license)

---

## Key Features

- **Smart Phonebook Scanner:** Automatically discovers repair and maintenance contacts from your phonebook using intelligent keyword matching while keeping your personal contacts 100% private and local.
- **Interactive Onboarding Walkthrough:** A sleek Material 3 carousel (`IntroActivity`) guiding new users step-by-step through local contact scanning, city filtering, cloud moderation, and quick actions.
- **Secure Cloud & City Moderation Queue:** Regular users can submit contact publication/deletion requests and custom city creation requests (`ADD_CITY`), which are securely reviewed and approved/rejected via an in-app Admin Moderation Panel (complete with requester name and email tracking).
- **Universal Public Cities & Offline Directory Caching:** Approved public cities (`public_cities` collection) and cloud contacts fetched from Firestore are cached locally in SQLite (`cloud_cache` table) for instant offline browsing and universal display across all devices.
- **Modern High-Contrast Dark UI:** Sleek, high-contrast dark design system built with Material 3 components, avatar initial circles, touch ripple feedback, and custom status pills.
- **Dynamic Category & City Filtering:** Instant filtering by service trade and target city directories with stable alphabetical name sorting and city alias matching (e.g. *Prayagraj / Allahabad*).
- **Blacklist / Do Not Call Warning:** Mark bad service providers or unreliable technicians with a visible warning banner to prevent accidental re-hiring.
- **SQLite Pre-Fetched Cursor Indexing & Async QR Generation:** Column indices pre-fetched once per query set for ~30% faster SQLite contact list parsing, paired with background thread QR matrix encoding.
- **Crashlytics Production Analytics:** Integrated Firebase Crashlytics with an Admin-only Crash Logs and Force Test Crash tool for robust error tracking.

---

## Architecture & Data Flow

```
+-----------------------------------------------------------------------+
|                             FixitBhai App                             |
|                                                                       |
|  +---------------------------+       +-----------------------------+  |
|  | Device Phonebook Scanner  |       |   Interactive Onboarding    |  |
|  | (100% Local On-Device)    |       |   Walkthrough (Intro)       |  |
|  +-------------+-------------+       +--------------+--------------+  |
|                |                                    |                 |
|                v                                    v                 |
|  +---------------------------+       +-----------------------------+  |
|  | Local SQLite Database     |<----->| Material 3 UI / RecyclerView|  |
|  | (FixitBhai.db)            |       | & Category/City Chips       |  |
|  | - contacts table          |       +--------------+--------------+  |
|  | - cloud_cache table       |                      |                 |
|  +---------------------------+                      |                 |
+-----------------------------------------------------|-----------------+
                                                      |
                                                      v
                                  +-------------------------------------+
                                  |    Firebase Cloud Services          |
                                  |                                     |
                                  | - Firebase Auth (Google & Email)    |
                                  | - Firestore (cities/{city}/contacts)|
                                  | - Firestore (moderation_requests)   |
                                  | - Firebase Crashlytics              |
                                  +-------------------------------------+
```

---

## Admin Moderation & Security

FixitBhai incorporates strict role-based authorization to protect the public cloud directory:
- **Admin Privilege Verification:** Determined securely via `ContactAdapter.isAdminEmail(userEmail)`.
- **Admin Moderation Panel:** Admins have a dedicated top-bar amber notification button opening a tabbed review dialog for **Add Requests** vs **Delete Requests**.
- **Admin Cloud Wipe Tool:** Allows admins to clear the public shared cloud directory on demand.
- **Privacy Guarantees:** Personal, family, or unselected phonebook contacts are never automatically uploaded to public cloud directories.

---

## Tech Stack

- **Language:** Java & Kotlin
- **Min SDK:** 24 (Android 7.0)
- **Target SDK:** 37
- **UI Framework:** Android Material Components (Material 3), RecyclerView, CoordinatorLayout, ConstraintLayout, ViewPager2
- **Database & Cloud:** SQLite (`DatabaseHelper` with `cloud_cache`), Firebase Authentication, Firebase Firestore, Firebase Crashlytics
- **Security & Analysis:** GitHub CodeQL, Dependabot Security Analysis, ProGuard/R8
- **Testing:** 37 Automated JUnit Unit Tests (100% pass rate), Espresso UI Instrumented Test Harness
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

## Building & Testing

### Prerequisites
- Android Studio Ladybug (2024.2.1) or newer
- JDK 17+
- Android SDK 35/37

### Build Steps & Test Suite
1. Clone the repository:
   ```bash
   git clone https://github.com/ShashankS1011/FixitBhai.git
   cd FixitBhai
   ```
2. Run automated unit tests & build debug APK:
   ```bash
   ./gradlew testDebugUnitTest assembleDebug
   ```

---

## License

This project is licensed under the **MIT License**. See the [LICENSE](LICENSE) file for details.
