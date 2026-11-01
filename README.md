# BattleApp
Implementation of Battle as an app.

## Project layout

- `core/`: the game rules (battles, fighters, actions, queues) as plain Kotlin with no Android code, so a server can share them. The battle has no clock; its owner calls `Battle.tick()` once per tick.
    - `core/color/`: the color system. `RgbColor` is a technique's or fighter's stat block (channels are power per type, their sum is total power), `TypeWheel` scores matchups (red beats green, green beats blue, blue beats red, mixes are cross-types), `Affinity` is the bonus for techniques near the fighter's aura hue, and `ColorTraits` turns a color into speed, strength, cost and healing (red fast but weaker, blue strong but slow, green efficient). Tuning constants live next to each rule.
    - Ticks: the action at the front of a fighter's queue winds up for its color's ticks (red 1, green 2, blue 3) before it lands, and the opponent sees the countdown. The queue holds 3 actions. Queuing spends energy, which comes back each tick (`Fighter`), so tapping faster doesn't buy more actions; canceling refunds half. A `Guard` blocks part of every attack that shares its color for 2 ticks, and guards land before attacks on the same tick, so a guard as fast as the attack it answers is in time.
    - `core/ai/`: `ComputerOpponent` reads the player's wind-ups and guards with the matching color when it can land in time, otherwise heals when low or attacks. It follows the same queue and energy rules as a player.
    - `core/online/`: the messages the app and the PvP server send each other over a WebSocket (`ClientMessage`, `ServerMessage`, as `OnlineJson`).
- `server/`: the PvP server, a Ktor WebSocket server on the same `core` rules. A phone connects to `/play`, sends `join` with its fighter and an arena, and waits for someone else in that arena. The server then runs the match itself: it ticks the battle on its own clock, the phones only send `queue` (a button slot), `cancel` and `leave`, and after every tick and every input both phones get the battle's state from their own side, so each sees the other's wind-ups. Giving up or dropping the connection loses the match; a match that runs out of ticks is a draw. `GET /health` answers `ok`.
    - For now matchmaking is first come, first served per arena, profiles come from the phone (the server dims every technique to the fighter's level), and online matches award no XP. Sign-in, server-side profiles and smarter matchmaking come next.
- `app/`: the Android client. `BattleViewModel` owns the battle and runs the tick loop (with pause and rematch); the screens are Jetpack Compose (`app/.../ui/`) and only draw the state it publishes.
    - Battle screen, top to bottom: the enemy's aura, health and energy; a shared timeline where each queued action is a block ending on the tick it lands (enemy lane on top, yours below, guards as bands), so you can see whether a guard covers an incoming attack; the red › green › blue type triangle with both auras ringed; your panel; and your techniques, each showing its cost, wind-up, damage/heal/block and its type matchup against the enemy (▲ strong, ▼ weak). When an action lands, its lane pulses, a streak in its color flies to whoever it hits, and the health change floats off their orb (`Battle.landed` says what landed).
    - Colors on screen always mean type and power; everything else is greyscale.

## Building

Needs JDK 17 or newer and the Android SDK.

```
./gradlew :core:test                # rules tests, no Android SDK needed
./gradlew :app:testDebugUnitTest    # app unit tests, including the Compose screens under Robolectric;
                                    # screenshots land in app/build/screenshots
./gradlew :app:assembleDebug        # debug APK in app/build/outputs/apk/debug
./gradlew :server:test               # PvP server tests, no Android SDK needed
./gradlew :server:run                # runs the PvP server on PORT (default 8080); TICK_LENGTH_MS sets the tick
```

Library and plugin versions live in `gradle/libs.versions.toml`. GitHub Actions runs the build and tests on every pull request (`.github/workflows/build.yml`).
