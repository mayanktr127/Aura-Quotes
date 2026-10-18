# PHASE 1 — AUDIT & BASELINE BUILD REPORT

This document represents the complete, unverified-to-verified code audit of **Aura Quotes** in accordance with the rules established in AGENTS.md. **No features were added or redesigned in this phase.**

---

## 1. Repository Architecture Map

```text
c:\Users\Mayank\Downloads\Aura Quotes\
├── AGENTS.md                              # Non-negotiable project guidelines & technical baselines
├── metadata.json                          # AI Studio project metadata & declared capabilities
├── .env.example                           # Template for environment secrets (Gemini API key placeholder)
├── build.gradle.kts                       # Root project Gradle script registering Android/Kotlin plugins
├── settings.gradle.kts                    # Root settings: repositories & plugin management
├── gradle.properties                      # JVM memory arguments & AndroidX flags
├── local.properties                       # Local SDK location (sdk.dir=C:\Users\Mayank\AppData\Local\Android\Sdk)
├── gradlew / gradlew.bat                  # Gradle wrapper execution binaries (restored in Phase 1)
├── gradle/
│   ├── libs.versions.toml                 # Gradle Version Catalog declaring all dependencies & versions
│   └── wrapper/
│       ├── gradle-wrapper.properties      # Distribution URL for Gradle 9.3.1
│       └── gradle-wrapper.jar             # Gradle wrapper executable archive (restored in Phase 1)
└── app/
    ├── build.gradle.kts                   # Application module build config (dependencies, signing, sdk targets)
    ├── proguard-rules.pro                 # R8/Proguard code obfuscation & shrinking rules
    └── src/
        ├── main/
        │   ├── AndroidManifest.xml        # App manifest declaring MainActivity, FileProvider, permissions
        │   ├── java/com/example/
        │   │   ├── MainActivity.kt        # Single Activity entry point with Edge-to-Edge & AuraTheme
        │   │   ├── data/
        │   │   │   ├── local/
        │   │   │   │   ├── AuraDatabase.kt           # Room Database definition with 7 entities (version 1)
        │   │   │   │   ├── QuoteDao.kt               # Room DAO for quotes, favorites, collections, history
        │   │   │   │   ├── QuoteEntity.kt            # Room entities (Quote, Favorite, Collection, Reflection, History, UserQuote)
        │   │   │   │   └── SeedQuotes.kt             # 700+ lines of initial offline quotes & metadata
        │   │   │   ├── model/
        │   │   │   │   ├── Quote.kt                  # Domain models (Quote, QuoteCollection, ReflectionNote, HistoryItem)
        │   │   │   │   └── QuoteStyle.kt             # CardStyle enums (colors/gradients) and QuoteFont typography enums
        │   │   │   ├── preferences/
        │   │   │   │   └── UserPreferencesRepository.kt # DataStore manager (themeMode, notification time, chosen categories)
        │   │   │   └── repository/
        │   │   │       └── QuoteRepository.kt        # Single source of truth combining Room DAO & seed population
        │   │   ├── notification/
        │   │   │   └── DailyReminderManager.kt       # Notification channel creation & instant quote notification builder
        │   │   ├── ui/
        │   │   │   ├── components/
        │   │   │   │   ├── MoodChipRow.kt            # Horizontal scrollable mood filter chips
        │   │   │   │   ├── QuoteCard.kt              # Core visual quote card with gesture favorites, share & copy
        │   │   │   │   └── StyleSelectorCarousel.kt  # Horizontal row of card style preset selector pills
        │   │   │   ├── navigation/
        │   │   │   │   ├── Screen.kt                 # Sealed class defining 9 navigation route strings & helpers
        │   │   │   │   └── AuraNavHost.kt            # NavHost graph, 5-tab bottom navigation, startDestination routing
        │   │   │   ├── screens/
        │   │   │   │   ├── home/
        │   │   │   │   │   ├── HomeScreen.kt         # Composable: Today view, hero quote, mood chips, saved shortcut
        │   │   │   │   │   └── HomeViewModel.kt      # Manages daily greeting, hero style, mood/category filtering
        │   │   │   │   ├── explore/
        │   │   │   │   │   ├── ExploreScreen.kt      # Composable: Browse by category, mood, moment, author
        │   │   │   │   │   └── ExploreViewModel.kt   # Dynamic filtering and category-quote flow mapping
        │   │   │   │   ├── designer/
        │   │   │   │   │   ├── QuoteDesignerScreen.kt # Composable: Card customizer, font/ratio selector, preview
        │   │   │   │   │   └── QuoteDesignerViewModel.kt # StateFlows for custom text, aspect ratios, image saving
        │   │   │   │   ├── favorites/
        │   │   │   │   │   ├── FavoritesScreen.kt    # Composable: 3-tab screen (Saved quotes, Collections, History)
        │   │   │   │   │   └── FavoritesViewModel.kt # StateFlows for saved items, custom collections, reflections
        │   │   │   │   ├── detail/
        │   │   │   │   │   ├── QuoteDetailScreen.kt  # Composable: Single quote view, reflection notes, collection adder
        │   │   │   │   │   └── QuoteDetailViewModel.kt # StateFlows for selected quote, related quotes, notes
        │   │   │   │   ├── search/
        │   │   │   │   │   ├── SearchScreen.kt       # Composable: Instant search input with tags & result list
        │   │   │   │   │   └── SearchViewModel.kt    # Debounced 300ms search query Flow
        │   │   │   │   ├── settings/
        │   │   │   │   │   ├── SettingsScreen.kt     # Composable: Theme toggle, daily reminder time, topic preferences
        │   │   │   │   │   └── SettingsViewModel.kt  # Handles DataStore preferences updates & test notifications
        │   │   │   │   └── onboarding/
        │   │   │   │       └── OnboardingScreen.kt   # Composable: 3-step intro (welcome, interest picker, reminder time)
        │   │   │   ├── theme/
        │   │   │   │   ├── Color.kt                  # Noir/Gold color tokens and card gradient definitions
        │   │   │   │   └── Theme.kt                  # Material 3 dark/light AuraColorScheme and typography theme
        │   │   │   │   └── Type.kt                   # Typography styles with Editorial Serif & Sans font families
        │   │   │   └── util/
        │   │   │       └── ImageExportUtil.kt        # Canvas bitmap generator & MediaStore/FileProvider image exporter
        │   │   └── res/
        │   │       ├── drawable/                     # 6 custom vector illustrations + 2 launcher backgrounds (0 photo files)
        │   │       ├── mipmap-*/                     # Default Android mipmap app icons
        │   │       ├── values/                       # colors.xml, strings.xml, themes.xml
        │   │       └── xml/                          # file_paths.xml (FileProvider), backup_rules, data_extraction
        └── test/
            └── java/com/example/
                ├── ExampleUnitTest.kt                # Basic addition unit test
                └── ExampleRobolectricTest.kt         # Robolectric test verifying Room DB, seed data & favorites toggle
```

---

## 2. Current Build Configuration

* **Application ID:** `com.aistudio.auraquotes.mrzb`
* **Namespace:** `com.example`
* **Compile SDK:** `36` (Android 16 preview) with `minorApiLevel = 1`
* **Target SDK:** `36`
* **Min SDK:** `24` *(Note: `AGENTS.md` mandates `minSdk = 26`)*
* **Version Code:** `1`
* **Version Name:** `1.0`
* **Kotlin Version:** `2.2.10`
* **Android Gradle Plugin (AGP):** `9.1.1`
* **Compose Compiler:** Integrated via `org.jetbrains.kotlin.plugin.compose` (Kotlin 2.2.10)
* **Compose BOM:** `2024.09.00`
* **Gradle Distribution:** `9.3.1`

### Declared Dependencies (from `gradle/libs.versions.toml` & `app/build.gradle.kts`):

| Library Name | Group & Artifact | Version / Reference |
| :--- | :--- | :--- |
| `androidx-core-ktx` | `androidx.core:core-ktx` | `1.18.0` |
| `androidx-activity-compose` | `androidx.activity:activity-compose` | `1.10.1` |
| `androidx-lifecycle-runtime-compose` | `androidx.lifecycle:lifecycle-runtime-compose` | `2.8.7` |
| `androidx-lifecycle-runtime-ktx` | `androidx.lifecycle:lifecycle-runtime-ktx` | `2.8.7` |
| `androidx-lifecycle-viewmodel-compose` | `androidx.lifecycle:lifecycle-viewmodel-compose` | `2.8.7` |
| `androidx-compose-material3` | `androidx.compose.material3:material3` | Via BOM `2024.09.00` |
| `androidx-compose-material-icons-core` | `androidx.compose.material:material-icons-core` | Via BOM |
| `androidx-compose-material-icons-extended` | `androidx.compose.material:material-icons-extended` | Via BOM |
| `androidx-navigation-compose` | `androidx.navigation:navigation-compose` | `2.8.9` |
| `androidx-room-runtime` | `androidx.room:room-runtime` | `2.7.0` |
| `androidx-room-ktx` | `androidx.room:room-ktx` | `2.7.0` |
| `androidx-room-compiler` | `androidx.room:room-compiler` | `2.7.0` (KSP) |
| `androidx-datastore-preferences` | `androidx.datastore:datastore-preferences` | `1.1.7` |
| `coil-compose` | `io.coil-kt:coil-compose` | `2.7.0` |
| `firebase-bom` | `com.google.firebase:firebase-bom` | `34.17.0` |
| `firebase-ai` | `com.google.firebase:firebase-ai` | Via BOM `34.17.0` |
| `firebase-appcheck-recaptcha` | `com.google.firebase:firebase-appcheck-recaptcha` | Via BOM |
| `firebase-appcheck-debug` | `com.google.firebase:firebase-appcheck-debug` | Via BOM |
| `retrofit` | `com.squareup.retrofit2:retrofit` | `2.12.0` |
| `converter-moshi` | `com.squareup.retrofit2:converter-moshi` | `2.12.0` |
| `moshi-kotlin` | `com.squareup.moshi:moshi-kotlin` | `1.15.2` |
| `okhttp` | `com.squareup.okhttp3:okhttp` | `4.10.0` |
| `logging-interceptor` | `com.squareup.okhttp3:logging-interceptor` | `4.10.0` |
| `kotlinx-coroutines-android` | `org.jetbrains.kotlinx:kotlinx-coroutines-android` | `1.10.2` |
| `kotlinx-coroutines-core` | `org.jetbrains.kotlinx:kotlinx-coroutines-core` | `1.10.2` |
| `robolectric` (test) | `org.robolectric:robolectric` | `4.16.1` |
| `google-devtools-ksp` (plugin) | `com.google.devtools.ksp` | `2.3.5` |
| `secrets-gradle-plugin` (plugin)| `com.google.android.libraries.mapsplatform.secrets-gradle-plugin` | `2.0.1` |
| `google-services` (plugin) | `com.google.gms.google-services` | `4.5.0` |

---

## 3. Baseline Build Verification

### Fixes Made to Compile (ONLY what was required):
1. **Missing Gradle Wrapper:** `gradlew`, `gradlew.bat`, and `gradle/wrapper/gradle-wrapper.jar` were completely missing from the cloned repository. Generated the standard wrapper scripts with `gradle wrapper --no-validate-url`.
2. **Missing `local.properties`:** Added local `sdk.dir=C:\Users\Mayank\AppData\Local\Android\Sdk` (which is properly gitignored).
3. **Missing `debug.keystore`:** Copied user's existing Android debug keystore to root `debug.keystore` (which is properly gitignored and referenced in `signingConfigs.debugConfig`).

### Build Execution Results:

```text
> Task :app:assembleDebug
BUILD SUCCESSFUL in 4m 41s
40 actionable tasks: 39 executed, 1 up-to-date

> Task :app:lintDebug
Wrote HTML report to file:///C:/Users/Mayank/Downloads/Aura%20Quotes/app/build/reports/lint-results-debug.html
BUILD SUCCESSFUL in 4m 13s
31 actionable tasks: 10 executed, 21 up-to-date

> Task :app:testDebugUnitTest
BUILD SUCCESSFUL in 1m 59s
33 actionable tasks: 9 executed, 24 up-to-date
```
*Generated APK:* `app/build/outputs/apk/debug/app-debug.apk` (23.3 MB, verified valid).

---

## 4. Health Report

### A. Crashes or Obvious Bugs Found in Code
1. **Notification Scheduling is Incomplete:** `SettingsViewModel.kt` saves the reminder hour/minute to DataStore, but **never registers an `AlarmManager` or `WorkManager` task**. Notifications will not trigger at the scheduled time unless manually prompted via the "Send Test Notification" button.
2. **Collection Detail Navigation Route:** `Screen.CollectionDetail` is defined in routes, but there is no Composable route registered in `AuraNavHost.kt`. Tapping a collection in Favorites could result in an unhandled navigation.

### B. Dead or Unused Code
1. **Unused Network & AI Dependencies:** `Retrofit`, `Moshi`, `OkHttp`, and `firebase-ai` are imported in `build.gradle.kts`, but there is **zero network code or Gemini AI calls in any Kotlin file**.
2. **Missing `google-services.json`:** The Google Services plugin is applied in `build.gradle.kts`, but there is no `google-services.json`. It builds only because `missingGoogleServicesStrategy = WARN` is enabled.

### C. Deprecated APIs Found by Compiler / Linter
1. `Icons.Filled.TrendingUp` is deprecated in Compose Material icons. Recommended replacement: `Icons.AutoMirrored.Filled.TrendingUp` (`ExploreScreen.kt#L399`).
2. `Divider(...)` is deprecated in Material 3. Recommended replacement: `HorizontalDivider(...)` (`SettingsScreen.kt#L211, L353`).
3. `SearchViewModel.kt` uses un-opted experimental coroutine flow APIs (`debounce`, `distinctUntilChanged`).

### D. Violations of `AGENTS.md` Rules Found
1. **MinSdk:** Current `minSdk = 24` in `app/build.gradle.kts#L17`. `AGENTS.md` mandates `minSdk = 26`.
2. **Room Schema Export:** Current `AuraDatabase.kt#L19` has `exportSchema = false`. `AGENTS.md` mandates `exportSchema = true` with committed schemas folder.
3. **No AdMob SDK Configured:** `AGENTS.md` specifies BuildConfig-based test vs release ad IDs. Neither the Google Mobile Ads SDK nor AdMob IDs exist in the app yet.

### E. Google Play Policy Risks
1. **Default Application ID / Namespace:** `namespace = "com.example"` and `applicationId = "com.aistudio.auraquotes.mrzb"`. Google Play **rejects** apps with `com.example`.
2. **Missing Privacy Policy URL:** Required by Google Play for apps that collect advertising IDs or use notifications.

---

## 5. Feature Inventory

| Feature | Screen / Component | Status | Notes from Code Audit |
| :--- | :--- | :--- | :--- |
| **Quote of the Day Hero** | `HomeScreen.kt` | **Working** | Displays featured quote, heart toggle, share text, copy text. |
| **Card Style Switcher** | `StyleSelectorCarousel.kt` | **Working** | Changes card colors/gradients live between 6 presets. |
| **Mood Filtering** | `HomeScreen.kt` | **Working** | Filters quotes by moods including multi-criteria "Need encouragement". |
| **Category Quick Needs** | `HomeScreen.kt` | **Working** | Tapping navigates to Explore screen. |
| **Explore & Category Browse** | `ExploreScreen.kt` | **Working** | Filter by category, mood, author, or moment with back handler. |
| **Search Functionality** | `SearchScreen.kt` | **Working** | Debounced search queries text, author, and tags. |
| **Quote Designer (Creator)** | `QuoteDesignerScreen.kt` | **Working** | Custom text, font picker, ratio selector, watermark toggle. |
| **Image Export to Gallery** | `ImageExportUtil.kt` | **Working** | Renders Bitmap canvas and saves to MediaStore `Pictures/AuraQuotes`. |
| **Favorites & Saved Quotes** | `FavoritesScreen.kt` | **Working** | Displays favorited quotes, history list, and custom user quotes. |
| **Reflection Notes** | `QuoteDetailScreen.kt` | **Working** | Saves personal reflections to Room database per quote. |
| **Theme Mode Switching** | `SettingsScreen.kt` | **Working** | Switches between System, Light, and Dark themes. |
| **Daily Reminder Time** | `SettingsScreen.kt` | **Partial** | Time saved to DataStore, instant test notification works, but background Alarm/WorkManager scheduler is missing. |
| **Photo Backgrounds** | `QuoteCard.kt` | **Missing** | Only solid colors and gradients are supported right now (0 photos). |
| **AdMob Ads** | Throughout App | **Missing** | No AdMob SDK, banner ads, or interstitial ads installed. |
| **Online Internet Sentinel** | App Level | **Missing** | App is currently 100% offline (no connection check or remote fetch). |

---

## 6. Phase 1 Walkthrough

* **Files Changed:**
  * `local.properties`: Added `sdk.dir` pointing to local Android SDK (gitignored).
  * `debug.keystore`: Restored debug keystore for debug signing configuration (gitignored).
  * `gradle/wrapper/gradle-wrapper.properties`: Set `validateDistributionUrl=false` to generate wrapper without external network timeout.
  * `gradlew`, `gradlew.bat`, `gradle/wrapper/gradle-wrapper.jar`: Generated Gradle wrapper binaries so builds can be run portably.
* **Manifest Permissions:**
  * `android.permission.POST_NOTIFICATIONS` (Minimum required for Android 13+ daily notifications).
* **How to test manually on a phone:**
  * Transfer `app/build/outputs/apk/debug/app-debug.apk` to an Android device and install it.
  * Verify onboarding flow, Today feed, card style carousel, favorites toggle, and export to gallery.
* **Known Limitations:**
  * Background photos do not exist yet.
  * Scheduled daily notifications do not fire automatically at the set hour (only test notifications fire).
* **Out-of-Scope Issues Found (Deferred per Rule 3):**
  * `minSdk = 24` needs to be updated to `26` in a future build-config phase.
  * Room `exportSchema = false` needs migration schema setup.
  * Package name needs refactor from `com.example` before Play Store submission.
