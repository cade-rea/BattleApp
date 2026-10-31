package com.caderea.battleapp.core.progression

import com.caderea.battleapp.core.color.Channel
import com.caderea.battleapp.core.color.RgbColor
import kotlinx.serialization.Serializable
import kotlin.math.roundToInt

/**
 * A fighter's aura as their running XP in each channel. Its mix is the fighter's color (their
 * defensive type and what their techniques get an affinity bonus for matching), and its total
 * sets their level.
 *
 * Every technique used earns XP that splits two ways: [BASE_SHARE] always goes to the
 * [base] color the fighter started as, and the rest follows the technique's own channels.
 * There's no penalty for using other colors and no floor, so the aura drifts toward what the
 * fighter actually uses while the base share keeps it anchored: a fighter who only ever uses
 * blue drifts toward a third of their base color, never all the way to none.
 */
@Serializable
data class Aura(
    val base: Channel,
    val red: Int = 0,
    val green: Int = 0,
    val blue: Int = 0,
) {

    init {
        require(red >= 0 && green >= 0 && blue >= 0) { "XP can't be negative: $red, $green, $blue" }
    }

    operator fun get(channel: Channel): Int = when (channel) {
        Channel.RED -> red
        Channel.GREEN -> green
        Channel.BLUE -> blue
    }

    val totalXp: Int
        get() = red + green + blue

    val level: Int
        get() = Levels.levelFor(totalXp)

    /** How far through the current level this aura is, from 0 to 1. */
    val levelProgress: Double
        get() = Levels.progress(totalXp)

    val powerBudget: Int
        get() = Levels.powerBudget(level)

    /** How much of the aura's XP is in [channel], from 0 to 1. */
    fun share(channel: Channel): Double =
        if (totalXp == 0) 0.0 else this[channel].toDouble() / totalXp

    /**
     * The aura as a color: the hue is the XP mix, and the brightness grows with level, from
     * [MIN_BRIGHTNESS] of full at level 1 to full at [FULL_BRIGHTNESS_LEVEL]. Only the mix
     * matters to the type wheel and affinity, so brightness is just for show.
     */
    val color: RgbColor
        get() {
            val top = Channel.entries.maxOf { this[it] }
            if (top == 0) return RgbColor.BLACK

            val rise = ((level - 1).toDouble() / (FULL_BRIGHTNESS_LEVEL - 1)).coerceAtMost(1.0)
            val brightness = 255 * (MIN_BRIGHTNESS + (1 - MIN_BRIGHTNESS) * rise)
            fun channel(xp: Int) = (brightness * xp / top).roundToInt().coerceIn(0, 255)
            return RgbColor(channel(red), channel(green), channel(blue))
        }

    /**
     * This aura after earning [xp] from a technique of [technique] color: [BASE_SHARE] to the
     * base color, the rest split by the technique's channel shares. Black has no channels, so
     * all of it goes to the base.
     */
    fun gain(xp: Int, technique: RgbColor): Aura = gain(listOf(xp to technique))

    /**
     * This aura after all of [earnings] (XP, and the color it was earned with) at once.
     * Splitting the total rather than each small amount keeps rounding from skewing the
     * shares, and no XP is lost to rounding.
     */
    fun gain(earnings: List<Pair<Int, RgbColor>>): Aura {
        require(earnings.all { it.first >= 0 }) { "XP can't be negative: $earnings" }
        val total = earnings.sumOf { it.first }
        if (total == 0) return this

        val exact = Channel.entries.associateWith { 0.0 }.toMutableMap()
        for ((xp, color) in earnings) {
            exact[base] = exact.getValue(base) + xp * BASE_SHARE
            val rest = xp * (1 - BASE_SHARE)
            if (color.power == 0) {
                exact[base] = exact.getValue(base) + rest
            } else {
                for (channel in Channel.entries) exact[channel] = exact.getValue(channel) + rest * color.share(channel)
            }
        }

        val earned = roundKeepingTotal(exact, total)
        return copy(
            red = red + earned.getValue(Channel.RED),
            green = green + earned.getValue(Channel.GREEN),
            blue = blue + earned.getValue(Channel.BLUE),
        )
    }

    companion object {
        /** The share of all XP that goes to the base color, whatever was used. */
        const val BASE_SHARE = 0.33
        const val MIN_BRIGHTNESS = 0.6
        const val FULL_BRIGHTNESS_LEVEL = 20

        /** A new fighter's aura: [Levels.STARTING_XP] in [base] and nothing else. */
        fun start(base: Channel): Aura = Aura(base).let { aura ->
            when (base) {
                Channel.RED -> aura.copy(red = Levels.STARTING_XP)
                Channel.GREEN -> aura.copy(green = Levels.STARTING_XP)
                Channel.BLUE -> aura.copy(blue = Levels.STARTING_XP)
            }
        }

        /**
         * Rounds [exact] amounts down to whole points, then gives the leftover points to the
         * largest remainders, so the parts always add up to [total].
         */
        private fun roundKeepingTotal(exact: Map<Channel, Double>, total: Int): Map<Channel, Int> {
            val parts = exact.mapValues { (_, value) -> value.toInt() }.toMutableMap()
            var left = total - parts.values.sum()
            for ((channel, value) in exact.entries.sortedByDescending { (c, value) -> value - parts.getValue(c) }) {
                if (left <= 0) break
                parts[channel] = value.toInt() + 1
                left--
            }
            return parts
        }
    }
}
