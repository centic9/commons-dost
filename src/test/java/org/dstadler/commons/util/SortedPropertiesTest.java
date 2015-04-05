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
}
