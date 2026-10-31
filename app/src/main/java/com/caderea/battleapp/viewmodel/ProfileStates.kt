package com.caderea.battleapp.viewmodel

import com.caderea.battleapp.core.action.Attack
import com.caderea.battleapp.core.action.Guard
import com.caderea.battleapp.core.action.Heal
import com.caderea.battleapp.core.color.RgbColor
import com.caderea.battleapp.core.environment.Environment
import com.caderea.battleapp.core.fighter.Fighter
import com.caderea.battleapp.core.fighter.FighterProfile
import com.caderea.battleapp.core.progression.BattleRewards
import com.caderea.battleapp.core.technique.Technique
import com.caderea.battleapp.core.technique.TechniqueKind
import kotlin.math.roundToInt

/** A fighter between battles, as the menu and loadout screens show them. */
data class FighterSummary(
    val name: String = "",
    val color: RgbColor = RgbColor.BLACK,
    val level: Int = 1,
    // how far to the next level, 0-1
    val levelProgress: Float = 0f,
    val wins: Int = 0,
    val losses: Int = 0,
)

/**
 * One technique from the catalog, with what it does for this fighter against a neutral
 * opponent, so techniques can be compared outside a battle.
 */
data class TechniqueCardState(
    val id: Int,
    val name: String,
    val kind: ActionKind,
    val color: RgbColor,
    val windUpTicks: Int,
    val cost: Int,
    // damage, health healed, or percent blocked, as on the battle buttons
    val effect: Int,
    // the technique's own potency roll as a percent above or below normal, such as +6
    val potencyPercent: Int,
    // its button in the loadout, from 0, or null if it stays home
    val slot: Int? = null,
)

/** What the player got out of a battle. */
data class RewardsState(
    val xp: Int,
    val level: Int,
    val levelsGained: Int,
    val levelProgress: Float,
    val auraBefore: RgbColor,
    val auraAfter: RgbColor,
    val newTechnique: TechniqueCardState?,
)

val TechniqueKind.actionKind: ActionKind
    get() = when (this) {
        TechniqueKind.ATTACK -> ActionKind.ATTACK
        TechniqueKind.HEAL -> ActionKind.HEAL
        TechniqueKind.GUARD -> ActionKind.GUARD
    }

fun FighterProfile.toSummary() = FighterSummary(
    name = name,
    color = aura.color,
    level = level,
    levelProgress = aura.levelProgress.toFloat(),
    wins = wins,
    losses = losses,
)

/** Grey has an equal share of every channel, so every matchup against it is neutral. */
private val NEUTRAL = RgbColor(85, 85, 85)

fun Technique.toCard(aura: RgbColor, slot: Int? = null): TechniqueCardState {
    val action = toAction()
    val performer = Fighter(color = aura)
    val environment = Environment()
    val effect = when (action) {
        is Attack -> action.damage(performer, Fighter(color = NEUTRAL), environment)
        is Heal -> action.amount(performer, environment)
        is Guard -> (action.strength(performer, environment).coerceAtMost(Guard.MAX_BLOCK) * 100).roundToInt()
        else -> 0
    }
    return TechniqueCardState(
        id = id,
        name = name,
        kind = kind.actionKind,
        color = color,
        windUpTicks = action.duration,
        cost = action.cost,
        effect = effect,
        potencyPercent = ((rolls.potency - 1) * 100).roundToInt(),
        slot = slot,
    )
}

fun BattleRewards.toState() = RewardsState(
    xp = xp,
    level = after.level,
    levelsGained = levelsGained,
    levelProgress = after.levelProgress.toFloat(),
    auraBefore = before.color,
    auraAfter = after.color,
    newTechnique = newTechnique?.toCard(after.color),
)
