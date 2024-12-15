package org.dstadler.commons.util;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;

import org.dstadler.commons.testing.PrivateConstructorCoverage;
import org.junit.jupiter.api.Test;

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
		assertTrue(file.delete(),
				"Could not delete file " + file);
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
