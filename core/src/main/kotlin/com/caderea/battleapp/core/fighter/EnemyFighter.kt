package com.caderea.battleapp.core.fighter

import com.caderea.battleapp.core.action.Attack
import com.caderea.battleapp.core.action.Guard
import com.caderea.battleapp.core.action.Heal
import com.caderea.battleapp.core.color.RgbColor

/**
 * The computer opponent's fighter: a blue aura with a strike in its own color, and green to guard and
 * heal with, since green is what beats blue.
 */
class EnemyFighter(color: RgbColor = RgbColor.BLUE) : Fighter(
    name = "Enemy",
    color = color,
    actions = listOf(
        Attack("Strike", color),
        Guard("Green Guard", RgbColor.GREEN),
        Heal("Green Mend", RgbColor.GREEN),
    ),
)
