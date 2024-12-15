package org.dstadler.commons.os;

import java.util.logging.Logger;

import org.junit.jupiter.api.Test;

public class SignalHandlingTest {
    private static final Logger log = Logger.getLogger(SignalHandling.class.getName());

    @Test
    public void testShutdownHook() {
        SignalHandling.setShutdownHook((a) -> log.info("Had shutdown hook: " + a));
    }

	// helper method to get coverage of the unused constructor
	@Test
	public void testPrivateConstructor() throws Exception {
		org.dstadler.commons.testing.PrivateConstructorCoverage.executePrivateConstructor(SignalHandling.class);
	}
}