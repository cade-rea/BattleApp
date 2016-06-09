package com.caderea.battleapp;

import java.util.Collection;
import java.util.Iterator;
import java.util.Queue;

/**
 * Created by Cade on 8/4/2014.
 */
public class BattleQueue implements Queue<BattleAction> {

    private static int MAX_QUEUE_SIZE = 10;

    private BattleAction[] queue;
    private int front;
    private int position;
    private boolean full;

    public BattleQueue() {
        queue = new BattleAction[MAX_QUEUE_SIZE];
        front = 0;
        position = 0;
        full = false;
    }

    private int getPlace() {
        int place = front + position;
        if(place >= MAX_QUEUE_SIZE)
            place -= MAX_QUEUE_SIZE;

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

        queue[position] = battleAction;

        if(++position == MAX_QUEUE_SIZE)
            position = 0;

        if(position == front) {
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

        if(++front == MAX_QUEUE_SIZE)
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
    public boolean isNotEmpty() {
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
        int size = position - front;
        if(size <= 0)
            size += MAX_QUEUE_SIZE;

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
        queue = new BattleAction[MAX_QUEUE_SIZE];
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

    public String toString() {
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < MAX_QUEUE_SIZE; ++i) {
            sb.append("[" + i + "] " + queue[i]);
        }
        return sb.toString();
    }

    public String getName(int index) {
        int target = front + index;

        if (target >= MAX_QUEUE_SIZE)
            target -= MAX_QUEUE_SIZE;

        if (queue[target] != null)
            return queue[target].toString();

        return "empty";
    }

    public BattleAction get(int index) {
        int target = front + index;

        if (target >= MAX_QUEUE_SIZE)
            target -= MAX_QUEUE_SIZE;

        if (queue[target] != null)
            return queue[target];

        return null;
    }

    public int getMAX_QUEUE_SIZE() {
        return MAX_QUEUE_SIZE;
    }
}
