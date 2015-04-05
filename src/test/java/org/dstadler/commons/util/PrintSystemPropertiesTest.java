package org.dstadler.commons.util;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;

import org.junit.Test;

import org.dstadler.commons.testing.PrivateConstructorCoverage;

public class PrintSystemPropertiesTest {

	@Test
	public void testMain() throws Exception {
		File file = new File("SystemProperties.log");
		// make sure it does not exist
		if(file.exists()) {
			assertTrue(file.delete());
		}
		PrintSystemProperties.main(new String[] {});
		assertTrue(file.exists());
		assertTrue("Could not delete file " + file,
				file.delete());
	}

	// helper method to get coverage of the unused constructor
	@Test
	public void testPrivateConstructor() throws Exception {
		PrintSystemProperties properties = new PrintSystemProperties();
		assertNotNull(properties);
		assertNotNull(properties.toString());

		PrivateConstructorCoverage.executePrivateConstructor(PrintSystemProperties.class);
	}
}
