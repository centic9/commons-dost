package org.dstadler.commons.collections;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Default implementation of a {@link MappedCounter}.
 *
 * @param <T> The type of the key for the mapped counter, often this
 *           will be String, but any type that can be used as key for
 *           a HashMap will work here.
 */
public class MappedCounterImpl<T> implements MappedCounter<T> {

    // sort on values, highest value first
    private Map<T, Integer> map = new HashMap<>();

    @Override
    public void addInt(T k, int v) {
        if (!map.containsKey(k)) {
            map.put(k, v);
        } else {
            map.put(k, map.get(k) + v);
        }
    }

    @Override
    public void count(Collection<T> items) {
        for(T item : items) {
            addInt(item, 1);
        }
    }

    @Override
    public int get(T k) {
        return map.getOrDefault(k, 0);
    }

    @Override
    public int remove(T key) {
        return map.remove(key);
    }

    @Override
    public Set<T> keys() {
        return map.keySet();
    }

    @Override
    public Set<Map.Entry<T, Integer>> entries() {
        return map.entrySet();
    }

    @Override
    public void clear() {
        map.clear();
    }

    @Override
    public Map<T, Integer> sortedMap() {
        List<Map.Entry<T, Integer>> list = new LinkedList<>(map.entrySet());
        list.sort((o1, o2) -> {
            // reverse ordering to get highest values first
            int ret = (-1) * o1.getValue().compareTo(o2.getValue());
            if (ret != 0) {
                return ret;
            }

            final T key1 = o1.getKey();
            final T key2 = o2.getKey();

            // we use a HashMap which allows null-keys
            if (key1 == null && key2 == null) {
                return 0;
            } else if (key1 == null) {
                return -1;
            } else if (key2 == null) {
                return 1;
            }

            if(key1 instanceof Comparable) {
                //noinspection unchecked
                return ((Comparable)key1).compareTo(key2);
            } else {
                return key1.toString().compareTo(key2.toString());
            }
        });

        Map<T, Integer> result = new LinkedHashMap<>();
        for (Map.Entry<T, Integer> entry : list) {
            result.put(entry.getKey(), entry.getValue());
        }
        return result;
    }

    @Override
    public int sum() {
        int sum = 0;
        for(Integer i : map.values()) {
            sum += i;
        }
        return sum;
    }

    @Override
    public String toString() {
        return map.toString();
    }
}
