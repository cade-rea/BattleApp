# BattleApp
Implementation of Battle as an app.

## Project layout

- `core/`: the game rules (battles, fighters, actions, queues) as plain Kotlin with no Android code, so a server can share them. The battle has no clock; its owner calls `Battle.tick()` once per tick.
    - `core/color/`: the color system. `RgbColor` is a technique's or fighter's stat block (channels are power per type, their sum is total power), `TypeWheel` scores matchups (red beats green, green beats blue, blue beats red, mixes are cross-types), `Affinity` is the bonus for techniques near the fighter's aura hue, and `ColorTraits` turns a color into speed, strength, cost and healing (red fast but weaker, blue strong but slow, green efficient). Tuning constants live next to each rule.
    - Ticks: the action at the front of a fighter's queue winds up for its color's ticks (red 1, green 2, blue 3) before it lands, and the opponent sees the countdown. The queue holds 3 actions. Queuing spends energy, which comes back each tick (`Fighter`), so tapping faster doesn't buy more actions; canceling refunds half. A `Guard` blocks part of every attack that shares its color for 2 ticks, and guards land before attacks on the same tick, so a guard as fast as the attack it answers is in time.
    - `core/ai/`: `ComputerOpponent` reads the player's wind-ups and guards with the matching color when it can land in time, otherwise heals when low or attacks. It follows the same queue and energy rules as a player.
- `app/`: the Android client. `BattleViewModel` owns the battle and runs the tick loop; the activities and views only draw the state it publishes.

## Building

Needs JDK 17 or newer and the Android SDK.

```
./gradlew :core:test                # rules tests, no Android SDK needed
./gradlew :app:testDebugUnitTest    # app unit tests
./gradlew :app:assembleDebug        # debug APK in app/build/outputs/apk/debug
```

Library and plugin versions live in `gradle/libs.versions.toml`. GitHub Actions runs the build and tests on every pull request (`.github/workflows/build.yml`).
