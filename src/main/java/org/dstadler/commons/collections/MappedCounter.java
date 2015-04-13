package org.dstadler.commons.collections;

import java.util.Map;
import java.util.Set;

/**
 * Interface for a Collection which simply counts a number
 * of items.
 *
 * @param <T>
 * @author dominik.stadler
 */
public interface MappedCounter<T> {
	void addInt(T k, int v);

	int get(T k);

    int remove(T key);

	Set<T> keys();

	Set<Map.Entry<T, Integer>> entries();

	int sum();

	Map<T, Integer> sortedMap();

	@Override
	String toString();
}
