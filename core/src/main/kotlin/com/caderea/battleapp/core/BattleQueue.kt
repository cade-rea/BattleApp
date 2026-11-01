package com.caderea.battleapp.core

import com.caderea.battleapp.core.action.BattleAction

/**
 * The actions a fighter has lined up, oldest first, holding at most [capacity]. The queue is
 * short on purpose, so each queued action is a commitment.
 *
 * Not thread-safe: whoever owns the battle should drive it from one thread.
 */
class BattleQueue(val capacity: Int = MAX_SIZE) : Iterable<QueuedAction> {

    private val actions = ArrayDeque<QueuedAction>(capacity)
    private var nextId = 0

    val size: Int
        get() = actions.size

    fun isEmpty() = actions.isEmpty()

    fun isFull() = actions.size >= capacity

    /**
     * Adds an action to the back of the queue.
     * @return false if the queue was full
     */
    fun offer(action: BattleAction): Boolean {
        if (isFull()) return false

        actions.addLast(QueuedAction(action, nextId++))
        return true
    }

    fun poll(): QueuedAction? = actions.removeFirstOrNull()

    fun peek(): QueuedAction? = actions.firstOrNull()

    /** Removes the action at the back of the queue, the one queued last. */
    fun removeLast(): QueuedAction? = actions.removeLastOrNull()

    /**
     * Replaces the front of the queue, to count down its wind-up. Does nothing if the
     * queue is empty.
     */
    fun replaceFront(action: QueuedAction) {
        if (actions.isNotEmpty()) actions[0] = action
    }

    /**
     * The action at [index], counted from the front of the queue, or null past the end.
     */
    operator fun get(index: Int): QueuedAction? = actions.getOrNull(index)

    /**
     * How many ticks until the action at [index] lands: its own wind-up plus everything
     * ahead of it. 1 means it lands on the next tick.
     */
    fun ticksUntilLands(index: Int): Int = actions.take(index + 1).sumOf { it.ticksLeft }

    /** How many ticks until everything queued has landed, so a new action could start. */
    val ticksUntilFree: Int
        get() = actions.sumOf { it.ticksLeft }

    fun clear() = actions.clear()

    /**
     * Replaces everything queued with [queued], ids and wind-ups included. For a phone showing
     * a battle the server runs, so its queue matches the server's exactly.
     */
    fun restore(queued: List<QueuedAction>) {
        require(queued.size <= capacity) { "the queue holds at most $capacity: ${queued.size}" }
        actions.clear()
        actions.addAll(queued)
        nextId = maxOf(nextId, (queued.maxOfOrNull { it.id } ?: -1) + 1)
    }

    // iterates front to back without removing anything
    override fun iterator(): Iterator<QueuedAction> = actions.toList().iterator()

    companion object {
        const val MAX_SIZE = 3
    }
}
