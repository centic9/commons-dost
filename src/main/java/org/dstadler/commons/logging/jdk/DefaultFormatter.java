package org.dstadler.commons.logging.jdk;

import static org.dstadler.commons.util.ClientConstants.*;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.Format;
import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.FastDateFormat;

/**
 * Logs a record with the pattern "YYYY-MM-DD HH:MM:SS LEVEL [CLASS] MESSAGE".
 * Additionally {@link Throwable}s are logged in a separate line.
 */
public class DefaultFormatter extends Formatter {
    private static final Format DATE_TIME_FORMAT = FastDateFormat.getInstance("yyyy-MM-dd HH:mm:ss");
    private static final String LINE_SEPARATOR = System.getProperty("line.separator");

    private static final StringBuilder BUILDER = new StringBuilder();
    private static final Date DATE = new Date();

    private static final int APP_DEFAULT_LENGTH = 7;
    private static final int LEVEL_DEFAULT_LENGTH = 7;
    private static final int CLASS_DEFAULT_LENGTH = 12;

    private static String appId = null;

    /** Allows to provide an application id for cases
     * where multiple applications log to the same output, e.g.
     * in the Launcher output window.
     *
     * @param appId The application id to use in log messages, null to
     *              not log an applicatoin id
     */
	public static void setAppId(String appId) {
		DefaultFormatter.appId = appId;
	}

	/**
     * Format the given LogRecord.
     *
     * @param record the log record to be formatted.
     * @return a formatted log record
     */
    @Override
	public synchronized String format(LogRecord record) {
        // clear string builder content of previous runs
        BUILDER.setLength(0);

        // build log message
        appendDateTime(record);
        appendApplication();
        appendLevel(record);
        appendClass(record);
        appendMessage(record);
        appendThrowable(record);
        appendNewLine();

        return BUILDER.toString();
    }

	private void appendDateTime(LogRecord record) {
        DATE.setTime(record.getMillis());
        BUILDER.append(DATE_TIME_FORMAT.format(DATE));
        BUILDER.append(WS);
    }

    private void appendApplication() {
		if(appId != null) {
			BUILDER.append(appId.substring(0, appId.length() > APP_DEFAULT_LENGTH ? APP_DEFAULT_LENGTH : appId.length()));
	        BUILDER.append(StringUtils.repeat(WS, APP_DEFAULT_LENGTH - appId.length()));
	        BUILDER.append(WS);
		}
	}

    private synchronized void appendLevel(LogRecord record) {
        String levelName = record.getLevel().getName();

        BUILDER.append(levelName);
        BUILDER.append(StringUtils.repeat(WS, LEVEL_DEFAULT_LENGTH - levelName.length()));
        BUILDER.append(WS);
    }

    private void appendClass(LogRecord record) {
        String className = record.getLoggerName();
        if(className == null) {
        	className = "<unknown>";
        }

        int lastdot = className.lastIndexOf(DOT);
        if (lastdot > 0) {
            className = className.substring(lastdot + 1);
        }

        BUILDER.append(LSBRA);
        BUILDER.append(className);
        BUILDER.append(RSBRA);

        BUILDER.append(StringUtils.repeat(WS, CLASS_DEFAULT_LENGTH - className.length()));

        BUILDER.append(WS);
    }

    private void appendMessage(LogRecord record) {
        BUILDER.append(formatMessage(record));
    }

    private void appendThrowable(LogRecord record) {
        // log exception
        Throwable throwable = record.getThrown();
        if (throwable == null) {
            return;
        }

        appendThrowableSource(record);
        appendNewLine();
        appendStackTrace(throwable);
    }

    private void appendThrowableSource(LogRecord record) {
        BUILDER.append(COLON);
        BUILDER.append(WS);

        if (record.getSourceClassName() != null) {
            BUILDER.append(record.getSourceClassName());
        }
        if (record.getSourceMethodName() != null) {
            BUILDER.append(WS);
            BUILDER.append(record.getSourceMethodName());
        }
    }


    private void appendStackTrace(Throwable throwable) {
        try {
            StringWriter sw = new StringWriter();
            try (PrintWriter pw = new PrintWriter(sw)) {
            	throwable.printStackTrace(pw);
            }
            BUILDER.append(sw.toString());
        } catch (Exception ex) { // NOSONAR // NOPMD
            //ok to ignore this
        }
    }

    private void appendNewLine() {
        BUILDER.append(LINE_SEPARATOR);
    }
}

