package com.caderea.battleapp.online

import com.caderea.battleapp.core.online.ClientMessage
import com.caderea.battleapp.core.online.OnlineJson
import com.caderea.battleapp.core.online.ServerMessage
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import java.util.concurrent.TimeUnit

/** One WebSocket connection to the PvP server. */
interface OnlineConnection {

    /**
     * Everything the server sends, in order. It completes when the server closes the
     * connection and fails if the connection breaks. Collect it once.
     */
    val messages: Flow<ServerMessage>

    fun send(message: ClientMessage)

    /** Hangs up. The server takes it as leaving. */
    fun close()
}

/** Opens connections, so tests can swap in a fake server. */
fun interface OnlineConnector {
    fun connect(url: String): OnlineConnection
}

/** The real connection, over OkHttp. Messages the app can't read are skipped. */
class OkHttpConnection(client: OkHttpClient, url: String) : OnlineConnection {

    private val incoming = Channel<ServerMessage>(Channel.UNLIMITED)

    private val socket: WebSocket = client.newWebSocket(
        Request.Builder().url(url).build(),
        object : WebSocketListener() {
            override fun onMessage(webSocket: WebSocket, text: String) {
                runCatching { OnlineJson.decodeServer(text) }.onSuccess { incoming.trySend(it) }
            }

            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                webSocket.close(NORMAL_CLOSURE, null)
                incoming.close()
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                incoming.close()
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                incoming.close(t)
            }
        },
    )

    override val messages: Flow<ServerMessage> = incoming.receiveAsFlow()

    override fun send(message: ClientMessage) {
        socket.send(OnlineJson.encode(message))
    }

    override fun close() {
        socket.close(NORMAL_CLOSURE, null)
        incoming.close()
    }

    companion object {
        private const val NORMAL_CLOSURE = 1000

        /** One client for the whole app, as OkHttp recommends. */
        val client: OkHttpClient by lazy {
            OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                // the server pings every 15 seconds; this notices a dead line from our side too
                .pingInterval(15, TimeUnit.SECONDS)
                .build()
        }

        val connector = OnlineConnector { url -> OkHttpConnection(client, url) }
    }
}
