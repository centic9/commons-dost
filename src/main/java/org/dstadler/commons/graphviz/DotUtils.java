package org.dstadler.commons.graphviz;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteWatchdog;
import org.apache.commons.exec.PumpStreamHandler;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

/**
 * Simple utility methods to run the dot-tool from Graphviz on a file.
 *
 * @author dominik.stadler
 *
 */
public class DotUtils {
	public static String DOT_EXE = SystemUtils.IS_OS_WINDOWS ? "C:\\cygwin\\bin\\dot.exe" : "/usr/bin/dot";

    private final static String FOOTER = "}\n";

    /**
     * Write out the string and a newline
     *
     * @param writer The writer for the .dot-file
     * @param string The text to write
     * @throws IOException if writing to the Writer fails
     */
    public static void writeln(final Writer writer, final String string) throws IOException {
        writeln(writer, string, 0);
    }

    /**
	 * Write out the string and a newline, appending a number of tabs to
	 * properly indent the resulting text-file.
	 *
	 * @param writer The writer for the .dot-file
	 * @param string The text to write
	 * @param indentLevel How much to indent the line
	 * @throws IOException if writing to the Writer fails
	 */
	public static void writeln(final Writer writer, final String string, int indentLevel) throws IOException {
        writer.write(StringUtils.repeat("\t", indentLevel) + string);
        writer.write("\n");
    }

	/**
	 * Write the header structure.
	 *
	 * @param writer A writer where the header is written to.
	 * @param dpi The resulting resolution, can be 0 for using the default DPI-setting of dot
	 * @param rankdir The direction of the graph, can be null
	 * @param id The id of the graph, cannot be null, needs to start with a alphabetical character, can contain numbers, alphabetic characters and underscore only.
	 * @param attribLines Additional attributes, can be null
	 *
	 * @throws IOException if writing to the Writer fails
	 */
    public static void writeHeader(Writer writer, int dpi, String rankdir, String id, List<String> attribLines) throws IOException {
        // Default settings
        if (attribLines == null) {
            attribLines = new ArrayList<>();
        } else {
        	attribLines = new ArrayList<>(attribLines);
        }
        attribLines.add("node [shape=box];");
        // add ...

        // DPI and Rankdir
        String header = "digraph " + id + " {\n";
        if (dpi > 0) {
            header += "dpi=" + dpi + ";\n";
        }
        header += "rankdir=" + (StringUtils.isNotBlank(rankdir) ? rankdir : "LR") + ";\n";

        // Additional lines
        for (String line : attribLines) {
            line = line.trim();
            header += line + (line.endsWith(";") ? "\n" : ";\n");
        }
        DotUtils.writeln(writer, header);
    }

    /**
     * Closes the graph-markup with the necessary closing statements.
     *
	 * @param writer A writer where the footer is written to.
	 *
	 * @throws IOException if writing to the Writer fails
     */
    public static void writeFooter(Writer writer) throws IOException {
        writeln(writer, FOOTER);
    }

	/**
	 * Call graphviz-dot to convert the .dot-file to a rendered graph.
     *
     * The file extension of the specified result file is being used as the filetype
     * of the rendering.
	 *
	 * @param dotfile The dot {@code File}  used for the graph generation
     * @param resultfile The {@code File} to which should be written
	 *
	 * @throws IOException if writing the resulting graph fails or other I/O
	 * 			problems occur
	 */
	public static void renderGraph(File dotfile, File resultfile) throws IOException {
		// call graphviz-dot via commons-exec
		CommandLine cmdLine = new CommandLine(DOT_EXE);
		cmdLine.addArgument("-T" + StringUtils.substringAfterLast(resultfile.getAbsolutePath(), "."));
		cmdLine.addArgument(dotfile.getAbsolutePath());
		DefaultExecutor executor = new DefaultExecutor();
		executor.setExitValue(0);
		ExecuteWatchdog watchdog = new ExecuteWatchdog(60000);
		executor.setWatchdog(watchdog);
		try {
			try (FileOutputStream out2 = new FileOutputStream(resultfile)) {
				executor.setStreamHandler(new PumpStreamHandler(out2, System.err));
				int exitValue = executor.execute(cmdLine);
				if(exitValue != 0) {
					throw new IOException("Could not convert graph to dot, had exit value: " + exitValue + "!");
				}
			}
		} catch (IOException e) {
			// if something went wrong the file should not be left behind...
			if(!resultfile.delete()) {
				System.out.println("Could not delete file " + resultfile);
			}

			throw e;
		}
	}

	/**
	 * Verify if dot can be started and print out the version to stdout.
	 *
	 * @return True if "dot -V" ran successfully, false otherwise
	 *
	 * @throws IOException If running dot fails.
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
		int exitValue = executor.execute(cmdLine);
		if(exitValue != 0) {
			System.err.println("Could not run '" + DOT_EXE + "', had exit value: " + exitValue + "!");
			return false;
		}

		return true;
	}

	/**
	 * Allows to define where the exe-file for dot can be found.
	 *
	 * @param dotExe The full pathname of the dot-executable.
     */
	public static void setDotExe(String dotExe) {
		DOT_EXE = dotExe;
	}
}
