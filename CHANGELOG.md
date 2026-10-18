# Changelog

All notable changes to **Stillwords** will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

---

## [1.0.0] - 2026-10-18

### Added
- Curated offline quotes database across mindful categories and reflective moods.
- Elegant Noir and Gold visual card designs with distinctive styling presets.
- Quote Designer tool supporting custom quote typography, aspect ratio formatting, and high-resolution export.
- Personal reflection journal attached to individual quotes.
- Custom quote collections and reading history tracker.
- Daily inspiration reminders powered by WorkManager (periodic background scheduling with reboot resilience).
- Google Play In-App Updates support (flexible flow with quiet update detection and non-blocking notification).
- Edge-to-edge layout design supporting modern Android window insets and predictive back navigation.

### Changed
- Renamed application to **Stillwords** and standardized package namespace to `com.myplanetservices.stillwords`.
- Upgraded target baseline to Android 17 compileSdk 37, targetSdk 36, minSdk 26.
- Hardened release build pipeline with R8 code shrinking and resource optimization.
- Consolidated versioning controls inside `gradle.properties`.
