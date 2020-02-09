package org.dstadler.commons.util;

import org.dstadler.commons.testing.PrivateConstructorCoverage;
import org.junit.Test;

public class ClientConstantsTest {
	// helper method to get coverage of the unused constructor
	@Test
	public void testPrivateConstructor() throws Exception {
		PrivateConstructorCoverage.executePrivateConstructor(ClientConstants.class);
	}
}
