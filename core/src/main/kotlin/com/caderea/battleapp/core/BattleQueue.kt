package com.caderea.battleapp.core

import com.caderea.battleapp.core.action.BattleAction

/**
 * The actions a fighter has lined up, oldest first, holding at most [capacity].
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

    /**
     * The action at [index], counted from the front of the queue, or null past the end.
     */
    operator fun get(index: Int): QueuedAction? = actions.getOrNull(index)

    fun clear() = actions.clear()

    // iterates front to back without removing anything
    override fun iterator(): Iterator<QueuedAction> = actions.toList().iterator()

    companion object {
        const val MAX_SIZE = 10
    }
}
