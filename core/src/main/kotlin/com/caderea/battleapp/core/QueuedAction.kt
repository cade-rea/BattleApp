package com.caderea.battleapp.core

import com.caderea.battleapp.core.action.BattleAction

/**
 * An action waiting in a fighter's queue. [id] counts up per queue, so the UI can tell
 * queued actions apart even when they are the same action.
 */
data class QueuedAction(val action: BattleAction, val id: Int)
