package com.caderea.battleapp.core.color

/**
 * The bonus a fighter gets for using techniques close to their own aura color. Closeness is
 * the angle between hues on the color wheel, so pure and mixed colors are treated alike.
 */
object Affinity {

    /** The bonus when the hues match exactly. It falls to nothing at the opposite hue. */
    const val MAX_BONUS = 0.25

    /**
     * The multiplier for a technique of [technique] color used by a fighter with [aura]:
     * from 1.0 (opposite hues) to 1 + [MAX_BONUS] (the same hue). A color with no hue,
     * such as grey, gets no bonus.
     */
    fun multiplier(technique: RgbColor, aura: RgbColor): Double {
        val techniqueHue = technique.hue ?: return 1.0
        val auraHue = aura.hue ?: return 1.0

        val closeness = 1 - RgbColor.hueDistance(techniqueHue, auraHue) / 180
        return 1 + MAX_BONUS * closeness
    }
}
