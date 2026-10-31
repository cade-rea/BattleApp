package com.caderea.battleapp.data

import android.content.Context
import com.caderea.battleapp.core.fighter.FighterProfile
import com.caderea.battleapp.core.fighter.ProfileJson
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File
import java.util.UUID

/** One saved character and the id its file is kept under. */
data class SavedProfile(val id: String, val profile: FighterProfile)

/**
 * Every character on the phone, one small JSON file each in [directory], plus a note of
 * which one is loaded. Screens watch [roster] and [active], and every change is written
 * straight back.
 *
 * Only the [active] character fights and earns XP; [select] switches. With no characters at
 * all, [active] is null and the menu sends the player to character creation.
 *
 * The files are a few kilobytes, so they're read and written on the calling thread. Once
 * PvP exists, the server keeps the real copies and this becomes a cache.
 *
 * @param legacyFile where the app kept its single fighter before multiple characters. If it
 * exists and no characters do, it's moved in as the first one.
 */
class ProfileRepository(
    private val directory: File,
    private val legacyFile: File? = null,
) {

    private val activeFile = File(directory, ACTIVE_FILE)

    private val _roster = MutableStateFlow(load())
    /** Every character, oldest first. */
    val roster: StateFlow<List<SavedProfile>> = _roster.asStateFlow()

    private val _activeId = MutableStateFlow(loadActiveId())
    val activeId: StateFlow<String?> = _activeId.asStateFlow()

    private val _active = MutableStateFlow(findActive())
    /** The loaded character, or null if there are none. */
    val active: StateFlow<FighterProfile?> = _active.asStateFlow()

    /** The loaded character, for screens that only open once there is one. */
    fun requireActive(): FighterProfile = checkNotNull(_active.value) { "no character is loaded" }

    /** Saves a new character and loads it. Returns its id. */
    fun add(profile: FighterProfile): String {
        val id = UUID.randomUUID().toString()
        writeAtomically(fileFor(id), ProfileJson.encode(profile))
        _roster.value = _roster.value + SavedProfile(id, profile)
        saveOrder(_roster.value)
        select(id)
        return id
    }

    /** Loads the character with [id]. Unknown ids are ignored. */
    fun select(id: String) {
        if (_roster.value.none { it.id == id }) return
        writeAtomically(activeFile, id)
        _activeId.value = id
        _active.value = findActive()
    }

    /**
     * Deletes the character with [id] for good. If it was loaded, the oldest remaining
     * character is loaded instead, or none if it was the last.
     */
    fun delete(id: String) {
        if (_roster.value.none { it.id == id }) return
        fileFor(id).delete()
        _roster.value = _roster.value.filterNot { it.id == id }
        saveOrder(_roster.value)
        if (_activeId.value == id) {
            val next = _roster.value.firstOrNull()?.id
            if (next != null) select(next) else {
                activeFile.delete()
                _activeId.value = null
                _active.value = null
            }
        }
    }

    /** Changes the loaded character and saves the result. */
    fun update(transform: (FighterProfile) -> FighterProfile): FighterProfile {
        val id = checkNotNull(_activeId.value) { "no character is loaded" }
        val updated = transform(requireActive())
        writeAtomically(fileFor(id), ProfileJson.encode(updated))
        _roster.value = _roster.value.map { if (it.id == id) SavedProfile(id, updated) else it }
        _active.value = updated
        return updated
    }

    private fun fileFor(id: String) = File(directory, "$id$PROFILE_SUFFIX")

    private fun load(): List<SavedProfile> {
        directory.mkdirs()
        migrateLegacy()
        return directory.listFiles { file -> file.name.endsWith(PROFILE_SUFFIX) }
            .orEmpty()
            .sortedWith(compareBy<File> { it.lastModified() }.thenBy { it.name })
            .mapNotNull { file ->
                // a damaged file is skipped rather than taking the other characters down with it
                runCatching { SavedProfile(file.name.removeSuffix(PROFILE_SUFFIX), ProfileJson.decode(file.readText())) }
                    .getOrNull()
            }
            .let { profiles -> order(profiles) }
    }

    private fun loadActiveId(): String? {
        val saved = runCatching { activeFile.readText().trim() }.getOrNull()
        return saved?.takeIf { id -> _roster.value.any { it.id == id } } ?: _roster.value.firstOrNull()?.id
    }

    private fun findActive(): FighterProfile? = _roster.value.firstOrNull { it.id == _activeId.value }?.profile

    /** Keeps the roster in the order characters were made, which file times can't promise. */
    private fun order(profiles: List<SavedProfile>): List<SavedProfile> {
        val saved = runCatching { File(directory, ORDER_FILE).readLines() }.getOrDefault(emptyList())
        val known = saved.mapNotNull { id -> profiles.firstOrNull { it.id == id } }
        val ordered = known + profiles.filter { it !in known }
        saveOrder(ordered)
        return ordered
    }

    private fun saveOrder(profiles: List<SavedProfile>) =
        writeAtomically(File(directory, ORDER_FILE), profiles.joinToString("\n") { it.id })

    private fun migrateLegacy() {
        val legacy = legacyFile?.takeIf { it.exists() } ?: return
        val hasProfiles = directory.listFiles { file -> file.name.endsWith(PROFILE_SUFFIX) }.orEmpty().isNotEmpty()
        val profile = runCatching { ProfileJson.decode(legacy.readText()) }.getOrNull()
        if (!hasProfiles && profile != null) {
            writeAtomically(fileFor(UUID.randomUUID().toString()), ProfileJson.encode(profile))
        }
        legacy.delete()
    }

    /** Writes a copy first, so a crash mid-write can't leave half a file behind. */
    private fun writeAtomically(file: File, text: String) {
        file.parentFile?.mkdirs()
        val temp = File(file.path + ".tmp")
        temp.writeText(text)
        if (!temp.renameTo(file)) {
            file.writeText(text)
            temp.delete()
        }
    }

    companion object {
        private const val DIRECTORY = "characters"
        private const val LEGACY_FILE = "fighter.json"
        private const val ACTIVE_FILE = "active"
        private const val ORDER_FILE = "order"
        private const val PROFILE_SUFFIX = ".json"

        @Volatile
        private var instance: ProfileRepository? = null

        /** The app's one repository. */
        fun get(context: Context): ProfileRepository =
            instance ?: synchronized(this) {
                instance ?: context.applicationContext.filesDir.let { dir ->
                    ProfileRepository(File(dir, DIRECTORY), legacyFile = File(dir, LEGACY_FILE))
                }.also { instance = it }
            }
    }
}
