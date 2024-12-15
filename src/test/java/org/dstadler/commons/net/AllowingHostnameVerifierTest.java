package org.dstadler.commons.net;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;


public class AllowingHostnameVerifierTest {
	@Test
	public void test() {
		assertTrue(new AllowingHostnameVerifier().verify(null, null));
	}

	@Test
	public void instance() {
		assertNotNull(AllowingHostnameVerifier.instance());
	}
}
