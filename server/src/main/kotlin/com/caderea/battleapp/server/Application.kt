package com.caderea.battleapp.server

import com.caderea.battleapp.core.environment.Arena
import com.caderea.battleapp.core.environment.Arenas
import com.caderea.battleapp.core.fighter.FighterProfile
import com.caderea.battleapp.core.online.ClientMessage
import com.caderea.battleapp.core.online.OnlineJson
import com.caderea.battleapp.core.online.ServerMessage
import com.caderea.battleapp.core.technique.TechniqueKind
import com.caderea.battleapp.server.match.MatchEngine
import com.caderea.battleapp.server.match.Matchmaker
import com.caderea.battleapp.server.match.Player
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import io.ktor.server.websocket.DefaultWebSocketServerSession
import io.ktor.server.websocket.WebSockets
import io.ktor.server.websocket.pingPeriod
import io.ktor.server.websocket.timeout
import io.ktor.server.websocket.webSocket
import io.ktor.websocket.CloseReason
import io.ktor.websocket.Frame
import io.ktor.websocket.close
import io.ktor.websocket.readText
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.seconds

/**
 * Settings for one server.
 * @param tickLengthMs how long a tick lasts in online matches; the app's default is 1000
 */
data class ServerConfig(
    val tickLengthMs: Long = 1000,
    val maxTicks: Int = MatchEngine.MAX_TICKS,
)

/**
 * The server's routes: `GET /health` for the host's health check, and the `/play` WebSocket,
 * where a phone joins matchmaking and then plays its match. Messages are [OnlineJson], one per
 * text frame.
 */
fun Application.module(config: ServerConfig = ServerConfig()) {
    install(WebSockets) {
        // a phone that goes quiet this long has dropped and loses its match
        pingPeriod = 15.seconds
        timeout = 30.seconds
    }

    // matches run in the application's scope, so a match outlives a dropped connection long
    // enough to award the win, and stops when the server does
    val matchmaker = Matchmaker(this, config.tickLengthMs, config.maxTicks)

    routing {
        get("/health") {
            call.respondText("ok")
        }

        webSocket("/play") {
            play(matchmaker)
        }
    }
}

/**
 * One phone's connection: the first message must be a [ClientMessage.Join]; after that,
 * inputs go to the match once one starts. The connection closes when the match is over.
 */
private suspend fun DefaultWebSocketServerSession.play(matchmaker: Matchmaker) {
    val player = Player()
    val writer = launch {
        for (message in player.outgoing) {
            send(Frame.Text(OnlineJson.encode(message)))
        }
        close(CloseReason(CloseReason.Codes.NORMAL, "done"))
    }

    var joined = false
    try {
        for (frame in incoming) {
            if (frame !is Frame.Text) continue

            val message = runCatching { OnlineJson.decodeClient(frame.readText()) }.getOrNull()
            if (message == null) {
                player.send(ServerMessage.Rejected("unreadable message"))
                continue
            }

            val seat = player.seat
            when {
                seat != null -> seat.match.input(seat.index, message)
                message is ClientMessage.Join && !joined -> {
                    val problem = problemWith(message)
                    if (problem != null) {
                        player.send(ServerMessage.Rejected(problem))
                        player.finish()
                    } else {
                        joined = true
                        matchmaker.join(player, cleaned(message.profile), Arenas.byId(message.arenaId))
                    }
                }
                message is ClientMessage.Leave && joined -> {
                    matchmaker.leave(player)
                    // a match may have started in the meantime; then leaving gives it up
                    player.seat?.let { it.match.input(it.index, message) } ?: player.finish()
                }
                joined -> player.send(ServerMessage.Rejected("no match yet"))
                else -> player.send(ServerMessage.Rejected("join first"))
            }
        }
    } finally {
        // leaving first means the seat below is settled: either we were still in line, or
        // we're seated and the match hears we dropped
        matchmaker.leave(player)
        player.seat?.let { it.match.disconnected(it.index) }
        player.finish()
        writer.cancel()
    }
}

/** Why the server won't match this join, or null if it's fine. */
private fun problemWith(join: ClientMessage.Join): String? {
    val arena: Arena = Arenas.ALL.firstOrNull { it.id == join.arenaId }
        ?: return "unknown arena: ${join.arenaId}"
    val techniques = join.profile.loadoutFor(arena.loadoutSize)
    return when {
        join.profile.name.isBlank() -> "a fighter needs a name"
        // without an attack nobody could win, and the match would run out the clock
        techniques.none { it.kind == TechniqueKind.ATTACK } -> "the loadout needs an attack"
        else -> null
    }
}

private fun cleaned(profile: FighterProfile): FighterProfile =
    profile.copy(name = profile.name.trim().take(MAX_NAME_LENGTH))

private const val MAX_NAME_LENGTH = 24
