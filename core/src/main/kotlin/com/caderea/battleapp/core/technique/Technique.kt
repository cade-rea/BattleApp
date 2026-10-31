package com.caderea.battleapp.core.technique

import com.caderea.battleapp.core.action.Attack
import com.caderea.battleapp.core.action.BattleAction
import com.caderea.battleapp.core.action.Guard
import com.caderea.battleapp.core.action.Heal
import com.caderea.battleapp.core.color.RgbColor
import kotlinx.serialization.Serializable

/** What a technique does when it lands. */
enum class TechniqueKind { ATTACK, HEAL, GUARD }

/** The one stat a signature technique is especially good at. */
enum class Standout {
    /** Hits, heals or guards harder. */
    STRONGER,
    /** Winds up faster. */
    FASTER,
    /** Costs less energy. */
    CHEAPER,
}

/**
 * A technique's own luck, rolled once when it is earned and kept for good. Each is a
 * multiplier around 1.0 on top of the color's traits, so a fast blue or a strong red is a
 * lucky roll rather than a different rule.
 */
@Serializable
data class TechniqueRolls(
    /** Multiplies the wind-up: below 1 is faster. */
    val windUp: Double = 1.0,
    /** Multiplies damage, healing or guarding. */
    val potency: Double = 1.0,
    /** Multiplies the energy cost: below 1 is cheaper. */
    val cost: Double = 1.0,
) {
    companion object {
        val NEUTRAL = TechniqueRolls()
    }
}

/**
 * A technique a fighter has earned and keeps in their catalog. It is plain data, so it can be
 * saved and sent over the network; [toAction] turns it into something a battle can use.
 *
 * @param id unique within one fighter's catalog
 * @param signature the especially good technique a fighter picks last at character creation
 */
@Serializable
data class Technique(
    val id: Int,
    val kind: TechniqueKind,
    val name: String,
    val color: RgbColor,
    val rolls: TechniqueRolls = TechniqueRolls.NEUTRAL,
    val signature: Boolean = false,
) {

    /**
     * The technique as a battle action. [budget] is the most power the arena allows: a
     * brighter technique is dimmed to fit, keeping its mix, so a level cap evens fights out.
     */
    fun toAction(budget: Int = Int.MAX_VALUE): BattleAction {
        val fitted = color.fitToBudget(budget)
        return when (kind) {
            TechniqueKind.ATTACK -> Attack(name, fitted, rolls)
            TechniqueKind.HEAL -> Heal(name, fitted, rolls)
            TechniqueKind.GUARD -> Guard(name, fitted, rolls)
        }
    }
}
