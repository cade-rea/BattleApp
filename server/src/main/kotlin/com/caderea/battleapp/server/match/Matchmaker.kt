package com.caderea.battleapp.server.match

import com.caderea.battleapp.core.environment.Arena
import com.caderea.battleapp.core.fighter.FighterProfile
import com.caderea.battleapp.core.online.ServerMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.UUID
import kotlin.random.Random

/**
 * Pairs players who want the same arena, first come first served. Smarter matching on level
 * and aura comes later; the arena's level cap is what evens fights out for now.
 *
 * @param scope where matches run, outliving the connections that started them
 */
class Matchmaker(
    private val scope: CoroutineScope,
    private val tickLengthMs: Long,
    private val maxTicks: Int = MatchEngine.MAX_TICKS,
    private val random: Random = Random.Default,
) {

    private val mutex = Mutex()
    // one player waiting per arena, at most
    private val waiting = mutableMapOf<String, Waiting>()

    /** Puts [player] in line for [arena], or starts a match if someone is already waiting there. */
    suspend fun join(player: Player, profile: FighterProfile, arena: Arena) {
        // seats are handed out under the lock, so a player who leaves at the same moment is
        // either still in line (and taken out) or already seated (and concedes)
        val match = mutex.withLock {
            val opponent = waiting.remove(arena.id)
            if (opponent == null) {
                waiting[arena.id] = Waiting(player, profile)
                null
            } else {
                val players = listOf(opponent.player, player)
                val engine = MatchEngine(
                    id = UUID.randomUUID().toString(),
                    arena = arena,
                    profiles = listOf(opponent.profile, profile),
                    random = random,
                    maxTicks = maxTicks,
                )
                Match(engine, players, tickLengthMs).also { match ->
                    players.forEachIndexed { index, it -> it.seat = Player.Seat(match, index) }
                }
            }
        }

        if (match == null) player.send(ServerMessage.Waiting(arena.id)) else match.start(scope)
    }

    /** Takes [player] out of line, if they're still waiting. */
    suspend fun leave(player: Player) {
        mutex.withLock {
            waiting.entries.removeIf { it.value.player === player }
        }
    }

    private class Waiting(val player: Player, val profile: FighterProfile)
}
