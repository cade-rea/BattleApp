package com.caderea.battleapp.viewmodel

import com.caderea.battleapp.core.QueuedAction

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
    // how far through the current tick we are, 0-100
    val tickProgress: Int = 0,
    val log: List<String> = emptyList(),
    val isOver: Boolean = false,
)

/**
 * One action button. An empty slot has an empty [name] and is never enabled.
 */
data class ActionButtonState(
    val name: String = "",
    val cost: Int = 0,
    // false when the slot is empty, the queue is full or the fighter can't afford it
    val enabled: Boolean = false,
)

data class FighterState(
    val name: String = "",
    val health: Int = 0,
    val maxHealth: Int = 0,
    val energy: Int = 0,
    val maxEnergy: Int = 0,
    // the name of the guard that's up, or null
    val guard: String? = null,
)
