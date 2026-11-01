package com.caderea.battleapp.server.match

import com.caderea.battleapp.core.online.ClientMessage
import com.caderea.battleapp.core.online.EndReason
import com.caderea.battleapp.core.online.ServerMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Runs a [MatchEngine] in real time: a tick every [tickLengthMs], and each player's inputs
 * in between, as they arrive. Ticks and inputs go through one channel and one coroutine, so
 * the battle is only ever touched from one place and arrives in a single order for both
 * players.
 *
 * The match runs in the server's scope rather than either player's connection, so it can
 * finish (and hand the win to the other player) when a connection drops.
 */
class Match(
    private val engine: MatchEngine,
    private val players: List<Player>,
    private val tickLengthMs: Long,
) {

    private val events = Channel<Event>(Channel.UNLIMITED)

    val id: String
        get() = engine.id

    /** Passes on an input from the player in [seat]. Ignored once the match is over. */
    fun input(seat: Int, message: ClientMessage) {
        events.trySend(Event.Input(seat, message))
    }

    /** The player in [seat] lost their connection. They lose the match if it's still on. */
    fun disconnected(seat: Int) {
        events.trySend(Event.Disconnected(seat))
    }

    fun start(scope: CoroutineScope): Job = scope.launch {
        players.forEachIndexed { seat, player ->
            player.send(engine.started(seat, tickLengthMs))
            player.send(engine.state(seat))
        }

        val ticker = launch {
            while (true) {
                delay(tickLengthMs)
                events.send(Event.Tick)
            }
        }

        try {
            for (event in events) {
                handle(event)
                if (engine.isOver) break
            }
        } finally {
            ticker.cancel()
            events.close()
            // only if the server is shutting down: nobody is to blame, so it's a draw
            engine.abandon()
            players.forEachIndexed { seat, player ->
                player.send(engine.result(seat))
                player.finish()
            }
        }
    }

    private fun handle(event: Event) {
        when (event) {
            Event.Tick -> {
                engine.tick()
                broadcast(withLanded = true)
            }
            is Event.Disconnected -> engine.concede(event.seat, EndReason.DISCONNECT)
            is Event.Input -> when (val message = event.message) {
                is ClientMessage.Queue ->
                    if (engine.queue(event.seat, message.slot)) broadcast()
                    else players[event.seat].send(ServerMessage.Rejected("can't queue slot ${message.slot} now"))
                ClientMessage.Cancel ->
                    if (engine.cancel(event.seat)) broadcast()
                ClientMessage.Leave -> engine.concede(event.seat, EndReason.FORFEIT)
                is ClientMessage.Join ->
                    players[event.seat].send(ServerMessage.Rejected("already in a match"))
            }
        }
    }

    private fun broadcast(withLanded: Boolean = false) {
        players.forEachIndexed { seat, player -> player.send(engine.state(seat, withLanded)) }
    }

    private sealed interface Event {
        data object Tick : Event
        data class Input(val seat: Int, val message: ClientMessage) : Event
        data class Disconnected(val seat: Int) : Event
    }
}
