# Changelog

All notable changes to StandBy are documented here.

## Unreleased

### Added

- Android CI for unit tests, lint, and debug assembly.
- Unit coverage for activation, widget-list invariants, and theme fallback behavior.
- English and Turkish UI resources.
- Accessibility labels and larger interaction targets for ambient customization controls.
- Dependabot configuration and a pull request verification checklist.
- A selectable secondary world-clock city with timezone-safe presets.
- A permission-free next-alarm widget backed by Android’s system alarm schedule.
- MIT licensing for reuse and redistribution.
- An opt-in current-weather widget powered by Open-Meteo and the selected world-clock city.
- A compact live alarm countdown card on the lower-left of the second clock page.

### Changed

- Application namespace and ID are now `com.emretenoz.standby`.
- Release builds enable code and resource shrinking.
- The stack editor and customization rows adapt better to compact landscape displays.
- Night mode copy now reflects the selected night theme instead of promising a fixed red palette.

### Fixed

- Missing OEM screen-saver settings no longer crash the app.
- Recoverable DataStore read failures fall back to safe defaults.

### Removed

- Notification-listener and media-control access from the GitHub build to avoid sensitive-access blocking during sideloading.
