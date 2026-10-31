package com.caderea.battleapp.core.fighter

import kotlinx.serialization.json.Json

/**
 * The saved form of a [FighterProfile]. Kept in core so the phone and the server read and
 * write the same format. Unknown fields are skipped, so an older app can still read a
 * profile saved by a newer one.
 */
object ProfileJson {

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    fun encode(profile: FighterProfile): String = json.encodeToString(FighterProfile.serializer(), profile)

    fun decode(text: String): FighterProfile = json.decodeFromString(FighterProfile.serializer(), text)
}
