package com.caderea.battleapp.data

import com.caderea.battleapp.core.color.Channel
import com.caderea.battleapp.core.fighter.FighterProfile
import java.nio.file.Files

/** A repository in a fresh temporary folder. */
fun emptyRepository(): ProfileRepository = ProfileRepository(Files.createTempDirectory("characters").toFile())

/** A repository in a fresh temporary folder with one character, loaded. */
fun repositoryWith(profile: FighterProfile = FighterProfile.create("Player", Channel.RED)): ProfileRepository =
    emptyRepository().apply { add(profile) }
