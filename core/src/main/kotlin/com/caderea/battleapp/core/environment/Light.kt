package com.caderea.battleapp.core.environment

import com.caderea.battleapp.core.color.Channel
import com.caderea.battleapp.core.color.RgbColor

/**
 * An arena's light: a multiplier for each channel. Every technique's effect is scaled by
 * the light on its channels, weighted by how much of each channel the technique has, so a
 * red light helps pure red fully, magenta half as much, and green not at all.
 *
 * 1.0 everywhere is plain light. Starting values to playtest live in the factories below.
 */
data class Light(
    val red: Double = 1.0,
    val green: Double = 1.0,
    val blue: Double = 1.0,
) {

    init {
        require(red >= 0 && green >= 0 && blue >= 0) { "light can't be negative: $red, $green, $blue" }
    }

    operator fun get(channel: Channel): Double = when (channel) {
        Channel.RED -> red
        Channel.GREEN -> green
        Channel.BLUE -> blue
    }

    /** The multiplier for a technique of [color]. Black has no channels, so it's unaffected. */
    fun multiplier(color: RgbColor): Double =
        if (color.power == 0) 1.0 else Channel.entries.sumOf { color.share(it) * this[it] }

    /** Whether every channel is plain, so the light changes nothing. */
    val isPlain: Boolean
        get() = red == 1.0 && green == 1.0 && blue == 1.0

    /**
     * The channel this light favors most, or null when no channel stands out, as in plain
     * or evenly dimmed light.
     */
    val favored: Channel?
        get() {
            val best = Channel.entries.maxBy { this[it] }
            return best.takeIf { channel -> Channel.entries.all { it == channel || this[it] < this[channel] } }
        }

    /** The channel this light holds back most, or null when none stands out. */
    val muted: Channel?
        get() {
            val worst = Channel.entries.minBy { this[it] }
            return worst.takeIf { channel -> Channel.entries.all { it == channel || this[it] > this[channel] } }
        }

    companion object {
        val PLAIN = Light()

        /** How much tinted light adds to its own channel. A starting value to playtest. */
        const val TINT_BONUS = 0.10
        /** How much of a technique's effect dim light leaves. A starting value to playtest. */
        const val DIM_LEVEL = 0.7
        /** How much of its channel a veil lets through. A starting value to playtest. */
        const val VEIL_LEVEL = 0.5

        /** Light tinted [channel]: that channel of every technique gets [bonus] more. */
        fun tinted(channel: Channel, bonus: Double = TINT_BONUS): Light = only(channel, 1 + bonus)

        /** Every channel scaled down to [level], for longer fights with less burst. */
        fun dim(level: Double = DIM_LEVEL): Light = Light(level, level, level)

        /** One channel let through at only [level], so that color is weaker here. */
        fun veiled(channel: Channel, level: Double = VEIL_LEVEL): Light = only(channel, level)

        private fun only(channel: Channel, value: Double) = Light(
            red = if (channel == Channel.RED) value else 1.0,
            green = if (channel == Channel.GREEN) value else 1.0,
            blue = if (channel == Channel.BLUE) value else 1.0,
        )
    }
}
