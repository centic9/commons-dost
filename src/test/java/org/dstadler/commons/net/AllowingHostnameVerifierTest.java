package org.dstadler.commons.net;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;


public class AllowingHostnameVerifierTest {
	@Test
	public void test() {
		assertTrue(new AllowingHostnameVerifier().verify(null, null));
	}

	@Test
	public void instance() throws Exception {
		assertNotNull(AllowingHostnameVerifier.instance());
	}
}
