package edu.geo4.duke.util;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public class CapacityQueue<E> extends LinkedList<E> {

    private static final long serialVersionUID = 1L;

    private int myCapacity;
    
    public CapacityQueue (int capacity) {
        setCapacity(capacity);
    }

    public synchronized List<E> setCapacity (int capacity) {
        myCapacity = capacity;
        if (myCapacity < 0) { throw new RuntimeException(
                                                         "Queue capcity must be greater than or equal to 0."); }
        List<E> excess = new LinkedList<E>();
        while (size() > myCapacity) {
            excess.add(super.remove());
        }
        return excess;
    }
    
    public synchronized int getCapacity() {
        return myCapacity;
    }

    @Override
    public synchronized boolean add (E o) {
        super.add(o);
        while (size() > myCapacity) {
            super.remove();
        }
        return true;
    }

    @Override
    public synchronized boolean offer (E o) {
        while (size() > myCapacity - 1 && size() >= 0) {
            super.remove();
        }
        return super.offer(o);
    }

    @Override
    public synchronized boolean addAll (Collection<? extends E> c) {
        while (size() > myCapacity - c.size() && size() >= 0) {
            super.remove();
        }
        return super.addAll(c);
    }
}
