package com.caderea.battleapp.server

import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty

/**
 * Starts the PvP server. It listens on `PORT` (8080 by default), as container hosts expect,
 * and `TICK_LENGTH_MS` sets how long a tick lasts.
 */
fun main() {
    val port = System.getenv("PORT")?.toIntOrNull() ?: 8080
    val config = ServerConfig(
        tickLengthMs = System.getenv("TICK_LENGTH_MS")?.toLongOrNull() ?: ServerConfig().tickLengthMs,
    )
    embeddedServer(Netty, port = port, host = "0.0.0.0") { module(config) }.start(wait = true)
}
