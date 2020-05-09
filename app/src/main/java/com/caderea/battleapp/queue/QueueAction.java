package com.caderea.battleapp.queue;

import android.graphics.Color;
import com.caderea.battleapp.action.BattleAction;
import lombok.Getter;
import org.apache.commons.collections4.iterators.LoopingIterator;

import java.util.Arrays;

/**
 * Created by cade-home on 6/12/16.
 */
@Getter
public class QueueAction {
    private static final LoopingIterator<Integer> colorIterator =
            new LoopingIterator<>(Arrays.asList(Color.RED, Color.BLUE, Color.YELLOW, Color.GREEN));

    private final BattleAction battleAction;
    protected final int color;

    public QueueAction(BattleAction battleAction) {
        this.battleAction = battleAction;
        color = colorIterator.next();
    }
}
