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
 */
public interface MappedCounter<T> {
	/**
	 * Add the given amount to the given key
	 *
	 * @param k The key for which to add a value
	 * @param v The amount to add
	 */
	void add(T k, long v);

	/**
	 * Add all counts of the given MappedCounter to
	 * this instance of the counter.
	 *
	 * @param counter The counts that should be added.
	 */
	default void addAll(MappedCounter<T> counter) {
		for (Map.Entry<T, Long> o : counter.sortedMap().entrySet()) {
			add(o.getKey(), o.getValue());
		}
	}

	/**
	 * Increase the value for the given key by one.
	 *
	 * @param k The key for which to increment the value
	 */
	void inc(T k);

	/**
	 * Add one for each item in the collection.
	 *
	 * @param items The collection of items to add.
	 */
	void count(Collection<T> items);

	/**
	 * Get the current value for the given key.
	 *
	 * @param k The key to look for.
	 *
	 * @return The current count for the key, 0 if no call was made with that key yet.
	 */
	long get(T k);

	/**
	 * Remove the given key and return the value that was associated with it.
	 *
	 * @param key The key to remove.
	 *
	 * @return The value that was assigned to this key
	 */
    long remove(T key);

	/**
	 * @return The set of keys that are currently stored in the counter
	 */
	Set<T> keys();

	/**
	 * @return The unsorted keys and their assigned count.
	 */
	Set<Map.Entry<T, Long>> entries();

	/**
	 * @return The sum of all values of all currently held keys.
	 */
	long sum();

	/**
	 * Remove all keys.
	 */
	void clear();

	/**
	 * Return a sorted Map of keys and their current count.
	 *
	 * Sorting is done first by count descending, if the count is
	 * equal, sorting is done by key. If the key is a Comparable
	 * {@link Comparable#compareTo(Object)} is used, otherwise
	 * comparing is done on the string-value of the key.
	 *
	 * @return A sorted map.
	 */
	Map<T, Long> sortedMap();

	/**
	 * @return A string-representation of all keys and their
	 * 	 * associated counts.
	 */
	@Override
	String toString();
}
