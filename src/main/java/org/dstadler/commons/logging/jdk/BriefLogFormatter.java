/*
 * http://www.javalobby.org/java/forums/t18515.html
 *
 *
 * handlers=java.util.logging.ConsoleHandler
 * java.util.logging.ConsoleHandler.level=ALL
 * java.util.logging.ConsoleHandler.formatter=logging.BriefLogFormatter
 */
package org.dstadler.commons.logging.jdk;

import java.text.Format;
import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.FastDateFormat;

/**
 * Changes the log format of the jdk-logging to not use a two-line format.
 *
 * Format is [&lt;Level&gt;|&lt;Thread&gt;|&lt;Date&gt;]: &lt;Message&gt;
 *
 * Use as follows in a file logging.properties in the classpath:
 *
 * <code>
handlers=java.util.logging.ConsoleHandler
java.util.logging.ConsoleHandler.level=ALL
java.util.logging.ConsoleHandler.formatter=org.dstadler.commons.logging.jdk.BriefLogFormatter
</code>
 *
 * and set the following system property:
 *  -Djava.util.logging.config.file=logging.properties
 */
public class BriefLogFormatter extends Formatter {

	private static final Format format = FastDateFormat.getInstance("HH:mm:ss");
	private static final String lineSep = System.getProperty("line.separator");

	/**
	 * A Custom format implementation that is designed for brevity.
	 */
	@Override
	public String format(LogRecord record) {
		/*String loggerName = record.getLoggerName();
		if (loggerName == null) {
			loggerName = "root";
		}*/
		// .append(loggerName)
		return "[" +
				StringUtils.repeat(" ", 8 - record.getLevel().toString().length()) +
				record.getLevel() + '|' +
				StringUtils.repeat(" ", 12 - Thread.currentThread().getName().length()) +
				Thread.currentThread().getName() + '|' +
				format.format(new Date(record.getMillis())) +
				"]: " +
				record.getMessage() + ' ' +
				lineSep;
	}
}
