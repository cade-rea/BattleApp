package com.caderea.battleapp.core;

import com.caderea.battleapp.core.action.BattleAction;

/**
 * Receives updates from a running Battle.
 *
 * Most methods are called from the battle thread, so implementations that touch
 * views must hand the work to the UI thread themselves.
 */
public interface BattleListener {
    void setQueues(BattleQueue playerQueue, BattleQueue enemyQueue);

    void updateButtons(BattleAction[] actions);

    void refreshQueues();

    void updateTickProgress(int progress);

    void update(String message);

    void notifyDone();
}
