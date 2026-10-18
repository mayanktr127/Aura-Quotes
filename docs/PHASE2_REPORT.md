# PHASE 2 — FOUNDATION, CLEANUP & RELEASE PLUMBING REPORT

This report documents the completion of **Phase 2** for **Stillwords** (formerly Aura Quotes) in accordance with `AGENTS.md`.

---

## 1. Project Phase Roadmap

* **Phase 1:** Code Audit & Baseline Build *(Completed)*
* **Phase 2:** Foundation, Cleanup & Release Plumbing *(Completed)*
* **Phase 3:** Quote Content & Photo Backgrounds *(Next)*
* **Phase 4:** UI Polish, Save & Share Enhancements
* **Phase 5:** AdMob Integration & UMP Consent Flow
* **Phase 6:** Google Play Store Readiness & Publication

---

## 2. Summary of Work Executed in Phase 2

### 1. Package Rename & Namespace Migration
* Changed `applicationId` to `com.myplanetservices.stillwords`.
* Changed `namespace` to `com.myplanetservices.stillwords`.
* Moved all source packages across `app/src/main/java`, `app/src/test/java`, and `app/src/androidTest/java` to `com/myplanetservices/stillwords/`.
* Renamed application from "Aura Quotes" to **"Stillwords"**, updating all user-facing copy, notification channels, export watermarks, save directory (`Pictures/Stillwords`), and internal class names (`StillwordsDatabase`, `StillwordsNavHost`, `StillwordsTheme`, `NOIR_STILLWORDS`).
* Updated all Kotlin imports, the `AndroidManifest.xml`, `FileProvider` authorities, Room DAO/Database, and navigation references.
* *Note:* The `applicationId` cannot be changed after the initial Google Play Console release upload.

### 2. SDK Targets & Edge-to-Edge Baseline
* Updated baseline configuration per `AGENTS.md`: `compileSdk = 37` (Android 17, final stable platform), `targetSdk = 36` (Google Play requirement), and `minSdk = 26` (Android 8.0+).
* **Android 17 / API 37 Stability Evidence:**
  * Official Google Android Developers release notes confirm Android 17 reached Platform Stability with Beta 3/4 and launched as the final stable release on **June 16, 2026**.
  * Installed SDK platform `platforms/android-37` verified via `source.properties`: `Platform.Version=17`, `AndroidVersion.ApiLevel=37.0`, `IsBaseSdk=true`, `PreviewSdkInt=0`, `CodeName=` (blank, non-preview).
* Configured full Edge-to-Edge support handling window insets on every screen (`statusBarsPadding()`, `navigationBarsPadding()`, `NavigationBarDefaults.windowInsets`).
* Standardized back navigation on Compose `BackHandler` and `NavController.popBackStack()`, ensuring full predictive-back compatibility without `onBackPressed()` or `KEYCODE_BACK` overrides.

### 3. Dependency Cleanup
* Removed unused libraries: `firebase-ai`, `firebase-appcheck-recaptcha`, `firebase-appcheck-debug`, `firebase-bom`, `retrofit`, `converter-moshi`, `moshi-kotlin`, `okhttp`, and `logging-interceptor`.
* Removed unneeded plugins: `secrets-gradle-plugin` and `google-services`.
* Deleted obsolete `.env.example`.
* Confirmed via project-wide scan that zero code references remain.

### 4. Modern API Replacements & Opt-ins
* Replaced deprecated `Divider` with Material 3 `HorizontalDivider` in `SettingsScreen.kt`.
* Replaced deprecated `Icons.Default.TrendingUp` with `Icons.AutoMirrored.Filled.TrendingUp` in `ExploreScreen.kt`.
* Added explicit `@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)` annotations to `SearchViewModel.kt`.
* Adopted Compose BOM 2026 lint standards: replaced non-observable `java.util.Locale.getDefault()` calls within composables with `androidx.compose.ui.platform.LocalLocale.current.platformLocale` in `HomeScreen.kt` and `SettingsScreen.kt`.

### 5. Gradle Wrapper Validation
* Restored `validateDistributionUrl=true` in `gradle/wrapper/gradle-wrapper.properties`. Confirmed wrapper checksum verification passes.

### 6. Room Schema Export & Migration Setup
* Enabled `exportSchema = true` in `StillwordsDatabase.kt`.
* Configured KSP compiler argument `room.schemaLocation` targeting `app/schemas`.
* Upgraded Room to `2.8.5` and KSP to `2.3.12`.
* Generated and committed Room schema JSON `app/schemas/com.myplanetservices.stillwords.data.local.StillwordsDatabase/1.json` (Database version 1).

### 7. Collection Detail Navigation Route
* Added `CollectionDetailScreen.kt` and `CollectionDetailViewModel.kt`.
* Registered `Screen.CollectionDetail.route` in `StillwordsNavHost.kt`.
* Connected collection cards in `FavoritesScreen.kt` so tapping a collection navigates directly to the collection's quotes with back handling and deletion support.

### 8. Daily Reminders (WorkManager)
* Replaced placeholder reminder logic with AndroidX `WorkManager` (`2.10.0`).
* Implemented `DailyReminderWorker` (`CoroutineWorker`) and periodic daily scheduling (`scheduleDailyReminder`) honouring user-selected hour and minute.
* Guaranteed survival across device reboots without exact alarms or `SCHEDULE_EXACT_ALARM`.
* Restricted `POST_NOTIFICATIONS` runtime permission requests strictly to when the user toggles reminders ON in Settings (never prompted on first launch or during onboarding).

### 9. Centralized Application Versioning
* Versioning maintained in `gradle.properties`:
  * `APP_VERSION_CODE=1` (Incremented on every Play Store release bundle)
  * `APP_VERSION_NAME=1.0.0` (User-facing version string)
* Documented release history in `CHANGELOG.md` with full 1.0.0 entry.

### 10. Production Release Signing
* Release signing dynamically loads credentials from `keystore.properties` (`storeFile`, `storePassword`, `keyAlias`, `keyPassword`).
* Configured `.gitignore` to strictly exclude `keystore.properties`, `*.jks`, `*.keystore`, `debug.keystore`, `local.properties`, `build/`, `.gradle/`, and `.idea/`.
* When `keystore.properties` is absent, release builds fail with an explicit notification instructing configuration rather than falling back to debug keys.
* Authored `RELEASE.md` documenting keytool keystore generation, backup procedures, Play App Signing, and update workflows.

### 11. Release Build Hardening (R8)
* Enabled `isMinifyEnabled = true` and `isShrinkResources = true` in `app/build.gradle.kts`.
* Configured R8 keep rules in `proguard-rules.pro` protecting Room, Coil, Coroutines, WorkManager, and Google Play Update APIs.
* Verified release compilation using temporary local keystore (immediately deleted after build verification).

### 12. Google Play Flexible In-App Updates
* Integrated `com.google.android.play:app-update` and `app-update-ktx` (`2.1.0`).
* Built `InAppUpdateHelper` quietly querying update availability on launch without blocking UI.
* Displays a non-blocking `Snackbar` with a "Restart" action upon background download completion.
* Fails silently when operating offline or outside the Play Store environment.

### 13. Cross-Platform Line Endings (.gitattributes)
* Created `.gitattributes` establishing `gradlew` with LF, `*.bat` with CRLF, and all other text files as `auto`.
* Re-normalized repository via `git add --renormalize .`.

### 14. WhatsApp & Social Image Card Sharing Fix
* **Problem:** Sharing a quote card to WhatsApp previously shared only the plain text string, omitting the card graphic.
* **Root Cause & Fix:**
  1. The sharing engine was updated in `ImageExportUtil.kt` to capture a high-quality JPEG bitmap (`CardAspectRatio.SQUARE_1_1`) and store it in `context.cacheDir/shared_quotes/`.
  2. Generated content URI via `FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", cacheFile)` ensuring authority matches the manifest (`${applicationId}.fileprovider`).
  3. Configured `Intent(Intent.ACTION_SEND)` with `type = "image/jpeg"`, `putExtra(Intent.EXTRA_STREAM, uri)`, and `putExtra(Intent.EXTRA_TEXT, "...")`.
  4. **Critical modern Android permission fix:** Added `intent.clipData = ClipData.newUri(context.contentResolver, "Stillwords Quote", uri)` and `intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)`. WhatsApp and Android 10+ share sheets require the content URI inside `ClipData` for grant read permissions to be honored.
  5. Updated all share touchpoints throughout the app (`HomeScreen`, `QuoteDetailScreen`, `ExploreScreen`, `FavoritesScreen`, `CollectionDetailScreen`, `SearchScreen`, `QuoteDesignerViewModel`) to invoke `ImageExportUtil.shareCardImage(...)`.

---

## 3. Dependency Inventory & Live XML Verification

Fetched live Maven metadata XML documents directly from Google Maven (`dl.google.com/android/maven2`) and Maven Central (`repo1.maven.org/maven2`):

| Dependency | Live Metadata XML URL | Latest Stable in XML | Selected Stable Version | Compatibility / Technical Rationale |
| :--- | :--- | :--- | :--- | :--- |
| **Android Gradle Plugin** | [AGP metadata](https://dl.google.com/android/maven2/com/android/tools/build/gradle/maven-metadata.xml) | `9.1.1` | `9.1.1` | Matches Gradle 9.3.1 wrapper and fully supports compileSdk 37. |
| **Kotlin** | [Kotlin metadata](https://repo1.maven.org/maven2/org/jetbrains/kotlin/kotlin-gradle-plugin/maven-metadata.xml) | `2.2.10` | `2.2.10` | JetBrains stable release. |
| **Compose BOM** | [Compose BOM metadata](https://dl.google.com/android/maven2/androidx/compose/compose-bom/maven-metadata.xml) | `2026.09.00` | `2026.09.00` | Latest stable Google release compiled with compileSdk 37. |
| **Core KTX** | [Core KTX metadata](https://dl.google.com/android/maven2/androidx/core/core-ktx/maven-metadata.xml) | `1.19.1` | `1.19.1` | Latest stable Google release compiled with compileSdk 37. |
| **Activity Compose** | [Activity Compose metadata](https://dl.google.com/android/maven2/androidx/activity/activity-compose/maven-metadata.xml) | `1.13.0` | `1.13.0` | Latest stable Google release compiled with compileSdk 37. |
| **Lifecycle KTX / Compose** | [Lifecycle metadata](https://dl.google.com/android/maven2/androidx/lifecycle/lifecycle-runtime-ktx/maven-metadata.xml) | `2.11.0` | `2.11.0` | Latest stable Google release compiled with compileSdk 37. |
| **Navigation Compose** | [Navigation metadata](https://dl.google.com/android/maven2/androidx/navigation/navigation-compose/maven-metadata.xml) | `2.10.2` | `2.10.2` | Latest stable Google release compiled with compileSdk 37. |
| **Room Runtime / KTX / Compiler** | [Room metadata](https://dl.google.com/android/maven2/androidx/room/room-runtime/maven-metadata.xml) | `2.8.5` | `2.8.5` | Upgraded from Phase 1 (`2.7.0`). Fully stable and compatible with API 36/37. |
| **Google KSP** | [KSP metadata](https://repo1.maven.org/maven2/com/google/devtools/ksp/com.google.devtools.ksp.gradle.plugin/maven-metadata.xml) | `2.3.12` | `2.3.12` | Upgraded from Phase 1 (`2.3.5`). Fully stable matching Kotlin 2.2.10. |
| **DataStore Preferences** | [DataStore metadata](https://dl.google.com/android/maven2/androidx/datastore/datastore-preferences/maven-metadata.xml) | `1.2.1` | `1.2.1` | Upgraded from Phase 1 (`1.1.7`). Fully stable and compatible with API 36/37. |
| **WorkManager Runtime / KTX** | [WorkManager metadata](https://dl.google.com/android/maven2/androidx/work/work-runtime-ktx/maven-metadata.xml) | `2.10.0` | `2.10.0` | Latest stable Google release. |
| **Play App Update** | [Play Update metadata](https://dl.google.com/android/maven2/com/google/android/play/app-update-ktx/maven-metadata.xml) | `2.1.0` | `2.1.0` | Latest stable Google Play In-App Updates library. |
| **Coil Compose** | [Coil metadata](https://repo1.maven.org/maven2/io/coil-kt/coil-compose/maven-metadata.xml) | `2.7.0` | `2.7.0` | Latest stable 2.x Coil release for Compose. |
| **Kotlinx Coroutines** | [Coroutines metadata](https://repo1.maven.org/maven2/org/jetbrains/kotlinx/kotlinx-coroutines-android/maven-metadata.xml) | `1.10.2` | `1.10.2` | Latest stable JetBrains release. |
| **JUnit 4** | [JUnit 4 metadata](https://repo1.maven.org/maven2/junit/junit/maven-metadata.xml) | `4.13.2` | `4.13.2` | Standard stable testing framework. |
| **AndroidX Test JUnit** | [AndroidX JUnit metadata](https://dl.google.com/android/maven2/androidx/test/ext/junit/maven-metadata.xml) | `1.3.0` | `1.3.0` | Latest stable release. |
| **Espresso Core** | [Espresso metadata](https://dl.google.com/android/maven2/androidx/test/espresso/espresso-core/maven-metadata.xml) | `3.7.0` | `3.7.0` | Latest stable release. |
| **AndroidX Test Core / Runner** | [Test Core metadata](https://dl.google.com/android/maven2/androidx/test/core/maven-metadata.xml) | `1.6.1` / `1.6.2` | `1.6.1` / `1.6.2` | Latest stable release. |
| **Robolectric** | [Robolectric metadata](https://repo1.maven.org/maven2/org/robolectric/robolectric/maven-metadata.xml) | `4.17` | `4.17` | Upgraded from Phase 1 (`4.16.1`). Latest stable release. |

---

## 4. Release Size Clarification & R8 Hardening

* **Release APK Size Clarification:**
  * The actual, disk-measured release APK size with latest dependencies and API 37 is **2.17 MB** (`2,270,354 bytes`).
  * In `app/build.gradle.kts`:
    ```kotlin
    isMinifyEnabled = true
    isShrinkResources = true
    ```
    Both R8 minification and resource shrinking are enabled and actively strip unused Compose components, icons, resources, and transitive code.
* **APK Size Comparison:**
  * Original Phase 1 Debug APK: **23.3 MB**
  * Phase 2 Debug APK (un-minified): **19.65 MB**
  * Phase 2 Release APK (R8 minified + shrunk): **2.17 MB** (90.7% reduction)

---

## 5. Verification Check Log

* `./gradlew clean assembleDebug`: **BUILD SUCCESSFUL** (6m 6s, 0 errors)
* `./gradlew testDebugUnitTest`: **BUILD SUCCESSFUL** (1m 17s, 0 test failures)
* `./gradlew lintDebug`: **BUILD SUCCESSFUL** (3m 5s, 0 errors)
* `./gradlew assembleRelease`: **BUILD SUCCESSFUL** (6m 49s, 48 tasks, R8 minification verified, temporary signing key immediately purged)

---

## 6. Quote Categories in Code (for Phase 3 Preparation)

The application code (`SeedQuotes.kt`) contains quotes across **22 unique categories**:

1. `Confidence`
2. `Courage`
3. `Creativity`
4. `Discipline`
5. `Fitness`
6. `Friendship`
7. `Gratitude`
8. `Growth`
9. `Happiness`
10. `Healing`
11. `Leadership`
12. `Life`
13. `Love`
14. `Mindfulness`
15. `Motivation`
16. `Peace`
17. `Relationships`
18. `Self-Love`
19. `Study`
20. `Success`
21. `Wisdom`
22. `Work`

*(Curated collections defined in code: "Morning Clarity", "Deep Focus", "Evening Calm", "Courage & Self-Trust", "Gratitude Daily".)*
