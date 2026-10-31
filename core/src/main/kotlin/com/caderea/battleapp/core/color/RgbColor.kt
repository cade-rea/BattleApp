package com.caderea.battleapp.core.color

import kotlinx.serialization.Serializable
import kotlin.math.abs
import kotlin.math.min

/**
 * A color as a stat block: each channel (0-255) is power in that type, the sum of the
 * channels is total power, and the mix of the channels is the type.
 */
@Serializable
data class RgbColor(val red: Int, val green: Int, val blue: Int) {

    init {
        require(red in CHANNEL_RANGE && green in CHANNEL_RANGE && blue in CHANNEL_RANGE) {
            "channels must be 0-255: $red, $green, $blue"
        }
    }

    /** Total power: the sum of the channels. */
    val power: Int
        get() = red + green + blue

    operator fun get(channel: Channel): Int = when (channel) {
        Channel.RED -> red
        Channel.GREEN -> green
        Channel.BLUE -> blue
    }

    /**
     * How much of the color is [channel], from 0 to 1. The three shares add up to 1,
     * except for black, which has no type and is 0 everywhere.
     */
    fun share(channel: Channel): Double =
        if (power == 0) 0.0 else this[channel].toDouble() / power

    /**
     * The hue in degrees on the color wheel (red 0, green 120, blue 240), or null for black,
     * white and greys, which have no hue.
     */
    val hue: Double?
        get() {
            val max = maxOf(red, green, blue)
            val min = minOf(red, green, blue)
            if (max == min) return null

            val range = (max - min).toDouble()
            val hue = when (max) {
                red -> 60 * ((green - blue) / range)
                green -> 60 * ((blue - red) / range + 2)
                else -> 60 * ((red - green) / range + 4)
            }
            return (hue + 360) % 360
        }

    /**
     * How much two colors share their mix, from 0 (no channel in common) to 1 (the same
     * mix): the sum over channels of the smaller share. Brightness doesn't matter.
     */
    fun overlap(other: RgbColor): Double =
        Channel.entries.sumOf { minOf(share(it), other.share(it)) }

    /**
     * This color with its channels scaled down so their sum fits in [budget]. The mix stays
     * the same; a color already within the budget is returned as it is.
     */
    fun fitToBudget(budget: Int): RgbColor {
        require(budget >= 0) { "budget can't be negative: $budget" }
        if (power <= budget) return this

        val scale = budget.toDouble() / power
        // rounding down keeps the sum within the budget
        return RgbColor((red * scale).toInt(), (green * scale).toInt(), (blue * scale).toInt())
    }

    /** The color as an opaque ARGB int, the format Android draws with. */
    val argb: Int
        get() = (0xFF shl 24) or (red shl 16) or (green shl 8) or blue

    val hex: String
        get() = "#%02X%02X%02X".format(red, green, blue)

    override fun toString() = hex

    companion object {
        private val CHANNEL_RANGE = 0..255

        /**
         * The most total power a technique can have at level 1. It grows with the fighter's
         * level (see Levels), and arenas with a level cap lower it again.
         */
        const val STARTING_POWER_BUDGET = 255

        val BLACK = RgbColor(0, 0, 0)
        val RED = RgbColor(255, 0, 0)
        val GREEN = RgbColor(0, 255, 0)
        val BLUE = RgbColor(0, 0, 255)

        /** Parses "#RRGGBB" or "RRGGBB". */
        fun fromHex(hex: String): RgbColor {
            val digits = hex.removePrefix("#")
            require(digits.length == 6) { "expected #RRGGBB: $hex" }
            val value = digits.toInt(16)
            return RgbColor((value shr 16) and 0xFF, (value shr 8) and 0xFF, value and 0xFF)
        }

        /**
         * The angle between two hues in degrees, from 0 (the same hue) to 180 (opposite).
         */
        fun hueDistance(a: Double, b: Double): Double {
            val difference = abs(a - b) % 360
            return min(difference, 360 - difference)
        }
    }
}
