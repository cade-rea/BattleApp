package com.caderea.battleapp.online

import com.caderea.battleapp.core.color.Channel
import com.caderea.battleapp.core.fighter.FighterProfile
import com.caderea.battleapp.core.online.ClientMessage
import com.caderea.battleapp.core.online.EndReason
import com.caderea.battleapp.core.online.MatchResult
import com.caderea.battleapp.core.online.ServerMessage
import com.caderea.battleapp.server.ServerConfig
import com.caderea.battleapp.server.module
import io.ktor.server.engine.EmbeddedServer
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.net.ServerSocket

/**
 * The app's real WebSocket client against the real PvP server, run right here on a free
 * port with short ticks: proof the two speak the same protocol end to end.
 */
class OnlineClientTest {

    private lateinit var server: EmbeddedServer<*, *>
    private lateinit var url: String

    @Before
    fun startServer() {
        val port = ServerSocket(0).use { it.localPort }
        server = embeddedServer(Netty, port = port, host = "127.0.0.1") { module(ServerConfig(tickLengthMs = 50)) }
        server.start(wait = false)
        url = "ws://127.0.0.1:$port/play"
    }

    @After
    fun stopServer() {
        server.stop(gracePeriodMillis = 0, timeoutMillis = 1000)
    }

    @Test
    fun twoPhonesPlayAMatch() = runBlocking {
        withTimeout(10_000) {
            val first = OkHttpConnection(OkHttpConnection.client, url)
            val second = OkHttpConnection(OkHttpConnection.client, url)
            // read each phone's messages in order, as the ViewModel does
            val firstIn = first.messages.iterator()
            val secondIn = second.messages.iterator()

            first.send(ClientMessage.Join(FighterProfile.create("Red", Channel.RED), "open"))
            assertEquals(ServerMessage.Waiting("open"), firstIn.next())
            second.send(ClientMessage.Join(FighterProfile.create("Blue", Channel.BLUE), "open"))

            val started = firstIn.until<ServerMessage.MatchStarted>()
            assertEquals("Blue", started.opponent.name)
            secondIn.until<ServerMessage.MatchStarted>()

            // a red strike lands a tick later, and the other phone sees it hit
            first.send(ClientMessage.Queue(0))
            val hit = secondIn.until<ServerMessage.State> { it.landed.isNotEmpty() }
            assertTrue(hit.you.health < 2000)

            second.send(ClientMessage.Leave)
            val over = firstIn.until<ServerMessage.MatchOver>()
            assertEquals(MatchResult.WON, over.result)
            assertEquals(EndReason.FORFEIT, over.reason)
            // and the server hangs up, which ends the stream
            assertEquals(MatchResult.LOST, secondIn.until<ServerMessage.MatchOver>().result)
            assertTrue(second.messages.toList().isEmpty())
        }
    }

    private class Messages(private val flow: Flow<ServerMessage>) {
        suspend fun next(): ServerMessage = flow.first()
    }

    private fun Flow<ServerMessage>.iterator() = Messages(this)

    private suspend inline fun <reified T : ServerMessage> Messages.until(
        crossinline accept: (T) -> Boolean = { true },
    ): T {
        while (true) {
            val message = next()
            if (message is T && accept(message)) return message
        }
    }
}
