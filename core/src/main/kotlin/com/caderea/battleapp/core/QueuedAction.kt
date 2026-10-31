package com.caderea.battleapp.core

import com.caderea.battleapp.core.action.BattleAction

/**
 * An action waiting in a fighter's queue. [id] counts up per queue, so the UI can tell
 * queued actions apart even when they are the same action.
 *
 * Only the action at the front of the queue winds up. [ticksLeft] is how many more ticks
 * it needs, counting the next one: it lands on the tick that brings it to zero.
 */
data class QueuedAction(
    val action: BattleAction,
    val id: Int,
    val ticksLeft: Int = action.duration,
) {
    /** Whether the action has spent any of its wind-up yet. */
    val isWindingUp: Boolean
        get() = ticksLeft < action.duration
}
