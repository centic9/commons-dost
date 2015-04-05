package org.dstadler.commons.util;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import org.dstadler.commons.testing.PrivateConstructorCoverage;

public class ClientConstantsTest {
	@Test
	public void testConstants() {
		assertNotNull(ClientConstants.BROWSER_CHOICES);
	}

	// helper method to get coverage of the unused constructor
	@Test
	public void testPrivateConstructor() throws Exception {
		PrivateConstructorCoverage.executePrivateConstructor(ClientConstants.class);
	}
}
