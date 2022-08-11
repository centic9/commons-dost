package org.dstadler.commons.exec;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.logging.Logger;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.SystemUtils;
import org.dstadler.commons.logging.jdk.LoggerFactory;
import org.dstadler.commons.testing.PrivateConstructorCoverage;
import org.dstadler.commons.testing.TestHelpers;
import org.junit.Test;

public class ExecutionHelperTest {
	private final static Logger log = LoggerFactory.make();

	public static final String SVN_CMD = "svn";

	@Test
	public void testGetCommandResult() throws IOException {
		CommandLine cmdLine = new CommandLine(SVN_CMD);
		cmdLine.addArgument("help");

		log.info("Working dir: " + new File(".").getAbsolutePath());
		try (InputStream result = ExecutionHelper.getCommandResult(cmdLine, new File("."), 0, 60000)) {
			assertNotNull(result);
			log.info("Svn-Update reported:\n" + IOUtils.toString(result, StandardCharsets.UTF_8));
		}
	}

	@Test
	public void testGetCommandResultWrongCmd() {
		CommandLine cmdLine = new CommandLine(SVN_CMD);
		cmdLine.addArgument("notExists");

		try {
			ExecutionHelper.getCommandResult(cmdLine, new File("."), 0, 60000);
			fail("Should throw exception");
		} catch (IOException e) {
			TestHelpers.assertContains(e, "Process exited with an error: 1", SVN_CMD, "notExists");
		}
	}

	@Test
	public void testGetCommandResultFailureNoOutput() {
		CommandLine cmdLine = new CommandLine("/bin/false");

		try {
			ExecutionHelper.getCommandResult(cmdLine, new File("."), 0, 60000);
			fail("Should throw exception");
		} catch (IOException e) {
			if (SystemUtils.IS_OS_WINDOWS) {
				TestHelpers.assertContains(e, "The system cannot find the file specified", "\\bin\\false");
			} else {
				TestHelpers.assertContains(e, "Process exited with an error: 1", "/bin/false");
			}
		}
	}

	@Test
	public void testGetCommandResultIgnoreExitValueStatus() throws IOException {
		CommandLine cmdLine = new CommandLine(SVN_CMD);
		cmdLine.addArgument("status");
		addDefaultArguments(cmdLine);

		try (InputStream result = ExecutionHelper.getCommandResult(cmdLine, new File("."), -1, 60000)) {
        	assertNotNull(result);
			String output = IOUtils.toString(result, StandardCharsets.UTF_8);
			log.info("Svn-Status reported:\n" + output);
			TestHelpers.assertNotContains(output, "status");
		}
	}

    @Test
    public void testGetCommandResultIgnoreExitValueHelp() throws IOException {
        CommandLine cmdLine = new CommandLine(SVN_CMD);
        cmdLine.addArgument("help");

        try (InputStream result = ExecutionHelper.getCommandResult(cmdLine, new File("."), -1, 60000)) {
        	assertNotNull(result);
			String output = IOUtils.toString(result, StandardCharsets.UTF_8);
			log.info("Svn-Help reported:\n" + output);
			TestHelpers.assertContains(output, "help");
        }
    }

    @Test
	public void testGetCommandResultIgnoreExitValueWrongCmd() throws IOException {
		CommandLine cmdLine = new CommandLine(SVN_CMD);
		cmdLine.addArgument("notexists");

		try (InputStream result = ExecutionHelper.getCommandResult(cmdLine, new File("."), -1, 60000)) {
			assertNotNull(result);
			String output = IOUtils.toString(result, StandardCharsets.UTF_8);
			log.info("Svn reported:\n" + output);
			TestHelpers.assertContains(output, "notexists");
		}
	}

    @Test
	public void testGetCommandResultIgnoreExitValueArgumentWithBlanks() throws IOException {
		CommandLine cmdLine = new CommandLine(SVN_CMD);
		cmdLine.addArgument("notexists and more notexists");

		try (InputStream result = ExecutionHelper.getCommandResult(cmdLine, new File("."), -1, 60000)) {
			assertNotNull(result);
			String output = IOUtils.toString(result, StandardCharsets.UTF_8);
			log.info("Svn reported:\n" + output);
			TestHelpers.assertContains(output.replace("'", "\""), "\"notexists and more notexists\"");
		}
	}

	@Test
	public void testGetCommandResultInputStream() throws IOException {
		CommandLine cmdLine = new CommandLine(SVN_CMD);
		cmdLine.addArgument("help");

		log.info("Working dir: " + new File(".").getAbsolutePath());
		try (InputStream result = ExecutionHelper.getCommandResult(cmdLine, new File("."), 0, 60000, new ByteArrayInputStream(new byte[] {}))) {
			assertNotNull(result);
			String output = IOUtils.toString(result, StandardCharsets.UTF_8);
			log.info("Svn-Help reported:\n" + output);
			TestHelpers.assertContains(output, "help");
		}
	}

	@Test
	public void testGetCommandResultWrongCmdInputStream() {
		CommandLine cmdLine = new CommandLine(SVN_CMD);
		cmdLine.addArgument("notexists");

		try {
			ExecutionHelper.getCommandResult(cmdLine, new File("."), 0, 60000, new ByteArrayInputStream(new byte[] {}));
			fail("Should throw exception");
		} catch (IOException e) {
			TestHelpers.assertContains(e, "Process exited with an error: 1");
		}
	}

	private static void addDefaultArguments(CommandLine cmdLine) {
		cmdLine.addArgument("--non-interactive");
		cmdLine.addArgument("--trust-server-cert");
	}

	@Test
	public void testGetCommandResultIgnoreExitValueInputStream() throws IOException {
		CommandLine cmdLine = new CommandLine(SVN_CMD);
		cmdLine.addArgument("status");
		addDefaultArguments(cmdLine);

		try (InputStream result = ExecutionHelper.getCommandResult(cmdLine, new File("."), -1, 60000, new ByteArrayInputStream(new byte[] {}))) {
			assertNotNull(result);
			String output = IOUtils.toString(result, StandardCharsets.UTF_8);
			log.info("Svn-status reported:\n" + output);
			TestHelpers.assertNotContains(output, "status");
		}
	}

	@Test
	public void testGetCommandResultIgnoreExitValueWrongCmdInputStream() throws IOException {
		CommandLine cmdLine = new CommandLine(SVN_CMD);
		cmdLine.addArgument("notexists");

		try (InputStream result = ExecutionHelper.getCommandResult(cmdLine, new File("."), -1, 60000, new ByteArrayInputStream(new byte[] {}))) {
			assertNotNull(result);
			String output = IOUtils.toString(result, StandardCharsets.UTF_8);
			log.info("Svn reported:\n" + output);
			TestHelpers.assertContains(output, "notexists");
		}
	}

	// helper method to get coverage of the unused constructor
	@Test
	public void testPrivateConstructor() throws Exception {
		PrivateConstructorCoverage.executePrivateConstructor(ExecutionHelper.class);
	}

	@Test
	public void testGetCommandResultStream() throws IOException {
		CommandLine cmdLine = new CommandLine(SVN_CMD);
		cmdLine.addArgument("help");

		try (ByteArrayOutputStream result = new ByteArrayOutputStream()) {
			ExecutionHelper.getCommandResultIntoStream(cmdLine, new File("."), 0, 60000, result);
			byte[] bytes = result.toByteArray();
			assertNotNull(bytes);
			assertTrue(bytes.length > 0);

			log.info("Had: " + new String(bytes));
		}
		//TestHelpers.assertContains(new String(bytes), "");
	}
}
