package org.dstadler.commons.collections;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import org.dstadler.commons.testing.PrivateConstructorCoverage;

/**
 *
 * @author dominik.stadler
 */
public class MapUtilsTest {

	@Test
	public void testSorting() {
		HashMap<String, Integer> map = new HashMap<>();
		map.put("One", 1);
		map.put("Two", 2);
		map.put("Three", 3);

		List<Map.Entry<String, Integer>> sorted = MapUtils.sortByValue(map);
		assertEquals("First", "One", sorted.get(0).getKey());
		assertEquals("Second", "Two", sorted.get(1).getKey());
		assertEquals("Third", "Three", sorted.get(2).getKey());
	}

	// helper method to get coverage of the unused constructor
	 @Test
	 public void testPrivateConstructor() throws Exception {
	 	PrivateConstructorCoverage.executePrivateConstructor(MapUtils.class);
	 }
}
