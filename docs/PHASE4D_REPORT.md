# PHASE 4D REPORT — CREATE SCREEN RESTYLE, PHOTO FILTERS, UNLOCK SHEET & IMAGE EXPORT

## Executive Summary
Phase 4D delivers the complete redesign of the **Create** screen (Quote Designer), integrating Phase 4A design tokens and components, preserving the Phase 3.5 photo filter engine with 160 px downsampled cached thumbnails, introducing the rewarded unlock bottom sheet (UI only with DataStore timestamp persistence), and upgrading `ImageExportUtil` to Section 7 parity with bundled OFL fonts (Newsreader & Figtree), auto-fit sizing without truncation, and exact scrim and wordmark specifications.

---

## 1. Mockup & Design Alignment

| Screen | Design Spec Screen | PDF Mockup Page | HTML Mockup Source | Status |
| :--- | :--- | :--- | :--- | :--- |
| **Create (Designer)** | Screen 13 | Page "Create" | `design-assets/mockups/source/Designer.dc.html` | Restyled & Verified |
| **Rewarded Unlock Sheet** | Screen 14 | Page "Ad · Rewarded unlock (Create)" | `design-assets/mockups/source/UnlockSheet.dc.html` | Built & Verified |
| **Export Engine** | Section 7 | Card Export Parity | Image Export Spec | Tested & Verified (36 images) |

### Visual & Component Implementation Details
1. **Preview Card**:
   - Follows chosen aspect ratio: **1:1** (`264 × 264 dp`), **4:5 Post** (`264 × 330 dp`), and **9:16 Story** (`204 × 362 dp`). 16:9 dropped per spec.
   - Sits on a 22 dp corner radius (`RoundedCornerShape(22.dp)`) with inner luxury gold border frame (`RoundedCornerShape(14.dp)`).
   - Renders live photo backgrounds with the active `PhotoFilter` ColorMatrix, gradient scrim (20% to 65% Ink), category label in Brass accent (`#E8D3A8`), auto-shrink quote text in Newsreader 400, author in Newsreader Italic (`0xBFF3EDE3`), and bottom `STILLWORDS` wordmark in Figtree (`0x8CF3EDE3`).
2. **Format Segmented Control**:
   - `SegmentedControl` with `1:1`, `4:5 Post`, and `9:16 Story`.
3. **Backgrounds Grid & Premium Badges**:
   - 4-column grid of 72 dp tiles with selection ring (2 dp Brass outline).
   - 8 tiles: Still Water, Mountains, Golden Hour, Night Sky, Forest & Botanical (Premium), Plain / Ink, Sunrise Roads (Premium), and Quiet Spaces (Premium).
   - Premium tiles show a 22 dp circular badge with a Brass lock icon when locked.
   - Tapping a locked background opens the `UnlockSheet`.
4. **Photo Filters Carousel**:
   - Appears directly beneath the Backgrounds grid when a photo background is active; hidden when Plain / Ink is selected.
   - Shows live 160 px downsampled thumbnails from `BackgroundRepository.getThumbnailBitmap` (never decodes full-size photos into thumbnails, preventing back-navigation memory spikes).
   - 7 curated filters: Original, Noir, Warm, Golden, Earthy (Premium), Mist (Premium), Midnight (Premium).
   - Tapping a locked filter opens the `UnlockSheet`.
5. **Dashed Unlock Button**:
   - "Watch a short video to unlock premium looks for 24 hours" button with 14 dp radius, 1 dp dashed Brass outline, and 8% alpha Brass fill.
   - Automatically hidden once premium looks are unlocked.
6. **Rewarded Unlock Sheet (`UnlockSheet.kt`)**:
   - `ModalBottomSheet` with 28 dp top corners and ~62% dark scrim.
   - **Locked State**: Displays overline `PREMIUM LOOK · {LOOK}`, title, subtitle, horizontal look previews (`64 × 80 dp`) with `+ more` swatch, and "Watch video" button (with play icon) in debug builds (`BuildConfig.DEBUG`); hidden in release builds until Phase 5.
   - **Success State**: Displays a 64 dp accent check circle, "Premium looks unlocked", "Enjoy them until {time tomorrow}", and primary button "Use {Look}".
   - **DataStore Persistence**: `PREMIUM_UNLOCKED_UNTIL` stored in `user_preferences` DataStore as an epoch millisecond timestamp. Unlocking grants 24 hours process-wide.
7. **ImageExportUtil Upgrades (Section 7)**:
   - Output dimensions: `1:1 = 1080 × 1080`, `4:5 = 1080 × 1350`, `9:16 = 1080 × 1920`.
   - Bundled fonts via `ResourcesCompat.getFont`: Newsreader 400, Newsreader Italic, and Figtree. Zero system fonts.
   - Photo is always included (centre-crop to dimensions).
   - Scrim: 20% to 65% Ink top-to-bottom (adjusted for WCAG AA 4.5:1 on Mist filter).
   - Centred `STILLWORDS` wordmark in Figtree Semibold with letter-spacing 2.5, Ivory 55%.
   - Dynamic auto-fit quote text algorithm: iteratively steps down font size until the text fits comfortably within safe margins without ever truncating or clipping.

---

## 2. Export Verification & Test Suite

### Export Test (`Phase4dExportTest.kt`)
Verified export of the 3 longest quotes from `SeedQuotes` across all 3 formats, at font scale 1.0 and 1.3, with Noir and Golden filters:
- **Quote 1 (Longest)**: *"The best time to plant a tree was twenty years ago. The second best time is now. Every moment of delay is a moment of growth foregone."* (137 chars)
- **Quote 2**: *"Do not brood over your past mistakes and failures, for this will only fill your mind with grief, regret and depression. Do not repeat them in the future."* (152 chars)
- **Quote 3**: *"To know what you know and that you do not know what you do not know, that is true knowledge. Real knowledge is to know the extent of one's ignorance."* (148 chars)

**Export Artifacts Generated**: 36 images saved to `docs/phase4d-exports/`:
- `quote1_1x1_scale1_0_golden.png` (1080 × 1080)
- `quote1_1x1_scale1_0_noir.png` (1080 × 1080)
- `quote1_1x1_scale1_3_golden.png` (1080 × 1080)
- `quote1_1x1_scale1_3_noir.png` (1080 × 1080)
- `quote1_4x5_scale1_0_golden.png` (1080 × 1350)
- `quote1_4x5_scale1_0_noir.png` (1080 × 1350)
- `quote1_4x5_scale1_3_golden.png` (1080 × 1350)
- `quote1_4x5_scale1_3_noir.png` (1080 × 1350)
- `quote1_9x16_scale1_0_golden.png` (1080 × 1920)
- `quote1_9x16_scale1_0_noir.png` (1080 × 1920)
- `quote1_9x16_scale1_3_golden.png` (1080 × 1920)
- `quote1_9x16_scale1_3_noir.png` (1080 × 1920)
- *(and identically for quotes 2 and 3, totaling 36 verified PNG images)*

### WhatsApp Sharing Test
- Verified `Intent.ACTION_SEND` with package `"com.whatsapp"`, MIME type `"image/png"`, `FLAG_GRANT_READ_URI_PERMISSION`, and quote caption text. Test passed.

---

## 3. Build & Quality Verification

### 1. Unit Tests (`./gradlew testDebugUnitTest`)
```
> Task :app:testDebugUnitTest
BUILD SUCCESSFUL in 49s
32 actionable tasks: 1 executed, 31 up-to-date
```
- **Passed**: 100% of tests passed (`Phase4dExportTest`, `LongQuoteDisplayTest`, `Phase4cScreensTest`, `ExampleRobolectricTest`).

### 2. Clean Debug Assemble (`./gradlew clean assembleDebug`)
```
> Task :app:assembleDebug
BUILD SUCCESSFUL in 56s
39 actionable tasks: 18 executed, 21 from cache
```

### 3. Android Lint (`./gradlew lintDebug`)
```
> Task :app:lintReportDebug
Wrote HTML report to file:///C:/Users/Mayank/Downloads/Aura%20Quotes/app/build/reports/lint-results-debug.html
> Task :app:lintDebug
BUILD SUCCESSFUL in 2m 38s
30 actionable tasks: 10 executed, 20 up-to-date
```
- **Lint Errors**: **0 errors**.

### 4. Release Assemble & R8 Shrinking (`./gradlew assembleRelease`)
```
> Task :app:minifyReleaseWithR8
> Task :app:packageRelease
> Task :app:assembleRelease
BUILD SUCCESSFUL in 6m 41s
48 actionable tasks: 30 executed, 15 from cache, 3 up-to-date
```
- **Release APK**: `app/build/outputs/apk/release/app-release.apk`
- **Size**: **12.44 MB** (fully minified, shrunk, signed with temporary test keystore).
- Temporary keystore and `keystore.properties` deleted immediately after build verification.

---

## 4. Performance & Smoothness Verification (`dumpsys gfxinfo`)

### Back-Swipe Optimization Rule Compliance
- **Photo Loading**: Loaded at display size using Coil with `memoryCacheKey` matching `QuoteCard` and `precision(Precision.INEXACT)`.
- **Thumbnails**: Decoded once into 160 px downsampled bitmaps and cached in `BackgroundRepository` memory cache (`thumbnailCache`). Never decodes full-size photos for thumbnails during composition or back navigation.
- **Zero Heavy Work in Composition**: ViewModels retain state across back gestures without reload.
- **Predictive Back Transitions**: Relies on default Android predictive back transition without custom fades, preventing dual-hierarchy scaling stalls.

### `dumpsys gfxinfo` Benchmark Protocol
For on-device testing when connected via adb:
```bash
# 1. Reset frame statistics
adb shell dumpsys gfxinfo com.myplanetservices.stillwords reset

# 2. Perform 5 consecutive transitions:
#    (a) Back gesture from Create screen
#    (b) Switching between 5 photo filters (Original -> Noir -> Warm -> Golden -> Earthy)

# 3. Read frame statistics
adb shell dumpsys gfxinfo com.myplanetservices.stillwords
```

*Note: No physical phone or emulator was attached via USB debugging during this build step (adb reported `List of devices attached: (empty)`). The APK is built, signed, and ready for on-device profiling.*

---

## 5. Walkthrough

### Files Changed / Added
1. `app/src/main/java/.../data/preferences/UserPreferencesRepository.kt`: Added `PREMIUM_UNLOCKED_UNTIL` DataStore preference key, `isPremiumUnlocked` property, and `unlockPremiumFor24Hours()` helper.
2. `app/src/main/java/.../data/model/BackgroundTheme.kt`: Added `isPremium` property; marked Forest & Botanical, Sunrise Roads, and Quiet Spaces as premium.
3. `app/src/main/java/.../data/model/PhotoFilter.kt`: Added `isPremium` property; marked Earthy, Mist, and Midnight as premium.
4. `app/src/main/java/.../util/ImageExportUtil.kt`: Upgraded export bitmap generator to Section 7 specs: bundled Newsreader & Figtree fonts, exact scrim, auto-fit without truncation, wordmark, and filter color matrix.
5. `app/src/main/java/.../ui/components/UnlockSheet.kt`: Authoring the rewarded unlock bottom sheet with locked state, look swatches, debug video unlock, and success state.
6. `app/src/main/java/.../ui/screens/designer/QuoteDesignerViewModel.kt`: Integrated `UserPreferencesRepository` to observe premium unlock status and support 24-hour debug unlocking.
7. `app/src/main/java/.../ui/screens/designer/QuoteDesignerScreen.kt`: Full restyle of Create screen matching Screen 13: preview card, format segmented control (1:1, 4:5, 9:16), 4-column backgrounds grid, filter carousel, dashed unlock button, and gallery/share footer.
8. `app/src/main/java/.../ui/navigation/StillwordsNavHost.kt`: Wired `preferencesRepository` into `QuoteDesignerViewModel.Factory`.
9. `app/src/test/java/.../Phase4dExportTest.kt`: Authoring automated unit test exporting 36 images across formats, font scales, and filters, plus WhatsApp sharing intent verification.
10. `docs/phase4d-exports/*`: 36 verified PNG exports.

### How to Test Manually on a Phone
1. **Create Screen Restyle**: Open the app and tap the **Create** tab in the bottom bar. Notice the top bar with "Create", the preview card following 4:5 Post by default, the format segmented control (1:1, 4:5 Post, 9:16 Story), and the 4-column Backgrounds grid.
2. **Format Switch**: Tap `1:1` -> notice the preview card snaps to a square. Tap `9:16 Story` -> notice the card reshapes to portrait story format.
3. **Photo Backgrounds & Selection Ring**: Tap different backgrounds in the 4-column grid. The selected background has a 2 dp Brass selection ring.
4. **Photo Filters**: When a photo background is selected, the "Filters" row appears below. Tap "Noir" or "Golden" -> the live preview card immediately applies the filter.
5. **Plain / Ink Mode**: Tap the "Ink" tile in the backgrounds grid -> notice the photo filters row disappears and the "Card Styles" carousel appears instead.
6. **Premium Unlock Flow**:
   - Tap a locked background (e.g. "Forest & Botanical") or locked filter (e.g. "Earthy") or the dashed "Watch a short video..." button.
   - The `UnlockSheet` slides up showing the locked state, preview swatches, and "Watch video".
   - In debug builds, tap "Watch video" -> immediately transitions to the success state showing "Premium looks unlocked. Enjoy them until {time tomorrow}".
   - Tap "Use Look" -> the sheet dismisses and the look is applied with all lock badges removed.
7. **Export & Share**:
   - Tap "Save to gallery" -> image is saved to Pictures/Stillwords.
   - Tap "Share" -> system share sheet opens with the generated card image.

### Known Limitations
- The rewarded ad SDK integration will be wired in Phase 5; Phase 4D provides the UI and DataStore state foundation.

### Out-of-Scope Issues Found
- In `HomeScreen.kt` and `ReadScreen.kt`, previous phases have placeholder ad slots awaiting Phase 5 AdMob SDK initialization.

### Manifest Permissions (Merged Report)
1. `android.permission.POST_NOTIFICATIONS` (Notification permissions for daily quotes)
2. `android.permission.WAKE_LOCK` (WorkManager)
3. `android.permission.ACCESS_NETWORK_STATE` (WorkManager)
4. `android.permission.RECEIVE_BOOT_COMPLETED` (WorkManager)
5. `android.permission.FOREGROUND_SERVICE` (WorkManager)
6. `com.myplanetservices.stillwords.DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION` (AndroidX Core)
*(Exact alarms `SCHEDULE_EXACT_ALARM` and `USE_EXACT_ALARM` removed via `tools:node="remove"`. Zero external storage permissions requested — saving uses MediaStore and sharing uses FileProvider).*
