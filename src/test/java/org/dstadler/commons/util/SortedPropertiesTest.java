package org.dstadler.commons.util;

import static org.junit.Assert.assertEquals;

import java.util.Enumeration;

import org.junit.Test;

public class SortedPropertiesTest {

	@Test
	public void testKeys() {
		SortedProperties properties = new SortedProperties();
		properties.put("2", "sometext");
		properties.put("1", "sometext");
		properties.put("5", "sometext");
		properties.put("4", "sometext");
		properties.put("3", "sometext");

		Enumeration<Object> enumeration = properties.keys();
		Object obj = enumeration.nextElement();
		assertEquals("5", obj);

		obj = enumeration.nextElement();
		assertEquals("4", obj);

		obj = enumeration.nextElement();
		assertEquals("3", obj);

		obj = enumeration.nextElement();
		assertEquals("2", obj);

		obj = enumeration.nextElement();
		assertEquals("1", obj);
	}

	@Test
	public void testKeysSort() {
		SortedProperties properties = new SortedProperties();
		properties.put("a test key", "sometext 5");
		properties.put("some other key", "sometext 4");
		properties.put("next key", "sometext 3");
		properties.put("let's sort this", "sometext 2");
		properties.put("please", "sometext 1");

		Enumeration<Object> enumeration = properties.keys();
		Object obj = enumeration.nextElement();
		assertEquals("some other key", obj);

		obj = enumeration.nextElement();
		assertEquals("please", obj);

		obj = enumeration.nextElement();
		assertEquals("next key", obj);

		obj = enumeration.nextElement();
		assertEquals("let's sort this", obj);

		obj = enumeration.nextElement();
		assertEquals("a test key", obj);
	}
}
