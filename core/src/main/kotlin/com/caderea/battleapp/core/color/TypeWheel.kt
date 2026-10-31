package com.caderea.battleapp.core.color

/**
 * Type matchups. Each channel of an attack is scored on its own against the defender's
 * color, so mixed colors become cross-types with no extra rules:
 *
 *     damage = sum over c of attack[c] * (1 + K * defenderShare(prey of c) - K * defenderShare(predator of c))
 *
 * Pure colors swing between 0.5x and 1.5x, and mixes stay close to neutral. The formula is
 * smooth, so two colors that look alike always play alike.
 */
object TypeWheel {

    /** How strongly a matchup swings damage. A starting value to playtest. */
    const val K = 0.5

    /**
     * The multiplier for one attacking channel against a defender of the given color.
     */
    fun channelMultiplier(attacking: Channel, defender: RgbColor): Double =
        1 + K * defender.share(attacking.prey) - K * defender.share(attacking.predator)

    /**
     * The attack's power after matchups: each channel times its multiplier, summed.
     */
    fun typedPower(attack: RgbColor, defender: RgbColor): Double =
        Channel.entries.sumOf { attack[it] * channelMultiplier(it, defender) }

    /**
     * How effective the attack is overall, as a multiplier on its total power: 1.0 is
     * neutral, 1.5 is the best a pure color can do and 0.5 the worst.
     */
    fun effectiveness(attack: RgbColor, defender: RgbColor): Double =
        if (attack.power == 0) 1.0 else typedPower(attack, defender) / attack.power
}
