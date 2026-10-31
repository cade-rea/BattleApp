package com.caderea.battleapp.core.environment

import com.caderea.battleapp.core.color.Channel
import com.caderea.battleapp.core.color.RgbColor
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class LightTest {

    private val magenta = RgbColor.fromHex("#800080")

    @Test
    fun plainLightChangesNothing() {
        assertEquals(1.0, Light.PLAIN.multiplier(RgbColor.RED))
        assertEquals(1.0, Light.PLAIN.multiplier(magenta))
        assertTrue(Light.PLAIN.isPlain)
        assertNull(Light.PLAIN.favored)
    }

    @Test
    fun tintedLightHelpsItsChannelByShare() {
        val red = Light.tinted(Channel.RED)

        assertEquals(1.1, red.multiplier(RgbColor.RED), 1e-9)
        // magenta is half red, so it gets half the bonus
        assertEquals(1.05, red.multiplier(magenta), 1e-9)
        assertEquals(1.0, red.multiplier(RgbColor.GREEN), 1e-9)
        assertEquals(Channel.RED, red.favored)
    }

    @Test
    fun dimLightScalesEverything() {
        val dim = Light.dim()

        assertEquals(0.7, dim.multiplier(RgbColor.BLUE), 1e-9)
        assertEquals(0.7, dim.multiplier(magenta), 1e-9)
        // no channel stands out in even light
        assertNull(dim.favored)
        assertNull(dim.muted)
    }

    @Test
    fun aVeilHoldsOneChannelBack() {
        val veil = Light.veiled(Channel.RED)

        assertEquals(0.5, veil.multiplier(RgbColor.RED), 1e-9)
        assertEquals(0.75, veil.multiplier(magenta), 1e-9)
        assertEquals(1.0, veil.multiplier(RgbColor.BLUE), 1e-9)
        assertEquals(Channel.RED, veil.muted)
    }

    @Test
    fun blackIsUnaffected() {
        assertEquals(1.0, Light.veiled(Channel.RED).multiplier(RgbColor.BLACK))
    }
}
