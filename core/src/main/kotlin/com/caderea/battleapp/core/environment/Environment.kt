package com.caderea.battleapp.core.environment

import com.caderea.battleapp.core.color.RgbColor

/**
 * Where a battle takes place. Besides its light, an arena sets how many techniques each
 * fighter brings and can cap levels so a fight is even.
 *
 * @param loadoutSize techniques each fighter brings, from [MIN_LOADOUT] to [MAX_LOADOUT]
 * @param levelCap if set, every fighter fights as if no higher than this level: their
 * techniques are dimmed to that level's power budget
 */
open class Environment(
    val loadoutSize: Int = MIN_LOADOUT,
    val levelCap: Int? = null,
) {

    init {
        require(loadoutSize in MIN_LOADOUT..MAX_LOADOUT) { "loadout size must be $MIN_LOADOUT-$MAX_LOADOUT: $loadoutSize" }
        require(levelCap == null || levelCap >= 1) { "level cap must be at least 1: $levelCap" }
    }

    /**
     * The multiplier the arena's light gives a technique of [color]. Neutral for now;
     * tinted light will boost the matching channels here.
     */
    open fun lightMultiplier(color: RgbColor): Double = 1.0

    companion object {
        const val MIN_LOADOUT = 4
        const val MAX_LOADOUT = 6
    }
}
