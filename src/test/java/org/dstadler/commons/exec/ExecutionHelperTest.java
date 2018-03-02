package org.dstadler.commons.exec;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Logger;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.io.IOUtils;
import org.junit.Test;

import org.dstadler.commons.testing.PrivateConstructorCoverage;
import org.dstadler.commons.testing.TestHelpers;
import org.dstadler.commons.logging.jdk.LoggerFactory;


/**
 *
 * @author dominik.stadler
 */
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
			log.info("Svn-Update reported:\n" + IOUtils.toString(result, "UTF-8"));
		}
	}

	@Test
	public void testGetCommandResultWrongCmd() throws Exception {
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
	public void testGetCommandResultIgnoreExitValueStatus() throws IOException {
		CommandLine cmdLine = new CommandLine(SVN_CMD);
		cmdLine.addArgument("status");
		addDefaultArguments(cmdLine);

		try (InputStream result = ExecutionHelper.getCommandResult(cmdLine, new File("."), -1, 60000)) {
        	assertNotNull(result);
			log.info("Svn-Update reported:\n" + IOUtils.toString(result, "UTF-8"));
		}
	}

    @Test
    public void testGetCommandResultIgnoreExitValueHelp() throws IOException {
        CommandLine cmdLine = new CommandLine(SVN_CMD);
        cmdLine.addArgument("help");

        try (InputStream result = ExecutionHelper.getCommandResult(cmdLine, new File("."), -1, 60000)) {
        	assertNotNull(result);
            log.info("Svn-Update reported:\n" + IOUtils.toString(result, "UTF-8"));
        }
    }

    @Test
	public void testGetCommandResultIgnoreExitValueWrongCmd() throws IOException {
		CommandLine cmdLine = new CommandLine(SVN_CMD);
		cmdLine.addArgument("notexists");

		try (InputStream result = ExecutionHelper.getCommandResult(cmdLine, new File("."), -1, 60000)) {
			assertNotNull(result);
			log.info("Svn-Update reported:\n" + IOUtils.toString(result, "UTF-8"));
		}
	}

	@Test
	public void testGetCommandResultInputStream() throws IOException {
		CommandLine cmdLine = new CommandLine(SVN_CMD);
		cmdLine.addArgument("help");

		log.info("Working dir: " + new File(".").getAbsolutePath());
		try (InputStream result = ExecutionHelper.getCommandResult(cmdLine, new File("."), 0, 60000, new ByteArrayInputStream(new byte[] {}))) {
			assertNotNull(result);
			log.info("Svn-Update reported:\n" + IOUtils.toString(result, "UTF-8"));
		}
	}

	@Test
	public void testGetCommandResultWrongCmdInputStream() throws Exception {
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
			log.info("Svn-Update reported:\n" + IOUtils.toString(result, "UTF-8"));
		}
	}

	@Test
	public void testGetCommandResultIgnoreExitValueWrongCmdInputStream() throws IOException {
		CommandLine cmdLine = new CommandLine(SVN_CMD);
		cmdLine.addArgument("notexists");

		try (InputStream result = ExecutionHelper.getCommandResult(cmdLine, new File("."), -1, 60000, new ByteArrayInputStream(new byte[] {}))) {
			assertNotNull(result);
			log.info("Svn-Update reported:\n" + IOUtils.toString(result, "UTF-8"));
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
