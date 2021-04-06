package org.dstadler.commons.os;

import org.junit.Test;

import java.util.logging.Logger;

public class SignalHandlingTest {
    private static final Logger log = Logger.getLogger(SignalHandling.class.getName());

    @Test
    public void testShutdownHook() {
        SignalHandling.setShutdownHook((a) -> log.info("Had shutdown hook: " + a));
    }
}