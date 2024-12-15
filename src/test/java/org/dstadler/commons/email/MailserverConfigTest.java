package org.dstadler.commons.email;

import org.dstadler.commons.testing.TestHelpers;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class MailserverConfigTest {
    @Test
    public void test() {
        MailserverConfig config = new MailserverConfig();

        assertEquals("", config.getServerAddress());
        config.setServerAddress("somehost");
        assertEquals("somehost", config.getServerAddress());

        assertEquals(25, config.getServerPort());
        config.setServerPort(834);
        assertEquals(834, config.getServerPort());

        assertEquals("", config.getPassword());
        config.setPassword("somepwd");
        assertEquals("somepwd", config.getPassword());
        config.setPassword(null);
        assertEquals("", config.getPassword());

        assertEquals("", config.getUserId());
        config.setUserId("someuser");
        assertEquals("someuser", config.getUserId());

        assertEquals("", config.getBounce());
        config.setBounce("someuser1");
        assertEquals("someuser1", config.getBounce());

        assertFalse(config.isDebug());
        config.setDebug(true);
        assertTrue(config.isDebug());

        assertEquals("", config.getSubjectPrefix());
        config.setSubjectPrefix("pref");
        assertEquals("pref", config.getSubjectPrefix());

        assertFalse(config.isSSLEnabled());
        config.setSSLEnabled(true);
        assertTrue(config.isSSLEnabled());

        TestHelpers.ToStringTest(config);
        TestHelpers.ToStringTest(new MailserverConfig());
    }
}