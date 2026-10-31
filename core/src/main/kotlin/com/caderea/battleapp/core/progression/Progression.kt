package com.caderea.battleapp.core.progression

import com.caderea.battleapp.core.Battle
import com.caderea.battleapp.core.color.RgbColor
import com.caderea.battleapp.core.fighter.FighterProfile
import com.caderea.battleapp.core.technique.Technique
import com.caderea.battleapp.core.technique.TechniqueRoller

/**
 * What a fighter got out of one battle.
 *
 * @param newTechnique rolled after a win, already in the catalog of the updated profile
 */
data class BattleRewards(
    val xp: Int,
    val before: Aura,
    val after: Aura,
    val won: Boolean,
    val newTechnique: Technique?,
) {
    val levelsGained: Int
        get() = after.level - before.level
}

/**
 * Turns a finished battle into XP and new techniques. All amounts are starting values to
 * playtest.
 *
 * Matches that award XP will run on the PvP server; for now the app applies these after
 * battles against the computer.
 */
object Progression {

    /** XP for each technique that lands, whatever its color. */
    const val XP_PER_TECHNIQUE = 5
    /** Extra XP for winning, split as if it came from the mix of everything used. */
    const val WIN_BONUS = 40

    /**
     * Rewards [profile] for a battle in which their techniques of the [used] colors landed.
     * Each one earns [XP_PER_TECHNIQUE] split by [Aura.gain]; a win adds [WIN_BONUS] and
     * rolls one new technique from the grown aura.
     * @return the updated profile and what changed
     */
    fun afterBattle(
        profile: FighterProfile,
        used: List<RgbColor>,
        won: Boolean,
        roller: TechniqueRoller = TechniqueRoller(),
    ): Pair<FighterProfile, BattleRewards> {
        val earnings = used.map { XP_PER_TECHNIQUE to it } + if (won) listOf(WIN_BONUS to mix(used)) else emptyList()
        val aura = profile.aura.gain(earnings)

        var updated = profile.copy(
            aura = aura,
            wins = profile.wins + if (won) 1 else 0,
            losses = profile.losses + if (won) 0 else 1,
        )
        val technique = if (won) roller.roll(aura, updated.nextTechniqueId) else null
        technique?.let { updated = updated.learn(it) }

        val rewards = BattleRewards(aura.totalXp - profile.aura.totalXp, profile.aura, aura, won, technique)
        return updated to rewards
    }

    /** [Battle] convenience: rewards the battle's player for what they landed. */
    fun afterBattle(
        profile: FighterProfile,
        battle: Battle,
        roller: TechniqueRoller = TechniqueRoller(),
    ): Pair<FighterProfile, BattleRewards> =
        afterBattle(profile, battle.used(battle.player).map { it.color }, battle.winner === battle.player, roller)

    /** The overall mix of [colors] as one color, or black if there are none. */
    private fun mix(colors: List<RgbColor>): RgbColor {
        if (colors.isEmpty()) return RgbColor.BLACK
        val red = colors.sumOf { it.red }.toLong()
        val green = colors.sumOf { it.green }.toLong()
        val blue = colors.sumOf { it.blue }.toLong()
        val top = maxOf(red, green, blue).coerceAtLeast(1)
        return RgbColor((255 * red / top).toInt(), (255 * green / top).toInt(), (255 * blue / top).toInt())
    }
}
