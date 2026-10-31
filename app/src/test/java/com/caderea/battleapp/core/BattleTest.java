package com.caderea.battleapp.core;

import com.caderea.battleapp.core.action.BattleAction;
import com.caderea.battleapp.core.fighter.Fighter;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Drives the battle one tick at a time, without the clock or the battle thread.
 */
public class BattleTest {

    private static final int ATTACK = 0;
    private static final int HEAL = 1;
    // a fight should be over well before this many ticks
    private static final int TICK_LIMIT = 100;

    private RecordingListener listener;
    private Battle battle;

    @Before
    public void setUp() {
        listener = new RecordingListener();
        battle = new Battle(listener);
    }

    @Test
    public void constructorHandsTheQueuesToTheListener() {
        assertSame(battle.getPlayer().getQueue(), listener.playerQueue);
        assertSame(battle.getEnemy().getQueue(), listener.enemyQueue);
    }

    @Test
    public void queuedAttackDamagesTheEnemy() {
        battle.queueAction(ATTACK);
        battle.tick();

        assertEquals(9, battle.getEnemy().getHealth());
        assertTrue(listener.messages.contains("Default Fighter attacks Enemy for 1 damage."));
    }

    @Test
    public void healingCannotGoAboveMaxHealth() {
        battle.queueAction(HEAL);
        battle.tick();

        Fighter player = battle.getPlayer();
        assertEquals(player.getMaxHealth(), player.getHealth());
    }

    @Test
    public void healingRestoresLostHealth() {
        Fighter player = battle.getPlayer();
        player.setHealth(5);

        battle.queueAction(HEAL);
        battle.tick();

        assertEquals(6, player.getHealth());
    }

    @Test
    public void emptyAndMissingButtonsQueueNothing() {
        battle.queueAction(2);
        battle.queueAction(3);
        battle.queueAction(-1);
        battle.queueAction(7);

        assertTrue(battle.getPlayer().getQueue().isEmpty());
    }

    @Test
    public void enemyAttacksEveryOtherTick() {
        for (int i = 0; i < 4; ++i) {
            battle.tick();
        }

        // the enemy queues an attack on ticks 2 and 4 and performs it on the following tick
        assertEquals(9, battle.getPlayer().getHealth());
        battle.tick();
        assertEquals(8, battle.getPlayer().getHealth());
    }

    @Test
    public void idlePlayerLosesAFullFight() {
        int ticks = runUntilOver();

        assertTrue(battle.isOver());
        assertTrue(ticks < TICK_LIMIT);
        assertEquals(0, battle.getPlayer().getHealth());
        assertEquals("Default Fighter has died.", lastMessage());
    }

    @Test
    public void attackingPlayerWinsAFullFight() {
        int ticks = 0;
        while (!battle.isOver() && ticks < TICK_LIMIT) {
            battle.queueAction(ATTACK);
            battle.tick();
            ++ticks;
        }

        assertTrue(battle.isOver());
        assertEquals(0, battle.getEnemy().getHealth());
        assertTrue(battle.getPlayer().getHealth() > 0);
        assertEquals("Enemy has died.", lastMessage());
    }

    @Test
    public void deadFighterDoesNotActAfterDying() {
        battle.getEnemy().setHealth(1);
        battle.queueAction(ATTACK);
        // give the enemy an attack it would perform this tick if it were still alive
        battle.getEnemy().addAttackToQueue();

        battle.tick();

        assertTrue(battle.isOver());
        assertEquals(10, battle.getPlayer().getHealth());
    }

    @Test
    public void stopEndsTheBattle() {
        assertFalse(battle.isOver());

        battle.stop();

        assertTrue(battle.isOver());
    }

    private int runUntilOver() {
        int ticks = 0;
        while (!battle.isOver() && ticks < TICK_LIMIT) {
            battle.tick();
            ++ticks;
        }
        return ticks;
    }

    private String lastMessage() {
        return listener.messages.get(listener.messages.size() - 1);
    }

    private static class RecordingListener implements BattleListener {
        final List<String> messages = new ArrayList<>();
        BattleQueue playerQueue;
        BattleQueue enemyQueue;

        @Override
        public void setQueues(BattleQueue playerQueue, BattleQueue enemyQueue) {
            this.playerQueue = playerQueue;
            this.enemyQueue = enemyQueue;
        }

        @Override
        public void updateButtons(BattleAction[] actions) {
        }

        @Override
        public void refreshQueues() {
        }

        @Override
        public void updateTickProgress(int progress) {
        }

        @Override
        public void update(String message) {
            messages.add(message);
        }

        @Override
        public void notifyDone() {
        }
    }
}
