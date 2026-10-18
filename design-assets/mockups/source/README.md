# Stillwords design source files

Exported from the Stillwords design canvas on 19 Oct 2026. Reference only; not app code.

- Each `.dc.html` file is one screen, drawn at 390 x 844. Treat CSS px as dp (text px as sp).
- They use a design-tool template format: `{{name}}` holes, `<sc-for>` loops, `<sc-if>` branches and a small script at the bottom. Read the inline CSS for exact sizes, colours, radii and spacing; ignore the template syntax.
- Gradient backgrounds stand in for real photos from `assets/backgrounds/<theme>/`.
- `docs/DESIGN_SPEC.md` is the written spec and wins if anything here disagrees.

| File | Screen |
|---|---|
| Foundations | Colours, type, components |
| Welcome | Onboarding 1 |
| Onboarding2 / Onboarding3 | Onboarding topics / reminder |
| Main | Today |
| Read | Full-screen feed |
| Quote | Quote detail |
| Explore | Explore |
| Search / EmptySearch | Search / no results |
| Saved / EmptySaved | Saved / empty |
| Collection | Collection detail |
| Designer | Create |
| UnlockSheet | Rewarded unlock sheet |
| Settings | Settings |
| MainLight, ExploreLight, SavedLight, SettingsLight | Light theme |
| AdNative / AdBanner | Ad layouts (Phase 5) |
