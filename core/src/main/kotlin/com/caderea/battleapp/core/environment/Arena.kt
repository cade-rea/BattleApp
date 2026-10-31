package com.caderea.battleapp.core.environment

import com.caderea.battleapp.core.color.Channel

/**
 * A named place to fight: an [Environment] the player can pick before choosing a loadout.
 *
 * @param id stable across versions, for saving and passing between screens
 */
class Arena(
    val id: String,
    val name: String,
    val description: String,
    loadoutSize: Int = MIN_LOADOUT,
    levelCap: Int? = null,
    lights: List<Light> = listOf(Light.PLAIN),
    ticksPerLight: Int = 0,
) : Environment(loadoutSize, levelCap, lights, ticksPerLight) {

    override fun toString() = "Arena($id)"
}

/**
 * Every arena, in the order the player sees them. Each one leans on one idea: tinted light
 * for picking a color, dim light for long fights, a turning light for timing, a veil that
 * holds one color back, and a level cap for even fights. Numbers are starting values to
 * playtest.
 */
object Arenas {

    /** Plain light and the smallest loadout: the fight as it was before arenas. */
    val OPEN = Arena(
        id = "open",
        name = "Open Field",
        description = "Plain light.",
    )

    val RED_GLOW = glow(Channel.RED, "red_glow", "Red Glow")
    val GREEN_GLOW = glow(Channel.GREEN, "green_glow", "Green Glow")
    val BLUE_GLOW = glow(Channel.BLUE, "blue_glow", "Blue Glow")

    /** Everything hits softer, so fights run long and a bigger loadout pays off. */
    val DIM = Arena(
        id = "dim",
        name = "Dim Field",
        description = "Every technique works at ${percent(Light.DIM_LEVEL)}. Long fights.",
        loadoutSize = 6,
        lights = listOf(Light.dim()),
    )

    /** The light turns red, green, blue in turn, so timing a big hit is a play. */
    val TURNING = Arena(
        id = "turning",
        name = "Turning Light",
        description = "The light turns red, green, then blue every $TURN_TICKS ticks, +${percent(TURNING_BONUS)} to its color.",
        loadoutSize = 6,
        lights = Channel.entries.map { Light.tinted(it, TURNING_BONUS) },
        ticksPerLight = TURN_TICKS,
    )

    /** Red comes through at half strength, so red fighters have to lean on other colors. */
    val RED_VEIL = Arena(
        id = "red_veil",
        name = "Red Veil",
        description = "Red works at ${percent(Light.VEIL_LEVEL)}.",
        loadoutSize = 5,
        lights = listOf(Light.veiled(Channel.RED)),
    )

    /** Everyone fights at level 1, so the fight is decided by the loadout and the reads. */
    val EVEN = Arena(
        id = "even",
        name = "Even Ground",
        description = "Everyone fights at level $EVEN_CAP.",
        levelCap = EVEN_CAP,
    )

    val ALL: List<Arena> = listOf(OPEN, RED_GLOW, GREEN_GLOW, BLUE_GLOW, DIM, TURNING, RED_VEIL, EVEN)

    val DEFAULT: Arena = OPEN

    /** The arena with [id], or the default if there is none, as after an arena is retired. */
    fun byId(id: String?): Arena = ALL.firstOrNull { it.id == id } ?: DEFAULT

    // a turning light changes every few ticks, so each phase is a bigger swing than a glow
    const val TURN_TICKS = 4
    const val TURNING_BONUS = 0.2
    const val EVEN_CAP = 1

    private fun glow(channel: Channel, id: String, name: String): Arena {
        val color = channel.name.lowercase()
        return Arena(
            id = id,
            name = name,
            description = "${color.replaceFirstChar(Char::uppercase)} light: $color techniques get +${percent(Light.TINT_BONUS)}.",
            loadoutSize = 5,
            lights = listOf(Light.tinted(channel)),
        )
    }

    private fun percent(share: Double) = "${Math.round(share * 100)}%"
}
