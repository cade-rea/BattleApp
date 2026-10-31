package com.caderea.battleapp.core.technique

import com.caderea.battleapp.core.color.Channel
import com.caderea.battleapp.core.color.ColorTraits
import com.caderea.battleapp.core.color.RgbColor
import com.caderea.battleapp.core.progression.Aura
import com.caderea.battleapp.core.progression.Levels
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.ln
import kotlin.math.roundToInt
import kotlin.math.sqrt
import kotlin.random.Random

/**
 * Rolls new techniques for a fighter. Attacks, heals and guards are all rolled the same way:
 *
 * 1. Pick the kind.
 * 2. Roll the mix around the fighter's aura, so a young red fighter mostly gets reds.
 * 3. Roll the total power as a share of the level's power budget.
 * 4. Roll wind-up, potency and cost multipliers around 1.0.
 *
 * Every spread widens with level, so stronger fighters get wilder colors and wilder stats,
 * both better and worse. All spreads are starting values to playtest.
 *
 * Pass a seeded [random] for repeatable rolls in tests, or on a server that must be able to
 * replay them.
 */
class TechniqueRoller(internal val random: Random = Random.Default) {

    /** Rolls a technique of a random kind. */
    fun roll(aura: Aura, id: Int): Technique = roll(aura, id, rollKind())

    fun roll(aura: Aura, id: Int, kind: TechniqueKind): Technique {
        val level = aura.level
        val color = rollColor(aura, level)
        val spread = statSpread(level)
        val rolls = TechniqueRolls(
            windUp = around(1.0, spread),
            potency = around(1.0, spread),
            cost = around(1.0, spread),
        )
        return Technique(id, kind, TechniqueNames.name(kind, color, rolls), color, rolls)
    }

    /**
     * Rolls a signature technique: the especially good move a new fighter picks last at
     * character creation. It is the pure [Aura.base] primary at the full power budget, its
     * [standout] stat rolls far past anything a normal roll can reach, and its other stats
     * only roll on the lucky side of normal.
     */
    fun rollSignature(aura: Aura, id: Int, kind: TechniqueKind, standout: Standout): Technique {
        val budget = Levels.powerBudget(aura.level)
        val color = pure(aura.base, budget)
        val spread = statSpread(aura.level)
        fun lucky(better: Double) = 1.0 + better * spread * random.nextDouble()
        fun standout(range: ClosedFloatingPointRange<Double>) =
            range.start + (range.endInclusive - range.start) * random.nextDouble()

        val rolls = TechniqueRolls(
            windUp = if (standout == Standout.FASTER) standout(SIGNATURE_WIND_UP) else lucky(-1.0),
            potency = if (standout == Standout.STRONGER) standout(SIGNATURE_POTENCY) else lucky(1.0),
            cost = if (standout == Standout.CHEAPER) standout(SIGNATURE_COST) else lucky(-1.0),
        )
        val name = "${standout.adjective} ${TechniqueNames.name(kind, color, rolls)}"
        return Technique(id, kind, name, color, rolls, signature = true)
    }

    fun rollKind(): TechniqueKind {
        val roll = random.nextDouble()
        return when {
            roll < ATTACK_CHANCE -> TechniqueKind.ATTACK
            roll < ATTACK_CHANCE + HEAL_CHANCE -> TechniqueKind.HEAL
            else -> TechniqueKind.GUARD
        }
    }

    private fun rollColor(aura: Aura, level: Int): RgbColor {
        val spread = colorSpread(level)
        val shares = Channel.entries.associateWith { channel ->
            (aura.share(channel) + gaussian() * spread).coerceAtLeast(0.0)
        }
        val sum = shares.values.sum()
        // every channel rolled to nothing: fall back to the aura's own mix
        val mix = if (sum == 0.0) Channel.entries.associateWith(aura::share) else shares.mapValues { it.value / sum }

        val budget = Levels.powerBudget(level)
        val power = budget * (1 - powerSpread(level) * random.nextDouble())
        fun channel(c: Channel) = (mix.getValue(c) * power).roundToInt().coerceIn(0, 255)
        return RgbColor(channel(Channel.RED), channel(Channel.GREEN), channel(Channel.BLUE)).fitToBudget(budget)
    }

    private fun around(center: Double, spread: Double) = center + spread * (2 * random.nextDouble() - 1)

    /** A standard normal sample, by Box-Muller, since kotlin.random has none. */
    private fun gaussian(): Double {
        val u = 1 - random.nextDouble() // in (0, 1], so the log is finite
        return sqrt(-2 * ln(u)) * cos(2 * PI * random.nextDouble())
    }

    companion object {
        const val ATTACK_CHANCE = 0.5
        /** Wind-up multiplier a signature FASTER technique rolls in. Normal rolls start at 0.7. */
        val SIGNATURE_WIND_UP = 0.55..0.7
        /** Potency a signature STRONGER technique rolls in. Normal rolls stop at 1.3. */
        val SIGNATURE_POTENCY = 1.3..1.45
        /** Cost multiplier a signature CHEAPER technique rolls in. */
        val SIGNATURE_COST = 0.55..0.7

        /**
         * The standouts that would actually help a signature in [base]'s pure color. Red
         * already winds up in a single tick, so a faster red would be no faster.
         */
        fun usefulStandouts(base: Channel): List<Standout> {
            val ticks = ColorTraits.of(pure(base, RgbColor.STARTING_POWER_BUDGET)).windUpTicks
            return Standout.entries.filter { it != Standout.FASTER || ticks > 1 }
        }

        private fun pure(channel: Channel, budget: Int): RgbColor {
            val power = budget.coerceAtMost(255)
            return when (channel) {
                Channel.RED -> RgbColor(power, 0, 0)
                Channel.GREEN -> RgbColor(0, power, 0)
                Channel.BLUE -> RgbColor(0, 0, power)
            }
        }

        private val Standout.adjective: String
            get() = when (this) {
                Standout.STRONGER -> "Mighty"
                Standout.FASTER -> "Swift"
                Standout.CHEAPER -> "Deft"
            }

        const val HEAL_CHANCE = 0.25

        /** How far each channel's share strays from the aura's, as a standard deviation. */
        fun colorSpread(level: Int): Double = (0.12 + 0.015 * (level - 1)).coerceAtMost(0.45)

        /** How far below the full budget a technique's power can roll. */
        fun powerSpread(level: Int): Double = (0.15 + 0.01 * (level - 1)).coerceAtMost(0.4)

        /** How far each stat multiplier can stray from 1.0 either way. */
        fun statSpread(level: Int): Double = (0.1 + 0.005 * (level - 1)).coerceAtMost(0.3)
    }
}
