package org.dstadler.commons.logging.log4j;

import java.io.File;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.RollingFileAppender;
import org.junit.Test;

import org.dstadler.commons.testing.PrivateConstructorCoverage;

public class Log4jUtilsTest {
	@Test
	public void testRolloverLogfile() throws Exception {
		Log4jUtils.rolloverLogfile();

		// add appender to cover more
		Logger.getLogger("somelogger");
		Logger.getRootLogger().addAppender(new ConsoleAppender());
		final RollingFileAppender rollingFileAppender = new RollingFileAppender();
		File file = File.createTempFile("log4j", ".log");
		rollingFileAppender.setFile(file.getAbsolutePath());
		Logger.getRootLogger().addAppender(rollingFileAppender);

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
