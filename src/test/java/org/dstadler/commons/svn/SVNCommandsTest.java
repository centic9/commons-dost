package org.dstadler.commons.svn;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeFalse;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.TimeZone;
import java.util.logging.Logger;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.dstadler.commons.exec.ExecutionHelper;
import org.dstadler.commons.logging.jdk.LoggerFactory;
import org.dstadler.commons.testing.PrivateConstructorCoverage;
import org.dstadler.commons.testing.TestHelpers;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.xml.sax.SAXException;

public class SVNCommandsTest {
    private final static Logger log = LoggerFactory.make();

    private static final String USERNAME = "";
    private static final String PASSWORD = null;

    private static final String BASE_URL;
    private static final File repoDir;
	private static File svnRepoDir;

	// use static-block to initialize "static final" members
    static {
		assertNotNull(Thread.currentThread().getContextClassLoader().getResource("logging.properties"),
				"Should have logging.properties, but had none with Classloader: " +
								Thread.currentThread().getContextClassLoader());

		try {
			LoggerFactory.initLogging();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		repoDir = createLocalSVNRepository();

		BASE_URL = (SystemUtils.IS_OS_WINDOWS ? "file:///" : "file://") + repoDir.getAbsolutePath().
				// local URL on Windows has limitations
						replace("\\", "/").replace("c:/", "/") + "/project1";
		log.info("Using baseUrl " + BASE_URL);
	}

	private static File createLocalSVNRepository() {
		// create directory for temporary SVN repository
		final File repoDir;
		try {
			repoDir = File.createTempFile("SVNCommandsTestRepo", ".dir");
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		assertTrue(repoDir.delete());
		assertTrue(repoDir.mkdir());

		CommandLine cmdLine = new CommandLine("svnadmin");
		cmdLine.addArgument("create");
		cmdLine.addArgument("project1");

		log.info("Creating local svn repository at " + repoDir);
		try (InputStream result = ExecutionHelper.getCommandResult(cmdLine, repoDir, 0, 360000)) {
			log.info("Svnadmin reported:\n" + SVNCommands.extractResult(result));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		return repoDir;
	}

	private static File checkoutSVNRepository() {
		final File svnRepoDir;

		// create directory for checkout of SVN repository
		try {
			svnRepoDir = File.createTempFile("SVNCommandsTestSVNRepo", ".dir");
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		assertTrue(svnRepoDir.delete());
		assertTrue(svnRepoDir.mkdir());

		try {
			// checkout to 2nd directory
			try (InputStream result = SVNCommands.checkout(BASE_URL,
					svnRepoDir, USERNAME, PASSWORD)) {
				final String ret = SVNCommands.extractResult(result);
				if (StringUtils.isNotBlank(ret)) {
					log.info("Svn-checkout reported:\n" + ret);
				}

				// There is a strange issue with the file-URL on Windows now which
				// I could not fix, so let's ignore this test here for now
				assumeFalse(SystemUtils.IS_OS_WINDOWS &&
						ret.contains("Unable to connect to a repository at URL"),
						"Checkout on Windows fails in some setups :(");
			}

			// add some minimal content
			FileUtils.writeStringToFile(new File(svnRepoDir, "README"), "test content", "UTF-8");
			CommandLine cmdLine = new CommandLine(SVNCommands.SVN_CMD);
			cmdLine.addArgument("add");
			cmdLine.addArgument("README");

			try (InputStream result = ExecutionHelper.getCommandResult(cmdLine, svnRepoDir, 0, 360000)) {
				log.info("Svn-add reported:\n" + SVNCommands.extractResult(result));
			}

			cmdLine = new CommandLine(SVNCommands.SVN_CMD);
			cmdLine.addArgument("commit");
			cmdLine.addArgument("-m");
			cmdLine.addArgument("comment");

			try (InputStream result = ExecutionHelper.getCommandResult(cmdLine, svnRepoDir, 0, 360000)) {
				log.info("Svn-commit reported:\n" + SVNCommands.extractResult(result));
			}
		} catch (IOException e) {
			FileUtils.deleteQuietly(repoDir);
			FileUtils.deleteQuietly(svnRepoDir);

			throw new RuntimeException(e);
		}

		return svnRepoDir;
	}

	@BeforeAll
    public static void setUpClass() {
		svnRepoDir = checkoutSVNRepository();

		assumeTrue(SVNCommands.checkSVNCommand(), "Could not execute the SVN-command, skipping tests");
    }

    @AfterAll
    public static void tearDownClass() throws IOException {
		if (repoDir != null) {
			FileUtils.deleteDirectory(repoDir);
		}
		if (svnRepoDir != null) {
			FileUtils.deleteDirectory(svnRepoDir);
		}
    }

    @Test
    public void testGetBranchLogRevisionRevision() throws Exception {
        assumeTrue(serverAvailable(), "SVN not available at " + BASE_URL);

        Map<Long, LogEntry> log = SVNCommands.getBranchLog(new String[]{""}, 0, 1, BASE_URL, USERNAME, PASSWORD);
        assertNotNull(log);
        assertTrue(log.size() > 0);
        final String date = log.values().iterator().next().date;
        // depends on actual time of check-in to the temp-repo: assertEquals("2017-04-24T19:54:31.089532Z", date);
        assertNotNull(DateFormatUtils.ISO_8601_EXTENDED_DATETIME_FORMAT.parse(date));
    }

    @Test
    public void testGetBranchLogRevision() throws Exception {
        assumeTrue(serverAvailable(), "SVN not available at " + BASE_URL);

        Map<Long, LogEntry> log = SVNCommands.getBranchLog(new String[]{""}, 0, BASE_URL, USERNAME, PASSWORD);
        assertNotNull(log);
        assertTrue(log.size() > 0);
        final String date = log.values().iterator().next().date;
        // depends on actual time of check-in to the temp-repo: assertEquals("2017-04-24T19:54:31.089532Z", date);
        assertNotNull(DateFormatUtils.ISO_8601_EXTENDED_DATETIME_FORMAT.parse(date));
    }

    @Test
    public void testGetBranchLogStream() throws Exception {
        assumeTrue(serverAvailable(), "SVN not available at " + BASE_URL);

        InputStream str = SVNCommands.getBranchLogStream(new String[]{""}, 0, BASE_URL, USERNAME, PASSWORD);
        String result = IOUtils.toString(str, StandardCharsets.UTF_8);
        assertNotNull(result);
        assertTrue(result.contains("revision=\"1\""));
        assertTrue(result.contains("/README"));
        str.close();
    }

    @Test
    public void testGetBranchLogDate() throws Exception {
        assumeTrue(serverAvailable(), "SVN not available at " + BASE_URL);

        Map<Long, LogEntry> log = SVNCommands.getBranchLog(new String[]{""}, new Date(0),
                DateUtils.addDays(new Date(), 1), BASE_URL, USERNAME, PASSWORD);
        assertNotNull(log);
        assertTrue(log.size() > 0);
        final String date = log.values().iterator().next().date;
        // depends on actual time of check-in to the temp-repo: assertEquals("2017-04-24T19:54:31.089532Z", date);
        assertNotNull(DateFormatUtils.ISO_8601_EXTENDED_DATETIME_FORMAT.parse(date));
    }

    @Test
    public void testSVNDirectAccess() throws IOException {
        assumeTrue(serverAvailable(), "SVN not available at " + BASE_URL);

        String content = IOUtils.toString(SVNCommands.getRemoteFileContent("/README", 1, BASE_URL, USERNAME, PASSWORD), StandardCharsets.UTF_8);
        assertNotNull(content);
        assertTrue(content.contains("test content"));
    }

    @Disabled("Does not work currently due to local file repo")
    @Test
    public void canLoadBranchLogForTimeframe() throws IOException, SAXException {
        assumeTrue(serverAvailable(), "SVN not available at " + BASE_URL);

        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        cal.set(2015, Calendar.SEPTEMBER, 1, 0, 0, 0);
        Date startDate = cal.getTime();
        cal.set(2015, Calendar.SEPTEMBER, 2, 0, 0, 0);
        Date endDate = cal.getTime();

        Map<Long, LogEntry> log = SVNCommands.getBranchLog(new String[]{"/README"}, startDate, endDate, BASE_URL, USERNAME, PASSWORD);
        assertNotNull(log);
        assertEquals(20, log.size());
    }

    private boolean serverAvailable() {
        // does not work with ssh-url
        //return UrlUtils.isAvailable(BASE_URL, false, true, 10000);
        return true;
    }

    @Test
    public void testCleanup() throws IOException {
        assumeTrue(serverAvailable(), "SVN not available at " + BASE_URL);

        File tempDir = File.createTempFile("SVNCommandsTest", ".dir");
        try {
            assertTrue(tempDir.delete());
            try (InputStream stream = SVNCommands.checkout(BASE_URL, tempDir, USERNAME, PASSWORD)) {
                System.out.println(IOUtils.toString(stream, StandardCharsets.UTF_8));
            }

            // check that the necessary structures were created
            assertTrue(tempDir.exists(), "Need to find: " + tempDir);
            assertTrue(tempDir.isDirectory(), "Needs to be a dir: " + tempDir);
            File svnDir = new File(tempDir, ".svn");
            assertTrue(svnDir.exists(), "Need to find: " + svnDir);
            assertTrue(svnDir.isDirectory(), "Need to be a dir: " + svnDir);

            SVNCommands.cleanup(tempDir);
        } finally {
            FileUtils.deleteDirectory(tempDir);
        }
    }

    @Test
    public void testMergeRevision() throws IOException {
        assumeTrue(serverAvailable(), "SVN not available at " + BASE_URL);

        File tempDir = File.createTempFile("SVNCommandsTest", ".dir");
        try {
            assertTrue(tempDir.delete());
            try (InputStream stream = SVNCommands.checkout(BASE_URL, tempDir, USERNAME, PASSWORD)) {
                System.out.println(IOUtils.toString(stream, StandardCharsets.UTF_8));
            }

            // check that the necessary structures were created
            assertEquals(SVNCommands.MergeResult.Normal, SVNCommands.mergeRevision(1, tempDir, "", BASE_URL));
        } finally {
            FileUtils.deleteDirectory(tempDir);
        }
    }

    @Test
    public void testGetBranchRevision() throws IOException {
        assumeTrue(serverAvailable(), "SVN not available at " + BASE_URL);

        File tempDir = File.createTempFile("SVNCommandsTest", ".dir");
        try {
            assertTrue(tempDir.delete());
            try (InputStream stream = SVNCommands.checkout(BASE_URL, tempDir, USERNAME, PASSWORD)) {
                System.out.println(IOUtils.toString(stream, StandardCharsets.UTF_8));
            }

            assertThrows(IOException.class,
					() -> SVNCommands.getBranchRevision("", BASE_URL));
        } finally {
            FileUtils.deleteDirectory(tempDir);
        }
    }

    @Test
    public void testBranchExists() throws IOException {
        assumeTrue(serverAvailable(), "SVN not available at " + BASE_URL);

        File tempDir = File.createTempFile("SVNCommandsTest", ".dir");
        try {
            assertTrue(tempDir.delete());
            try (InputStream stream = SVNCommands.checkout(BASE_URL, tempDir, USERNAME, PASSWORD)) {
                System.out.println(IOUtils.toString(stream, StandardCharsets.UTF_8));
            }

            assertFalse(SVNCommands.branchExists("/not_existing", BASE_URL));
            assertTrue(SVNCommands.branchExists("/README", BASE_URL));
        } finally {
            FileUtils.deleteDirectory(tempDir);
        }
    }

    @Test
    public void testCopyBranch() throws IOException {
        assumeTrue(serverAvailable(), "SVN not available at " + BASE_URL);

        File tempDir = File.createTempFile("SVNCommandsTest", ".dir");
        try {
            assertTrue(tempDir.delete());
            try (InputStream stream = SVNCommands.checkout(BASE_URL, tempDir, USERNAME, PASSWORD)) {
                System.out.println(IOUtils.toString(stream, StandardCharsets.UTF_8));
            }

            SVNCommands.copyBranch("/", "/newbranch", 1, BASE_URL);
        } finally {
            FileUtils.deleteDirectory(tempDir);
        }
    }

    @Test
    public void testUpdate() throws IOException {
        assumeTrue(serverAvailable(), "SVN not available at " + BASE_URL);

        File tempDir = File.createTempFile("SVNCommandsTest", ".dir");
        try {
            assertTrue(tempDir.delete());
            try (InputStream stream = SVNCommands.checkout(BASE_URL, tempDir, USERNAME, PASSWORD)) {
                System.out.println(IOUtils.toString(stream, StandardCharsets.UTF_8));
            }

            SVNCommands.update(tempDir);
        } finally {
            FileUtils.deleteDirectory(tempDir);
        }
    }

    @Test
    public void testVerifyNoPendingChanges() throws IOException {
        assumeTrue(serverAvailable(), "SVN not available at " + BASE_URL);

        File tempDir = File.createTempFile("SVNCommandsTest", ".dir");
        try {
            assertTrue(tempDir.delete());
            try (InputStream stream = SVNCommands.checkout(BASE_URL, tempDir, USERNAME, PASSWORD)) {
                System.out.println(IOUtils.toString(stream, StandardCharsets.UTF_8));
            }

            assertFalse(SVNCommands.verifyNoPendingChanges(tempDir));

            FileUtils.writeStringToFile(new File(tempDir, "README"), "some other string", "UTF-8");

            assertTrue(SVNCommands.verifyNoPendingChanges(tempDir));
        } finally {
            FileUtils.deleteDirectory(tempDir);
        }
    }

    @Test
    public void testRevertAll() throws IOException {
        assumeTrue(serverAvailable(), "SVN not available at " + BASE_URL);

        File tempDir = File.createTempFile("SVNCommandsTest", ".dir");
        try {
            assertTrue(tempDir.delete());
            try (InputStream stream = SVNCommands.checkout(BASE_URL, tempDir, USERNAME, PASSWORD)) {
                System.out.println(IOUtils.toString(stream, StandardCharsets.UTF_8));
            }

            SVNCommands.revertAll(tempDir);
        } finally {
            FileUtils.deleteDirectory(tempDir);
        }
    }

    @Test
    public void testCommitMergeInfo() throws IOException {
        assumeTrue(serverAvailable(), "SVN not available at " + BASE_URL);

        File tempDir = File.createTempFile("SVNCommandsTest", ".dir");
        try {
            assertTrue(tempDir.delete());
            try (InputStream stream = SVNCommands.checkout(BASE_URL, tempDir, USERNAME, PASSWORD)) {
                System.out.println(IOUtils.toString(stream, StandardCharsets.UTF_8));
            }

            SVNCommands.commitMergeInfo("some merge", tempDir);
        } finally {
            FileUtils.deleteDirectory(tempDir);
        }
    }

    @Test
    public void testGetConflicts() throws IOException {
        assumeTrue(serverAvailable(), "SVN not available at " + BASE_URL);

        File tempDir = File.createTempFile("SVNCommandsTest", ".dir");
        try {
            assertTrue(tempDir.delete());
            try (InputStream stream = SVNCommands.checkout(BASE_URL, tempDir, USERNAME, PASSWORD)) {
                System.out.println(IOUtils.toString(stream, StandardCharsets.UTF_8));
            }

            assertEquals("", SVNCommands.getConflicts(tempDir));
        } finally {
            FileUtils.deleteDirectory(tempDir);
        }
    }

    @Test
    public void testGetLastRevision() throws IOException {
        assumeTrue(serverAvailable(), "SVN not available at " + BASE_URL);

        File tempDir = File.createTempFile("SVNCommandsTest", ".dir");
        try {
            assertTrue(tempDir.delete());
            try (InputStream stream = SVNCommands.checkout(BASE_URL, tempDir, USERNAME, PASSWORD)) {
                System.out.println(IOUtils.toString(stream, StandardCharsets.UTF_8));
            }

            long rev = SVNCommands.getLastRevision("", BASE_URL, USERNAME, PASSWORD);
            assertTrue(rev >= 1);
        } finally {
            FileUtils.deleteDirectory(tempDir);
        }
    }

    @Test
    public void testRecordMerge() throws IOException {
        assumeTrue(serverAvailable(), "SVN not available at " + BASE_URL);

        File tempDir = File.createTempFile("SVNCommandsTest", ".dir");
        try {
            assertTrue(tempDir.delete());
            try (InputStream stream = SVNCommands.checkout(BASE_URL, tempDir, USERNAME, PASSWORD)) {
                System.out.println(IOUtils.toString(stream, StandardCharsets.UTF_8));
            }

            // add some minimal content
            assertTrue(new File(tempDir, "bugfix").mkdir());

            CommandLine cmdLine = new CommandLine(SVNCommands.SVN_CMD);
            cmdLine.addArgument("add");
            cmdLine.addArgument("bugfix");

             try (InputStream result = ExecutionHelper.getCommandResult(cmdLine, tempDir, 0, 360000)) {
                log.info("Svn-add reported:\n" + SVNCommands.extractResult(result));
            }

            cmdLine = new CommandLine(SVNCommands.SVN_CMD);
            cmdLine.addArgument("commit");
            cmdLine.addArgument("-m");
            cmdLine.addArgument("comment");

            try (InputStream result = ExecutionHelper.getCommandResult(cmdLine, tempDir, 0, 360000)) {
                log.info("Svn-commit reported:\n" + SVNCommands.extractResult(result));
            }

            SVNCommands.update(tempDir);

            long rev = SVNCommands.getLastRevision("/bugfix", BASE_URL, USERNAME, PASSWORD);

            //assertEquals(rev, SVNCommands.getBranchRevision("/bugfix", BASE_URL));

            InputStream bugfix = SVNCommands.recordMerge(tempDir, "/bugfix", rev);
            assertNotNull(bugfix);

            /*String revs = SVNCommands.getMergedRevisions(tempDir, "");
            assertNotNull(revs);
            assertEquals("" + rev, revs);*/
        } finally {
            FileUtils.deleteDirectory(tempDir);
        }
    }

    @Test
    public void testMergeResult() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        TestHelpers.EnumTest(SVNCommands.MergeResult.Normal, SVNCommands.MergeResult.class,
                "Normal");
    }

    // helper method to get coverage of the unused constructor
    @Test
    public void testPrivateConstructor() throws Exception {
        PrivateConstructorCoverage.executePrivateConstructor(SVNCommands.class);
    }
}
