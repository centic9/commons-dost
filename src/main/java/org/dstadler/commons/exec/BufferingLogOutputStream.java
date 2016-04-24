package org.dstadler.commons.exec;

import java.io.IOException;
import java.util.logging.Logger;

import org.apache.commons.exec.LogOutputStream;
import org.dstadler.commons.logging.jdk.LoggerFactory;

/**
 * Implementation of the LogOutputStream from commons-exec which buffers
 * the log somewhat to not flood the output if there are many lines of text printed.
 *
 * It will print out all remaining data upon "close()".
 *
 * @author dominik.stadler
 */
public final class BufferingLogOutputStream extends LogOutputStream {
	private final static Logger log = LoggerFactory.make();

	/**
	 * The max number of characters of output that are collected from the external application before reporting them to our log-system.
	 *
	 * A final flush is done at the end to capture all output.
	 */
	private static final int LOG_FLUSH_LIMIT = 5000;

	private static final int LOG_FLUSH_TIME_LIMIT_SEC = 5;

	/**
	 * Buffer for output from the external application. Constrained by {@link #LOG_FLUSH_LIMIT}
	 */
	private final StringBuilder logBuffer = new StringBuilder();

	private long lastFlush = System.currentTimeMillis();

	@Override
	protected void processLine(String line, int level) {
		synchronized (logBuffer) {
			if(line != null && line.length() > 0) {
				logBuffer.append(line).append("\n");

				if(logBuffer.length() > LOG_FLUSH_LIMIT ||
						lastFlush < (System.currentTimeMillis() - 1000*LOG_FLUSH_TIME_LIMIT_SEC)) {
					log.info(logBuffer.toString());
					logBuffer.setLength(0);
					lastFlush = System.currentTimeMillis();
				}
			}
		}
	}

	/**
	 * Flush any pending data in the {@link #logBuffer}
	 * @throws IOException If closing the stream fails.
	 */
	@Override
	public void close() throws IOException {
		// first close the parent so we get all remaining data
		super.close();

		// then ensure that any remaining buffer is logged
		synchronized (logBuffer) {
			if(logBuffer.length() > 0) {
				log.info(logBuffer.toString());
				logBuffer.setLength(0);
				lastFlush = System.currentTimeMillis();
			}
		}
	}
}
