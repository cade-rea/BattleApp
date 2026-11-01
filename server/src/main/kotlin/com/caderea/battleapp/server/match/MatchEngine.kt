package com.caderea.battleapp.server.match

import com.caderea.battleapp.core.Battle
import com.caderea.battleapp.core.environment.Arena
import com.caderea.battleapp.core.fighter.Fighter
import com.caderea.battleapp.core.fighter.FighterProfile
import com.caderea.battleapp.core.online.EndReason
import com.caderea.battleapp.core.online.FighterInfo
import com.caderea.battleapp.core.online.FighterState
import com.caderea.battleapp.core.online.GuardUp
import com.caderea.battleapp.core.online.LandedAction
import com.caderea.battleapp.core.online.MatchResult
import com.caderea.battleapp.core.online.QueuedTechnique
import com.caderea.battleapp.core.online.ServerMessage
import com.caderea.battleapp.core.online.Side
import com.caderea.battleapp.core.progression.Levels
import com.caderea.battleapp.core.technique.Technique
import kotlin.random.Random

/**
 * One online match between two players, as plain rules with no clock or network: whoever
 * owns it ([Match]) calls [tick] once per tick and passes in each player's inputs, always from
 * the same coroutine. Players are seats 0 and 1, in the order they were matched.
 *
 * The battle is the same [Battle] the app plays against the computer, with no computer
 * opponent. [Battle] lets its player go first among equals on a tick, so which seat is the
 * battle's player is a coin flip.
 *
 * @param maxTicks the match is a draw if nobody has won by then
 */
class MatchEngine(
    val id: String,
    val arena: Arena,
    profiles: List<FighterProfile>,
    random: Random = Random.Default,
    val maxTicks: Int = MAX_TICKS,
) {

    init {
        require(profiles.size == 2) { "a match needs two players: ${profiles.size}" }
    }

    private val infos: List<FighterInfo> = profiles.map { infoFor(it, arena) }
    private val fighters: List<Fighter> = infos.map { info ->
        Fighter(info.name, info.color, info.maxHealth, info.techniques.map { it?.toAction() })
    }

    // which seat is the battle's player, the one that goes first among equals
    private val firstSeat = random.nextInt(2)

    private val battle = Battle(
        player = fighters[firstSeat],
        enemy = fighters[1 - firstSeat],
        environment = arena,
        opponent = null,
    )

    /** Who won and why, once the match is over. A null winner is a draw. */
    var outcome: Outcome? = null
        private set

    val isOver: Boolean
        get() = outcome != null

    val tickCount: Int
        get() = battle.tickCount

    /** The message that opens the match for [seat]. */
    fun started(seat: Int, tickLengthMs: Long) = ServerMessage.MatchStarted(
        matchId = id,
        arenaId = arena.id,
        tickLengthMs = tickLengthMs,
        you = infos[seat],
        opponent = infos[other(seat)],
    )

    /** The battle as [seat] sees it: their own fighter as you, with what landed on the last tick. */
    fun state(seat: Int, withLanded: Boolean = false) = ServerMessage.State(
        tick = battle.tickCount,
        you = stateOf(fighters[seat]),
        opponent = stateOf(fighters[other(seat)]),
        landed = if (!withLanded) emptyList() else battle.landed.map { landing ->
            val performer = fighters.indexOfFirst { it === landing.performer }
            LandedAction(
                side = if (performer == seat) Side.YOU else Side.OPPONENT,
                slot = landing.performer.actions.indexOfFirst { it === landing.action },
            )
        },
    )

    /** How the match ended for [seat], once it's over. */
    fun result(seat: Int): ServerMessage.MatchOver {
        val outcome = checkNotNull(outcome) { "the match isn't over" }
        val result = when (outcome.winner) {
            null -> MatchResult.DRAW
            seat -> MatchResult.WON
            else -> MatchResult.LOST
        }
        return ServerMessage.MatchOver(result, outcome.reason, battle.tickCount)
    }

    /**
     * Queues the technique in [slot] for [seat].
     * @return false if the match is over or the fighter can't queue it right now
     */
    fun queue(seat: Int, slot: Int): Boolean = !isOver && battle.queueAction(fighters[seat], slot)

    /**
     * Takes back [seat]'s last queued technique.
     * @return false if the match is over or nothing is queued
     */
    fun cancel(seat: Int): Boolean = !isOver && battle.cancelLast(fighters[seat])

    /** Resolves one tick. Ends the match on a knockout, or as a draw once it runs out of ticks. */
    fun tick() {
        if (isOver) return

        battle.tick()
        when {
            battle.isOver -> {
                val winner = battle.winner
                outcome = Outcome(winner?.let { w -> fighters.indexOfFirst { it === w } }, EndReason.KNOCKOUT)
            }
            battle.tickCount >= maxTicks -> end(winner = null, EndReason.TIME)
        }
    }

    /** Ends the match because [seat] gave up or dropped: the other player wins. */
    fun concede(seat: Int, reason: EndReason) {
        if (isOver) return
        end(winner = other(seat), reason)
    }

    /** Ends the match as a draw, as when the server shuts down mid-match. */
    fun abandon() {
        if (isOver) return
        end(winner = null, EndReason.DISCONNECT)
    }

    private fun end(winner: Int?, reason: EndReason) {
        battle.stop()
        outcome = Outcome(winner, reason)
    }

    private fun stateOf(fighter: Fighter) = FighterState(
        health = fighter.health,
        energy = fighter.energy,
        queue = fighter.queue.map { queued ->
            QueuedTechnique(
                slot = fighter.actions.indexOfFirst { it === queued.action },
                id = queued.id,
                ticksLeft = queued.ticksLeft,
            )
        },
        guard = fighter.activeGuard?.let { guard ->
            GuardUp(fighter.actions.indexOfFirst { it === guard.guard }, guard.strength, guard.ticksLeft)
        },
    )

    /** How a match ended. [winner] is a seat, or null for a draw. */
    data class Outcome(val winner: Int?, val reason: EndReason)

    companion object {
        /** About 15 minutes at one tick a second. A starting value to playtest. */
        const val MAX_TICKS = 900

        fun other(seat: Int) = 1 - seat

        /**
         * The fighter [profile] brings to [arena]: the arena's share of their loadout, each
         * technique dimmed to the power budget of their level, or of the arena's level cap if
         * that's lower.
         *
         * Until profiles live on the server, the level budget is what keeps a hand-edited
         * profile from bringing techniques brighter than its level allows.
         */
        fun infoFor(profile: FighterProfile, arena: Arena): FighterInfo {
            val level = arena.levelCap?.let { minOf(it, profile.level) } ?: profile.level
            val budget = Levels.powerBudget(level)
            val techniques: List<Technique?> = profile.loadoutFor(arena.loadoutSize)
                .map { it.copy(color = it.color.fitToBudget(budget)) }
            return FighterInfo(
                name = profile.name,
                color = profile.aura.color,
                level = profile.level,
                maxHealth = Fighter.DEFAULT_MAX_HEALTH,
                techniques = techniques + List(arena.loadoutSize - techniques.size) { null },
            )
        }
    }
}
