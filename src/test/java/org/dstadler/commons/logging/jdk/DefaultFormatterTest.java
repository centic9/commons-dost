package org.dstadler.commons.logging.jdk;

import static org.junit.Assert.assertNotNull;

import java.io.PrintWriter;
import java.util.logging.Level;
import java.util.logging.LogRecord;

import org.junit.Test;


public class DefaultFormatterTest {

	@Test
	public void testFormatLogRecord() {
		DefaultFormatter formatter = new DefaultFormatter();

		DefaultFormatter.setAppId("newapp");

		LogRecord record = new LogRecord(Level.INFO, "somemessage");
		record.setLoggerName("logger");
		assertNotNull(formatter.format(record));

		DefaultFormatter.setAppId("newappwithmorelengthwhichiscutofflater");

		record = new LogRecord(Level.INFO, "somemessage");
		record.setLoggerName("logger");
		assertNotNull(formatter.format(record));

		// use classname with packagename
		record = new LogRecord(Level.INFO, "somemessage");
		record.setLoggerName("com.dstadler.logger");
		assertNotNull(formatter.format(record));

		record = new LogRecord(Level.INFO, "somemessage");
		record.setLoggerName("logger");
		record.setThrown(new Exception());
		assertNotNull(formatter.format(record));

		record = new LogRecord(Level.INFO, "somemessage");
		record.setLoggerName("logger");
		record.setThrown(new Exception());
		record.setSourceClassName("source");
		record.setSourceMethodName("sourcemethod");
		assertNotNull(formatter.format(record));
	}

	@Test
	public void testNullAppId() {
		DefaultFormatter formatter = new DefaultFormatter();

		DefaultFormatter.setAppId(null);

		LogRecord record = new LogRecord(Level.INFO, "somemessage");
		record.setLoggerName("logger");
		assertNotNull(formatter.format(record));
	}

	@Test
	public void testThrowableThrowsInAppendStackTrace() {
		DefaultFormatter formatter = new DefaultFormatter();

		LogRecord record = new LogRecord(Level.INFO, "somemessage");
		record = new LogRecord(Level.INFO, "somemessage");
		record.setLoggerName("logger");
		record.setThrown(new Exception() {
			private static final long serialVersionUID = 1L;

			@Override
			public void printStackTrace(PrintWriter s) {
				throw new IllegalStateException("testexception");
			}
		});
		assertNotNull(formatter.format(record));
	}
}
