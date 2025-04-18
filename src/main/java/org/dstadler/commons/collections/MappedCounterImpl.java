package org.dstadler.commons.collections;

import java.util.Collection;
import java.util.Comparator;
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
    private final Map<T, Long> map = new HashMap<>();

    @Override
    public void add(T k, long v) {
        if (!map.containsKey(k)) {
            map.put(k, v);
        } else {
            map.put(k, map.get(k) + v);
        }
    }

	@Override
	public void inc(T k) {
        add(k, 1);
	}

	@Override
    public void count(Collection<T> items) {
        for(T item : items) {
            add(item, 1);
        }
    }

    @Override
    public long get(T k) {
        return map.getOrDefault(k, 0L);
    }

    @Override
    public long remove(T key) {
		final Long removed = map.remove(key);
		if (removed == null) {
			return 0;
		}

		return removed;
    }

    @Override
    public Set<T> keys() {
        return map.keySet();
    }

    @Override
    public Set<Map.Entry<T, Long>> entries() {
        return map.entrySet();
    }

    @Override
    public void clear() {
        map.clear();
    }

    @Override
    public Map<T, Long> sortedMap() {
        List<Map.Entry<T, Long>> list = new LinkedList<>(map.entrySet());
        list.sort(new CounterComparator<>());

        Map<T, Long> result = new LinkedHashMap<>();
        for (Map.Entry<T, Long> entry : list) {
            result.put(entry.getKey(), entry.getValue());
        }
        return result;
    }

    @Override
    public long sum() {
        long sum = 0;
        for(Long i : map.values()) {
            sum += i;
        }
        return sum;
    }

    @Override
    public String toString() {
        return map.toString();
    }

	protected static class CounterComparator<T> implements Comparator<Map.Entry<T, Long>> {

		@Override
		public int compare(Map.Entry<T, Long> o1, Map.Entry<T, Long> o2) {
			if (o1 == null && o2 == null) {
				return 0;
			} else if (o1 == null) {
				return 1;
			} else if (o2 == null) {
				return -1;
			}

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

			if (key1 instanceof Comparable comparable && key2 instanceof Comparable &&
					// e.g. String.compareTo() expects a String as parameter!
				key1.getClass().isAssignableFrom(key2.getClass())) {
				//noinspection unchecked,rawtypes
				return comparable.compareTo(key2);
			} else {
				return key1.toString().compareTo(key2.toString());
			}
		}
	}
}
