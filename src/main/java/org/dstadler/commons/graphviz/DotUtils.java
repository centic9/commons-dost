package org.dstadler.commons.graphviz;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteException;
import org.apache.commons.exec.ExecuteWatchdog;
import org.apache.commons.exec.PumpStreamHandler;
import org.apache.commons.lang3.SystemUtils;

/**
 * Simple utility methods to run the dot-tool from Graphviz on a file.
 *
 * @author dominik.stadler
 *
 */
public class DotUtils {
	public static final String DOT_EXE = SystemUtils.IS_OS_WINDOWS ? "C:\\cygwin\\bin\\dot.exe" : "/usr/bin/dot";
	private static final String PNG_TYPE = "png";

	/**
	 * Call graphviz-dot to convert the .dot-file to a PNG
	 *
	 * @param dotfile
	 * @return resulting file or null if it failed.
	 *
	 * @throws IOException
	 */
	public static File renderGraph(File dotfile) throws IOException {
		File out = new File(dotfile.getAbsolutePath() + "." + PNG_TYPE);

		// call graphviz-dot via commons-exec
		CommandLine cmdLine = new CommandLine(DOT_EXE);
		cmdLine.addArgument("-T" + PNG_TYPE);
		cmdLine.addArgument(dotfile.getAbsolutePath());
		
		DefaultExecutor executor = new DefaultExecutor();
		executor.setExitValue(0);
		
		ExecuteWatchdog watchdog = new ExecuteWatchdog(60000);
		executor.setWatchdog(watchdog);
		
		try (FileOutputStream out2 = new FileOutputStream(out)) {
			executor.setStreamHandler(new PumpStreamHandler(out2, System.err));
			executor.execute(cmdLine);
		}

		return out;
	}

	/**
	 * Verify if dot can be started and print out the version to stdout.
	 *
	 * @return True if "dot -V" ran successfully, false otherwise
	 *
	 * @throws ExecuteException
	 * @throws IOException
	 */
	public static boolean checkDot() throws IOException {
		// call graphviz-dot via commons-exec
		CommandLine cmdLine = new CommandLine(DOT_EXE);
		cmdLine.addArgument("-V");
		DefaultExecutor executor = new DefaultExecutor();
		executor.setExitValue(0);
		ExecuteWatchdog watchdog = new ExecuteWatchdog(60000);
		executor.setWatchdog(watchdog);
		executor.setStreamHandler(new PumpStreamHandler(System.out, System.err));
		executor.execute(cmdLine);

		return true;
	}
}
