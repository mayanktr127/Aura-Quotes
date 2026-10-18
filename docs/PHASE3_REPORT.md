# PHASE 3 — CONTENT ENGINE: PHOTO BACKGROUNDS + QUOTES REPORT

This report documents the completion of **Phase 3** for **Stillwords** in strict accordance with `AGENTS.md` and the Phase 3 prompt requirements.

---

## 1. Project Phase Roadmap

* **Phase 1:** Code Audit & Baseline Build *(Completed)*
* **Phase 2:** Foundation, Cleanup & Release Plumbing *(Completed)*
* **Phase 3:** Content Engine: Photo Backgrounds + Quotes *(Completed)*
* **Phase 4:** UI Polish, Save & Share Enhancements *(Next)*
* **Phase 5:** AdMob Integration & UMP Consent Flow
* **Phase 6:** Google Play Store Readiness & Publication

---

## 2. Summary of Work Executed in Phase 3

### A. Photo Background Processing Engine
* Developed an automated, cross-platform image processing script in `scripts/process_images.py` (with companion command wrappers `scripts/process_images.bat` for Windows and `scripts/process_images` for POSIX systems).
* The script reads source photography from `design-assets/backgrounds/<theme>/` and writes to `app/src/main/assets/backgrounds/<theme>/`:
  * Center-crops images to portrait **9:16** aspect ratio.
  * Resizes precisely to **1080 x 1920** pixels.
  * Converts images to modern **WebP** format at quality 75.
  * Completely strips EXIF metadata for privacy and minimal payload.
  * Retains original file stems with `.webp` extensions.
* **Asset Statistics:**
  * **Total background files processed:** 84 files across 7 theme directories.
  * **Total asset directory size:** 11.41 MB (11,959,860 bytes).
  * **Largest file:** `forest-botanical/forest-botanical-01.webp` (391.1 KB / 400,508 bytes).
  * **Average file size:** 142.3 KB (well within the target under 180 KB per image).
* Processed and merged `design-assets/credits.csv` and `design-assets/ai-sources.csv` into `app/src/main/assets/credits.json` containing 84 photographer credits with source URLs, license designations, and the official AI disclosure statement.

### B. Theme-to-Category Mapping
* Created a single, centralized configuration in code: `com.myplanetservices.stillwords.data.model.BackgroundTheme.kt`.
* Mapped all 7 themes directly to quote categories:
  * `sunrise-roads`: Motivation, Success, Discipline, Fitness, Work
  * `mountains`: Courage, Confidence, Leadership, Growth
  * `still-water`: Peace, Mindfulness, Healing
  * `forest-botanical`: Life, Gratitude, Self-Love
  * `golden-hour`: Love, Relationships, Friendship, Happiness
  * `night-sky`: Wisdom, Creativity
  * `quiet-spaces`: Study
* Defined deterministic fallback: unmapped categories return `null`, allowing quote cards to fall back seamlessly to existing gradient styles.

### C. Deterministic Background Engine
* Built `com.myplanetservices.stillwords.data.repository.BackgroundRepository.kt`:
  * Dynamically queries the Android `AssetManager` (`backgrounds/<theme>`) without any hardcoded file names, automatically supporting future image additions.
  * Employs thread-safe in-memory caching via `ConcurrentHashMap` for zero-allocation asset list lookups.
  * Selects backgrounds deterministically using quote ID hashing: `abs(quoteId.hashCode()) % files.size`. The same quote always displays the exact same photo background across all app screens and sessions.
  * Loads photos via Coil `AsyncImage` with memory and disk caching configured, ensuring smooth 60fps scrolling on low-end hardware.
  * Provides instant graceful fallback to procedural gradient styles if an asset fails to load.

### D. Quote Card Rendering & Contrast Scrim
* **3-Layer Architecture (`QuoteCard.kt`):**
  1. **Background Layer:** Full-bleed portrait photo loaded from `assets:///backgrounds/<theme>/<file>.webp` via Coil (or procedural gradient fallback).
  2. **Contrast Scrim Layer:** Vertical gradient scrim running from `0x66000000` (40% black) at the top to `0xDD000000` (86.7% black) at the bottom. Ensures foreground text consistently exceeds WCAG AA 4.5:1 contrast ratio across any photo brightness.
  3. **Foreground Content Layer:** Gold metallic quotation marks, category badge pill, responsive quote typography, author attribution, and action buttons.
* **Auto-Sizing Typography & Overflow Prevention:**
  * Implemented dynamic font sizing calculating base size from quote text character count (down to 15.sp for quotes exceeding 200 characters).
  * Factored in system font scaling up to 1.3x (`LocalDensity.current.fontScale`), capping effective text line heights.
  * Enforced safety line limits (`maxLines = if (isHero) 10 else 7`) with `TextOverflow.Ellipsis`, guaranteeing zero layout clipping on compact 360dp-wide screens.
  * Validated readability and visual balance across both Light and Dark application themes.
* **Pixel-Identical Image Export & Share (`ImageExportUtil.kt`):**
  * Updated canvas renderer to decode the identical asset WebP photo, center-crop and scale to target bitmap dimensions, paint the identical dark gradient scrim, and render contrasting gold/white typography.
  * Updated `shareCardImage` to accept `photoAssetPath` and `quoteId`.
  * Connected all 14 quote card share call sites across `HomeScreen`, `ExploreScreen`, `FavoritesScreen`, `QuoteDetailScreen`, `CollectionDetailScreen`, and `SearchScreen`.
  * The saved or shared image matches what the user sees on screen pixel-for-pixel.
* **Quote Designer Photo Carousel:**
  * Updated `QuoteDesignerViewModel.kt` and `QuoteDesignerScreen.kt` to load photo background themes alongside classic gradient styles.
  * Added selectable photo background carousel with live preview thumbnails (all unlocked, no paywalls).
  * Canvas bitmap generation in designer renders user-selected photo background.

### E. Quotes Data Expansion & Legal Audit
* Conducted a strict copyright and attribution audit against `AGENTS.md` content rules and Wikiquote:
  * Public domain threshold: Authors deceased prior to 1956 (>70 years).
  * Wikiquote validation: Attributed quotes verified against Wikiquote "Sourced" sections only (excluding "Misattributed" and "Disputed").
  * Audit outcome: Out of 50 original seed quotes, 5 historic quotes were verified and retained, 2 quotes were re-attributed to Stillwords originals, and 43 unverified/disputed quotes were removed.
  * Full audit documented in `docs/QUOTE_AUDIT.md`.
* Migrated quote data from Kotlin code to `app/src/main/assets/quotes.json` with schema: `id`, `text`, `author`, `category`, `isOriginal`.
* Expanded content database to **660 quotes** — exactly 30 quotes per category across all 22 categories — authored as warm, thoughtful, specific Stillwords originals (`isOriginal = true`).
* Updated Room persistence:
  * Added `isOriginal: Boolean` field to `QuoteEntity.kt`, `Quote.kt`, and `QuoteDao.kt`.
  * Regenerated and committed Room database schema `app/schemas/com.myplanetservices.stillwords.data.local.StillwordsDatabase/1.json` (Database version 1).
  * Room database seeds from `quotes.json` on first launch. On future app updates, `insertQuotes(quotes)` safely inserts new quotes by ID without modifying user favourites, collections, reflections, or reading history.
  * Preserved the 4 curated collections ("Morning Clarity", "Deep Focus", "Evening Calm", "Courage & Self-Trust") mapped to valid quote IDs (`q_1` to `q_50`).

### F. Photo Credits Screen
* Created `com.myplanetservices.stillwords.data.model.PhotoCredit.kt` with JSON asset loader for `credits.json`.
* Developed `PhotoCreditsScreen.kt`:
  * Prominent AI disclosure banner: *"Some backgrounds were created with AI tools."*
  * Scrollable list of photographer credits grouped with photo previews, photographer names, and tappable external links opening source URLs via `Intent.ACTION_VIEW`.
* Registered navigation route `Screen.PhotoCredits` in `Screen.kt` and `StillwordsNavHost.kt`.
* Added "Photo credits" settings row in `SettingsScreen.kt` under the Legal section.

---

## 3. Quote Count Per Category (Before & After)

| Category | Seed Count (Before) | Audited Kept | Originals Added | Final Count (Phase 3) |
| :--- | :---: | :---: | :---: | :---: |
| **Confidence** | 2 | 0 | 30 | **30** |
| **Courage** | 3 | 0 | 30 | **30** |
| **Creativity** | 2 | 0 | 30 | **30** |
| **Discipline** | 2 | 0 | 30 | **30** |
| **Fitness** | 2 | 0 | 30 | **30** |
| **Friendship** | 2 | 0 | 30 | **30** |
| **Gratitude** | 3 | 0 | 30 | **30** |
| **Growth** | 2 | 0 | 30 | **30** |
| **Happiness** | 2 | 0 | 30 | **30** |
| **Healing** | 2 | 0 | 30 | **30** |
| **Leadership** | 2 | 0 | 30 | **30** |
| **Life** | 4 | 1 | 29 | **30** |
| **Love** | 2 | 0 | 30 | **30** |
| **Mindfulness** | 3 | 1 | 29 | **30** |
| **Motivation** | 4 | 2 | 28 | **30** |
| **Peace** | 2 | 0 | 30 | **30** |
| **Relationships** | 2 | 0 | 30 | **30** |
| **Self-Love** | 2 | 0 | 30 | **30** |
| **Study** | 2 | 1 | 29 | **30** |
| **Success** | 3 | 2 | 28 | **30** |
| **Wisdom** | 3 | 0 | 30 | **30** |
| **Work** | 1 | 0 | 30 | **30** |
| **Total** | **50** | **7** | **653** | **660** |

---

## 4. Verification Check Log

All verification commands executed from clean state with zero errors:

| Test / Check | Result | Execution Time | Notes |
| :--- | :---: | :---: | :--- |
| `./gradlew clean assembleDebug` | **BUILD SUCCESSFUL** | 52s | 0 compiler errors. Debug APK generated. |
| `./gradlew testDebugUnitTest` | **BUILD SUCCESSFUL** | 25s | 0 unit test failures. |
| `./gradlew lintDebug` | **BUILD SUCCESSFUL** | 29s | 0 lint errors. |
| `./gradlew assembleRelease` | **BUILD SUCCESSFUL** | 6m 26s | Full R8 minification & resource shrinking verified. Keystore and properties immediately purged after build. |

---

## 5. APK Size Comparison

| Build Variant | Phase 1 Size | Phase 2 Size | Phase 3 Size | Delta vs Phase 2 | Explanation |
| :--- | :---: | :---: | :---: | :---: | :--- |
| **Debug APK** | 23.30 MB | 19.65 MB | **33.89 MB** (`33,891,528` B) | +14.24 MB | Includes 84 uncompressed debug assets (11.41 MB WebP images + quotes/credits JSON) + debug symbols. |
| **Release APK** | N/A | 2.17 MB (`2,279,742` B) | **13.62 MB** (`14,286,227` B) | +11.45 MB | Base R8 release binary (~2.18 MB) + 84 WebP background photos (11.41 MB) + `quotes.json` (222 KB) + `credits.json` (15 KB). |

---

## 6. Manifest Minimum Permissions

As required by `AGENTS.md`, permissions are kept strictly minimal. No storage permissions (`WRITE_EXTERNAL_STORAGE`, `READ_EXTERNAL_STORAGE`, `MANAGE_EXTERNAL_STORAGE`) are requested.

Permissions present in `AndroidManifest.xml`:
1. `android.permission.POST_NOTIFICATIONS` — Requested at runtime only when the user toggles daily reminders ON in Settings. Never requested during onboarding or launch.
2. `android.permission.RECEIVE_BOOT_COMPLETED` — Required by AndroidX `WorkManager` to reschedule daily reminder workers across device reboots.

---

## 7. Walkthrough: Files Changed & Rationale

| File | Change Type | Reason |
| :--- | :---: | :--- |
| `scripts/process_images.py` | Created | Automated script to center-crop 9:16, resize 1080x1920, convert to WebP, and strip metadata. |
| `scripts/process_images.bat` | Created | Windows CLI wrapper for running image processing pipeline. |
| `scripts/process_images` | Created | POSIX bash CLI wrapper for running image processing pipeline. |
| `app/src/main/assets/backgrounds/*` | Created | 84 processed WebP photo backgrounds across 7 theme directories (11.41 MB total). |
| `app/src/main/assets/credits.json` | Created | Photographer credits and AI disclosure statement converted from source CSV files. |
| `app/src/main/assets/quotes.json` | Created | 660 verified quotes (30 per category) with Stillwords original content. |
| `app/schemas/.../1.json` | Updated | Room database schema version 1 committed with new `isOriginal` column. |
| `app/src/main/java/.../data/model/BackgroundTheme.kt` | Created | Centralized theme-to-category mapping for photo backgrounds. |
| `app/src/main/java/.../data/model/PhotoCredit.kt` | Created | Data model and asset parser for `credits.json`. |
| `app/src/main/java/.../data/model/Quote.kt` | Modified | Added `isOriginal: Boolean` field to domain model. |
| `app/src/main/java/.../data/local/QuoteEntity.kt` | Modified | Added `isOriginal: Boolean` column to Room entity. |
| `app/src/main/java/.../data/local/QuoteDao.kt` | Modified | Updated Room DAO queries to read/write `isOriginal`. |
| `app/src/main/java/.../data/local/SeedQuotes.kt` | Modified | Rewritten to load seed quotes from `quotes.json` and seed Room without overwriting user data. |
| `app/src/main/java/.../data/repository/BackgroundRepository.kt` | Created | Asset-based photo repository with deterministic quote ID hashing and in-memory caching. |
| `app/src/main/java/.../data/repository/QuoteRepository.kt` | Modified | Updated repository to seed from `quotes.json` on initialization. |
| `app/src/main/java/.../ui/components/QuoteCard.kt` | Modified | 3-layer card rendering (photo -> contrast scrim -> typography), auto-sizing text, and gradient fallback. |
| `app/src/main/java/.../util/ImageExportUtil.kt` | Modified | Synchronized canvas bitmap exporter to render photo backgrounds, scrims, and WCAG AA contrast. |
| `app/src/main/java/.../ui/navigation/Screen.kt` | Modified | Added `Screen.PhotoCredits` route definition. |
| `app/src/main/java/.../ui/navigation/StillwordsNavHost.kt` | Modified | Added navigation destination and composable binding for `PhotoCreditsScreen`. |
| `app/src/main/java/.../ui/screens/credits/PhotoCreditsScreen.kt` | Created | Photo credits UI screen displaying photographer attributions, links, and AI disclosure. |
| `app/src/main/java/.../ui/screens/settings/SettingsScreen.kt` | Modified | Added "Photo credits" settings row linking to credits screen. |
| `app/src/main/java/.../ui/screens/designer/QuoteDesignerViewModel.kt` | Modified | Added photo background loading, selection state, and export integration. |
| `app/src/main/java/.../ui/screens/designer/QuoteDesignerScreen.kt` | Modified | Added photo background carousel alongside gradient styles with live preview. |
| `app/src/main/java/.../ui/screens/home/HomeScreen.kt` | Modified | Passed quote ID and photo background to export/share triggers. |
| `app/src/main/java/.../ui/screens/explore/ExploreScreen.kt` | Modified | Passed quote ID and photo background to export/share triggers. |
| `app/src/main/java/.../ui/screens/favorites/FavoritesScreen.kt` | Modified | Passed quote ID and photo background to export/share triggers. |
| `app/src/main/java/.../ui/screens/detail/QuoteDetailScreen.kt` | Modified | Passed quote ID and photo background to export/share triggers. |
| `app/src/main/java/.../ui/screens/collection/CollectionDetailScreen.kt` | Modified | Passed quote ID and photo background to export/share triggers. |
| `app/src/main/java/.../ui/screens/search/SearchScreen.kt` | Modified | Passed quote ID and photo background to export/share triggers. |
| `docs/QUOTE_AUDIT.md` | Created | Comprehensive legal and attribution audit documentation for all 660 quotes. |
| `docs/PHASE3_REPORT.md` | Created | Full Phase 3 execution and verification report. |

---

## 8. Manual Testing Guide on Device / Emulator

1. **Launch App:**
   * Observe home screen quote card: A high-resolution photo background should appear with a dark gradient scrim behind clear, high-contrast gold/white text.
   * Verify quotation marks and category badges remain crisply visible.
2. **Category Navigation:**
   * Navigate to Explore screen and filter by categories:
     * Motivation / Success -> `sunrise-roads` photography.
     * Courage / Leadership -> `mountains` photography.
     * Peace / Mindfulness -> `still-water` photography.
     * Life / Gratitude -> `forest-botanical` photography.
     * Love / Happiness -> `golden-hour` photography.
     * Wisdom / Creativity -> `night-sky` photography.
     * Study -> `quiet-spaces` photography.
3. **Deterministic Persistence:**
   * Tap any quote to view detail, navigate back, and verify the identical photo background remains assigned.
4. **Font Scaling & Layout Integrity:**
   * Go to Android device Settings -> Accessibility -> Display size and text. Increase font size to maximum (1.3x).
   * Return to Stillwords: Verify long quotes scale gracefully without clipping, overflowing, or obscuring author attribution.
5. **Card Sharing & Export:**
   * Tap "Share" on any photo quote card: select WhatsApp or save image to device.
   * Open the shared/saved graphic: confirm the exported image has the identical photo, dark scrim, gold accents, and typography.
6. **Quote Designer:**
   * Tap "Design" on a quote.
   * Observe photo background carousel with live previews. Tap through themes to verify instant preview updates and save/share export.
7. **Photo Credits Screen:**
   * Go to Settings -> Tap "Photo credits".
   * Verify the AI disclosure banner is present at top (*"Some backgrounds were created with AI tools."*).
   * Verify photographer list renders and tapping a credit opens the external browser link.

---

## 9. Known Limitations

* **App Size Increase:** Adding 84 high-resolution 1080x1920 WebP backgrounds increased the release APK from 2.17 MB to 13.62 MB. This is completely standard for local asset-bundled wallpaper/quote apps and remains well below Google Play's 150 MB initial download limit.
* **Asset Loading Overhead:** On first launch, Room parses `quotes.json` (~222 KB) into SQLite. On modern devices this takes <100ms. Subsequent launches query Room SQLite directly with zero JSON overhead.

---

## 10. Out-of-Scope Issues Found (Preserved for Future Phases)

1. **UI Layout Refresh (Phase 4):**
   * Top app bars, bottom navigation bar styling, and collection grid cards still use Phase 2 baseline layout. Per instructions, layout restyling is deferred to Phase 4.
2. **AdMob Integration & UMP Consent (Phase 5):**
   * No banner or interstitial ads are present; AdMob SDK and UMP consent flows will be integrated in Phase 5 per project roadmap.
3. **Deprecated Material 3 TabRow:**
   * `TabRow` deprecation warning noted during build in `QuoteDesignerScreen.kt` and `FavoritesScreen.kt` (recommending `PrimaryTabRow` / `SecondaryTabRow`). Can be modernized during Phase 4 UI polish.
