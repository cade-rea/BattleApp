package com.caderea.battleapp.online

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ServerAddressTest {

    @Test
    fun aBareAddressGetsTheDefaults() {
        assertEquals("ws://192.168.1.20:8080/play", ServerAddress.normalize("192.168.1.20"))
        assertEquals("ws://192.168.1.20:8080/play", ServerAddress.normalize("  192.168.1.20 "))
        assertEquals("ws://my-pc.local:8080/play", ServerAddress.normalize("my-pc.local"))
    }

    @Test
    fun whatIsTypedIsKept() {
        assertEquals("ws://10.0.2.2:9000/play", ServerAddress.normalize("10.0.2.2:9000"))
        assertEquals("ws://10.0.2.2:9000/play", ServerAddress.normalize("ws://10.0.2.2:9000/play"))
        assertEquals("ws://host:9000/custom", ServerAddress.normalize("host:9000/custom"))
        // a hosted server has a certificate and the standard port
        assertEquals("wss://battle.example.com/play", ServerAddress.normalize("wss://battle.example.com"))
        assertEquals("wss://battle.example.com/play", ServerAddress.normalize("https://battle.example.com"))
        assertEquals("ws://host:8080/play", ServerAddress.normalize("http://host"))
    }

    @Test
    fun nonsenseIsRefused() {
        assertNull(ServerAddress.normalize(""))
        assertNull(ServerAddress.normalize("two words"))
        assertNull(ServerAddress.normalize("ftp://host"))
        assertNull(ServerAddress.normalize("ws://"))
    }

    @Test
    fun theDefaultIsAlreadyFull() {
        assertEquals(ServerAddress.DEFAULT, ServerAddress.normalize(ServerAddress.DEFAULT))
    }
}
