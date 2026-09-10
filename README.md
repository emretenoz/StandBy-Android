# StandBy for Android

A polished, native Android ambient display inspired by iPhone StandBy. While the app is open, it watches charging and orientation state. When automatic mode is enabled and the device is both charging and in landscape, it enters an immersive, AMOLED-black StandBy interface. An Android `DreamService` provides the same experience after display timeout on supported devices.

The four horizontally swipeable pages show:

- Two side-by-side, vertically swipeable widget stacks
- A customizable full-screen clock
- An editorial calendar view
- A visual battery and charging view

Five clock faces are included: Digital, Digital Split, Analog, Solar / Orbit, and World Clock. Long-press the full-screen clock to select a face, curated clock color, seconds, 12/24-hour time, and date visibility.

Settings include automatic activation, screen timeout behavior, keeping the screen awake, clock appearance, manual Night Mode, and OLED burn-in protection. Settings are persisted with DataStore Preferences.

The widget page follows the dual-stack model: swipe either half vertically to change its widget, or long-press a stack to open an editor directly over StandBy. Clock, date, battery, charging, and world-clock widgets can be independently added, removed, and reordered. At least one widget is retained in each stack.

Night Mode keeps the OLED background fully black and changes ambient content to a dim deep red. Burn-in protection is enabled by default and moves the static content through a subtle three-pixel pattern once per minute without continuously animating it.

## Activation and Android limitations

StandBy activates when all three conditions are true:

1. The app is in the foreground.
2. Android reports that the battery is charging (AC, USB, or wireless).
3. The orientation sensor or Android configuration reports landscape orientation. This also works when system auto-rotate is disabled, provided the device has an orientation sensor.

An ordinary Android app cannot reliably launch an activity from the background whenever power is connected. Modern Android background-activity launch restrictions intentionally prevent this, so this MVP does not attempt unreliable workarounds. Open the app before docking or charging; it will react immediately to charging and rotation changes while it is running.

For activation after the display times out or locks, the app also provides an Android `DreamService`. In the app, tap **Open Android screen saver settings**, select **StandBy**, and configure the system screen saver to start **while charging**. Android then owns activation and can safely show StandBy after screen timeout without background-activity hacks. Menu wording and availability vary by device manufacturer; some OEMs disable or replace Android screen savers.

The system-state and UI layers are separated so future versions can add an opt-in foreground service with a persistent notification, a notification action, OEM-specific integration, or another user-granted capability without rewriting the StandBy UI or activation logic.

Immersive mode hides system bars while StandBy is active. Android may temporarily reveal them after an edge swipe, and device policy or OEM behavior can limit fullscreen behavior.

While the activity is already running and StandBy is active, it requests permission to remain visible above the keyguard using Android's supported `setShowWhenLocked` API. The keep-awake setting prevents normal screen timeout. Deliberately turning the display off with the physical power button is still respected; the app does not use wake-lock or background-launch workarounds to override an explicit user lock action.

## Project structure

```text
app/src/main/java/com/example/standby/
├── MainActivity.kt                  Activity and window behavior
├── StandByDreamService.kt           System screen saver using shared StandBy UI
├── data/settings/                  DataStore model and repository
├── system/                         Charging/orientation observers and state repository
└── ui/
    ├── SettingsScreen.kt            Categorized Material 3 settings
    ├── StandByViewModel.kt          Combined observable UI state
    ├── standby/                     Ambient pager, clock faces, widgets, editors
    └── theme/                       Application color theme
```

`ChargingObserver` reads the sticky `ACTION_BATTERY_CHANGED` broadcast, checks `BatteryManager.isCharging` as an OEM fallback, and continues listening for updates. `OrientationObserver` combines physical orientation-sensor readings with Android configuration callbacks. `SystemStateRepository` exposes both as `StateFlow`; `StandByViewModel` combines them with persisted settings into a single UI state.

## Build and run

Requirements: Android Studio with JDK 17 and Android SDK 35 or newer.

1. Open this directory in Android Studio.
2. Allow Gradle sync to finish.
3. Run the `app` configuration on a physical device or emulator running Android 9 (API 28) or later.
4. Open StandBy, connect power, and rotate the device to landscape.
5. For locked/timeout activation, use the in-app button to select StandBy as Android's charging screen saver.

From a terminal:

```shell
./gradlew assembleDebug
```

On Windows, use `gradlew.bat assembleDebug`.
