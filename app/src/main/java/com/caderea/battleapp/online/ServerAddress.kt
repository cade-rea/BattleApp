package com.caderea.battleapp.online

import android.content.Context

/**
 * Where the PvP server is: the hosted one by default, or one run on a computer for testing,
 * typed in as anything from `192.168.1.20` to `ws://192.168.1.20:8080/play`; the missing
 * parts are filled in.
 */
object ServerAddress {

    /** The hosted server on Fly.io (see fly.toml). */
    const val DEFAULT = "wss://battleapp-pvp.fly.dev/play"

    const val DEFAULT_PORT = 8080
    const val PATH = "/play"

    /** [text] as a full WebSocket URL, or null if it can't be one. */
    fun normalize(text: String): String? {
        var address = text.trim()
        if (address.isEmpty() || address.any { it.isWhitespace() }) return null

        address = when {
            address.startsWith("ws://") || address.startsWith("wss://") -> address
            address.startsWith("http://") -> "ws://" + address.removePrefix("http://")
            address.startsWith("https://") -> "wss://" + address.removePrefix("https://")
            "://" in address -> return null
            else -> "ws://$address"
        }

        val scheme = address.substringBefore("://")
        val rest = address.substringAfter("://")
        val hostAndPort = rest.substringBefore('/')
        if (hostAndPort.isEmpty()) return null
        val path = rest.substring(hostAndPort.length).ifEmpty { PATH }

        // a plain ws:// address without a port is a dev server on the default port
        val withPort = if (':' in hostAndPort || scheme == "wss") hostAndPort else "$hostAndPort:$DEFAULT_PORT"
        return "$scheme://$withPort$path"
    }

    private const val PREFS = "online"
    private const val KEY = "server"

    fun load(context: Context): String =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(KEY, null) ?: DEFAULT

    fun save(context: Context, address: String) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putString(KEY, address).apply()
    }
}
