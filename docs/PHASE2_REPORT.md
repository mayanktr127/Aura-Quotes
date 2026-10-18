# PHASE 2 — FOUNDATION, CLEANUP & RELEASE PLUMBING REPORT

This report documents the completion of **Phase 2** for **Aura Quotes** in accordance with `AGENTS.md`.

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
* Changed `applicationId` to `com.myplanetservices.auraquotes`.
* Changed `namespace` to `com.myplanetservices.auraquotes`.
* Moved all source packages across `app/src/main/java`, `app/src/test/java`, and `app/src/androidTest/java` to `com/myplanetservices/auraquotes/`.
* Updated all Kotlin imports, the `AndroidManifest.xml`, `FileProvider` authorities, Room DAO/Database, and navigation references.
* *Note:* The `applicationId` cannot be changed after the initial Google Play Console release upload.

### 2. SDK Targets & Edge-to-Edge Baseline
* Updated build target parameters to `compileSdk = 36`, `targetSdk = 36`, and `minSdk = 26` (Android 8.0+).
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
* Localized all `String.format` invocations with explicit `Locale.getDefault()`.

### 5. Gradle Wrapper Validation
* Restored `validateDistributionUrl=true` in `gradle/wrapper/gradle-wrapper.properties`. Confirmed wrapper checksum verification passes.

### 6. Room Schema Export & Migration Setup
* Enabled `exportSchema = true` in `AuraDatabase.kt`.
* Configured KSP compiler argument `room.schemaLocation` targeting `app/schemas`.
* Upgraded Room to `2.8.5` and KSP to `2.3.12`.
* Generated and committed Room schema JSON `app/schemas/com.myplanetservices.auraquotes.data.local.AuraDatabase/1.json` (Database version 1).

### 7. Collection Detail Navigation Route
* Added `CollectionDetailScreen.kt` and `CollectionDetailViewModel.kt`.
* Registered `Screen.CollectionDetail.route` in `AuraNavHost.kt`.
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
  4. **Critical modern Android permission fix:** Added `intent.clipData = ClipData.newUri(context.contentResolver, "Aura Quote", uri)` and `intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)`. WhatsApp and Android 10+ share sheets require the content URI inside `ClipData` for grant read permissions to be honored.
  5. Updated all share touchpoints throughout the app (`HomeScreen`, `QuoteDetailScreen`, `ExploreScreen`, `FavoritesScreen`, `CollectionDetailScreen`, `SearchScreen`, `QuoteDesignerViewModel`) to invoke `ImageExportUtil.shareCardImage(...)`.

---

## 3. Dependency Inventory & Live XML Verification

Fetched live Maven metadata XML documents directly from Google Maven (`dl.google.com/android/maven2`) and Maven Central (`repo1.maven.org/maven2`):

| Dependency | Live Metadata XML URL | Latest Stable in XML | Selected Stable Version | Compatibility / Technical Rationale |
| :--- | :--- | :--- | :--- | :--- |
| **Android Gradle Plugin** | [AGP metadata](https://dl.google.com/android/maven2/com/android/tools/build/gradle/maven-metadata.xml) | `9.1.1` | `9.1.1` | Matches Gradle 9.3.1 wrapper. |
| **Kotlin** | [Kotlin metadata](https://repo1.maven.org/maven2/org/jetbrains/kotlin/kotlin-gradle-plugin/maven-metadata.xml) | `2.2.10` | `2.2.10` | JetBrains stable release. |
| **Compose BOM** | [Compose BOM metadata](https://dl.google.com/android/maven2/androidx/compose/compose-bom/maven-metadata.xml) | `2026.09.00` | `2025.02.00` | BOMs newer than 2025.02.00 declare `compileSdk 37` in AAR metadata, which conflicts with project mandate `compileSdk 36`. `2025.02.00` is the highest stable compatible with API 36. |
| **Core KTX** | [Core KTX metadata](https://dl.google.com/android/maven2/androidx/core/core-ktx/maven-metadata.xml) | `1.19.1` | `1.18.0` | `1.19.0` and `1.19.1` declare `compileSdk 37` in their AAR metadata. `1.18.0` (Phase 1 audit value) is the highest stable compatible with API 36. |
| **Activity Compose** | [Activity Compose metadata](https://dl.google.com/android/maven2/androidx/activity/activity-compose/maven-metadata.xml) | `1.13.0` | `1.10.1` | `1.11.0`+ declare `compileSdk 37`. `1.10.1` is the highest stable compatible with API 36. |
| **Lifecycle KTX / Compose** | [Lifecycle metadata](https://dl.google.com/android/maven2/androidx/lifecycle/lifecycle-runtime-ktx/maven-metadata.xml) | `2.11.0` | `2.8.7` | `2.9.0`+ declare `compileSdk 37`. `2.8.7` is the highest stable compatible with API 36. |
| **Navigation Compose** | [Navigation metadata](https://dl.google.com/android/maven2/androidx/navigation/navigation-compose/maven-metadata.xml) | `2.10.2` | `2.8.9` | `2.9.0`+ declare `compileSdk 37`. `2.8.9` (Phase 1 audit value) is the highest stable compatible with API 36. |
| **Room Runtime / KTX / Compiler** | [Room metadata](https://dl.google.com/android/maven2/androidx/room/room-runtime/maven-metadata.xml) | `2.8.5` | `2.8.5` | Upgraded from Phase 1 (`2.7.0`). Fully stable and compatible with API 36. |
| **Google KSP** | [KSP metadata](https://repo1.maven.org/maven2/com/google/devtools/ksp/com.google.devtools.ksp.gradle.plugin/maven-metadata.xml) | `2.3.12` | `2.3.12` | Upgraded from Phase 1 (`2.3.5`). Fully stable matching Kotlin 2.2.10. |
| **DataStore Preferences** | [DataStore metadata](https://dl.google.com/android/maven2/androidx/datastore/datastore-preferences/maven-metadata.xml) | `1.2.1` | `1.2.1` | Upgraded from Phase 1 (`1.1.7`). Fully stable and compatible with API 36. |
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
  * The actual, disk-measured release APK size is **1.96 MB** (`2,058,863 bytes`).
  * The figure of `9.88 MB` was an unverified chat estimate prior to building.
  * In `app/build.gradle.kts` (lines 63–64):
    ```kotlin
    isMinifyEnabled = true
    isShrinkResources = true
    ```
    Both R8 minification and resource shrinking are enabled and actively strip unused Compose components, icons, resources, and transitive code.
* **APK Size Comparison:**
  * Original Phase 1 Debug APK: **23.3 MB**
  * Phase 2 Debug APK (un-minified): **19.65 MB**
  * Phase 2 Release APK (R8 minified + shrunk): **1.96 MB** (91.6% reduction)

---

## 5. Verification Check Log

* `./gradlew clean assembleDebug`: **BUILD SUCCESSFUL** (3m 23s, 0 errors)
* `./gradlew testDebugUnitTest`: **BUILD SUCCESSFUL** (1m 50s, 0 test failures)
* `./gradlew lintDebug`: **BUILD SUCCESSFUL** (2m 33s, 0 errors)
* `./gradlew assembleRelease`: **BUILD SUCCESSFUL** (8m 2s, 48 tasks, R8 minification verified, signing key immediately purged)
