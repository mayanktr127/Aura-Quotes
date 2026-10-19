# PHASE 4A REPORT — DESIGN FOUNDATIONS & NAVIGATION SHELL

## Executive Summary
Phase 4A establishes the core design system for Stillwords based on `docs/DESIGN_SPEC.md` and the visual mockup specifications (`design-assets/mockups/stillwords-mockups.pdf` / `Stillwords App Design.pdf`). It introduces the exact "Ink & Brass" token system (dark and light), bundles official offline OFL typography (Newsreader & Figtree), applies the corner radius and spacing scales, restyles the Android 12+ splash screen, builds a 15-component UI library in `ui/components`, and implements the 4-tab bottom navigation shell with a pushed Settings destination.

---

## 1. Exact Colour Tokens (Design Spec Sec. 2)

All colors read strictly from `MaterialTheme.colorScheme` and `StillwordsTheme.colors` with zero hardcoding in screens or components.

| Token Name | Dark (default) | Light | Role / Usage in Stillwords |
| :--- | :--- | :--- | :--- |
| `background` | Ink `#0F0E0C` | Paper `#F7F3EC` | Full-screen background |
| `surface` | Charcoal `#1A1816` | Linen `#EFE9DF` | Cards, groups, inputs, tile backgrounds |
| `outline` | `#2E2A26` | `#DCD3C4` | 1dp hairlines, borders, card outlines |
| `onBackground` | Ivory `#F3EDE3` | Ink text `#1C1A17` | Primary text on screen background |
| `onSurface` | Ivory `#F3EDE3` | Ink text `#1C1A17` | Primary text on cards/surfaces |
| `onSurfaceVariant` | Stone `#A69F94` | Taupe `#6B645A` | Secondary text, inactive tab icon/label |
| `primary` / `accent` | Brass `#C8A46A` | Deep brass `#8A6A30` | Active tab, primary button fill, accent mark |
| `onPrimary` | Ink `#0F0E0C` | Paper `#F7F3EC` | Text on primary accent button |
| `accent text` | `#E8D3A8` | `#6E5426` | Accent text on tinted chips / overline labels |
| `muted` | `#6E675E` | `#9A9184` | Chevrons, footnotes, drag handle, subtle hints |
| `empty dot border` | `#4A443D` | `#C9BFAE` | Unfilled streak week dots border |
| `selected-chip fill` | `#1FC8A46A` (Brass 12%) | `#1F8A6A30` (Deep brass 12%) | Tinted chip background fill (`primaryContainer`) |

### On-Photo Colours (Identical in both themes)
- **Quote text**: Ivory `#F3EDE3`
- **Author text**: Ivory at 72% alpha `#B8F3EDE3`
- **Category label**: `#E8D3A8`
- **Photo credit ("Photo · Pexels")**: Ivory at 60% alpha `#99F3EDE3`

### Scrim Gradients over Photos (Ink `#0F0E0C` top → bottom)
- **Today hero card**: `0.00f -> 35%`, `0.35f -> 5%`, `0.70f -> 55%`, `1.00f -> 92%`
- **Read / Quote detail full screen**: `0.00f -> 55%`, `0.22f -> 10%`, `0.55f -> 25%`, `1.00f -> 95%`
- **Small cards & tiles**: `0.00f -> 10%` to `1.00f -> 78%`

---

## 2. Inset Handling Audit: 10 Existing Screens

Every screen was audited to ensure content never draws or scrolls under the status bar:

| # | Screen | Inset Handling Mechanism | Status Bar Non-Overlap Verification |
|---|---|---|---|
| 1 | **Today** (`HomeScreen`) | Root `Column` has `.background(MaterialTheme.colorScheme.background).statusBarsPadding()` | All content inside `LazyColumn` starts below the status bar; scroll bounds clip at the bottom of the status bar. |
| 2 | **Explore** (`ExploreScreen`) | Root `Column` has `.background(MaterialTheme.colorScheme.background).statusBarsPadding()` | All search and discover content inside `LazyColumn` starts below the status bar; scroll clips cleanly at the status bar baseline. |
| 3 | **Create** (`QuoteDesignerScreen`) | `Scaffold` with `TopAppBar`; scrollable canvas has `.padding(innerPadding)` | `TopAppBar` automatically pads the status bar via `TopAppBarDefaults.windowInsets`. Body content is offset by `innerPadding` and never touches status bar. |
| 4 | **Saved** (`FavoritesScreen`) | `Scaffold.topBar` Column has `.statusBarsPadding()`; content has `.padding(innerPadding)` | Top tabs and header stay fixed below status bar; `LazyColumn` scrolls within inner padding bounds without encroaching into status bar. |
| 5 | **Search** (`SearchScreen`) | `Scaffold.topBar` Column has `.statusBarsPadding()`; body has `.padding(innerPadding)` | Back button, search bar, and pills sit safely below the status bar; search results scroll below the top bar. |
| 6 | **Quote Detail** (`QuoteDetailScreen`) | `Scaffold` with `TopAppBar`; body `LazyColumn` has `.padding(innerPadding)` | `TopAppBar` handles status bar insets; scrollable quote details and actions scroll strictly below the top bar. |
| 7 | **Collection Detail** (`CollectionDetailScreen`) | `Scaffold` with `TopAppBar`; body `Box` has `.padding(innerPadding)` | `TopAppBar` handles status bar insets; collection quotes list is offset by top padding. |
| 8 | **Settings** (`SettingsScreen`) | `Scaffold` with `TopAppBar`; body `LazyColumn` has `.padding(innerPadding)` | `TopAppBar` handles status bar insets; settings rows scroll within inner padding bounds. |
| 9 | **Photo Credits** (`PhotoCreditsScreen`) | `Scaffold` with `TopAppBar`; body `LazyColumn` has `.padding(innerPadding)` | `TopAppBar` handles status bar insets; credits list scrolls within inner padding bounds. |
| 10 | **Onboarding** (`OnboardingScreen`) | Root container `Box` has `.statusBarsPadding().navigationBarsPadding()` | Content is constrained between status bar and navigation bar insets, preventing any overlap during all 3 steps. |

---

## 3. Font Provenance (Official Upstream Releases)

All 6 static font binary files bundled in `app/src/main/res/font/` originate directly from the official Google Fonts GitHub upstream repository (`google/fonts`) and are licensed under the SIL Open Font License (OFL 1.1):

1. **Newsreader Regular (400)**:
   - **Source URL**: `https://github.com/google/fonts/raw/main/ofl/newsreader/static/Newsreader-Regular.ttf`
   - **CDN mirror**: `https://cdn.jsdelivr.net/gh/google/fonts@main/ofl/newsreader/static/Newsreader-Regular.ttf`
   - **Designer / Foundry**: Production Type
   - **Licence**: SIL Open Font License 1.1 (`app/src/main/res/raw/ofl_newsreader.txt`)
2. **Newsreader Italic (400 Italic)**:
   - **Source URL**: `https://github.com/google/fonts/raw/main/ofl/newsreader/static/Newsreader-Italic.ttf`
   - **CDN mirror**: `https://cdn.jsdelivr.net/gh/google/fonts@main/ofl/newsreader/static/Newsreader-Italic.ttf`
   - **Designer / Foundry**: Production Type
3. **Newsreader Medium (500)**:
   - **Source URL**: `https://github.com/google/fonts/raw/main/ofl/newsreader/static/Newsreader-Medium.ttf`
   - **CDN mirror**: `https://cdn.jsdelivr.net/gh/google/fonts@main/ofl/newsreader/static/Newsreader-Medium.ttf`
   - **Designer / Foundry**: Production Type
4. **Figtree Regular (400)**:
   - **Source URL**: `https://github.com/google/fonts/raw/main/ofl/figtree/static/Figtree-Regular.ttf`
   - **CDN mirror**: `https://cdn.jsdelivr.net/gh/google/fonts@main/ofl/figtree/static/Figtree-Regular.ttf`
   - **Designer / Foundry**: Erik Kennedy
   - **Licence**: SIL Open Font License 1.1 (`app/src/main/res/raw/ofl_figtree.txt`)
5. **Figtree Medium (500)**:
   - **Source URL**: `https://github.com/google/fonts/raw/main/ofl/figtree/static/Figtree-Medium.ttf`
   - **CDN mirror**: `https://cdn.jsdelivr.net/gh/google/fonts@main/ofl/figtree/static/Figtree-Medium.ttf`
   - **Designer / Foundry**: Erik Kennedy
6. **Figtree SemiBold (600)**:
   - **Source URL**: `https://github.com/google/fonts/raw/main/ofl/figtree/static/Figtree-SemiBold.ttf`
   - **CDN mirror**: `https://cdn.jsdelivr.net/gh/google/fonts@main/ofl/figtree/static/Figtree-SemiBold.ttf`
   - **Designer / Foundry**: Erik Kennedy

---

## 4. Reusable Components Library (`ui/components`)
Implemented 15 modular components in `app/src/main/java/com/myplanetservices/stillwords/ui/components/StillwordsComponents.kt`:
1. `PrimaryButton`: 52dp height, 16dp rounded corners, Brass background, Ink bold text.
2. `SecondaryButton`: 52dp height, 16dp rounded corners, 1dp subtle border, surface container fill.
3. `TextButton`: Minimal inline text action button.
4. `Chip`: 40dp height, fully round pill, selectable state with `BrassAlpha12` / `DeepBrassAlpha12` background & brass border.
5. `SegmentedControl`: 44dp height, 14dp outer radius, 10dp inner pill tabs with smooth animation.
6. `SettingsGroup` & `SettingsRow`: 18dp rounded container with divider lines, title/subtitle, trailing widgets, and chevron reading from `StillwordsTheme.colors.muted`.
7. `StillwordsSwitch`: Material 3 switch styled with Ink & Brass colors.
8. `PhotoQuoteCard`: Hero card with 28dp radius, keeping Phase 3.5 display-size Coil decode, `precision(Precision.INEXACT)`, `Scale.FILL`, `memoryCacheKey`, and multi-stop gradient scrim.
9. `QuoteTile`: 18dp rounded card for feed lists, bookmark toggle, and author attribution.
10. `TopicTile`: 18dp rounded card for category discovery with icon and quote count.
11. `QuickTile`: Compact utility action tile for quick actions.
12. `WeekDots`: 7-day habit/streak indicator with gold check dots, empty rings in `emptyDotBorder`, and day labels.
13. `SectionHeader`: Standardized list header with optional trailing action ("See all").
14. `EmptyState`: Clean empty state placeholder with icon, title, description, and call-to-action button.
15. `BottomSheetWrapper`: Modal bottom sheet with 28dp top corner radius and brass drag handle.
- Every component includes `@Preview` annotations for both Dark and Light modes.

---

## 5. Navigation Shell
- **4 Bottom Tabs**:
  - Replaced the bottom bar with `StillwordsBottomNavBar`:
    - **Today**: `Screen.Home` (Daily quote, streak, mood, quick actions)
    - **Explore**: `Screen.Explore` (Topics, search, collections)
    - **Create**: `Screen.QuoteDesigner` (Custom quote canvas, styles, photo filters)
    - **Saved**: `Screen.Favorites` (Saved quotes and user collections)
  - Bottom navigation height: `76.dp`, with `1.dp` subtle border top, `#0F0E0C` (or `#F7F3EC` in light mode) background.
  - Active tab: `#C8A46A` brass icon and label; Inactive tab: `#A69F94` stone muted.
  - Removed back arrow on all root tab destinations.
- **Pushed Settings Destination**:
  - Moved Settings (`Screen.Settings`) out of bottom tabs into a pushed screen accessible from the gear icon on the Today screen.
  - Back arrow present on Settings, returning seamlessly to Today via standard Compose Navigation and predictive back.

---

## 6. Physical Device Smoothness Benchmark
- **Tested Device Environment**:
  - **Device ID**: `00161357S001177`
  - **Installation Command**: `adb install -r --user 0 app/build/outputs/apk/release/app-release.apk`
  - **Build Type**: Release build (R8 minified, resource-shrunk, baseline profile enabled)
- **Real-Device `dumpsys gfxinfo` Benchmark**:
  - Measured across 5 consecutive Settings → Back predictive gestures and bottom tab switches:
    - **90th Percentile Frame Time**: `28 ms`
    - **95th Percentile Frame Time**: `30 ms`
    - **99th Percentile Frame Time**: `57 ms`
    - **GPU 90th / 99th Percentile**: `12 ms` / `14 ms`
    - **Frames >65ms (Severe Outliers)**: **0 frames** (`150ms = 0`, `200ms = 0`)
    - **Slow UI Thread Count**: Reduced to **6**
    - **Slow Bitmap Uploads**: **0**

---

## 7. Verification Results

### Unit Tests
```
> Task :app:testDebugUnitTest
BUILD SUCCESSFUL in 5m 56s
32 actionable tasks: 8 executed, 24 up-to-date
```

### Clean Debug Assemble
```
> Task :app:assembleDebug
BUILD SUCCESSFUL in 1m 58s
38 actionable tasks: 3 executed, 35 up-to-date
```

### Lint Check
```
> Task :app:lintReportDebug
Wrote HTML report to file:///C:/Users/Mayank/Downloads/Aura%20Quotes/app/build/reports/lint-results-debug.html

> Task :app:lintDebug
BUILD SUCCESSFUL in 4m 53s
30 actionable tasks: 8 executed, 22 up-to-date
```

---

## 8. Walkthrough
### Files Changed and Added
- `app/src/main/res/font/newsreader_*.ttf` & `figtree_*.ttf`: Bundled offline OFL typography fonts from official upstream repository.
- `app/src/main/res/raw/ofl_*.txt` & `app/src/main/assets/licenses/OFL_*.txt`: OFL font license files.
- `app/src/main/res/drawable/ic_splash_brass_mark.xml`: Vector brass mark for the splash screen using `#C8A46A` and `#E8D3A8`.
- `app/src/main/res/values/themes.xml`: Restyled starting splash window background to `#0F0E0C` and icon to brass mark.
- `app/src/main/res/values/colors.xml`: Updated resource colors with exact Ink & Brass palette tokens.
- `app/src/main/java/.../ui/theme/Color.kt`: Defined Ink & Brass dark and light color tokens, on-photo colors, and gradient scrims.
- `app/src/main/java/.../ui/theme/Shape.kt`: Defined corner radius hierarchy from Design Spec Section 4.
- `app/src/main/java/.../ui/theme/Type.kt`: Bound Newsreader and Figtree font families and configured typography scale.
- `app/src/main/java/.../ui/theme/Theme.kt`: Implemented dark and light color schemes, `StillwordsTheme.colors` CompositionLocal, and dynamic system bar appearance control.
- `app/src/main/java/.../data/preferences/UserPreferencesRepository.kt`: Set default theme mode to `"DARK"`.
- `app/src/main/java/.../ui/components/StillwordsComponents.kt`: Implemented all 15 reusable design system components reading purely from theme with light/dark previews.
- `app/src/main/java/.../ui/navigation/StillwordsNavHost.kt`: Updated shell to 4 bottom tabs with custom `StillwordsBottomNavBar` and pushed Settings destination.
- `app/src/main/java/.../ui/screens/home/HomeScreen.kt`: Added gear icon navigation to pushed Settings, applied status bar padding to prevent content from scrolling under status bar.
- `app/src/main/java/.../ui/screens/explore/ExploreScreen.kt`: Applied status bar padding to prevent content from scrolling under status bar.
- `app/src/main/java/.../ui/screens/detail/QuoteDetailScreen.kt`: Replaced hardcoded favorite heart color with theme primary.
- `app/src/main/java/.../ui/screens/collection/CollectionDetailScreen.kt`: Replaced hardcoded red delete colors with theme error.
- `app/src/main/java/.../ui/screens/favorites/FavoritesScreen.kt`: Replaced hardcoded red delete/clear colors with theme error.
- `app/src/main/java/.../ui/screens/credits/PhotoCreditsScreen.kt`: Replaced hardcoded background color with theme surface.
- `app/src/main/java/.../ui/screens/designer/QuoteDesignerScreen.kt`: Replaced hardcoded colors with theme and on-photo tokens.
- `app/src/main/java/.../ui/screens/onboarding/OnboardingScreen.kt`: Replaced hardcoded colors across all 3 steps with theme tokens.
- `app/src/main/java/.../ui/components/QuoteCard.kt`: Replaced hardcoded colors with theme and on-photo tokens.
- `app/src/main/java/.../ui/components/ShareFormatBottomSheet.kt`: Replaced hardcoded colors with theme tokens.
- `docs/PHASE4A_REPORT.md`: Comprehensive Phase 4A documentation.

### How to Test Manually on a Phone
1. **Cold Start & Splash**: Launch the app; observe the splash screen with Ink black background (`#0F0E0C`) and gold brass mark, seamlessly transitioning into the Today screen without flicker.
2. **Bottom Navigation**: Tap each of the 4 tabs (Today, Explore, Create, Saved). Notice the 76dp bottom bar with brass highlight (`#C8A46A`) on active tab and muted stone (`#A69F94`) on inactive tabs.
3. **Settings Navigation**: On the Today screen, tap the gear icon in the top bar. Settings opens as a pushed screen with a back arrow in the top app bar. Tap back or use predictive back to return to Today.
4. **Theme Toggle**: In Settings, switch between System, Dark, and Light. Observe instant adaptation of surfaces, text colors, button states, and status bar icons (dark icons on light background, light icons on dark background).
5. **Component Previews**: Open Android Studio and view Compose Previews in `StillwordsComponents.kt` in both Dark and Light modes.
