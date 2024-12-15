package org.dstadler.commons.email;

import org.dstadler.commons.testing.TestHelpers;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

public class EmailConfigTest {
    @Test
    public void test() {
        EmailConfig config = new EmailConfig();

        assertEquals("", config.getSubject());
        config.setSubject("testsubject");
        assertEquals("testsubject", config.getSubject());

        assertNull(config.getFrom());
        config.setFrom("testfrom");
        assertEquals("testfrom", config.getFrom());

        assertTrue(config.getTo().isEmpty());
        config.addTo("test@to");
        assertEquals("[test@to]", config.getTo().toString());
        assertEquals("test@to", config.getToAsEmail());
        config.addTo("2nd@to");
        assertEquals("[test@to, 2nd@to]", config.getTo().toString());
        assertEquals("test@to,2nd@to", config.getToAsEmail());

        config.setTo(null);
        assertTrue(config.getTo().isEmpty());
        config.setTo(Collections.singletonList("3rd@to"));
        assertEquals("[3rd@to]", config.getTo().toString());

        assertTrue(config.getCc().isEmpty());
        config.addCc("test@Cc");
        assertEquals("[test@Cc]", config.getCc().toString());
        assertEquals("test@Cc", config.getCcAsEmail());
        config.addCc("2nd@Cc");
        assertEquals("[test@Cc, 2nd@Cc]", config.getCc().toString());
        assertEquals("test@Cc,2nd@Cc", config.getCcAsEmail());

        config.setCc(null);
        assertTrue(config.getCc().isEmpty());
        config.setCc(Collections.singletonList("3rd@Cc"));
        assertEquals("[3rd@Cc]", config.getCc().toString());

        assertTrue(config.getBcc().isEmpty());
        config.addBcc("test@Bcc");
        assertEquals("[test@Bcc]", config.getBcc().toString());
        assertEquals("test@Bcc", config.getBccAsEmail());
        config.addBcc("2nd@Bcc");
        assertEquals("[test@Bcc, 2nd@Bcc]", config.getBcc().toString());
        assertEquals("test@Bcc,2nd@Bcc", config.getBccAsEmail());

        config.setBcc(null);
        assertTrue(config.getBcc().isEmpty());
        config.setBcc(Collections.singletonList("3rd@Bcc"));
        assertEquals("[3rd@Bcc]", config.getBcc().toString());

        assertEquals("test@list", EmailConfig.listToEmail(Collections.singletonList("test@list")));
        assertEquals("", EmailConfig.listToEmail(Collections.singletonList((String)null)));
        assertEquals("", EmailConfig.listToEmail(Collections.singletonList("")));

        TestHelpers.ToStringTest(config);
        TestHelpers.ToStringTest(new EmailConfig());
    }
}
