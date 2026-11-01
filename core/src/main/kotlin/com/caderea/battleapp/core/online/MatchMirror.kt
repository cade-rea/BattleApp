package com.caderea.battleapp.core.online

import com.caderea.battleapp.core.Landing
import com.caderea.battleapp.core.QueuedAction
import com.caderea.battleapp.core.action.ActiveGuard
import com.caderea.battleapp.core.action.Guard
import com.caderea.battleapp.core.environment.Arena
import com.caderea.battleapp.core.environment.Arenas
import com.caderea.battleapp.core.environment.Environment
import com.caderea.battleapp.core.fighter.Fighter

/**
 * A phone's copy of a match the server runs. It never resolves a tick itself: it holds two
 * [Fighter]s built from [ServerMessage.MatchStarted] and copies each [ServerMessage.State]
 * onto them, so the screen can show and preview an online battle with the same code as an
 * offline one.
 */
class MatchMirror(started: ServerMessage.MatchStarted) {

    val matchId: String = started.matchId
    val arena: Arena = Arenas.byId(started.arenaId)
    val tickLengthMs: Long = started.tickLengthMs

    val you: Fighter = started.you.toFighter()
    val opponent: Fighter = started.opponent.toFighter()

    /** The last tick the server resolved. */
    var tick: Int = 0
        private set

    /** What landed on the last tick, in order. */
    var landed: List<Landing> = emptyList()
        private set

    init {
        you.target = opponent
        opponent.target = you
    }

    /** The arena as it stands for the next tick, for previews. */
    val conditions: Environment
        get() = arena.at(tick + 1)

    /** Copies [state] onto the fighters. */
    fun apply(state: ServerMessage.State) {
        tick = state.tick
        you.copyFrom(state.you)
        opponent.copyFrom(state.opponent)
        landed = state.landed.mapNotNull { landing ->
            val performer = if (landing.side == Side.YOU) you else opponent
            performer.actions.getOrNull(landing.slot)?.let { Landing(performer, it) }
        }
    }

    private fun FighterInfo.toFighter() = Fighter(name, color, maxHealth, techniques.map { it?.toAction() })

    private fun Fighter.copyFrom(state: FighterState) {
        health = state.health
        energy = state.energy
        queue.restore(
            state.queue.mapNotNull { queued ->
                actions.getOrNull(queued.slot)?.let { QueuedAction(it, queued.id, queued.ticksLeft) }
            }
        )
        activeGuard = state.guard?.let { up ->
            (actions.getOrNull(up.slot) as? Guard)?.let { ActiveGuard(it, up.strength, up.ticksLeft) }
        }
    }
}
