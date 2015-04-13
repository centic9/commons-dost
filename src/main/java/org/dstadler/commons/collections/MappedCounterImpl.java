package org.dstadler.commons.collections;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Default implementation of a {@link MappedCounter}.
 *
 * @param <T>
 * @author dominik.stadler
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
	public int get(T k) {
		return map.containsKey(k) ? map.get(k) : 0;
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
	public Map<T, Integer> sortedMap() {
		List<Map.Entry<T, Integer>> list = new LinkedList<>(map.entrySet());
		Collections.sort(list, new Comparator<Map.Entry<T, Integer>>() {

			@Override
			public int compare(Map.Entry<T, Integer> o1, Map.Entry<T, Integer> o2) {
				// reverse ordering to get highest values first
				return (-1) * o1.getValue().compareTo(o2.getValue());
			}
		});

		Map<T, Integer> result = new LinkedHashMap<>();
		for (Iterator<Map.Entry<T, Integer>> it = list.iterator(); it.hasNext();) {
			Map.Entry<T, Integer> entry = it.next();
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
