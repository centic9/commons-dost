package org.dstadler.commons.logging.log4j;

import java.util.Enumeration;

import org.apache.log4j.Logger;
import org.apache.log4j.RollingFileAppender;

public class Log4jUtils {
	/**
	 * Manually roll over logfiles to start with a new logfile
	 */
	public static void rolloverLogfile() {
		Logger logger = Logger.getRootLogger();       // NOSONAR - local logger used on purpose here
		@SuppressWarnings("unchecked")
		Enumeration<Object> appenders = logger.getAllAppenders();
		while(appenders.hasMoreElements()) {
			Object obj = appenders.nextElement();
			if(obj instanceof RollingFileAppender) {
				((RollingFileAppender)obj).rollOver();
			}
		}
	}
}
