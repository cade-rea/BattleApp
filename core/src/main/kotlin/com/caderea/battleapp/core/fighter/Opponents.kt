package com.caderea.battleapp.core.fighter

import com.caderea.battleapp.core.color.Channel
import com.caderea.battleapp.core.color.RgbColor
import com.caderea.battleapp.core.environment.Environment
import com.caderea.battleapp.core.progression.Aura
import com.caderea.battleapp.core.progression.Levels
import com.caderea.battleapp.core.technique.TechniqueRoller
import kotlin.random.Random

/**
 * Computer opponents for battles outside PvP, made to match the player's level.
 */
object Opponents {

    const val NAME = "Enemy"
    /** How much of an opponent's XP beyond the start drifts into one other color. */
    const val MAX_TINT = 0.3

    /**
     * A fighter at [level] with a random base primary and a slight tint toward one other
     * color, as if they'd played a while: the starter kit plus a few rolled techniques, more
     * at higher levels, with a loadout picked for them.
     */
    fun atLevel(level: Int, random: Random = Random.Default): FighterProfile {
        val base = Channel.entries.random(random)
        val tint = Channel.entries.filter { it != base }.random(random)
        val start = Aura.start(base)
        val toGo = Levels.xpFor(level) - start.totalXp
        val tintShare = MAX_TINT * random.nextDouble()
        val drift = RgbColor(
            red = channelLevel(Channel.RED, base, tint, tintShare),
            green = channelLevel(Channel.GREEN, base, tint, tintShare),
            blue = channelLevel(Channel.BLUE, base, tint, tintShare),
        )
        val aura = start.gain(toGo.coerceAtLeast(0), drift)

        val roller = TechniqueRoller(random)
        val kit = FighterProfile.starterKit(base)
        val rolls = (level + 1).coerceAtMost(Environment.MAX_LOADOUT)
        val catalog = kit + List(rolls) { index -> roller.roll(aura, id = kit.size + index + 1) }
        return FighterProfile(NAME, aura, catalog).withAutoLoadout()
    }

    /**
     * An opponent for a player at [playerLevel] in [environment]: the player's level, or the
     * arena's level cap if that's lower, so a capped arena doesn't pit a capped player
     * against a fighter with a full catalog of high-level rolls.
     */
    fun forArena(playerLevel: Int, environment: Environment, random: Random = Random.Default): FighterProfile =
        atLevel(environment.levelCap?.let { minOf(it, playerLevel) } ?: playerLevel, random)

    private fun channelLevel(channel: Channel, base: Channel, tint: Channel, tintShare: Double): Int = when (channel) {
        base -> (255 * (1 - tintShare)).toInt()
        tint -> (255 * tintShare).toInt()
        else -> 0
    }
}
