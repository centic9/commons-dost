package org.dstadler.commons.exec;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.time.Duration;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteException;
import org.apache.commons.exec.ExecuteWatchdog;
import org.apache.commons.exec.Executor;
import org.apache.commons.exec.PumpStreamHandler;
import org.dstadler.commons.arrays.ArrayUtils;
import org.dstadler.commons.logging.jdk.LoggerFactory;

/**
 * Helper class which provides convenience support for execution of commandline processes via commons-exec.
 *
 * Use getCommandResult() for processes where the output is small as they will store the output in
 * memory. Use getCommandResultIntoStream() in order to properly stream out potentially large
 * amounts of output from the executable.
 */
public class ExecutionHelper {
	private final static Logger log = LoggerFactory.make();

	/**
	 * Run the given commandline in the given directory and verify that the tool
	 * has the expected exit code and does finish in the timeout.
	 *
	 * Note: The resulting output is stored in memory, running a command
	 * which prints out a huge amount of data to stdout or stderr will
	 * cause memory problems.
	 *
     * @param cmdLine The commandline object filled with the executable and command line arguments
     * @param dir The working directory for the command
     * @param expectedExit The expected exit value or -1 to not fail on any exit value
	 * @param timeoutMillis The timeout in milliseconds or ExecuteWatchdog.INFINITE_TIMEOUT
	 * @return An InputStream which provides the output of the command.
     *
     * @throws IOException Execution of sub-process failed or the
     *          sub-process returned an exit value indicating a failure
	 */
	public static InputStream getCommandResult(
			CommandLine cmdLine, File dir, int expectedExit,
			long timeoutMillis) throws IOException {
		return getCommandResult(cmdLine, dir, expectedExit, timeoutMillis, null);
	}

	/**
	 * Run the given commandline in the given directory and provide the given input to the command.
	 * Also verify that the tool has the expected exit code and does finish in the timeout.
	 *
	 * Note: The resulting output is stored in memory, running a command
	 * which prints out a huge amount of data to stdout or stderr will
	 * cause memory problems.
	 *
	 * @param cmdLine The commandline object filled with the executable and command line arguments
	 * @param dir The working directory for the command
	 * @param expectedExit The expected exit value or -1 to not fail on any exit value
	 * @param timeoutMillis The timeout in milliseconds or ExecuteWatchdog.INFINITE_TIMEOUT
	 * @param input Input for the command-execution
	 * @return An InputStream which provides the output of the command.
     *
     * @throws IOException Execution of sub-process failed or the
     *          sub-process returned an exit value indicating a failure
	 */
	public static InputStream getCommandResult(
			CommandLine cmdLine, File dir, int expectedExit,
			long timeoutMillis, InputStream input) throws IOException {
		Executor executor = getDefaultExecutor(dir, expectedExit, timeoutMillis);

		try (ByteArrayOutputStream outStr = new ByteArrayOutputStream()) {
			executor.setStreamHandler(new PumpStreamHandler(outStr, outStr, input));
			try {
				execute(cmdLine, dir, executor, null);

				return new ByteArrayInputStream(outStr.toByteArray());
			} catch (IOException e) {
				if (outStr.size() > 0) {
					log.warning("Had output before error: \n" + outStr + "\n\nException: " + e);
				}
				throw new IOException(e);
			}
		}
	}

	/**
	 * Run the given commandline in the given directory and verify that the tool
	 * has the expected exit code and does finish in the timeout.
	 *
     * @param cmdLine The commandline object filled with the executable and command line arguments
     * @param dir The working directory for the command
     * @param expectedExit The expected exit value or -1 to not fail on any exit value
	 * @param timeoutMillis The timeout in milliseconds or ExecuteWatchdog.INFINITE_TIMEOUT
	 * @param stream An OutputStream which receives the output of the executed command
     *
     * @throws IOException Execution of sub-process failed or the
     *          sub-process returned an exit value indicating a failure
	 */
	public static void getCommandResultIntoStream(
			CommandLine cmdLine, File dir, int expectedExit,
			long timeoutMillis, OutputStream stream) throws IOException {
		getCommandResultIntoStream(cmdLine, dir, expectedExit, timeoutMillis, stream, null);
	}

	/**
	 * Run the given commandline in the given directory and verify that the tool
	 * has the expected exit code and does finish in the timeout.
	 *
     * @param cmdLine The commandline object filled with the executable and command line arguments
     * @param dir The working directory for the command
     * @param expectedExit The expected exit value or -1 to not fail on any exit value
	 * @param timeoutMillis The timeout in milliseconds or ExecuteWatchdog.INFINITE_TIMEOUT
     * @param stream An OutputStream which receives the output of the executed command
	 * @param environment Environment variables that should be set for the execution of the command
	 *
	 * @throws IOException Execution of subprocess failed or the
     *          subprocess returned an exit value indicating a failure
	 */
	public static void getCommandResultIntoStream(
			CommandLine cmdLine, File dir, int expectedExit,
			long timeoutMillis, OutputStream stream, Map<String,String> environment) throws IOException {
		Executor executor = getDefaultExecutor(dir, expectedExit, timeoutMillis);
		executor.setStreamHandler(new PumpStreamHandler(stream));
		execute(cmdLine, dir, executor, environment);
	}

	private static Executor getDefaultExecutor(File dir, int expectedExit, long timeoutMillis) {
		Executor executor = DefaultExecutor.builder().get();
		if(expectedExit != -1) {
			executor.setExitValue(expectedExit);
		} else {
			executor.setExitValues(null);
		}

		ExecuteWatchdog watchdog = new ExecuteWatchdog.Builder().setTimeout(Duration.ofMillis(timeoutMillis)).get();
		executor.setWatchdog(watchdog);
		executor.setWorkingDirectory(dir);
		return executor;
	}

	private static void execute(CommandLine cmdLine, File dir, Executor executor,
								Map<String,String> environment) throws IOException {
		log.info("-Executing(" + dir + "): " + toString(cmdLine));
		try {
			int exitValue = executor.execute(cmdLine, environment);
			if (exitValue != 0) {
				log.info("Had exit code " + exitValue + " when calling(" + dir + "): " + toString(cmdLine));
			}
		} catch (ExecuteException e) {
			throw new ExecuteException(
					(executor.getWatchdog().killedProcess() ? "Killed by Watchdog, maybe timeout reached. " : "") +
					"While executing (" + dir + "); " + toString(cmdLine), e.getExitValue(), e);
		}
	}

	private static String toString(CommandLine cmdLine) {
		return ArrayUtils.toString(cmdLine.toStrings(), " ", "", "");
	}

	/**
	 * Allows to overwrite the log-level set for the logger of this class.
	 *
	 * @param level The new log-level to be used
	 */
	public static void setLogLevel(Level level) {
		log.setLevel(level);
	}
}
