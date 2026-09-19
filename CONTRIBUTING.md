# Contributing

## Local setup

Use JDK 17 and Android SDK 35 or newer. Open the project in Android Studio or run:

```shell
./gradlew testDebugUnitTest lintDebug assembleDebug assembleRelease
```

On Windows, use `gradlew.bat`.

## Pull requests

- Keep system-state collection separate from Compose presentation code.
- Do not introduce background activity-launch workarounds.
- Add tests for state or persistence behavior.
- Put user-visible copy in both `values/strings.xml` and `values-tr/strings.xml`.
- Check compact landscape layouts and increased system font sizes.
- Never commit signing keys, API keys, `local.properties`, or generated build output.

## Release checklist

1. Update `versionCode`, `versionName`, and `CHANGELOG.md`.
2. Run unit tests, lint, and both debug and release builds.
3. Test foreground activation, preview mode, and DreamService on a physical device.
4. Verify upgrade behavior and DataStore settings migration.
5. Sign the release with credentials stored outside the repository.
