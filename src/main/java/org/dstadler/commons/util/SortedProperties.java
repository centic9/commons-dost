package org.dstadler.commons.util;

import java.util.Collections;
import java.util.Enumeration;
import java.util.Properties;
import java.util.Vector;

/**
 * Helper class to write Java properties sorted
 *
 * See http://www.rgagnon.com/javadetails/java-0614.html
 *
 * @author dominik.stadler
 *
 */
public class SortedProperties extends Properties {
	private static final long serialVersionUID = 1L;

	/**
	 * Overrides, called by the store method.
	 */
	@Override
	@SuppressWarnings({ "unchecked" })
	public synchronized Enumeration<Object> keys() {
		Enumeration<Object> keysEnum = super.keys();
		@SuppressWarnings("rawtypes")
		Vector keyList = new Vector<>();	// NOPMD - vector used on purpose here...
		while (keysEnum.hasMoreElements()) {
			keyList.add(keysEnum.nextElement());
		}

		Collections.sort(keyList);

		// reverse this list to have the newes items on top
		Collections.reverse(keyList);

		return keyList.elements();
	}
}
