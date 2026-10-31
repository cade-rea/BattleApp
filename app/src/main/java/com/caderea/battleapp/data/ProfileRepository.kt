package com.caderea.battleapp.data

import android.content.Context
import com.caderea.battleapp.core.color.Channel
import com.caderea.battleapp.core.fighter.FighterProfile
import com.caderea.battleapp.core.fighter.ProfileJson
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File

/**
 * The player's fighter, kept as one small JSON file on the phone. Screens watch [profile],
 * and every [update] is written straight back.
 *
 * The file is a few kilobytes, so it's read and written on the calling thread. Once PvP
 * exists, the server keeps the real copy and this becomes a cache.
 *
 * @param newProfile makes the fighter used when nothing is saved yet. Character creation
 * will replace this default.
 */
class ProfileRepository(
    private val file: File,
    private val newProfile: () -> FighterProfile = { FighterProfile.create(DEFAULT_NAME, Channel.RED) },
) {

    private val _profile = MutableStateFlow(load())
    val profile: StateFlow<FighterProfile> = _profile.asStateFlow()

    /** Changes the fighter and saves the result. */
    fun update(transform: (FighterProfile) -> FighterProfile): FighterProfile {
        val updated = transform(_profile.value)
        _profile.value = updated
        save(updated)
        return updated
    }

    private fun load(): FighterProfile {
        val saved = runCatching { ProfileJson.decode(file.readText()) }.getOrNull()
        return saved ?: newProfile().also(::save)
    }

    private fun save(profile: FighterProfile) {
        file.parentFile?.mkdirs()
        // write a copy first, so a crash mid-write can't leave half a fighter behind
        val temp = File(file.path + ".tmp")
        temp.writeText(ProfileJson.encode(profile))
        if (!temp.renameTo(file)) {
            file.writeText(temp.readText())
            temp.delete()
        }
    }

    companion object {
        const val DEFAULT_NAME = "Player"
        private const val FILE_NAME = "fighter.json"

        @Volatile
        private var instance: ProfileRepository? = null

        /** The app's one repository. */
        fun get(context: Context): ProfileRepository =
            instance ?: synchronized(this) {
                instance ?: ProfileRepository(File(context.applicationContext.filesDir, FILE_NAME)).also { instance = it }
            }
    }
}
