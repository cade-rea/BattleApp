package com.caderea.battleapp.viewmodel

import com.caderea.battleapp.core.Landing
import com.caderea.battleapp.core.action.Attack
import com.caderea.battleapp.core.action.BattleAction
import com.caderea.battleapp.core.action.Guard
import com.caderea.battleapp.core.action.Heal
import com.caderea.battleapp.core.color.TypeWheel
import com.caderea.battleapp.core.environment.Environment
import com.caderea.battleapp.core.fighter.Fighter
import kotlin.math.roundToInt

/*
 * Turns fighters into what the battle screen shows. Shared by the offline battle and the
 * online one, so both look and preview the same.
 */

/**
 * One button per action slot. [conditions] is the arena for the next tick, which is when a
 * red technique would land, so previews use its light.
 */
fun Fighter.toButtons(conditions: Environment, isOver: Boolean): List<ActionButtonState> =
    actions.mapIndexed { slot, action ->
        action?.toButton(this, conditions, enabled = !isOver && canQueue(slot)) ?: ActionButtonState()
    }

fun BattleAction.toButton(player: Fighter, conditions: Environment, enabled: Boolean): ActionButtonState {
    val button = ActionButtonState(
        name = name,
        cost = cost,
        enabled = enabled,
        kind = kind,
        color = color,
        windUpTicks = duration,
    )
    return when (this) {
        is Attack -> button.copy(
            effect = damage(player, player.target, conditions),
            effectiveness = TypeWheel.effectiveness(color, player.target.color),
        )
        is Heal -> button.copy(effect = amount(player, conditions))
        is Guard -> button.copy(
            effect = (strength(player, conditions).coerceAtMost(Guard.MAX_BLOCK) * 100).roundToInt(),
        )
        else -> button
    }
}

fun Fighter.toState(healthChange: Int) = FighterState(
    name = name,
    color = color,
    health = health,
    maxHealth = maxHealth,
    energy = energy,
    maxEnergy = Fighter.MAX_ENERGY,
    guard = activeGuard?.let { GuardState(it.guard.name, it.guard.color, it.ticksLeft) },
    healthChange = healthChange,
)

/** What landed, with [player]'s actions on the player's side. */
fun List<Landing>.toLanded(player: Fighter): List<LandedAction> = map { landing ->
    val side = if (landing.performer === player) Side.PLAYER else Side.ENEMY
    LandedAction(side, landing.action.kind, landing.action.color)
}
