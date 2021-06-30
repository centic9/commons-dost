package org.dstadler.commons.logging.log4j;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

public class Log4jUtils {
	/**
	 * Manually roll over logfiles to start with a new logfile
	 */
	public static void rolloverLogfile() {
		Logger logger = LogManager.getRootLogger();       // NOSONAR - local logger used on purpose here

		// use reflection to not have to link to include log4j-core in this core project
		if ("org.apache.logging.log4j.core.Logger".equals(logger.getClass().getCanonicalName())) {
			try {
				Method getAppenders = logger.getClass().getMethod("getAppenders");

				//noinspection unchecked
				Map<String, Object> map = (Map<String, Object>) getAppenders.invoke(logger);
				for (Object appender : map.values()) {
					// for any RollingFileAppender, trigger "getManager.rollover()"
					if ("org.apache.logging.log4j.core.appender.RollingFileAppender".equals(appender.getClass().getCanonicalName())) {
						Method getManager = appender.getClass().getMethod("getManager");
						Object manager = getManager.invoke(appender);
						Method rollover = manager.getClass().getMethod("rollover");
						rollover.invoke(manager);
					}
				}
			} catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
				logger.log(Level.FATAL, "Error rolling over log-files for Log4j", e);
			}
		}
	}
}
