package org.dstadler.commons.collections;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.dstadler.commons.testing.PrivateConstructorCoverage;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MapUtilsTest {

	@Test
	public void testSortingByValue() {
		HashMap<String, Integer> map = new HashMap<>();
		map.put("One", 1);
		map.put("Two", 2);
		map.put("Three", 3);

		List<Map.Entry<String, Integer>> sorted = MapUtils.sortByValue(map);
		assertEquals("One", sorted.getFirst().getKey(), "First");
		assertEquals("Two", sorted.get(1).getKey(), "Second");
		assertEquals("Three", sorted.get(2).getKey(), "Third");
	}

	@Test
	public void testSortingByValueEqualValue() {
		HashMap<String, Integer> map = new HashMap<>();
		map.put("One", 3);
		map.put("Two", 2);
		map.put("Three", 2);
		map.put("Four", 1);

		List<Map.Entry<String, Integer>> sorted = MapUtils.sortByValue(map);
		assertEquals("Four", sorted.getFirst().getKey());
		assertEquals("Two", sorted.get(1).getKey());
		assertEquals("Three", sorted.get(2).getKey());
		assertEquals("One", sorted.get(3).getKey());
	}

	@Test
	public void testSortingByValueAndKey() {
		HashMap<String, Integer> map = new HashMap<>();
		map.put("One", 1);
		map.put("Two", 2);
		map.put("Three", 3);

		List<Map.Entry<String, Integer>> sorted = MapUtils.sortByValueAndKey(map);
		assertEquals("One", sorted.getFirst().getKey(), "First");
		assertEquals("Two", sorted.get(1).getKey(), "Second");
		assertEquals("Three", sorted.get(2).getKey(), "Third");

		map.put("Threa", 3);
		map.put("Threb", 3);
		map.put("Threc", 3);
		map.put("Thred", 3);
		map.put("Thref", 3);

		sorted = MapUtils.sortByValueAndKey(map);
		assertEquals("One", sorted.getFirst().getKey(), "First");
		assertEquals("Two", sorted.get(1).getKey(), "Second");
		assertEquals("Threa", sorted.get(2).getKey(), "Third");
		assertEquals("Threb", sorted.get(3).getKey(), "Third");
		assertEquals("Threc", sorted.get(4).getKey(), "Third");
		assertEquals("Thred", sorted.get(5).getKey(), "Third");
		assertEquals("Three", sorted.get(6).getKey(), "Third");
		assertEquals("Thref", sorted.get(7).getKey(), "Third");
	}

	@Test
	public void testSortingByValueAndKeyEqualValue() {
		HashMap<String, Integer> map = new HashMap<>();
		map.put("One", 1);
		map.put("Two", 1);
		map.put("Three", 1);

		List<Map.Entry<String, Integer>> sorted = MapUtils.sortByValueAndKey(map);
		assertEquals("One", sorted.getFirst().getKey(), "First");
		assertEquals("Three", sorted.get(1).getKey(), "Second");
		assertEquals("Two", sorted.get(2).getKey(), "Third");
	}

	// helper method to get coverage of the unused constructor
	 @Test
	 public void testPrivateConstructor() throws Exception {
	 	PrivateConstructorCoverage.executePrivateConstructor(MapUtils.class);
	 }
}
