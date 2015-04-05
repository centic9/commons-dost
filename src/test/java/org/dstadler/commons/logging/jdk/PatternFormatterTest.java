package org.dstadler.commons.logging.jdk;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.ByteArrayInputStream;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.LogRecord;

import org.junit.Test;

import org.dstadler.commons.testing.TestHelpers;

public class PatternFormatterTest {

	@Test
	public void testFormatLogRecord() {
		PatternFormatter formatter = new PatternFormatter();
		formatter.setExceptionPattern("Logger: %LOGGER%, Message %MESSAGE%, Time: %TIME%");

		final LogRecord record = new LogRecord(Level.INFO, "somemessage");
		String str = formatter.format(record);
		TestHelpers.assertContains(str, "somemessage");

		formatter.setTimeFormat("%d");
		str = formatter.format(record);
		TestHelpers.assertContains(str, "somemessage");

		record.setThrown(new Exception("testexception"));
		str = formatter.format(record);
		TestHelpers.assertContains(str, "somemessage"/* not included because of used format, "testexception"*/);

		record.setThrown(new Exception("testexception", new Exception("testinnerexception")));
		str = formatter.format(record);
		TestHelpers.assertContains(str, "somemessage"/* not included because of used format, "testexception"*/);

		record.setThrown(new Exception("testexception", new Exception("testinnerexception", new Exception("testinnerinnerexception"))));
		str = formatter.format(record);
		TestHelpers.assertContains(str, "somemessage"/* not included because of used format, "testexception"*/);

		assertNotNull(formatter.getExceptionPattern());
		assertNotNull(formatter.getLogPattern());
		assertNotNull(formatter.getTimeFormat());
	}

	@Test
	public void testFormatLogRecordEmptyValues() throws Exception {
		LogManager logManager = LogManager.getLogManager();
		String cname = PatternFormatter.class.getName();

		String props = cname + ".timeFormat=dd-MM\n" +
				cname + ".logPattern=hello{3}\n" +
				cname + ".exceptionPattern={4}: {6} someexception\n";

		logManager.readConfiguration(new ByteArrayInputStream(props.getBytes()));

		PatternFormatter formatter = new PatternFormatter();
		formatter.setExceptionPattern("Logger: %LOGGER%, Message %MESSAGE%, Time: %TIME%");

		final LogRecord record = new LogRecord(Level.INFO, "somemessage");
		String str = formatter.format(record);
		TestHelpers.assertContains(str, "somemessage", "hello");
	}

	@Test
	public void testConstructor() throws Exception {
		LogManager manager = LogManager.getLogManager();

		manager.readConfiguration(new ByteArrayInputStream(new byte[] {}));

		String cname = PatternFormatter.class.getName();

		assertNull(manager.getProperty(cname + ".timeFormat"));
		assertNull(manager.getProperty(cname + ".logPattern"));
		assertNull(manager.getProperty(cname + ".exceptionPattern"));

		PatternFormatter formatter = new PatternFormatter();
		assertNotNull(formatter);

		manager.readConfiguration();
	}
}
