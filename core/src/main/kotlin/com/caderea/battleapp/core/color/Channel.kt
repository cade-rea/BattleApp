package com.caderea.battleapp.core.color

/**
 * One of the three primaries. Each beats the next in a cycle: red beats green, green beats
 * blue, and blue beats red.
 */
enum class Channel {
    RED,
    GREEN,
    BLUE;

    /** The channel this one beats. */
    val prey: Channel
        get() = when (this) {
            RED -> GREEN
            GREEN -> BLUE
            BLUE -> RED
        }

    /** The channel that beats this one. */
    val predator: Channel
        get() = when (this) {
            RED -> BLUE
            GREEN -> RED
            BLUE -> GREEN
        }
}
