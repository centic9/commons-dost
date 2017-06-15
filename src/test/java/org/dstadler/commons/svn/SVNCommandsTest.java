package org.dstadler.commons.svn;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.dstadler.commons.exec.ExecutionHelper;
import org.dstadler.commons.logging.jdk.LoggerFactory;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.TimeZone;
import java.util.logging.Logger;

import static org.junit.Assert.*;
import static org.junit.Assume.assumeTrue;

public class SVNCommandsTest {
    private final static Logger log = LoggerFactory.make();

    private static final String USERNAME = "";
    private static final String PASSWORD = null;

    private static final String BASE_URL;
    private static final File svnRepoDir;
    private static final File repoDir;
    static {
        try {
            repoDir = File.createTempFile("SVNCommandsTestRepo", ".dir");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        assertTrue(repoDir.delete());
        assertTrue(repoDir.mkdir());

        try {
            svnRepoDir = File.createTempFile("SVNCommandsTestSVNRepo", ".dir");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        assertTrue(svnRepoDir.delete());
        assertTrue(svnRepoDir.mkdir());

        CommandLine cmdLine = new CommandLine("svnadmin");
        cmdLine.addArgument("create");
        cmdLine.addArgument("project1");

        log.info("Creating local svn repository at " + repoDir);
        try (InputStream result = ExecutionHelper.getCommandResult(cmdLine, repoDir, 0, 360000)) {
            log.info("Svnadmin reported:\n" + SVNCommands.extractResult(result));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        BASE_URL = "file://" + repoDir.getAbsolutePath() + "/project1";
        log.info("Using baseUrl " + BASE_URL);

        try {
            // checkout to 2nd directory
            try (InputStream result = SVNCommands.checkout("file://" + repoDir.getAbsolutePath() + "/project1",
                    svnRepoDir, USERNAME, PASSWORD)) {
                log.info("Svn-checkout reported:\n" + SVNCommands.extractResult(result));
            }

            // add some minimal content
            FileUtils.writeStringToFile(new File(svnRepoDir, "README"), "test content", "UTF-8");
            cmdLine = new CommandLine(SVNCommands.SVN_CMD);
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
            throw new RuntimeException(e);
        }
    }

    @BeforeClass
    public static void setUpClass() {
        assumeTrue("Could not execute the SVN-command, skipping tests",
                SVNCommands.checkSVNCommand());
    }

    @AfterClass
    public static void tearDownClass() throws IOException {
        FileUtils.deleteDirectory(repoDir);
        FileUtils.deleteDirectory(svnRepoDir);
    }

    @Test
    public void testGetBranchLog() throws Exception {
        assumeTrue("SVN not available at " + BASE_URL, serverAvailable());

        Map<Long, LogEntry> log = SVNCommands.getBranchLog(new String[]{""}, 0, 1, BASE_URL, USERNAME, PASSWORD);
        assertNotNull(log);
        assertTrue(log.size() > 0);
        final String date = log.values().iterator().next().date;
        // depends on actual time of check-in to the temp-repo: assertEquals("2017-04-24T19:54:31.089532Z", date);
        assertNotNull(DateFormatUtils.ISO_8601_EXTENDED_DATETIME_FORMAT.parse(date));
    }

    @Test
    public void testSVNDirectAccess() throws IOException {
        assumeTrue("SVN not available at " + BASE_URL, serverAvailable());

        String content = IOUtils.toString(SVNCommands.getRemoteFileContent("/README", 1, BASE_URL, USERNAME, PASSWORD), "UTF-8");
        assertNotNull(content);
        assertTrue(content.contains("test content"));
    }

    @Ignore("Does not work currently due to local file repo")
    @Test
    public void canLoadBranchLogForTimeframe() throws IOException, SAXException {
        assumeTrue("SVN not available at " + BASE_URL, serverAvailable());

        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        cal.set(2015, Calendar.SEPTEMBER, 1, 0, 0, 0);
        Date startDate = cal.getTime();
        cal.set(2015, Calendar.SEPTEMBER, 2, 0, 0, 0);
        Date endDate = cal.getTime();

        Map<Long, LogEntry> log = SVNCommands.getBranchLog(new String[]{"/README"}, startDate, endDate, BASE_URL, USERNAME, PASSWORD);
        assertNotNull(log);
        assertEquals(20, log.size());
    }

    private boolean serverAvailable() throws IOException {
        // does not work with ssh-url
        //return UrlUtils.isAvailable(BASE_URL, false, true, 10000);
        return true;
    }

    @Test
    public void testCleanup() throws IOException {
        assumeTrue("SVN not available at " + BASE_URL, serverAvailable());

        File tempDir = File.createTempFile("SVNCommandsTest", ".dir");
        try {
            assertTrue(tempDir.delete());
            try (InputStream stream = SVNCommands.checkout(BASE_URL, tempDir, USERNAME, PASSWORD)) {
                System.out.println(IOUtils.toString(stream, "UTF-8"));
            }

            // check that the necessary structures were created
            assertTrue("Need to find: " + tempDir, tempDir.exists());
            assertTrue("Needs to be a dir: " + tempDir, tempDir.isDirectory());
            File svnDir = new File(tempDir, ".svn");
            assertTrue("Need to find: " + svnDir, svnDir.exists());
            assertTrue("Need to be a dir: " + svnDir, svnDir.isDirectory());

            SVNCommands.cleanup(tempDir);
        } finally {
            FileUtils.deleteDirectory(tempDir);
        }
    }
}
