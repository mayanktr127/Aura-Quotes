# Stillwords — Phase 4 Design Spec

Source of truth for the UI redesign. Pair it with:
- `design-assets/mockups/stillwords-mockups.pdf` — every screen as a picture, one per page (page list in section 9)
- `design-assets/mockups/source/*.dc.html` — the design files the PDF was made from. Open them as text for exact sizes, colours and spacing. They use a custom template format (`{{...}}`, `<sc-for>`, `<sc-if>`); read the CSS, ignore the template syntax.

Units: the designs are drawn on a 390 × 844 phone. Treat every CSS `px` value as `dp` (text sizes as `sp`).
Gradients in the mockups are stand-ins for real photos. In the app, every photo area uses a real image from `assets/backgrounds/<theme>/` for the quote's category.

---

## 1. Ground rules

1. **UI only.** Do not change the Room schema, quote data, `quotes.json`, repositories, WorkManager logic or navigation destinations, except where this spec says so. Keep every existing feature.
2. **Small new UI features are allowed** when they only read data the app already has (dateline, greeting, week dots, mood chips, moments chips, search filter chips). If one needs new stored data, the only allowed addition is small DataStore keys (e.g. days the app was opened). Anything bigger: stop and ask.
3. **Do not invent content.** No new quotes, authors or numbers. Placeholder text in the mockups (e.g. `[1.0.0]`, ad placeholders) comes from real data or BuildConfig.
4. **Edge-to-edge** (required at targetSdk 36): call `enableEdgeToEdge()`, pad content with `WindowInsets.safeDrawing` / `systemBars`. Nothing scrolls under the status bar.
5. **Status bar icons**: light icons on dark theme and on full-photo screens; dark icons on Paper (light theme). This fixes the invisible-icons bug.
6. Touch targets ≥ 48 dp. Every icon-only button has a `contentDescription`. Layouts must work at 360 dp width and font scale 1.3.
7. No quote is ever truncated (no `maxLines` + ellipsis on quote text). Use auto-shrink, as in the Phase 3 follow-up.

---

## 2. Colour tokens

| Token | Dark (default) | Light |
|---|---|---|
| background | Ink `#0F0E0C` | Paper `#F7F3EC` |
| surface (cards, groups, inputs) | Charcoal `#1A1816` | Linen `#EFE9DF` |
| outline / hairline | `#2E2A26` | `#DCD3C4` |
| onBackground (main text) | Ivory `#F3EDE3` | Ink text `#1C1A17` |
| onSurfaceVariant (secondary text) | Stone `#A69F94` | Taupe `#6B645A` |
| primary / accent | Brass `#C8A46A` | Deep brass `#8A6A30` |
| onPrimary (text on accent button) | `#0F0E0C` | `#F7F3EC` |
| accent text on tinted chip | `#E8D3A8` | `#6E5426` |
| muted (chevrons, footnotes) | `#6E675E` | `#9A9184` |
| empty dot border | `#4A443D` | `#C9BFAE` |
| selected-chip fill | Brass at 12% alpha | Brass at 12% alpha |

**On-photo colours (same in both themes):** quote text Ivory `#F3EDE3`; author Ivory at 72–75% alpha; category label `#E8D3A8`; credit "Photo · Pexels" Ivory at 55–60% alpha.

**Scrims over photos** (Ink `#0F0E0C` at the given alpha, top → bottom):
- Today hero card: 35% at 0%, 5% at 35%, 55% at 70%, 92% at 100%.
- Read / Quote detail full screen: 55% at 0%, 10% at 22%, 25% at 55%, 85–95% at 100%.
- Small cards and tiles: 10% → 78–80% at the bottom.

Accent is used sparingly: active tab, category labels, the one primary button per screen, selected chips, week dots.

Theme setting: System / Dark / Light, **default Dark**.

---

## 3. Typography

Fonts (both SIL Open Font License, bundle in `res/font/` so the app stays offline; include the OFL licence files, and add both fonts to the in-app licences/credits if one exists):
- **Newsreader**: Regular 400, Italic 400, Medium 500. Used for quotes, screen titles, section titles, author names.
- **Figtree**: Regular 400, Medium 500, SemiBold 600. Used for all UI text.

| Style | Font | Size / line height | Notes |
|---|---|---|---|
| quoteLarge (Read, Quote detail) | Newsreader 400 | 32–34 / 1.22 | auto-shrink, balanced wrap |
| quoteMedium (Today card) | Newsreader 400 | 27 / 1.28 | auto-shrink |
| quoteSmall (cards, lists) | Newsreader 400 | 16–17 / 1.3 | |
| screenTitle | Newsreader 400 | 30 (24–26 on pushed screens) | letter-spacing −0.3 |
| onboardingTitle | Newsreader 400 | 32–40 / 1.1–1.15 | |
| sectionTitle | Newsreader 400 | 18–20 | |
| author | Newsreader Italic | 15–16 | "— Name" |
| body | Figtree 400 | 14–15 / 1.5 | |
| button | Figtree 600 (primary), 500 (secondary) | 15–16 | |
| chip | Figtree 500 | 13–14 | |
| label (overline) | Figtree 500 | 11, ALL CAPS, letter-spacing 2 | accent colour |
| tab label | Figtree 500 (600 when active) | 11 | |
| caption | Figtree 400 | 12 | |

---

## 4. Shape, spacing, icons

- Corner radius: hero/photo cards 28, cards and tiles 18, list groups 18, small cards 16, buttons 16, inputs 14, segmented control 14 (inner 10), chips fully round, bottom sheet top corners 28.
- Spacing on an 8 dp grid. Screen side padding 20 dp (24 on onboarding and sheets). Gaps between sections 18–22 dp.
- Borders are 1 dp hairlines. No drop shadows, no gradients except photo scrims.
- Icons: thin outline style, 22 dp, ~1.6 stroke, round caps. Use the closest Material Symbols **Outlined** at light weight, or convert the SVG paths in the source files to `ImageVector`s. Never filled icons except the filled heart for "saved".

---

## 5. Components

| Component | Spec |
|---|---|
| **PrimaryButton** | 52–56 dp tall, radius 16, accent fill, onPrimary text Figtree 600 |
| **SecondaryButton** | same size, transparent, 1 dp outline, main text colour |
| **TextButton** | 48 dp tall, secondary text colour, no border ("Skip", "Not now") |
| **Chip** | 40–42 dp tall, horizontal padding 16, fully round. Off: outline border, main text. On: accent border, accent 12% fill, accent-text colour |
| **SegmentedControl** | surface fill, outline border, radius 14, 4 dp inner padding. Selected segment: outline-colour fill + main text; others transparent + secondary text. Used for Saved tabs, Create formats, Theme |
| **SettingsGroup / Row** | surface card, radius 18, rows ≥ 54 dp, hairline between rows, value text right-aligned in secondary colour, chevron in muted colour |
| **Switch** | 48 × 28, accent track when on, knob 22 dp (Ink on Brass when on; secondary colour on outline track when off) |
| **PhotoQuoteCard** | photo + scrim + category label (top-left) + "Photo · Pexels" (top-right) + quote + author + action row |
| **QuoteTile** (Saved grid) | photo or Ink card, category label + quote, bottom-aligned, staggered 2-column grid, gap 10 |
| **TopicTile** (Explore) | 120 dp tall photo tile, topic name Newsreader 20 bottom-left |
| **QuickTile** (Explore) | 60 dp tall surface card, 34 dp icon box (accent 12% fill, accent icon) + label |
| **WeekDots** | 7 dots, 10 dp. Filled accent = opened that day; empty = 1 dp border in empty-dot colour. Day initials under each (M T W T F S S), week starts Monday |
| **SectionHeader** | sectionTitle left, optional accent link right ("See all", "All 22") |
| **EmptyState** | illustration made of outline shapes + accent icon, title Newsreader 24, body 14 secondary, one primary or secondary button |
| **BottomNav** | 4 tabs: Today, Explore, Create, Saved. Top hairline, background colour, icon + label, active = accent. Sits above the gesture inset |
| **BottomSheet** | surface colour, top radius 28, 40 × 4 drag handle, 24 dp padding, scrim behind at ~62% black |
| **AdNativeCard** | see screen 17 |
| **AdBannerSlot** | see screen 18 |

---

## 6. Screens

### 1 · Welcome (onboarding step 1)
Full-screen night-sky photo, bottom fade to background. Logo mark (two brass quote marks) + "Stillwords". Title "Words for the quiet moments." Body line. PrimaryButton "Begin" → step 2. TextButton "Skip for now" → Today. Progress bar: 3 segments, current one is 22 dp wide in accent.

### 2 · Onboarding step 2 — Topics
Back arrow, step indicator (2 of 3), "Skip" top-right → Today. Label "STEP 2 OF 3", title "What would you like to hear more of?", body "Pick three or more…". Wrapping chips for **the 22 real categories only** (no "Resilience"). Footer text "Choose N more" / "N topics chosen". "Continue" is disabled (outline fill, muted text) until 3 are chosen. Saves to the existing preferred-topics setting.

### 3 · Onboarding step 3 — Reminder
Back arrow, step indicator 3 of 3. Title "When should today's quote find you?". Large time preview card (Newsreader 52). 2 × 2 grid of options: Early morning 6:30 AM, Morning 7:30 AM (default), Lunch break 1:00 PM, Before sleep 10:00 PM. PrimaryButton "Turn on reminder" (asks for notification permission on Android 13+, then schedules via the existing WorkManager reminder) and TextButton "Not now". Both go to Today.

### 4 · Today (Home)
- Header: overline dateline "No. {day of year} · {Weekday}, {d} {Month}" in accent; title greeting "Good morning / Good afternoon / Good evening" by local time. Right: Search icon → Search, Settings icon → Settings.
- PhotoQuoteCard, 440 dp tall, radius 28: label "QUOTE OF THE DAY · {CATEGORY}", credit "Photo · Pexels", quote, author, actions: Save (heart), Share, Download, Edit (→ Create with this quote).
- Week card: "{n} quiet days" (Newsreader 18) + "Your week of stillness" + WeekDots.
- "For this moment" + mood chips: Calm, Focused, Hopeful, Need a lift. Tapping opens the Read feed filtered to mapped categories. **Proposed mapping (confirm in plan):** Calm → Peace, Mindfulness, Healing · Focused → Discipline, Study, Work · Hopeful → Growth, Life, Happiness · Need a lift → Motivation, Courage, Self-Love.
- Tapping the card opens Read starting at today's quote.

### 5 · Read (full-screen feed)
Vertical pager, one quote per page, full-bleed photo + scrim. Top: close (X) left, overline "{CATEGORY} · {i} OF {n}" centred. Centre: brass quote mark, quoteLarge, author. Bottom: 4 round 52 dp action buttons (Save, Share, Download, Edit), "Swipe up for the next quote" hint (show only for the first few uses), "Photo · Pexels" credit. Saved state: accent border + filled accent heart.

### 6 · Quote detail
Full-bleed photo. Top: back, category label centred, more (⋯). Bottom block: quoteLarge (32), author left + "Photo · Pexels" right. Action row of 5 round 48 dp buttons with labels: Save/Saved, Collect (add to collection), Copy, Share, Restyle (accent-filled, opens Create with this quote). Section "More on {topic}" + "See all", horizontal row of 120 × 150 mini cards.

### 7 · Explore
Title "Explore". Search field (tapping opens Search). 2 × 2 QuickTiles: Favourites, Collections, My quotes, Recently read. "Topics" header with "All {count}" link + 2-column TopicTiles. "Moments" chips: Morning, Before work, Study time, Before sleep (opens Read with mapped categories; **propose a mapping in the plan**).

### 8 · Search
Back + focused search field (accent border) with clear button. Filter chips: All, Quotes, Topics, Authors. Overline result count ("24 QUOTES · 2 TOPICS"). Matching topics as small surface cards with a photo thumb. Results list: 56 × 72 photo thumb + quote (Newsreader 17) with the search term highlighted (accent 22% background) + "{Category} · {Author}". Hairline between rows. Tap → Quote detail.

### 9 · Search — no results
Same header. Dashed circle with search icon, title "No quotes for "{term}"", body "Try a shorter word, a feeling, or one of the topics below.", overline "TRY INSTEAD" + 5 real category chips, SecondaryButton "Browse all topics" → Explore.

### 10 · Saved
Title "Saved" + round "+" button (new collection). SegmentedControl: "Quotes · {n}", "Collections · {n}", "History". Quotes tab: staggered 2-column grid of QuoteTiles. Collections tab: list of collections using the Collection detail cover style (reuse patterns). History: recently read list (reuse the Search result row).

### 11 · Saved — empty
Same header and tabs. Stacked-cards illustration with dashed card + accent heart, "Nothing saved yet", "When a quote stays with you, tap the heart. It will wait for you here.", PrimaryButton "Start reading" → Read.

### 12 · Collection detail
260 dp photo header (first quote's photo) fading to background, back + rename + more icons, overline "COLLECTION · {n} QUOTES", title Newsreader 32. Buttons: PrimaryButton "Read all" (play icon) → Read with this collection, SecondaryButton "Add quotes". List rows: 48 × 60 thumb, quote, category, drag handle (reorder). Bottom nav visible, Saved active.

### 13 · Create
Title "Create" + Download + Share icons. Preview card 264 × 330 (shape follows chosen format). Format SegmentedControl: 1:1, 4:5 Post, 9:16 Story. "Backgrounds" header + 4-column grid of 72 dp tiles (selected = 2 dp accent ring). Locked looks show a small lock badge. Dashed accent button "Watch a short video to unlock premium looks for 24 hours" → Unlock sheet. Footer: SecondaryButton "Save to gallery" + PrimaryButton "Share". Keep the existing Text / Font controls, restyled with the same chips and segmented controls.

### 14 · Unlock sheet (rewarded, UI only in Phase 4)
BottomSheet over Create. Overline "PREMIUM LOOK · {name}", title "Unlock every premium look for 24 hours", body, row of 64 × 80 look previews + "+ more" tile, PrimaryButton "Watch video" (play icon), caption "Ad · about 30 seconds", TextButton "Not now", footnote "If no video is available, try again in a little while. Your design is kept." Success state: accent check circle, "Premium looks unlocked", "Enjoy them until {time tomorrow}", PrimaryButton "Use {look}". In Phase 4 the button just shows the success state behind a debug flag; Phase 5 connects the rewarded ad.

### 15 · Settings (pushed screen, no bottom nav, back arrow)
Groups with overline labels:
- DAILY QUOTE: Morning reminder (switch + subtitle "A gentle note with today's quote"), Reminder time (value), Topics I like (value "Peace, Courage +3").
- LOOK: Theme SegmentedControl System / Dark / Light; Quote text size slider with small and large "Aa".
- ABOUT: Rate Stillwords, Share with a friend, Photo credits (value "84 Pexels photos"), Privacy policy, Ad privacy choices (opens the UMP privacy options form in Phase 5; hide until then).
- Footer: "Stillwords · version {versionName}" Newsreader italic 13, muted.

### 16 · Light theme
Today, Explore, Saved, Settings are drawn in light (the "· Light" pages in the PDF). Every other screen follows the same token swap from section 2. Full-photo screens (Welcome, Read, Quote detail, Create preview, Collection header) look the same in both themes. Text on photo cards stays Ivory in light mode.

### 17 · Native ad card (Phase 5 wiring, Phase 4 layout)
A page in the Read feed after every 8 quotes. Plain background (no photo), overline "SPONSORED" at the top. Card: surface, radius 28, outline border. Row: 40 dp app icon, advertiser name + rating/price, **"Ad" badge** (accent fill, 11 sp), AdChoices icon. Media view (16:9 to 1:1). Headline (17 SemiBold), body (13, max 2 lines), call-to-action as an outlined accent button. Hint "Swipe up to keep reading". Build as a composable with fake data in a preview only; do not show it in the app until Phase 5.

### 18 · Banner slot (Phase 5)
On Explore and Saved, an anchored adaptive banner sits between the content and the bottom nav: top hairline, slightly darker strip (`#151311` dark), small "Ad" badge, height = the adaptive banner height (~60 dp). Reserve the space only when ads are enabled and consent allows, so it never shows as an empty gap. Phase 4: build the slot composable, hidden.

---

## 7. Shared / exported image (ImageExportUtil)

The saved/shared image must match the new card:
- Sizes: 1:1 = 1080 × 1080, 4:5 = 1080 × 1350, 9:16 = 1080 × 1920.
- Chosen photo (centre-crop) + scrim 20% → 65% Ink top to bottom.
- Quote in Newsreader 400, Ivory, auto-sized to fit with ~9% side margins, balanced lines, never truncated.
- Author in Newsreader Italic, Ivory 75%.
- Small "STILLWORDS" wordmark centred near the bottom, Figtree, letter-spacing ~2.5, Ivory 55%.
- Fonts must be the bundled ones, not system fonts.

---

## 8. Navigation map

Welcome → Onboarding 2 → Onboarding 3 → Today. Tabs: Today, Explore, Create, Saved. Today → Search, Settings (pushed). Today card → Read. Read / Quote detail → Create (Edit/Restyle). Explore search field → Search. Search result → Quote detail. Saved tile → Read or Quote detail. Saved collection → Collection detail. Create unlock button → Unlock sheet.

---

## 9. Mockup files

`design-assets/mockups/stillwords-mockups.pdf` holds every screen, one per page. Pages follow the order below; each screen also has its title, so match by title if the order differs. Do not convert, split or rename the PDF.

| Screen title in the PDF | Screen | Source file in `source/` |
|---|---|---|
| Foundations · Ink & Brass | Colours, type, components | Foundations.dc.html |
| Welcome | Onboarding 1 | Welcome.dc.html |
| Today | Today (Home) | Main.dc.html |
| Read (full-screen feed) | Read feed | Read.dc.html |
| Explore | Explore | Explore.dc.html |
| Create | Create | Designer.dc.html |
| Saved | Saved | Saved.dc.html |
| Settings | Settings | Settings.dc.html |
| Search | Search | Search.dc.html |
| Quote detail | Quote detail | Quote.dc.html |
| Collection detail | Collection detail | Collection.dc.html |
| Onboarding 2 · Topics | Onboarding 2 | Onboarding2.dc.html |
| Onboarding 3 · Reminder | Onboarding 3 | Onboarding3.dc.html |
| Empty · Saved | Saved, empty | EmptySaved.dc.html |
| Empty · Search | Search, no results | EmptySearch.dc.html |
| Ad · Native card in feed | Native ad page | AdNative.dc.html |
| Ad · Banner slot (Explore) | Explore with banner slot | AdBanner.dc.html |
| Ad · Rewarded unlock (Create) | Unlock sheet | UnlockSheet.dc.html |
| Today · Light | Today, light theme | MainLight.dc.html |
| Explore · Light | Explore, light theme | ExploreLight.dc.html |
| Saved · Light | Saved, light theme | SavedLight.dc.html |
| Settings · Light | Settings, light theme | SettingsLight.dc.html |

---

## 10. Motion and scrolling

Goal: calm and quick. Motion explains what changed; nothing bounces, loops or shows off. Durations 150–300 ms, standard Material easing (FastOutSlowIn / emphasized). No animation runs longer than 400 ms.

**Respect the system "Remove animations" setting**: with it on, everything snaps instantly. Test with Developer options → animation scales at 0x and 1x.

| Where | What |
|---|---|
| Screen to screen (push) | Fade + slight rise/slide (about 8% of width), 250 ms. Back reverses it |
| Switching tabs | Crossfade only, 200 ms. No sliding between tabs |
| Today, first open of the day | Hero card fades in and rises 12 dp, 300 ms, once |
| Photos loading | Coil crossfade 300 ms. Placeholder = surface colour, never a spinner or shimmer |
| Read feed | VerticalPager with default snap fling. Quote text fades in 200 ms once a page settles. Photo stays still (no parallax) |
| Save (heart) | Scale 1 → 1.2 → 1 (spring, no visible bounce) + fill colour 200 ms + light haptic |
| Chips, switches, segmented control | Colour change 150 ms; segmented indicator slides to the new segment, 200 ms |
| Week dots | Today's dot fills once (scale 0 → 1, 250 ms) the first time the app opens that day |
| Bottom sheets | Standard Material 3 slide-up with scrim fade |
| Onboarding | Step indicator width animates 200 ms; Continue button colour animates when it becomes enabled |
| Lists and grids | Items animate on add, remove and reorder (`Modifier.animateItem()`); no staggered entrance on every scroll |
| Unlock success | Check circle scales in 250 ms |
| Search | Results crossfade 150 ms as the query changes; highlight has no animation |

**Smooth scrolling (no jank):**
- Every `LazyColumn`, `LazyVerticalStaggeredGrid`, `LazyRow` and pager uses stable `key`s and `contentType`.
- Images are loaded at the size they are shown (Coil `size`), with memory and disk cache on. No full-resolution decoding into small tiles.
- No heavy work in composition: sort, filter and format in the ViewModel. Pass stable/immutable UI state; avoid lambdas and objects that change identity on every frame.
- Scrim gradients and brushes are created once (`remember`), not per frame.
- Add a **Baseline Profile** (and startup profile) covering app start, Today, Read feed scrolling and Saved grid scrolling.
- Judge smoothness only on a **release** build on a real phone, never a debug build.
