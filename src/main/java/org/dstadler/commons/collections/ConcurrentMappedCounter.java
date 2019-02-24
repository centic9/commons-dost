package org.dstadler.commons.collections;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * Thread-Safe variant of {@link MappedCounter},
 * currently internally delegates to a {@link MappedCounterImpl}
 *
 * @param <T> The type of the key for the mapped counter, often this
 *           will be String, but any type that can be used as key for
 *           a HashMap will work here.
 */
public class ConcurrentMappedCounter<T> implements MappedCounter<T> {
    // simply delegate to a normal MappedCounter in synchronized blocks for now
    // a better implementation, e.g. by using ConcurrentHashMap can be added later if necessary
    private final MappedCounter<T> counter = new MappedCounterImpl<>();

    @Override
    public void addInt(T k, int v) {
        synchronized (counter) {
            counter.addInt(k, v);
        }
    }

    @Override
    public void count(Collection<T> items) {
        synchronized (counter) {
            counter.count(items);
        }
    }

    @Override
    public int get(T k) {
        synchronized (counter) {
            return counter.get(k);
        }
    }

    @Override
    public int remove(T key) {
        synchronized (counter) {
            return counter.remove(key);
        }
    }

    @Override
    public Set<T> keys() {
        synchronized (counter) {
            return counter.keys();
        }
    }

    @Override
    public Set<Map.Entry<T, Integer>> entries() {
        synchronized (counter) {
            return counter.entries();
        }
    }

    @Override
    public void clear() {
        synchronized (counter) {
            counter.clear();
        }
    }

    @Override
    public Map<T, Integer> sortedMap() {
        synchronized (counter) {
            return counter.sortedMap();
        }
    }

    @Override
    public int sum() {
        synchronized (counter) {
            return counter.sum();
        }
    }

    @Override
    public String toString() {
        return "Concurrent: " + counter.toString();
    }
}
