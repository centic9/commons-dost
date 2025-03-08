package org.dstadler.commons.exec;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.Executor;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.SystemUtils;
import org.dstadler.commons.logging.jdk.LoggerFactory;
import org.dstadler.commons.testing.PrivateConstructorCoverage;
import org.dstadler.commons.testing.TestHelpers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertThrows;

class ExecutionHelperTest {
	private final static Logger log = LoggerFactory.make();

	public static final String SVN_CMD = "svn";

	private static void assumeCommand(String cmd, String arg) {
		Executor executor = DefaultExecutor.builder().get();
		executor.setExitValues(null);

		CommandLine cmdLine = new CommandLine(cmd);
		cmdLine.addArgument(arg);

		try {
			Assumptions.assumeTrue(0 == executor.execute(cmdLine));
		} catch (IOException e) {
			Assumptions.assumeTrue(false, "Command " + cmd + " " + arg + " not available: " + e.getMessage());
		}
	}

	@Test
	void testGetCommandResult() throws IOException {
		assumeCommand(SVN_CMD, "-h");

		CommandLine cmdLine = new CommandLine(SVN_CMD);
		cmdLine.addArgument("help");

		log.info("Working dir: " + new File(".").getAbsolutePath());
		try (InputStream result = ExecutionHelper.getCommandResult(cmdLine, new File("."), 0, 60000)) {
			Assertions.assertNotNull(result);
			log.info("Svn-Update reported:\n" + IOUtils.toString(result, StandardCharsets.UTF_8));
		}
	}

	@Test
	void testGetCommandResultWrongCmd() {
		assumeCommand(SVN_CMD, "-h");

		CommandLine cmdLine = new CommandLine(SVN_CMD);
		cmdLine.addArgument("notExists");

		try {
			ExecutionHelper.getCommandResult(cmdLine, new File("."), 0, 60000);
			Assertions.fail("Should throw exception");
		} catch (IOException e) {
			TestHelpers.assertContains(e, "While executing (.)", SVN_CMD, "notExists");
			Assertions.assertNotNull(e.getCause());
			Assertions.assertNotNull(e.getCause().getCause());
			TestHelpers.assertContains(e.getCause().getCause(), "Process exited with an error: 1");
		}
	}

	@Test
	void testGetCommandResultFailureNoOutput() {
		CommandLine cmdLine = new CommandLine("/bin/false");

		try {
			ExecutionHelper.getCommandResult(cmdLine, new File("."), 0, 60000);
			Assertions.fail("Should throw exception");
		} catch (IOException e) {
			if (SystemUtils.IS_OS_WINDOWS) {
				TestHelpers.assertContains(e, "The system cannot find the file specified", "\\bin\\false");
			} else {
				TestHelpers.assertContains(e, "While executing (.)", "/bin/false");
				Assertions.assertNotNull(e.getCause());
				Assertions.assertNotNull(e.getCause().getCause());
				TestHelpers.assertContains(e.getCause().getCause(), "Process exited with an error: 1");
			}
		}
	}

	@Test
	void testGetCommandResultIgnoreExitValueStatus() throws IOException {
		assumeCommand(SVN_CMD, "-h");

		CommandLine cmdLine = new CommandLine(SVN_CMD);
		cmdLine.addArgument("status");
		addDefaultArguments(cmdLine);

		try (InputStream result = ExecutionHelper.getCommandResult(cmdLine, new File("."), -1, 60000)) {
        	Assertions.assertNotNull(result);
			String output = IOUtils.toString(result, StandardCharsets.UTF_8);
			log.info("Svn-Status reported:\n" + output);
			TestHelpers.assertNotContains(output, "status");
		}
	}

    @Test
    void testGetCommandResultIgnoreExitValueHelp() throws IOException {
		assumeCommand(SVN_CMD, "-h");

		CommandLine cmdLine = new CommandLine(SVN_CMD);
        cmdLine.addArgument("help");

        try (InputStream result = ExecutionHelper.getCommandResult(cmdLine, new File("."), -1, 60000)) {
        	Assertions.assertNotNull(result);
			String output = IOUtils.toString(result, StandardCharsets.UTF_8);
			log.info("Svn-Help reported:\n" + output);
			TestHelpers.assertContains(output, "help");
        }
    }

    @Test
	void testGetCommandResultIgnoreExitValueWrongCmd() throws IOException {
		assumeCommand(SVN_CMD, "-h");

		CommandLine cmdLine = new CommandLine(SVN_CMD);
		cmdLine.addArgument("notexists");

		try (InputStream result = ExecutionHelper.getCommandResult(cmdLine, new File("."), -1, 60000)) {
			Assertions.assertNotNull(result);
			String output = IOUtils.toString(result, StandardCharsets.UTF_8);
			log.info("Svn reported:\n" + output);
			TestHelpers.assertContains(output, "notexists");
		}
	}

    @Test
	void testGetCommandResultIgnoreExitValueArgumentWithBlanks() throws IOException {
		assumeCommand(SVN_CMD, "-h");

		CommandLine cmdLine = new CommandLine(SVN_CMD);
		cmdLine.addArgument("notexists and more notexists");

		try (InputStream result = ExecutionHelper.getCommandResult(cmdLine, new File("."), -1, 60000)) {
			Assertions.assertNotNull(result);
			String output = IOUtils.toString(result, StandardCharsets.UTF_8);
			log.info("Svn reported:\n" + output);
			TestHelpers.assertContains(output.replace("'", "\""), "\"notexists and more notexists\"");
		}
	}

	@Test
	void testGetCommandResultInputStream() throws IOException {
		assumeCommand(SVN_CMD, "-h");

		CommandLine cmdLine = new CommandLine(SVN_CMD);
		cmdLine.addArgument("help");

		log.info("Working dir: " + new File(".").getAbsolutePath());
		try (InputStream result = ExecutionHelper.getCommandResult(cmdLine, new File("."), 0, 60000, new ByteArrayInputStream(new byte[] {}))) {
			Assertions.assertNotNull(result);
			String output = IOUtils.toString(result, StandardCharsets.UTF_8);
			log.info("Svn-Help reported:\n" + output);
			TestHelpers.assertContains(output, "help");
		}
	}

	@Test
	void testGetCommandResultWrongCmdInputStream() {
		assumeCommand(SVN_CMD, "-h");

		CommandLine cmdLine = new CommandLine(SVN_CMD);
		cmdLine.addArgument("notexists");

		try {
			ExecutionHelper.getCommandResult(cmdLine, new File("."), 0, 60000, new ByteArrayInputStream(new byte[] {}));
			Assertions.fail("Should throw exception");
		} catch (IOException e) {
			TestHelpers.assertContains(e, "While executing (.)", SVN_CMD, "notexists");
			Assertions.assertNotNull(e.getCause());
			Assertions.assertNotNull(e.getCause().getCause());
			TestHelpers.assertContains(e.getCause().getCause(), "Process exited with an error: 1");
		}
	}

	private static void addDefaultArguments(CommandLine cmdLine) {
		cmdLine.addArgument("--non-interactive");
		cmdLine.addArgument("--trust-server-cert");
	}

	@Test
	void testGetCommandResultIgnoreExitValueInputStream() throws IOException {
		assumeCommand(SVN_CMD, "-h");

		CommandLine cmdLine = new CommandLine(SVN_CMD);
		cmdLine.addArgument("status");
		addDefaultArguments(cmdLine);

		try (InputStream result = ExecutionHelper.getCommandResult(cmdLine, new File("."), -1, 60000, new ByteArrayInputStream(new byte[] {}))) {
			Assertions.assertNotNull(result);
			String output = IOUtils.toString(result, StandardCharsets.UTF_8);
			log.info("Svn-status reported:\n" + output);
			TestHelpers.assertNotContains(output, "status");
		}
	}

	@Test
	void testGetCommandResultIgnoreExitValueWrongCmdInputStream() throws IOException {
		assumeCommand(SVN_CMD, "-h");

		CommandLine cmdLine = new CommandLine(SVN_CMD);
		cmdLine.addArgument("notexists");

		try (InputStream result = ExecutionHelper.getCommandResult(cmdLine, new File("."), -1, 60000, new ByteArrayInputStream(new byte[] {}))) {
			Assertions.assertNotNull(result);
			String output = IOUtils.toString(result, StandardCharsets.UTF_8);
			log.info("Svn reported:\n" + output);
			TestHelpers.assertContains(output, "notexists");
		}
	}

	// helper method to get coverage of the unused constructor
	@Test
	void testPrivateConstructor() throws Exception {
		PrivateConstructorCoverage.executePrivateConstructor(ExecutionHelper.class);
	}

	@Test
	void testGetCommandResultStream() throws IOException {
		assumeCommand(SVN_CMD, "-h");

		CommandLine cmdLine = new CommandLine(SVN_CMD);
		cmdLine.addArgument("help");

		try (ByteArrayOutputStream result = new ByteArrayOutputStream()) {
			ExecutionHelper.getCommandResultIntoStream(cmdLine, new File("."), 0, 60000, result);
			byte[] bytes = result.toByteArray();
			Assertions.assertNotNull(bytes);
			Assertions.assertTrue(bytes.length > 0);

			log.info("Had: " + new String(bytes));
		}
		//TestHelpers.assertContains(new String(bytes), "");
	}

	@Test
	void testTriggerTimeout() {
		CommandLine cmdLine = new CommandLine("sleep");
		cmdLine.addArgument("5");

		log.info("Working dir: " + new File(".").getAbsolutePath());

		IOException exc = assertThrows(
				IOException.class,
				() -> ExecutionHelper.getCommandResult(cmdLine, new File("."), 0, 10,
						new ByteArrayInputStream(new byte[] {})),
				"Did expect very short timeout to kick in");

		TestHelpers.assertContains(exc,
				"Killed by Watchdog, maybe timeout reached");
	}

	@Test
	void testSetLevel() {
		// this can only be verified visually
		log.info("Test 1");
		testTriggerTimeout();

		ExecutionHelper.setLogLevel(Level.WARNING);

		log.info("Test 2");
		testTriggerTimeout();

		ExecutionHelper.setLogLevel(Level.INFO);

		log.info("Test 3");
		testTriggerTimeout();
	}
}
