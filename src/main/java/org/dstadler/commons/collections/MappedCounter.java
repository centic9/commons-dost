package org.dstadler.commons.collections;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * Interface for a Collection which simply counts a number
 * of items. This encapsulates all the boilerplate code for deciding
 * if a map already contains the key.
 *
 * @param <T> The type of the key for the mapped counter, often this
 *           will be String, but any type that can be used as key for
 *           a HashMap will work here.
 *
 * @author dominik.stadler
 */
public interface MappedCounter<T> {
	void addInt(T k, int v);

	void count(Collection<T> items);

	int get(T k);

    int remove(T key);

	Set<T> keys();

	Set<Map.Entry<T, Integer>> entries();

	int sum();

	void clear();

	Map<T, Integer> sortedMap();

	@Override
	String toString();
}
