package org.dstadler.commons.logging.jdk;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

/**
 * Taken with explicit allowance from Dr Heinz M. Kabutz on 04.04.2007 from
 * http://www.javaspecialists.co.za/archive/newsletter.do?issue=137:
 *
 * Dear Reinhold,
 * please go ahead - at your own risk though :-)  Please acknowledge the source.
 * Be aware that a IoC framework like Spring can give you incorrect results with the loggers.
 * Thanks for asking :-) und viele Gruesse aus Kreta
 * Regards
 *  Heinz
 *  --
 * Dr Heinz M. Kabutz (PhD CompSci)
 * Author of "The Java(tm) Specialists' Newsletter"
 * Sun Java Champion
 * http://www.javaspecialists.eu
 * Tel: +30 69 72 850 460
 * Skype: kabutz
 */
public class LoggerFactory {

	protected LoggerFactory() {
		// Hide c'tor to avoid instantiation
	}

	public static Logger make() {
		Throwable t = new Throwable();	// NOPMD - we are just retrieving the stacktrace here
		StackTraceElement directCaller = t.getStackTrace()[1];
		return Logger.getLogger(directCaller.getClassName());
	}

	/**
	 * Initialize Logging from a file "logging.properties" which needs to be found in the classpath.
	 *
	 * It also applies a default format to make JDK Logging use a more useful format for log messages.
	 *
	 * Note: Call this method at the very first after main
	 *
	 * @throws IOException If the file "logging.properties" is not found in the classpath.
	 * @author dstadler
	 */
	public static void initLogging() throws IOException {
		sendCommonsLogToJDKLog();

		try (InputStream resource = Thread.currentThread().getContextClassLoader().getResourceAsStream("logging.properties")) {
			// apply configuration
			if(resource != null) {
				try {
					LogManager.getLogManager().readConfiguration(resource);
				} finally {
					resource.close();
				}
			}

			// apply a default format to the log handlers here before throwing an exception further down
			Logger log = Logger.getLogger("");    // NOSONAR - local logger used on purpose here
			for (Handler handler : log.getHandlers()) {
				handler.setFormatter(new DefaultFormatter());
			}

			if(resource == null) {
				throw new IOException("Did not find a file 'logging.properties' in the classpath");
			}
		}
	}

	/**
	 * Set a system property which instructs Apache Commons Logging to use JDK Logging
	 * instead of the default Log4J implementation.
	 */
	public static void sendCommonsLogToJDKLog() {
		// set a property which instructs Commons Logging to use JDK Logging instead of Log4j
		System.setProperty("org.apache.commons.logging.Log", "org.apache.commons.logging.impl.Jdk14Logger");
	}

	/**
	 * Resort to reflection to make any FileHandler that is currently active roll-over
	 * the current logfile and start a new one, e.g. this may be useful before a big
	 * batch-job starts to have a clean start of log-entries.
	 *
	 * @throws IllegalStateException If there is a problem calling the rotate-method of the {@link FileHandler}
	 */
    public static void rolloverLogfile() {
        Logger log = Logger.getLogger("");    // NOSONAR - local logger used on purpose here
        for (Handler handler : log.getHandlers()) {
            if(handler instanceof FileHandler) {
                try {
                    Method m = FileHandler.class.getDeclaredMethod("rotate");
                    m.setAccessible(true);
                    if (!Level.OFF.equals(handler.getLevel())) { //Assume not closed.
                            m.invoke(handler);
                    }
                } catch (IllegalAccessException | IllegalArgumentException |
                        InvocationTargetException | NoSuchMethodException | SecurityException e) {
                    throw new IllegalStateException(e);
                }
            }
        }

	}
}
