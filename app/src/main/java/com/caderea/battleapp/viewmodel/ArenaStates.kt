package com.caderea.battleapp.viewmodel

import com.caderea.battleapp.core.environment.Arena
import com.caderea.battleapp.core.environment.Light

/** One arena as the arena list shows it. */
data class ArenaCardState(
    val id: String,
    val name: String,
    val description: String,
    val loadoutSize: Int,
    // null when fighters keep their own level
    val levelCap: Int? = null,
    // more than one when the light turns, in order
    val lights: List<Light> = listOf(Light.PLAIN),
    val ticksPerLight: Int = 0,
)

/** The arena during a battle: its name and the light right now. */
data class ArenaHudState(
    val name: String = "",
    // the light for the next tick to resolve
    val light: Light = Light.PLAIN,
    // for a turning light: ticks left in this light, counting the next one, and the light after
    val ticksLeftInLight: Int? = null,
    val nextLight: Light? = null,
    val levelCap: Int? = null,
)

fun Arena.toCard() = ArenaCardState(
    id = id,
    name = name,
    description = description,
    loadoutSize = loadoutSize,
    levelCap = levelCap,
    lights = lights,
    ticksPerLight = ticksPerLight,
)

/** The arena while tick number [nextTick] is the next one to resolve. */
fun Arena.toHud(nextTick: Int) = ArenaHudState(
    name = name,
    light = lightAt(nextTick),
    ticksLeftInLight = ticksLeftInLight(nextTick),
    nextLight = nextLight(nextTick),
    levelCap = levelCap,
)
