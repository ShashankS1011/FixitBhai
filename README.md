# Fixit Bhai

Fixit Bhai is an Android application engineered to automatically scan, identify, and categorize local service technicians and home repair contacts directly from an Android device's contact list. 

By utilizing dynamic string matching and predefined exclusion rules, the application aggregates essential service providers—such as AC technicians, plumbers, electricians, and mechanics—without manual contact searches.

---

## Downloads

* **Direct APK Download:** [Download Latest Release](https://github.com/YOUR_USERNAME/FixitBhai/releases/latest)

---

## Technical Architecture and Implementation

### 1. Contact Processing & Categorization
* **Targeted Scanning:** Queries the system `ContactsContract.CommonDataKinds.Phone` ContentProvider to retrieve name and phone number records.
* **Regex Word-Boundary Matching:** Employs word-boundary regular expressions (`\bkeyword\b`) to match specific service keywords against contact display names.
* **Keyword Prioritization Logic:** Prioritizes explicit trades over broad service descriptors (e.g., matching "Sujeet AC Service" to **AC Repair** instead of a generic **Service** category).
* **Exclusion Filtering:** Implements a family blacklist containing regional and English familial terms (e.g., *Bhaiya*, *Uncle*, *Cousin*) to prevent personal contacts from entering the service directory.

### 2. UI & Component Architecture
* **Dynamic Chip Generation:** Programmatically inflates Material `Chip` components inside a `ChipGroup` based on the unique categories present in the filtered contact list.
* **Interactive Filtering:** Integrates `Chip` selection directly with the custom `Filter` implementation in `ContactAdapter` to update `RecyclerView` results instantly.
* **Runtime Permissions:** Implements the modern Android runtime permission pattern (`ContextCompat.checkSelfPermission` and `ActivityCompat.requestPermissions`) to handle `READ_CONTACTS` requests safely without application crashes.

### 3. Data Storage & Persistence
* **SharedPreferences & Gson:** Serializes the processed `Contact` model objects into JSON format for permanent storage, enabling instant local loading without re-querying the system provider on every startup.

---

## Technical Specifications

* **Language:** Java
* **Build System:** Gradle (Kotlin DSL / Groovy)
* **Target SDK:** Android 14 (API level 34)
* **Minimum SDK:** Android 6.0 (API level 23)
* **Architecture Pattern:** Model-View-Adapter (MVA)
* **UI Components:** Android Material Components (`RecyclerView`, `ChipGroup`, `FloatingActionButton`, `ImageButton`)
* **JSON Serialization:** Google Gson `2.10+`

---

## Application Workflow

1. **Permission Check:** Upon launching `MainActivity`, the application checks for the `android.permission.READ_CONTACTS` runtime permission.
2. **Local Cache Load:** Saved contact records are deserialized from `SharedPreferences` and loaded into the `RecyclerView`.
3. **Contact Auto-Scan:** If permission is granted, `ContentResolver` queries the address book:
   * Normalizes incoming phone numbers to 10-digit standard strings for accurate deduplication.
   * Evaluates names against the relative blacklist.
   * Categorizes valid records according to the prioritized keyword list.
4. **UI Update:** The application updates the adapter dataset, appends newly discovered categories to the `ChipGroup`, and persists the updated dataset locally.

---

## Project Setup and Installation

### Prerequisites
* Android Studio Jellyfish (2023.3.1) or newer
* Java Development Kit (JDK) 17 or higher
* Physical Android device or Emulator running Android 6.0 (API Level 23) or higher

### Installation Steps

1. **Clone the Repository**
   ```bash
   git clone [https://github.com/YOUR_USERNAME/FixitBhai.git](https://github.com/YOUR_USERNAME/FixitBhai.git)
   ```

2. **Open in Android Studio**
   * Launch Android Studio.
   * Select **File > Open** and select the cloned `FixitBhai` project root directory.

3. **Sync Gradle and Build**
   * Allow Android Studio to download required dependencies and sync Gradle.
   * Connect an Android device or launch an emulator.
   * Select **Run > Run 'app'** (or press `Shift + F10`).
   * Grant **Contacts Access** when prompted by the runtime dialog to execute the initial contact import.

---

## License

This project is licensed under the MIT License. See the `LICENSE` file for full details.
