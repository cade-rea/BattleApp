package com.caderea.battleapp.viewmodel

import com.caderea.battleapp.core.QueuedAction

/**
 * Everything the battle screen shows. The screen redraws itself from this alone.
 */
data class BattleUiState(
    // one label per action button; empty for an empty slot
    val actionNames: List<String> = emptyList(),
    val playerQueue: List<QueuedAction> = emptyList(),
    val enemyQueue: List<QueuedAction> = emptyList(),
    // how far through the current tick we are, 0-100
    val tickProgress: Int = 0,
    val log: List<String> = emptyList(),
    val isOver: Boolean = false,
)
