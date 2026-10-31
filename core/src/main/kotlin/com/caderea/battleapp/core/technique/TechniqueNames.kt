package com.caderea.battleapp.core.technique

import com.caderea.battleapp.core.color.ColorTraits
import com.caderea.battleapp.core.color.RgbColor

/**
 * Plain names for rolled techniques: a word for the hue, then one for what the technique
 * does. Attacks are named by speed, so the name hints at a lucky roll: a "Blue Jab" is a
 * fast blue.
 */
object TechniqueNames {

    // one word per 30 degrees of hue, starting at red
    private val HUES = listOf(
        "Red", "Orange", "Yellow", "Lime", "Green", "Jade",
        "Cyan", "Azure", "Blue", "Violet", "Magenta", "Rose",
    )

    fun hueName(color: RgbColor): String {
        val hue = color.hue ?: return "Grey"
        return HUES[((hue + 15) / 30).toInt() % HUES.size]
    }

    fun name(kind: TechniqueKind, color: RgbColor, rolls: TechniqueRolls): String {
        val action = when (kind) {
            TechniqueKind.HEAL -> "Mend"
            TechniqueKind.GUARD -> "Guard"
            TechniqueKind.ATTACK -> when (ColorTraits.of(color).windUpTicks(rolls.windUp)) {
                1 -> "Jab"
                2 -> "Strike"
                else -> "Blast"
            }
        }
        return "${hueName(color)} $action"
    }
}
