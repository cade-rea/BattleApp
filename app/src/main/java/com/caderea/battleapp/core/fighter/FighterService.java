package com.caderea.battleapp.core.fighter;

import android.util.Log;
import com.caderea.battleapp.core.BattleConstants;
import com.caderea.battleapp.view.battle.queue.QueueAction;

public class FighterService {
    public void queueAction(Fighter fighter, int actionIndex) {
        Log.d(BattleConstants.TAG_BATTLE, "queing action" + actionIndex + "::" + fighter.getActions()[actionIndex]);

        if (fighter.getActions()[actionIndex] != null) {
            fighter.getQueue().offer(new QueueAction(fighter.getActions()[actionIndex]));
        }
    }
}
