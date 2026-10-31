package com.caderea.battleapp.core.fighter

import com.caderea.battleapp.core.color.Channel
import com.caderea.battleapp.core.color.RgbColor
import com.caderea.battleapp.core.environment.Environment
import com.caderea.battleapp.core.progression.Aura
import com.caderea.battleapp.core.progression.Levels
import com.caderea.battleapp.core.technique.Technique
import com.caderea.battleapp.core.technique.TechniqueKind
import com.caderea.battleapp.core.technique.TechniqueRoller
import kotlinx.serialization.Serializable

/**
 * A long-lived fighter between battles: their aura (and so their level), every technique
 * they've earned, and the loadout they'd like to bring. A [Fighter] is one of these inside
 * a single battle; [toFighter] makes it.
 *
 * Plain, immutable data, so it can be saved as JSON on the phone now and kept on the PvP
 * server later. Character creation will make one with [create].
 *
 * @param loadout technique ids in button order, up to [Environment.MAX_LOADOUT]. An arena
 * with fewer slots takes the first ones.
 */
@Serializable
data class FighterProfile(
    val name: String,
    val aura: Aura,
    val catalog: List<Technique>,
    val loadout: List<Int> = catalog.take(Environment.MAX_LOADOUT).map { it.id },
    val wins: Int = 0,
    val losses: Int = 0,
) {

    init {
        require(catalog.map { it.id }.toSet().size == catalog.size) { "technique ids must be unique" }
        require(loadout.size <= Environment.MAX_LOADOUT) { "loadout can't hold more than ${Environment.MAX_LOADOUT}" }
        require(loadout.toSet().size == loadout.size) { "a technique can only be in the loadout once" }
        require(loadout.all { id -> catalog.any { it.id == id } }) { "the loadout can only hold known techniques" }
    }

    val level: Int
        get() = aura.level

    /** The id the next technique learned should get. */
    val nextTechniqueId: Int
        get() = (catalog.maxOfOrNull { it.id } ?: 0) + 1

    fun technique(id: Int): Technique? = catalog.firstOrNull { it.id == id }

    /**
     * The techniques brought to an arena with [size] slots: the loadout in order, topped up
     * from the rest of the catalog if the loadout is short.
     */
    fun loadoutFor(size: Int): List<Technique> {
        val chosen = loadout.mapNotNull(::technique).take(size)
        val extra = catalog.filter { it.id !in loadout }.take(size - chosen.size)
        return chosen + extra
    }

    fun withLoadout(ids: List<Int>): FighterProfile = copy(loadout = ids)

    /** Adds a newly earned technique to the catalog. The loadout stays as the player set it. */
    fun learn(technique: Technique): FighterProfile {
        require(technique(technique.id) == null) { "technique ${technique.id} is already known" }
        return copy(catalog = catalog + technique)
    }

    /**
     * The fighter for one battle in [environment]: as many techniques as the arena allows,
     * dimmed to the arena's level cap if it has one. Slots the catalog can't fill stay empty.
     */
    fun toFighter(environment: Environment = Environment()): Fighter {
        val budget = environment.levelCap?.let { cap -> Levels.powerBudget(minOf(level, cap)) } ?: Int.MAX_VALUE
        val actions = loadoutFor(environment.loadoutSize).map { it.toAction(budget) }
        return Fighter(
            name = name,
            color = aura.color,
            actions = actions + List(environment.loadoutSize - actions.size) { null },
        )
    }

    /**
     * This fighter with a loadout picked for them, for the computer opponent or as a
     * starting suggestion: the starter kit's roles (a strike in the base color, a heal, a
     * strike that beats the base color's predator, a guard against it), each filled by the
     * strongest technique that fits the role, then the strongest of the rest.
     */
    fun withAutoLoadout(): FighterProfile {
        val chosen = mutableListOf<Technique>()
        for (role in roles(aura.base)) {
            catalog
                .filter { it !in chosen && it.kind == role.kind && it.color.overlap(role.color) >= ROLE_OVERLAP }
                .maxByOrNull { it.color.power * it.color.overlap(role.color) * it.rolls.potency }
                ?.let(chosen::add)
        }
        chosen += catalog
            .filter { it !in chosen }
            .sortedByDescending { it.color.power * it.rolls.potency }
            .take(Environment.MAX_LOADOUT - chosen.size)
        return copy(loadout = chosen.map { it.id })
    }

    companion object {
        /** How much a technique's mix must share with a role's color to fill that role. */
        const val ROLE_OVERLAP = 0.5
        /** Techniques rolled on top of the starter kit when a fighter is created. */
        const val STARTING_ROLLS = 2

        /**
         * A new level 1 fighter whose aura is the [base] primary, with the starter kit and
         * [extraRolls] rolled techniques. The loadout is the starter kit.
         */
        fun create(
            name: String,
            base: Channel,
            roller: TechniqueRoller = TechniqueRoller(),
            extraRolls: Int = STARTING_ROLLS,
        ): FighterProfile {
            val aura = Aura.start(base)
            val kit = starterKit(base)
            val rolled = List(extraRolls) { index -> roller.roll(aura, id = kit.size + index + 1) }
            return FighterProfile(name, aura, kit + rolled, loadout = kit.map { it.id })
        }

        /**
         * Four full-power pure techniques for a [base] fighter: a strike in their own color,
         * a green heal (green heals best), a strike in the color that beats their predator,
         * and a guard in their predator's color. For red that's Red Strike, Green Mend,
         * Green Strike and Blue Guard.
         */
        fun starterKit(base: Channel): List<Technique> = roles(base).mapIndexed { index, role ->
            Technique(index + 1, role.kind, "${role.channel.displayName} ${role.noun}", role.color)
        }

        private fun roles(base: Channel) = listOf(
            Role(TechniqueKind.ATTACK, base, "Strike"),
            Role(TechniqueKind.HEAL, Channel.GREEN, "Mend"),
            // the base color's prey is the color that beats its predator
            Role(TechniqueKind.ATTACK, base.prey, "Strike"),
            Role(TechniqueKind.GUARD, base.predator, "Guard"),
        )

        private val Channel.pure: RgbColor
            get() = when (this) {
                Channel.RED -> RgbColor.RED
                Channel.GREEN -> RgbColor.GREEN
                Channel.BLUE -> RgbColor.BLUE
            }

        private val Channel.displayName: String
            get() = name.lowercase().replaceFirstChar(Char::uppercase)

        private class Role(val kind: TechniqueKind, val channel: Channel, val noun: String) {
            val color: RgbColor = channel.pure
        }
    }
}
