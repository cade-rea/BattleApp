package com.caderea.battleapp.core.online

import com.caderea.battleapp.core.color.RgbColor
import com.caderea.battleapp.core.fighter.FighterProfile
import com.caderea.battleapp.core.technique.Technique
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/*
 * What the phone and the PvP server say to each other over the WebSocket, one JSON object per
 * text frame. It lives in core so both sides read and write the same format.
 *
 * The server owns the battle: a phone only says which technique it wants to queue (by its
 * button slot) or that it takes back its last one, and the server answers with the state of
 * the battle as it stands. Every message is written from the reader's side, so "you" is
 * always the phone getting it.
 */

/** Messages a phone sends. */
@Serializable
sealed interface ClientMessage {

    /**
     * Looks for a match in [arenaId] with [profile]. The first message on a connection.
     *
     * Until sign-in lands the server takes the profile as sent, after dimming its
     * techniques to the profile's level, and online matches award no XP.
     */
    @Serializable
    @SerialName("join")
    data class Join(val profile: FighterProfile, val arenaId: String) : ClientMessage

    /** Queues the technique in button [slot]. */
    @Serializable
    @SerialName("queue")
    data class Queue(val slot: Int) : ClientMessage

    /** Takes back the technique queued last. */
    @Serializable
    @SerialName("cancel")
    data object Cancel : ClientMessage

    /** Stops looking for a match, or gives up the one being played. */
    @Serializable
    @SerialName("leave")
    data object Leave : ClientMessage
}

/** Messages the server sends. */
@Serializable
sealed interface ServerMessage {

    /** The phone is waiting in matchmaking for an opponent. */
    @Serializable
    @SerialName("waiting")
    data class Waiting(val arenaId: String) : ServerMessage

    /**
     * A match has started. The first [State] follows right away, and then one after every
     * tick, [tickLengthMs] apart.
     */
    @Serializable
    @SerialName("match_started")
    data class MatchStarted(
        val matchId: String,
        val arenaId: String,
        val tickLengthMs: Long,
        val you: FighterInfo,
        val opponent: FighterInfo,
    ) : ServerMessage

    /**
     * The battle as it stands after [tick]. Sent after every tick, with what [landed] on it,
     * and again whenever either fighter queues or cancels, with nothing landed.
     */
    @Serializable
    @SerialName("state")
    data class State(
        val tick: Int,
        val you: FighterState,
        val opponent: FighterState,
        val landed: List<LandedAction> = emptyList(),
    ) : ServerMessage

    /** The match is over. Nothing else is sent about it. */
    @Serializable
    @SerialName("match_over")
    data class MatchOver(val result: MatchResult, val reason: EndReason, val tick: Int) : ServerMessage

    /** A message the server couldn't act on, such as a full queue or an unknown arena. */
    @Serializable
    @SerialName("rejected")
    data class Rejected(val reason: String) : ServerMessage
}

/**
 * A fighter as the match sees them, fixed for the whole match.
 *
 * @param techniques one per button slot, already dimmed to the arena's level cap, so
 * [Technique.toAction] gives the same action the server uses; null for an empty slot
 */
@Serializable
data class FighterInfo(
    val name: String,
    val color: RgbColor,
    val level: Int,
    val maxHealth: Int,
    val techniques: List<Technique?>,
)

/** A fighter's changing state. */
@Serializable
data class FighterState(
    val health: Int,
    val energy: Int,
    /** Front of the queue first. Only the front one winds up. */
    val queue: List<QueuedTechnique>,
    val guard: GuardUp? = null,
)

/**
 * A technique waiting in a queue.
 * @param id counts up per queue, to tell two of the same technique apart
 * @param ticksLeft ticks until it lands if it is at the front
 */
@Serializable
data class QueuedTechnique(val slot: Int, val id: Int, val ticksLeft: Int)

/** A guard that is up: the one in [slot], blocking up to [strength] for [ticksLeft] ticks. */
@Serializable
data class GuardUp(val slot: Int, val strength: Double, val ticksLeft: Int)

/** A technique that landed on the last tick, in the order they landed. */
@Serializable
data class LandedAction(val side: Side, val slot: Int)

@Serializable
enum class Side { YOU, OPPONENT }

@Serializable
enum class MatchResult { WON, LOST, DRAW }

@Serializable
enum class EndReason {
    /** A fighter's health ran out. */
    KNOCKOUT,
    /** A player gave up. */
    FORFEIT,
    /** A player's connection dropped. */
    DISCONNECT,
    /** The match ran out of ticks. */
    TIME,
}

/** The JSON both sides use for [ClientMessage] and [ServerMessage]. */
object OnlineJson {

    // unknown fields are skipped, so an older app can talk to a newer server
    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        classDiscriminator = "type"
    }

    fun encode(message: ClientMessage): String = json.encodeToString(ClientMessage.serializer(), message)

    fun encode(message: ServerMessage): String = json.encodeToString(ServerMessage.serializer(), message)

    fun decodeClient(text: String): ClientMessage = json.decodeFromString(ClientMessage.serializer(), text)

    fun decodeServer(text: String): ServerMessage = json.decodeFromString(ServerMessage.serializer(), text)
}
