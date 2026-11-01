package com.caderea.battleapp.online

import com.caderea.battleapp.core.online.ClientMessage
import com.caderea.battleapp.core.online.ServerMessage
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow

/** A connection to a pretend server: the test says what the server sends and reads what the app sent. */
class FakeConnection : OnlineConnection {

    private val fromServer = Channel<ServerMessage>(Channel.UNLIMITED)

    val sent = mutableListOf<ClientMessage>()
    var closed = false
        private set

    override val messages: Flow<ServerMessage> = fromServer.receiveAsFlow()

    override fun send(message: ClientMessage) {
        sent += message
    }

    override fun close() {
        closed = true
        fromServer.close()
    }

    fun receive(message: ServerMessage) {
        fromServer.trySend(message)
    }

    /** The server hangs up, cleanly or with [error]. */
    fun hangUp(error: Throwable? = null) {
        fromServer.close(error)
    }
}

/** Hands out a fresh [FakeConnection] per connect, and remembers them. */
class FakeConnector : OnlineConnector {
    val connections = mutableListOf<FakeConnection>()
    val urls = mutableListOf<String>()

    val last: FakeConnection
        get() = connections.last()

    override fun connect(url: String): OnlineConnection {
        urls += url
        return FakeConnection().also { connections += it }
    }
}
