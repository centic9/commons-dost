package org.dstadler.commons.logging.log4j;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.ConsoleAppender;
import org.apache.logging.log4j.core.appender.RollingFileAppender;
import org.apache.logging.log4j.core.appender.rolling.RollingFileManager;
import org.apache.logging.log4j.core.appender.rolling.TriggeringPolicy;
import org.dstadler.commons.testing.PrivateConstructorCoverage;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import java.util.logging.Logger;

public class Log4jUtilsTest {
	@Test
	public void testRolloverLogfile() throws Exception {
		Log4jUtils.rolloverLogfile();

		// ensure some loggers are created
		Logger logger = Logger.getLogger("somelogger");

		// add appender to cover more
		File file = File.createTempFile("log4j", ".log");
		final RollingFileAppender rollingFileAppender = RollingFileAppender.newBuilder().
				withFileName(file.getAbsolutePath()).
				setName("test-roll").
				withFilePattern("*").
				withPolicy(new TriggeringPolicy() {
					@Override
					public void initialize(RollingFileManager manager) {
					}

					@Override
					public boolean isTriggeringEvent(LogEvent logEvent) {
						return true;
					}
				}).
				build();
		assertNotNull(rollingFileAppender);

		((org.apache.logging.log4j.core.Logger)LogManager.getRootLogger()).addAppender(rollingFileAppender);
		((org.apache.logging.log4j.core.Logger)LogManager.getRootLogger()).addAppender(ConsoleAppender.newBuilder().
				setName("test-append").
				build());

		logger.info("Some test log");
		LogManager.getRootLogger().log(Level.WARN, "Some root-log");

		Log4jUtils.rolloverLogfile();

		file.deleteOnExit();
		new File(file.getAbsoluteFile() + ".1").deleteOnExit();
	}

	 // helper method to get coverage of the unused constructor
	 @Test
	 public void testPrivateConstructor() throws Exception {
	 	PrivateConstructorCoverage.executePrivateConstructor(Log4jUtils.class);
	 }
}
