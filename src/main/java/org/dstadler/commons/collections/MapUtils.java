package org.dstadler.commons.collections;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;


/**
 * Some helper methods related to Maps, e.g. sorting by value.
 *
 * @author dominik.stadler
 */
public final class MapUtils {
	// private constructor to prevent instantiation of this class
	private MapUtils() {
	}

	/**
	 * Sorts the provided Map by the value instead of by key like TreeMap does.
	 *
	 * The Value-Type needs to implement the Comparable-interface.
	 *
	 * Note: this will require one Map.Entry for each element in the Map, so for very large
	 * Maps this will incur some memory overhead but will not fully duplicate the contents of the map.
	 *
	 * @param map A map with some elements which should be sorted by Value.
	 *
	 * @return Returns a new List of Map.Entry values sorted by value.
	 */
	public static <K, V extends Comparable<V>> List<Entry<K, V>> sortByValue(Map<K, V> map) {
		List<Entry<K, V>> entries = new ArrayList<>(map.entrySet());
		Collections.sort(entries, new ByValue<K, V>());
		return entries;
	}

	// Helper class for sorting maps by value
	private static class ByValue<K, V extends Comparable<V>> implements Comparator<Entry<K, V>>, Serializable {
		private static final long serialVersionUID = 1L;

		@Override
		public int compare(Entry<K, V> o1, Entry<K, V> o2) {
			return o1.getValue().compareTo(o2.getValue());
		}
	}
}
