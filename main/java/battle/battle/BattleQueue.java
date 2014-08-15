package battle.battle;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.widget.TextView;

import java.util.Collection;
import java.util.Iterator;
import java.util.Queue;

/**
 * Created by Cade on 8/4/2014.
 */
public class BattleQueue implements Queue<BattleAction>{
    private BattleAction[] queue;
    private int maxSize;
    private int front;
    private int position;
    boolean full;

    public BattleQueue(){
        maxSize = 5;
        queue = new BattleAction[maxSize];
        front = 0;
        position = 0;
        full = false;
    }

    private int getPlace(){
        int place = front + position;
        if(place >= maxSize)
            place -= maxSize;

        return place;
    }

    @Override
    public boolean add(BattleAction battleAction) {
        return offer(battleAction);
    }

    @Override
    public boolean addAll(Collection<? extends BattleAction> battleActions) {
        return false;
    }

    @Override
    public boolean offer(BattleAction battleAction) {
        if(full)
            return false;
        /*int place = position;
        if(place >= maxSize)
            place -= maxSize;*/

        queue[position] = battleAction;

        if(++position == maxSize)
            position = 0;

        if(position == front){
            full = true;
        }

        return true;
    }

    @Override
    public BattleAction remove() {
        return poll();
    }

    @Override
    public BattleAction poll() {
        if(isEmpty())
            return null;

        BattleAction toReturn = queue[front];
        queue[front] = null;

        if(++front == maxSize)
            front = 0;

        full = false;

        return toReturn;
    }

    @Override
    public BattleAction element() {
        return peek();
    }

    @Override
    public BattleAction peek() {
        return queue[front];
    }

    @Override
    public boolean isEmpty() {
        return ((front == position) && !full);
    }

    //possibly more useful than isEmpty for Battle
    public boolean isNotEmpty(){
        return (full || (front != position));
    }

    @Override
    public Iterator<BattleAction> iterator() {
        return new Iterator<BattleAction>() {
            @Override
            public boolean hasNext() {
                return !isEmpty();
            }

            @Override
            public BattleAction next() {
                return poll();
            }

            @Override
            public void remove() {
                //this method doesn't do anything
                return;
            }
        };
    }

    @Override
    public boolean remove(Object o) {
        return false;
    }

    @Override
    public boolean removeAll(Collection<?> objects) {
        return false;
    }

    @Override
    public boolean retainAll(Collection<?> objects) {
        return false;
    }

    @Override
    public int size() {
        int size = position-front;
        if(size <= 0)
            size += maxSize;

        return size;
    }

    @Override
    public Object[] toArray() {
        return queue;
    }

    @Override
    public <T> T[] toArray(T[] ts) {
        return null;
    }

    @Override
    public void clear() {
        queue = new BattleAction[maxSize];
        front = position = 0;
        full = false;
    }

    @Override
    public boolean contains(Object o) {
        return false;
    }

    @Override
    public boolean containsAll(Collection<?> objects) {
        return false;
    }

    public String toString(){
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < maxSize; ++i){
            sb.append("[" + i + "] " + queue[i]);
        }
        return sb.toString();
    }

    public String getName(int index) {
        int target = front + index;

        if (target >= maxSize)
            target -= maxSize;

        if (queue[target] != null)
            return queue[target].toString();

        return "empty";
    }

    public int getMaxSize(){
        return maxSize;
    }
}
