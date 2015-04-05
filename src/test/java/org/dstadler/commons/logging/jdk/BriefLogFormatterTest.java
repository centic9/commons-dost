package org.dstadler.commons.logging.jdk;

import static org.junit.Assert.assertNotNull;

import java.util.logging.Level;
import java.util.logging.LogRecord;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

import org.dstadler.commons.testing.TestHelpers;

public class BriefLogFormatterTest {
	private final static String threadName = StringUtils.repeat(" ", 12 - Thread.currentThread().getName().length()) + Thread.currentThread().getName();


	@Test
	public void testFormatLogRecord() {
		BriefLogFormatter formatter = new BriefLogFormatter();
		String str = formatter.format(new LogRecord(Level.INFO, "somemessage"));
		assertNotNull(str);
		TestHelpers.assertContains(str, "INFO|" + threadName + "|", "]: somemessage");

		LogRecord record = new LogRecord(Level.INFO, "somemessage");
		record.setLoggerName("somelogger");
		str = formatter.format(record);
		assertNotNull(str);
		TestHelpers.assertContains(str, "INFO|" + threadName + "|", "]: somemessage");

		record = new LogRecord(Level.INFO, "somemessage");
		record.setLoggerName(null);
		str = formatter.format(record);
		assertNotNull(str);
		TestHelpers.assertContains(str, "INFO|" + threadName + "|", "]: somemessage");
	}
}
