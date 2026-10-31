package com.caderea.battleapp.core.environment

import com.caderea.battleapp.core.color.RgbColor

/**
 * Where a battle takes place. Environments will add bonuses and set the loadout size.
 */
open class Environment {

    /**
     * The multiplier the arena's light gives a technique of [color]. Neutral for now;
     * tinted light will boost the matching channels here.
     */
    open fun lightMultiplier(color: RgbColor): Double = 1.0
}
