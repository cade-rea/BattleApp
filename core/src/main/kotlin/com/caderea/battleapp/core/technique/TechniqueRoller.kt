package com.caderea.battleapp.core.technique

import com.caderea.battleapp.core.color.Channel
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
class TechniqueRoller(private val random: Random = Random.Default) {

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

    private fun rollKind(): TechniqueKind {
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
        const val HEAL_CHANCE = 0.25

        /** How far each channel's share strays from the aura's, as a standard deviation. */
        fun colorSpread(level: Int): Double = (0.12 + 0.015 * (level - 1)).coerceAtMost(0.45)

        /** How far below the full budget a technique's power can roll. */
        fun powerSpread(level: Int): Double = (0.15 + 0.01 * (level - 1)).coerceAtMost(0.4)

        /** How far each stat multiplier can stray from 1.0 either way. */
        fun statSpread(level: Int): Double = (0.1 + 0.005 * (level - 1)).coerceAtMost(0.3)
    }
}
