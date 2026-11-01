package com.caderea.battleapp.server.match

import com.caderea.battleapp.core.online.ServerMessage
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel

/**
 * One connected phone, from the server's side. Messages for it are buffered here and its
 * connection writes them out, so a slow phone never holds up the match.
 */
class Player {

    private val _outgoing = Channel<ServerMessage>(Channel.UNLIMITED)

    /** What to send to the phone, in order. Closed once the phone has nothing more coming. */
    val outgoing: ReceiveChannel<ServerMessage>
        get() = _outgoing

    /** The match this player is in and their seat in it, once matched. */
    @Volatile
    var seat: Seat? = null

    fun send(message: ServerMessage) {
        _outgoing.trySend(message)
    }

    /** Sends nothing more: the connection closes once what's buffered is written. */
    fun finish() {
        _outgoing.close()
    }

    data class Seat(val match: Match, val index: Int)
}
