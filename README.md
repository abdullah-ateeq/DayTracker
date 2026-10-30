# DayTracker 📅

**DayTracker** is a sleek, modern Android application built with **Jetpack Compose** and **Material 3**. It helps you track significant life events, countdowns to upcoming dates, and keep track of days passed since special milestones and anniversaries.

---

## ✨ Features

- ⏳ **Days & Milestone Tracker**: Calculate exact days, weeks, months, and years passed or remaining for any date.
- 🎂 **Anniversary Engine**: Automatically computes next upcoming anniversaries and days remaining.
- 📂 **Categorization & Customization**:
  - Filter events by categories: *Personal, Work, Education, Travel, Other*.
  - Customize each event with icons and color accents.
- 🔍 **Search & Sorting**:
  - Real-time search across event titles and descriptions.
  - Sort by date (newest/oldest), title, or days passed/remaining.
- 📊 **Detailed Insights & Sharing**:
  - Detailed event view showing breakdown in years, months, and days.
  - One-tap sharing and quick copy of event statistics to clipboard.
- 🎨 **Material 3 & Theming**:
  - Support for **Dark Theme**, **Light Theme**, and **System Default**.
  - Adaptive edge-to-edge design with fluid transitions and custom animations.
  - Customizable date formats (e.g., `MMM dd, yyyy`, `yyyy-MM-dd`, `dd/MM/yyyy`).
- 🔒 **Offline-First & Local Storage**:
  - Powered by **Room Database** for high performance and offline durability.
  - Preferences saved securely via **DataStore Preferences**.
- 🛠️ **Sample Data Loader**: Easily pre-fill or clear demo events for quick testing.

---

## 🛠️ Tech Stack & Architecture

- **Language**: [Kotlin](https://kotlinlang.org/)
- **UI Framework**: [Jetpack Compose](https://developer.android.com/jetpack/compose) with **Material3** & **Compose Navigation**
- **Architecture**: MVVM (Model-View-ViewModel) + Clean Architecture principles
- **Local Persistence**:
  - [Room Database](https://developer.android.com/training/data-storage/room) (with KSP)
  - [DataStore Preferences](https://developer.android.com/topic/libraries/architecture/datastore)
- **Async & Reactive Flow**: Kotlin Coroutines & `StateFlow`
- **Dependency & Build Management**: Gradle with Version Catalog (`libs.versions.toml`)
- **Min SDK**: 26 (Android 8.0) | **Target SDK**: 37 (Android 15+)

---

## 📁 Project Structure

```text
com.example.daytracker
├── data
│   ├── local        # Room Database (AppDatabase, EventDao, EventEntity, Converters)
│   ├── preferences  # DataStore Preferences (UserPreferencesRepository)
│   └── repository   # EventRepository implementation
├── domain
│   ├── DayTrackerCalculator.kt   # Calculation logic for days/periods/anniversaries
│   ├── EventCalculation.kt       # Data model for calculated metrics
│   ├── EventCategory.kt          # Event categories & default styling
│   └── EventIcon.kt              # Visual icons and color palettes
└── ui
    ├── components   # Reusable UI components (StatCard, EventCard, AnimatedCounter, etc.)
    ├── screens      # Compose screens (HomeScreen, EventDetailsScreen, AddEditEventScreen, SettingsScreen, AboutScreen, SplashScreen)
    ├── theme        # Material 3 colors, typography, and theme definitions
    ├── DayTrackerApp.kt
    └── DayTrackerViewModel.kt
```

---

## 🚀 Getting Started

### Prerequisites
- **Android Studio** (Ladybug or newer recommended)
- **JDK 11** or higher
- Android Device or Emulator running **Android 8.0 (API 26)** or higher

### Build & Run
1. **Clone the repository**:
   ```bash
   git clone https://github.com/abdullah-ateeq/DayTracker.git
   cd DayTracker
   ```
2. **Open in Android Studio**:
   Open Android Studio, choose **Open**, and select the `DayTracker` root folder.
3. **Build & Run**:
   Select the `app` configuration and hit **Run** (`Shift + F10`) to deploy to your device or emulator.

---

## 🧪 Testing

Run unit tests via command line or Android Studio:
```bash
./gradlew test
```

---

## 📄 License

This project is created by **Abdullah Ateeq**. All rights reserved.
