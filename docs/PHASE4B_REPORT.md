# PHASE 4B REPORT — TODAY, READ FEED & QUOTE DETAIL SCREENS

## Executive Summary
Phase 4B of the Stillwords UI redesign completes the migration of the primary browsing and reading surfaces:
1. **Today (Screen 4)**: Exact dateline header matching `docs/DESIGN_SPEC.md` screen 4 (`"No. {day of year} · {Weekday}, {d} {Month}"`, e.g. `"No. 293 · Tuesday, 20 October"`) in overline style and accent colour (`Brass`), time-of-day greeting (`"Good morning"` 5:00–11:59, `"Good afternoon"` 12:00–16:59, `"Good evening"` 17:00–4:59), search and settings ghost action buttons, hero `PhotoQuoteCard` with click-to-Read interaction and ghost action buttons (Save, Share, Download, Edit), `WeekCard` with title `"{n} quiet days"` and subtitle `"Your week of stillness"` with `WeekDots`, and `"For this moment"` mood chips using the confirmed expanded category mapping (Calm, Focused, Hopeful, Need a lift) navigating into the curated Read feed. The legacy style carousel and extra sections were cleanly removed from the UI while preserving all underlying data models.
2. **Read Feed (Screen 5)**: Full-screen vertical pager (`VerticalPager`) with snap fling behavior, exact multi-stop photo scrim (`FullScreenPhotoScrim`), category and page position header (`{CATEGORY} · i OF n`), decorative brass quotation mark, auto-shrinking quote typography (`Newsreader`, down to 16sp floor, zero ellipsis), four circular 52dp action buttons (Save with brass fill toggle, Share, Download, Edit), first-3-launches swipe hint with DataStore counter, and Pexels photo credit caption. Preloads previous and next page images with Coil using memory and disk caching.
3. **Quote Detail (Screen 6)**: Full-bleed hero photo card with scrim and auto-shrinking quote text, five circular 48dp action buttons (Save, Collect, Copy, Share, Restyle), source and translator caption for classic historical quotes (`QuoteFormatUtil`, e.g., *"Meditations · tr. George Long, 1862"*; omitted for Stillwords originals), restyled private reflection note card ("Take a moment"), and "More on {topic}" horizontal quote row with a "See all" entry point to the Read feed.
4. **Share & Download Integration**: Restyled `ShareFormatBottomSheet` with Phase 4A tokens (28dp top radius, brass drag handle, primary and secondary buttons) wired across all action buttons on all three screens.
5. **No Ellipsis & Auto-Shrink Verification**: Verified that quote text scales dynamically across font sizes and screen constraints without clipping or ellipsis, passing targeted tests for the catalog's 3 longest quotes at 360dp width and 1.3 font scale.
6. **Preservation of Phase 3.5 Fixes**: Splash screen startup guard, predictive-back smoothness (`Precision.INEXACT`, `Scale.FILL`, stable keys), dynamic light/dark status bar insets, and MediaStore image exports all remain intact.

---

## 1. Screen Architecture & Spec Implementation

### 1.1 Today Screen (`HomeScreen.kt`)
- **Dateline & Header**:
  - Displays dateline matching spec: `"No. {day of year} · {Weekday}, {d} {Month}"` (e.g., `"No. 293 · Tuesday, 20 October"`).
  - Styled with `LabelOverlineStyle` (`Figtree Medium`, 11sp, 2sp letter spacing) in `MaterialTheme.colorScheme.primary` (accent brass).
  - Accompanied by exact time-of-day greeting in `ScreenTitleStyle` (`Newsreader`, 30sp):
    - **5:00–11:59**: `"Good morning"`
    - **12:00–16:59**: `"Good afternoon"`
    - **17:00–4:59**: `"Good evening"`
  - Ghost action buttons for Search and Settings in top header.
- **Hero Card (`PhotoQuoteCard`)**:
  - Exact 440dp height with 28dp corner radius (`PhotoCardShape`).
  - Darkened multi-stop scrim gradient ensuring WCAG AA contrast.
  - Category label uppercase with letter spacing, auto-shrinking quote text (`Newsreader` serif), and italic author attribution.
  - Ghost action buttons (Save toggle, Share, Download, Edit in Designer).
  - Tapping the card opens the full-screen Read feed anchored to today's quote.
- **Week Card (`WeekCard` / `WeekDots`)**:
  - Exact Title: `"{n} quiet days"` (e.g. `"5 quiet days"`, `Newsreader 18`).
  - Exact Subtitle: `"Your week of stillness"` (`Figtree 12`, muted secondary colour).
  - Backed by lightweight DataStore persistence (`UserPreferencesRepository.openedDates`) recording distinct ISO dates on launch without Room database churn.
  - Renders 7 dots representing Monday through Sunday with brass indicators for active days and neutral border indicators for inactive days.
- **Mood Chips ("For this moment")**:
  - Confirmed and implemented with the approved **Expanded Mapping**:
    - **Calm** $\rightarrow$ `Peace`, `Mindfulness`, `Healing`, `Gratitude`
    - **Focused** $\rightarrow$ `Discipline`, `Study`, `Work`, `Wisdom`
    - **Hopeful** $\rightarrow$ `Growth`, `Life`, `Happiness`, `Creativity`
    - **Need a lift** $\rightarrow$ `Motivation`, `Courage`, `Self-Love`, `Confidence`
  - Tapping any mood chip routes directly into the Read feed filtered by those specific categories.
- **Removed from UI**:
  - Removed "Choose Style" carousel, "For You", "Popular Today", "What do you need right now", and "Saved" shortcut row.
  - Underlying ViewModel methods and database queries remain completely intact.

### 1.2 Read Screen (`ReadScreen.kt`)
- **Full-Screen Vertical Pager**:
  - Compose `VerticalPager` configured with `rememberPagerState` and snap fling behavior.
  - Seamless edge-to-edge rendering with `WindowInsets` padding for status bar and navigation bars.
  - Preloads adjacent pages (current $-1$ and $+1$) into Coil image cache using `Precision.INEXACT` and `Scale.FILL` for zero-jitter snapping.
- **Header**:
  - Circular 44dp translucent close button ($X$) dismissing back to the previous screen.
  - Uppercase category and index indicator: `"{CATEGORY} · {i} OF {n}"`.
- **Typographic Layout**:
  - Custom canvas-drawn brass quotation mark (`BrassQuoteMark`, 34x26dp).
  - Auto-shrinking quote text starting at 34sp with scale compensation down to a 16sp floor.
  - Smooth 200ms alpha fade-in of quote text once the page settles.
- **Action Buttons & Swipe Hint**:
  - Four 52dp round action buttons: Save (toggles active Brass background and icon), Share, Download, Edit in Designer.
  - Swipe hint ("Swipe up for next quote" with subtle arrow icon) shown only for the first 3 launches, persisted via DataStore `readSwipeHintCount`.
  - Photo credit caption ("Photo · Pexels") at the bottom.

### 1.3 Quote Detail Screen (`QuoteDetailScreen.kt`)
- **Full-Bleed Header & Layout**:
  - Full-width hero photo container with multi-stop scrim.
  - Auto-shrinking quote text and italic author.
  - Classic quote attribution via `QuoteFormatUtil`:
    - Reads `source` and `translator` fields from seed data (e.g., *"Meditations · tr. George Long, 1862"*).
    - Formatted as `LabelSmall` under the author.
    - Omitted for Stillwords originals.
- **Action Bar**:
  - Five 48dp round action buttons: Save (toggle), Collect (opens Add to Collection sheet), Copy (copies formatted quote text to clipboard), Share (opens restyled `ShareFormatBottomSheet`), and Restyle (opens Quote Designer).
- **Reflection Note ("Take a moment")**:
  - Restyled private reflection note card utilizing surface card tokens, 18dp radius, and subtle border.
  - Supports private journaling with timestamp recording.
- **"More on {topic}" Carousel**:
  - Horizontal row of 120x150dp mini quote cards with category photos and snippet text.
  - "See all" button that launches the Read feed filtered by that quote's category.

---

## 2. Long Quote Check (Auto-Shrink Verification)

Requirement 5 stipulates: *"Quote text uses auto-shrink everywhere, never ellipsis. Verify the 3 longest quotes at 360 dp width and font scale 1.3 on all three screens."*

### The 3 Longest Quotes in Catalog
1. **`q_classic_86` (149 characters)**:
   > *"Consider thyself to be dead, and to have completed thy life up to the present time; and live according to nature the remainder which is allowed thee."* — Marcus Aurelius
2. **`q_classic_139` (123 characters)**:
   > *"There are two classes of poets — the poets by education and practice, these we respect; and poets by nature, these we love."* — Ralph Waldo Emerson
3. **`q_classic_83` (121 characters)**:
   > *"The point is, not how long you live, but how nobly you live. And often this living nobly means that you cannot live long."* — Seneca the Younger

### Automated Test Verification
Authored `app/src/test/java/com/myplanetservices/stillwords/LongQuoteDisplayTest.kt` with Robolectric 4.17 and Compose UI Testing at `Density(density = 2.5f, fontScale = 1.3f)` and constraint width `360.dp`:
- `verifyThreeLongestQuotesOnTodayCard_at360dp_fontScale1_3`: **PASSED**
- `verifyThreeLongestQuotesOnReadScreen_at360dp_fontScale1_3`: **PASSED**
- `verifyThreeLongestQuotesOnQuoteDetail_at360dp_fontScale1_3`: **PASSED**

Result: All 3 quotes fit cleanly inside their layouts without ellipsis, clipping, or text truncation.

---

## 3. Smoothness & Performance Measurement

> [!NOTE]
> **Hardware Status**: Per user instructions, **no phone was connected for this step**. The `adb` / `dumpsys gfxinfo` measurements were skipped.
>
> All performance optimizations from Phase 3.5 ensuring sub-5% jank were strictly maintained:
> - Coil request tuning: `precision(Precision.INEXACT)`, `scale(Scale.FILL)`, `crossfade(false)`, explicit memory and disk cache keys.
> - Selective preloading: `ReadScreen` only pre-fetches page $i-1$ and $i+1$, preventing memory spikes during vertical paging.
> - Stable keys across all Lazy components (`LazyRow`, `LazyColumn`, `VerticalPager`).
> - Baseline profiles in `app/src/main/baseline-prof.txt` covering startup, navigation, and quote rendering.

---

## 4. Verification Results & Build Logs

### 4.1 Unit Tests (`./gradlew testDebugUnitTest`)
```text
> Task :app:testDebugUnitTest

BUILD SUCCESSFUL in 2m 15s
32 actionable tasks: 10 executed, 2 from cache, 20 up-to-date
Configuration cache entry reused.
```

### 4.2 Assemble Debug (`./gradlew assembleDebug`)
```text
> Task :app:assembleDebug
> Task :app:createDebugApkListingFileRedirect UP-TO-DATE

BUILD SUCCESSFUL in 48s
38 actionable tasks: 3 executed, 35 up-to-date
Configuration cache entry reused.
```

### 4.3 Lint Debug (`./gradlew lintDebug`)
```text
> Task :app:lintReportDebug UP-TO-DATE
> Task :app:lintDebug

BUILD SUCCESSFUL in 3m 59s
30 actionable tasks: 7 executed, 23 up-to-date
Configuration cache entry reused.
```
*Zero errors, zero fatal issues.*

---

## 5. Walkthrough

### 5.1 Files Changed and Added
| File | Status | Description |
|---|---|---|
| `app/src/main/java/com/myplanetservices/stillwords/data/model/Quote.kt` | Modified | Added optional `translator: String? = null` property to in-memory `Quote` data class without modifying Room entity schema. |
| `app/src/main/java/com/myplanetservices/stillwords/data/local/SeedQuotes.kt` | Modified | Added `getTranslatorMap()` helper to parse classic translator credits from assets. |
| `app/src/main/java/com/myplanetservices/stillwords/data/repository/QuoteRepository.kt` | Modified | Maps translator metadata into `Quote` domain objects upon loading. |
| `app/src/main/java/com/myplanetservices/stillwords/data/preferences/UserPreferencesRepository.kt` | Modified | Added DataStore preference keys for `openedDates` (active week tracking) and `readSwipeHintCount` (first 3 launches). |
| `app/src/main/java/com/myplanetservices/stillwords/util/QuoteFormatUtil.kt` | Added | Formats classic quote source and translator captions while safely returning `null` for Stillwords originals. |
| `app/src/main/java/com/myplanetservices/stillwords/ui/components/StillwordsComponents.kt` | Modified | Implemented `AutoShrinkQuoteText` auto-shrink algorithm without ellipsis, updated `PhotoQuoteCard` with 440dp height and ghost buttons, updated `WeekDots` default title to `"{n} quiet days"` and subtitle `"Your week of stillness"`. |
| `app/src/main/java/com/myplanetservices/stillwords/ui/components/ShareFormatBottomSheet.kt` | Modified | Restyled share bottom sheet with Phase 4A tokens (28dp top radius, brass drag handle, standard buttons). |
| `app/src/main/java/com/myplanetservices/stillwords/ui/navigation/Screen.kt` | Modified | Added `Screen.Read` route accepting optional `initialQuoteId` and `categories` parameters. |
| `app/src/main/java/com/myplanetservices/stillwords/ui/navigation/StillwordsNavHost.kt` | Modified | Wired navigation routes between `HomeScreen`, `ReadScreen`, `QuoteDetailScreen`, and `QuoteDesignerScreen`. |
| `app/src/main/java/com/myplanetservices/stillwords/ui/screens/home/HomeViewModel.kt` | Modified | Injected `UserPreferencesRepository` to record daily open events, updated greeting logic to Good morning/afternoon/evening by hour ranges, and exposed week day statuses. |
| `app/src/main/java/com/myplanetservices/stillwords/ui/screens/home/HomeScreen.kt` | Modified | Redesigned Today screen per spec: exact dateline `No. {day of year} · {Weekday}, {d} {Month}` in overline style and accent colour, time greeting, hero card, week card (`{n} quiet days` / `Your week of stillness`), and mood chips with confirmed expanded category mapping. |
| `app/src/main/java/com/myplanetservices/stillwords/ui/screens/read/ReadViewModel.kt` | Added | ViewModel managing vertical pager quote ordering, category filtering, and swipe hint persistence. |
| `app/src/main/java/com/myplanetservices/stillwords/ui/screens/read/ReadScreen.kt` | Added | Full-screen vertical reading feed with snap paging, brass quote mark, round action buttons, and adjacent page caching. |
| `app/src/main/java/com/myplanetservices/stillwords/ui/screens/detail/QuoteDetailScreen.kt` | Modified | Redesigned Quote Detail with full-bleed hero, 5 round action buttons, source/translator caption, and restyled reflection note. |
| `app/src/test/java/com/myplanetservices/stillwords/LongQuoteDisplayTest.kt` | Added | Automated Compose/Robolectric test verifying catalog's 3 longest quotes fit at 360dp width and 1.3 font scale without truncation. |

### 5.2 How to Test Manually on a Phone
1. **Today Screen**:
   - Launch app. Verify the dateline renders `"No. {day of year} · {Weekday}, {d} {Month}"` (e.g. `"No. 293 · Tuesday, 20 October"`) in accent colour with overline typography.
   - Verify the greeting:
     - 5:00–11:59 $\rightarrow$ `"Good morning"`
     - 12:00–16:59 $\rightarrow$ `"Good afternoon"`
     - 17:00–4:59 $\rightarrow$ `"Good evening"`
   - Confirm Search and Settings ghost icons in the top bar navigate to their respective screens.
   - Inspect the Today hero card: verify photo background, category tag, quote text, author, and ghost action buttons (Save, Share, Download, Edit).
   - Tap Save: verify the favorite icon fills with brass color.
   - Tap Share / Download: verify the restyled 3-format bottom sheet opens with Post 4:5, Story 9:16, Square 1:1.
   - Tap Edit: verify Quote Designer opens with the quote loaded.
   - Observe the Week card: verify title reads `"{n} quiet days"` (e.g. `"5 quiet days"`), subtitle reads `"Your week of stillness"`, and active days show glowing brass dots.
   - Tap any mood chip:
     - **Calm** $\rightarrow$ opens Read feed with Peace, Mindfulness, Healing, Gratitude quotes.
     - **Focused** $\rightarrow$ opens Read feed with Discipline, Study, Work, Wisdom quotes.
     - **Hopeful** $\rightarrow$ opens Read feed with Growth, Life, Happiness, Creativity quotes.
     - **Need a lift** $\rightarrow$ opens Read feed with Motivation, Courage, Self-Love, Confidence quotes.
2. **Read Feed**:
   - From the Today card, tap the quote body to open Read feed.
   - Verify full-screen edge-to-edge layout with `{CATEGORY} · i OF n` header and close ($X$) button.
   - Verify brass quote mark and fade-in of text when settled.
   - Swipe vertically up and down: verify smooth snap transitions without stutter.
   - Verify swipe hint appears at the bottom on the first 3 launches, then ceases.
   - Tap Close ($X$): verify smooth return to Today.
3. **Quote Detail**:
   - Open a classic quote in Detail: verify source caption under author (e.g., *"Meditations · tr. George Long, 1862"*).
   - Open a Stillwords original quote: verify no extra source caption appears.
   - Tap the 5 round action buttons:
     - Save: toggles favorite state.
     - Collect: opens Add to Collection sheet.
     - Copy: copies quote to clipboard with toast notification.
     - Share: opens share format sheet.
     - Restyle: opens Quote Designer.
   - Scroll down: test "Take a moment" reflection card, typing and saving a reflection note.
   - Check "More on {topic}" cards at the bottom: tap a card to navigate, or tap "See all" to view that category in the Read feed.
4. **Light / Dark Mode**:
   - Switch system theme or in-app theme between Dark and Light.
   - Verify light theme colors match `MainLight.dc.html` with dark status bar icons and proper contrast.

### 5.3 Permissions in Manifest
As required by the project rules, here is the full list of permissions in `AndroidManifest.xml`:
- **`android.permission.POST_NOTIFICATIONS`**: Used solely for scheduled daily quote notifications (user opt-in).
- **`android.permission.SCHEDULE_EXACT_ALARM`**: Explicitly removed via `tools:node="remove"`.
- **`android.permission.USE_EXACT_ALARM`**: Explicitly removed via `tools:node="remove"`.
- **Zero storage permissions**: Storage uses Android's modern `MediaStore` API; no `READ_EXTERNAL_STORAGE`, `WRITE_EXTERNAL_STORAGE`, or `MANAGE_EXTERNAL_STORAGE` is present.

### 5.4 Known Limitations
- Smoothness metrics (`dumpsys gfxinfo`) were skipped because no physical device or emulator with adb was connected.

### 5.5 Out-of-Scope Issues Found
- In `AndroidManifest.xml`, a build warning notes: *"Warning: SDK processing. This version only understands SDK XML versions up to 3 but an SDK XML file of version 4 was encountered."* This is an AGP/Android SDK tools mismatch that does not affect runtime or build success.
- In `QuoteDesignerScreen.kt`, some legacy styling components from Phase 3 remain in use until Phase 4C addresses the Quote Designer and Collection screens.
