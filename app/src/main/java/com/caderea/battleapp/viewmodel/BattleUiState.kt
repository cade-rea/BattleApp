package com.caderea.battleapp.viewmodel

import com.caderea.battleapp.core.QueuedAction
import com.caderea.battleapp.core.action.Attack
import com.caderea.battleapp.core.action.BattleAction
import com.caderea.battleapp.core.action.Guard
import com.caderea.battleapp.core.action.Heal
import com.caderea.battleapp.core.color.RgbColor

/**
 * Everything the battle screen shows. The screen redraws itself from this alone.
 */
data class BattleUiState(
    // one entry per action button
    val actions: List<ActionButtonState> = emptyList(),
    val player: FighterState = FighterState(),
    val enemy: FighterState = FighterState(),
    val playerQueue: List<QueuedAction> = emptyList(),
    val enemyQueue: List<QueuedAction> = emptyList(),
    // ticks played so far
    val tick: Int = 0,
    // how far through the current tick we are, 0-100
    val tickProgress: Int = 0,
    val isPaused: Boolean = false,
    // oldest first
    val log: List<LogEntry> = emptyList(),
    // null while the battle is on
    val outcome: Outcome? = null,
) {
    val isOver: Boolean
        get() = outcome != null
}

enum class Outcome { WON, LOST, STOPPED }

enum class ActionKind { ATTACK, HEAL, GUARD }

val BattleAction.kind: ActionKind
    get() = when (this) {
        is Heal -> ActionKind.HEAL
        is Guard -> ActionKind.GUARD
        is Attack -> ActionKind.ATTACK
        // anything new does damage until it gets a kind of its own
        else -> ActionKind.ATTACK
    }

/**
 * One action button. An empty slot has an empty [name] and is never enabled.
 */
data class ActionButtonState(
    val name: String = "",
    val cost: Int = 0,
    // false when the slot is empty, the queue is full or the fighter can't afford it
    val enabled: Boolean = false,
    val kind: ActionKind = ActionKind.ATTACK,
    val color: RgbColor = RgbColor.BLACK,
    val windUpTicks: Int = 0,
    // what it does right now: damage against the enemy before guards, health healed, or the
    // percent of a same-colored attack a guard blocks
    val effect: Int = 0,
    // attacks only: the type matchup against the enemy's aura, 1.0 is neutral
    val effectiveness: Double = 1.0,
)

data class FighterState(
    val name: String = "",
    val color: RgbColor = RgbColor.BLACK,
    val health: Int = 0,
    val maxHealth: Int = 0,
    val energy: Int = 0,
    val maxEnergy: Int = 0,
    // the guard that's up, or null
    val guard: GuardState? = null,
)

data class GuardState(
    val name: String,
    val color: RgbColor,
    // ticks it stays up, counting the current one
    val ticksLeft: Int,
)

enum class Side { PLAYER, ENEMY }

/**
 * One line of the battle log. [side] is who it's about, or null for lines about the
 * battle itself.
 */
data class LogEntry(val tick: Int, val text: String, val side: Side? = null)
