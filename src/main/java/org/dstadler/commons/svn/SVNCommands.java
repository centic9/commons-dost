package org.dstadler.commons.svn;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.dstadler.commons.arrays.ArrayUtils;
import org.dstadler.commons.exec.ExecutionHelper;
import org.dstadler.commons.logging.jdk.LoggerFactory;
import org.xml.sax.SAXException;


/**
 * Helper class which provide a number of SVN commands including merging, commit, updating, ...
 */
@SuppressWarnings("unused")
public class SVNCommands {
    private final static Logger log = LoggerFactory.make();

    private static final int COMMIT_RETRY_COUNT = 3;

    public static final String SVN_CMD = "svn";

    private static final String INFINITY = "infinity";
    private static final String AT_REVISION = " at revision ";

    private static final String OPT_XML = "--xml";
    private static final String OPT_DEPTH = "--depth";

    private static final String CMD_MERGE = "merge";
    private static final String CMD_STATUS = "status";
    private static final String CMD_REVERT = "revert";
    private static final String CMD_LOG = "log";
    private static final String CMD_CAT = "cat";

    public enum MergeResult {
        OnlyMergeInfo,
        Normal,
        Conflicts
    }

    /**
     * Retrieve the XML-log of changes on the given branch, starting with the given
     * revision up to HEAD
     *
     * @param branches The list of branches to fetch logs for, currently only the first entry is used!
     * @param startRevision The SVN revision to use as starting point for the log-entries.
     * @param baseUrl       The SVN url to connect to
     * @param user          The SVN user or null if the default user from the machine should be used
     * @param pwd           The SVN password or null if the default user from the machine should be used   @return A mapping of revision numbers to the {@link LogEntry}.
     *
     * @return All matching log-entries as map of timestamp to {@link LogEntry}
     *
     * @throws IOException Execution of the SVN sub-process failed or the
     *          sub-process returned a exit value indicating a failure
     * @throws SAXException If the resulting SVN XML log output could not be parsed
     */
    public static Map<Long, LogEntry> getBranchLog(String[] branches, long startRevision, String baseUrl, String user, String pwd) throws IOException, SAXException {
        try (InputStream inStr = getBranchLogStream(branches, startRevision, -1, baseUrl, user, pwd)) {
            return new SVNLogFileParser(branches).parseContent(inStr);
        } catch (SAXException e) {
            throw new SAXException("While parsing branch-log of " + Arrays.toString(branches) + ", start: " + startRevision + " with user " + user, e);
        }
    }

    /**
     * Retrieve the XML-log of changes on the given branch, in a specified range of revisions.
     *
     * @param branches The list of branches to fetch logs for, currently only the first entry is used!
     * @param startRevision The SVN revision to use as starting point for the log-entries.
     * @param endRevision   The SVN revision to use as end point for the log-entries. In case <code>endRevision</code> is <code>-1</code>, HEAD revision is being used
     * @param baseUrl       The SVN url to connect to
     * @param user          The SVN user or null if the default user from the machine should be used
     * @param pwd           The SVN password or null if the default user from the machine should be used   @return A mapping of revision numbers to the {@link LogEntry}.
     *
     * @return All matching log-entries as map of timestamp to {@link LogEntry}
     *
     * @throws IOException Execution of the SVN sub-process failed or the
     *          sub-process returned a exit value indicating a failure
     * @throws SAXException If the resulting SVN XML log output could not be parsed
     */
    public static Map<Long, LogEntry> getBranchLog(String[] branches, long startRevision, long endRevision, String baseUrl, String user, String pwd) throws IOException, SAXException {
        try (InputStream inStr = getBranchLogStream(branches, startRevision, endRevision, baseUrl, user, pwd)) {
            return new SVNLogFileParser(branches).parseContent(inStr);
        } catch (SAXException e) {
            throw new SAXException("While parsing branch-log of " + Arrays.toString(branches) + ", start: " + startRevision + ", end: " + endRevision + " with user " + user, e);
        }
    }

    /**
     * Retrieve the XML-log of changes on the given branch, in a specific time frame.
     *
     * @param branches The list of branches to fetch logs for, currently only the first entry is used!
     * @param startDate The starting date for the log-entries that are fetched
     * @param endDate   The end date for the log-entries that are fetched
     * @param baseUrl       The SVN url to connect to
     * @param user      The SVN user or null if the default user from the machine should be used
     * @param pwd       The SVN password or null if the default user from the machine should be used   @return A mapping of revision numbers to the {@link LogEntry}.
     *
     * @return All matching log-entries as map of timestamp to {@link LogEntry}
     *
     * @throws IOException Execution of the SVN sub-process failed or the
     *          sub-process returned a exit value indicating a failure
     * @throws SAXException If the resulting SVN XML log output could not be parsed
     */
    public static Map<Long, LogEntry> getBranchLog(String[] branches, Date startDate, Date endDate, String baseUrl, String user, String pwd) throws IOException, SAXException {
        try (InputStream inStr = getBranchLogStream(branches, startDate, endDate, baseUrl, user, pwd)) {
            return new SVNLogFileParser(branches).parseContent(inStr);
        } catch (SAXException e) {
            throw new SAXException("While parsing branch-log of " + Arrays.toString(branches) + ", start: " + startDate + ", end: " + endDate + " with user " + user, e);
        }
    }

    /**
     * Same as {@link #getBranchLogStream(String[], long, long, String, String, String)}
     * but always uses SVN HEAD revision as end revision.
     *
     * @param branches The list of branches to fetch logs for, currently only the first entry is used!
     * @param startRevision The SVN revision to use as starting point for the log-entries.
     * @param baseUrl       The SVN url to connect to
     * @param user          The SVN user or null if the default user from the machine should be used
     * @param pwd           The SVN password or null if the default user from the machine should be used   @return The result of the "svn log -xml" call, should be closed by the caller
     *
     * @return An InputStream which provides the XML-log response
     *
     * @throws IOException Execution of the SVN sub-process failed or the
     *          sub-process returned a exit value indicating a failure
     */
    public static InputStream getBranchLogStream(String[] branches, long startRevision, String baseUrl, String user, String pwd) throws IOException {
        return getBranchLogStream(branches, startRevision, -1, baseUrl, user, pwd);
    }

    /**
     * Retrieve the XML-log of changes on the given branch, starting with the given
     * revision up to HEAD. This method returns an {@link InputStream} that can be used
     * to read and process the XML data without storing the complete result. This is useful
     * when you are potentially reading many revisions and thus need to avoid being limited
     * in memory or disk.
     *
     * @param branches The list of branches to fetch logs for, currently only the first entry is used!
     * @param startRevision The SVN revision to use as starting point for the log-entries.
     * @param endRevision   The SVN revision to use as end point for the log-entries. In case <code>endRevision</code> is <code>-1</code>, HEAD revision is being used
     * @param baseUrl       The SVN url to connect to
     * @param user          The SVN user or null if the default user from the machine should be used
     * @param pwd           The SVN password or null if the default user from the machine should be used   @return A stream that can be used to read the XML data, should be closed by the caller
     *
     * @return An InputStream which provides the XML-log response
     *
     * @throws IOException Execution of the SVN sub-process failed or the
     *          sub-process returned a exit value indicating a failure
     */
    public static InputStream getBranchLogStream(String[] branches, long startRevision, long endRevision, String baseUrl, String user, String pwd) throws IOException {
        CommandLine cmdLine = getCommandLineForXMLLog(user, pwd);
        cmdLine.addArgument(startRevision + ":" + (endRevision != -1 ? endRevision : "HEAD")); // use HEAD if no valid endRevision given (= -1)
        cmdLine.addArgument(baseUrl + branches[0]);

        return ExecutionHelper.getCommandResult(cmdLine, new File("."), 0, 120000);
    }

    /**
     * Retrieve the XML-log of changes on the given branch, starting from and ending with a specific date
     * This method returns an {@link InputStream} that can be used
     * to read and process the XML data without storing the complete result. This is useful
     * when you are potentially reading many revisions and thus need to avoid being limited
     * in memory or disk.
     *
     * @param branches The list of branches to fetch logs for, currently only the first entry is used!
     * @param startDate The starting date for the log-entries that are fetched
     * @param endDate   In case <code>endDate</code> is not specified, the current date is used
     * @param baseUrl       The SVN url to connect to
     * @param user      The SVN user or null if the default user from the machine should be used
     * @param pwd       The SVN password or null if the default user from the machine should be used   @return A stream that can be used to read the XML data, should be closed by the caller
     *
     * @return An InputStream which provides the XML-log response
     *
     * @throws IOException Execution of the SVN sub-process failed or the
     *          sub-process returned a exit value indicating a failure
     */
    public static InputStream getBranchLogStream(String[] branches, Date startDate, Date endDate, String baseUrl, String user, String pwd) throws IOException {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mmZ", Locale.ROOT);
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

        CommandLine cmdLine = getCommandLineForXMLLog(user, pwd);
        cmdLine.addArgument("{" + dateFormat.format(startDate) + "}:{" + dateFormat.format(endDate) + "}");
        cmdLine.addArgument(baseUrl + branches[0]);

        return ExecutionHelper.getCommandResult(cmdLine, new File("."), 0, 120000);
    }

    private static CommandLine getCommandLineForXMLLog(String user, String pwd) {
        CommandLine cmdLine = new CommandLine(SVN_CMD);
        cmdLine.addArgument(CMD_LOG);
        cmdLine.addArgument(OPT_XML);
        addDefaultArguments(cmdLine, user, pwd);
        cmdLine.addArgument("-v");    // to get paths as well
        cmdLine.addArgument("-r");
        return cmdLine;
    }

    /**
     * Retrieve the contents of a file from the web-interface of the SVN server.
     *
     * @param file The file to fetch from the SVN server via
     * @param revision      The SVN revision to use
     * @param baseUrl       The SVN url to connect to
     * @param user The SVN user or null if the default user from the machine should be used
     * @param pwd  The SVN password or null if the default user from the machine should be used   @return The contents of the file.
     *
     * @return An InputStream which provides the content of the revision of the specified file
     *
     * @throws IOException Execution of the SVN sub-process failed or the
     *          sub-process returned a exit value indicating a failure
     */
    public static InputStream getRemoteFileContent(String file, long revision, String baseUrl, String user, String pwd) throws IOException {
        // svn cat -r 666 file
        CommandLine cmdLine = new CommandLine(SVN_CMD);
        cmdLine.addArgument(CMD_CAT);
        addDefaultArguments(cmdLine, user, pwd);
        cmdLine.addArgument("-r");
        cmdLine.addArgument(Long.toString(revision));
        cmdLine.addArgument(baseUrl + file);

        return ExecutionHelper.getCommandResult(cmdLine, new File("."), 0, 120000);
    }

    /**
     * Check if the given branch already exists
     *
     * @param branch The name of the branch including the path to the branch, e.g. branches/4.2.x
     * @param baseUrl       The SVN url to connect to
     * @return true if the branch already exists, false otherwise.
     */
    public static boolean branchExists(String branch, String baseUrl) {
        CommandLine cmdLine = new CommandLine(SVN_CMD);
        cmdLine.addArgument(CMD_LOG);
        cmdLine.addArgument(OPT_XML);
        addDefaultArguments(cmdLine, null, null);
        cmdLine.addArgument("-r");
        cmdLine.addArgument("HEAD:HEAD");
        cmdLine.addArgument(baseUrl + branch);

        try (InputStream inStr = ExecutionHelper.getCommandResult(cmdLine, new File("."), 0, 120000)) {
            return true;
        } catch (IOException e) {
            log.log(Level.FINE, "Branch " + branch + " not found or other error", e);
            return false;
        }
    }

    /**
     * Return the revision from which the branch was branched off.
     *
     * @param branch The name of the branch including the path to the branch, e.g. branches/4.2.x
     * @param baseUrl       The SVN url to connect to
     * @return The revision where the branch was branched off from it's parent branch.
     * @throws IOException Execution of the SVN sub-process failed or the
     *          sub-process returned a exit value indicating a failure
     */
    public static long getBranchRevision(String branch, String baseUrl) throws IOException {
        CommandLine cmdLine = new CommandLine(SVN_CMD);
        cmdLine.addArgument(CMD_LOG);
        cmdLine.addArgument(OPT_XML);
        addDefaultArguments(cmdLine, null, null);
        cmdLine.addArgument("-v");
        cmdLine.addArgument("-r0:HEAD");
        cmdLine.addArgument("--stop-on-copy");
        cmdLine.addArgument("--limit");
        cmdLine.addArgument("1");
        cmdLine.addArgument(baseUrl + branch);

        try (InputStream inStr = ExecutionHelper.getCommandResult(cmdLine, new File("."), 0, 120000)) {
            String xml = IOUtils.toString(inStr, StandardCharsets.UTF_8);
            log.info("XML:\n" + xml);

            // read the revision
            Matcher matcher = Pattern.compile("copyfrom-rev=\"([0-9]+)\"").matcher(xml);
            if (!matcher.find()) {
                throw new IOException("Could not find copyfrom-rev entry in xml: " + xml);
            }

            log.info("Found copyfrom-rev: " + matcher.group(1));
            return Long.parseLong(matcher.group(1));
        }
    }

    /**
     * Return the last revision of the given branch. Uses the full svn repository if branch is ""
     *
     * @param branch The name of the branch including the path to the branch, e.g. branches/4.2.x
     * @param baseUrl       The SVN url to connect to
     * @param user The SVN user or null if the default user from the machine should be used
     * @param pwd  The SVN password or null if the default user from the machine should be used   @return The contents of the file.
     * @return The last revision where a check-in was made on the branch
     * @throws IOException Execution of the SVN sub-process failed or the
     *          sub-process returned a exit value indicating a failure
     */
    public static long getLastRevision(String branch, String baseUrl, String user, String pwd) throws IOException {
        CommandLine cmdLine = new CommandLine(SVN_CMD);
        cmdLine.addArgument("info");
        addDefaultArguments(cmdLine, user, pwd);
        cmdLine.addArgument(baseUrl + branch);

        try (InputStream inStr = ExecutionHelper.getCommandResult(cmdLine, new File("."), 0, 120000)) {
            String info = IOUtils.toString(inStr, StandardCharsets.UTF_8);
            log.info("Info:\n" + info);

			/* svn info http://...

			 	Repository Root: https://svn-lnz.emea.cpwr.corp/svn/dev
				Repository UUID: 35fb04cf-4f84-b44d-92fa-8d0d0442729e
				Revision: 390864
				Node Kind: directory
			*/

            // read the revision
            Matcher matcher = Pattern.compile("Revision: ([0-9]+)").matcher(info);
            if (!matcher.find()) {
                throw new IOException("Could not find Revision entry in info-output: " + info);
            }

            log.info("Found rev: " + matcher.group(1) + " for branch " + branch);
            return Long.parseLong(matcher.group(1));
        }
    }

    private static void addDefaultArguments(CommandLine cmdLine, String user, String pwd) {
        cmdLine.addArgument("--non-interactive");
        cmdLine.addArgument("--trust-server-cert");
        if (StringUtils.isNotEmpty(user) && StringUtils.isNoneEmpty(pwd)) {
            cmdLine.addArgument("--username");
            cmdLine.addArgument(user);
            cmdLine.addArgument("--password");
            cmdLine.addArgument(pwd);
            // do not store this username/password combination in the local configuration of the machine
            cmdLine.addArgument("--no-auth-cache");
        }
    }

    /**
     * Performs a "svn status" and returns the list of changes in the svn tree at the given directory
     * in the format that the svn command provides them.
     *
     * @param directory The local working directory
     * @return A stream which returns a textual list of changes as reported by "svn status", should be closed by the caller
     * @throws IOException Execution of the SVN sub-process failed or the
     *          sub-process returned a exit value indicating a failure
     */
    public static InputStream getPendingCheckins(File directory) throws IOException {
        CommandLine cmdLine = new CommandLine(SVN_CMD);
        cmdLine.addArgument(CMD_STATUS);
        addDefaultArguments(cmdLine, null, null);

        return ExecutionHelper.getCommandResult(cmdLine, directory, -1, 120000);
    }

    /**
     * Performs a "svn status" and returns any conflicting change that is found in the svn tree at the given directory
     * in the format that the svn command provides them (including the leading 'C').
     *
     * @param directory The local working directory
     * @return A stream which returns the textual list of conflicts as reported by "svn status"
     * @throws IOException Execution of the SVN sub-process failed or the
     *          sub-process returned a exit value indicating a failure
     */
    public static String getConflicts(File directory) throws IOException {
        try (InputStream stream = getPendingCheckins(directory)) {
            StringBuilder result = new StringBuilder();
            List<String> lines = IOUtils.readLines(stream, "UTF-8");
            for (String line : lines) {
                // first char "C" is a normal conflict, C at second position is a property-conflict
                if (line.length() >= 2 &&
                        (line.charAt(0) == 'C' || line.charAt(1) == 'C')) {
                    result.append(line).append("\n");
                }
            }
            return result.toString();
        }
    }

    /**
     * Will commit the merge info on the root branch.
     * <p>
     * Attention: This will revert all changes on the working copy except the property on the
     * root-dir. Be careful to not run this on a working copy with other relevant changes!
     *
     * @param comment The comment to use for the commit
     * @param directory The local working directory
     * @throws IOException Execution of the SVN sub-process failed or the
     *          sub-process returned a exit value indicating a failure
     */
    public static void commitMergeInfo(String comment, File directory) throws IOException {
        // all these additional merge-info updates on the sub-directories and items just
        // clobbers the svn history, revert all those and just keep the merge-info on the root-directory
        revertAllButRootDir(directory);

        IOException exception = null;
        for (int i = 0; i < COMMIT_RETRY_COUNT; i++) {
            try {
                // update once more
                SVNCommands.update(directory);

                // now we can commit the resulting file, alternatively we could also just commit "." here and revert everything else afterwards
                CommandLine cmdLine = new CommandLine(SVN_CMD);
                cmdLine.addArgument("commit");
                addDefaultArguments(cmdLine, null, null);
                cmdLine.addArgument("-m");
                cmdLine.addArgument(comment);
                //cmdLine.addArgument(".");	// current dir

                try (InputStream result = ExecutionHelper.getCommandResult(cmdLine, directory, 0, 120000)) {
                    log.info("Svn-Commit reported:\n" + IOUtils.toString(result, StandardCharsets.UTF_8));
                }
                return;
            } catch (IOException e) {
                // try again if we find the general error message which usually indicates that another
                // check-in was made in between and we need to re-update once more
                if (e.getMessage().contains("Process exited with an error: 1 (Exit value: 1)")) {
                    exception = e;

                    log.log(Level.WARNING, "Retrying merge info commit as we had an error which usually indicates that another developer checked in something: " + e.getMessage());
                    continue;
                }

                throw e;
            }
        }

        throw exception;
    }

    private static void revertAllButRootDir(File trunkDir) throws IOException {
        log.info("Reverting all but the main directory in " + trunkDir);

        CommandLine cmdLine = new CommandLine(SVN_CMD);
        cmdLine.addArgument(CMD_REVERT);
        addDefaultArguments(cmdLine, null, null);
        cmdLine.addArgument(OPT_DEPTH);
        cmdLine.addArgument(INFINITY);
        cmdLine.addArgument("*");

        try (InputStream result = ExecutionHelper.getCommandResult(cmdLine, trunkDir, 0, 120000)) {
            log.info("Svn-RevertAllButRootdir reported:\n" + extractResult(result));
        }
    }

    /**
     * Performs a "svn up" in the given local working directory.
     *
     * @param directory The local working directory
     * @throws IOException Execution of the SVN sub-process failed or the
     *          sub-process returned a exit value indicating a failure
     */
    public static void update(File directory) throws IOException {
        log.info("Updating SVN Working copy at " + directory);

        CommandLine cmdLine = new CommandLine(SVN_CMD);
        addDefaultArguments(cmdLine, null, null);
        cmdLine.addArgument("up");

        // allow up to 5 minutes for update, sometimes it takes a while if there were many changes
        try (InputStream result = ExecutionHelper.getCommandResult(cmdLine, directory, 0, 5 * 60 * 1000)) {
            log.info("Svn-Update reported:\n" + extractResult(result));
        }
    }

    /**
     * Record a manual merge from one branch to the local working directory.
     *
     * @param directory The local working directory
     * @param branchName The branch that was merged in manually
     * @param revisions The list of merged revisions.
     * @return A stream which provides the output of the "svn merge" command, should be closed by the caller
     * @throws IOException Execution of the SVN sub-process failed or the
     *          sub-process returned a exit value indicating a failure
     */
    public static InputStream recordMerge(File directory, String branchName, long... revisions) throws IOException {
        // 				svn merge -c 3328 --record-only ^/calc/trunk
        CommandLine cmdLine = new CommandLine(SVN_CMD);
        cmdLine.addArgument(CMD_MERGE);
        addDefaultArguments(cmdLine, null, null);
        cmdLine.addArgument("--record-only");
        cmdLine.addArgument("-c");
        StringBuilder revs = new StringBuilder();
        for (long revision : revisions) {
            revs.append(revision).append(",");
        }
        cmdLine.addArgument(revs.toString());
        // leads to "non-inheritable merges"
        // cmdLine.addArgument("--depth");
        // cmdLine.addArgument("empty");
        cmdLine.addArgument("^" + branchName);
        //cmdLine.addArgument(".");	// current dir

        return ExecutionHelper.getCommandResult(cmdLine, directory, 0, 120000);
    }

    /**
     * Check if there are pending changes on the branch, returns true if changes are found.
     *
     * @param directory The local working directory
     * @return True if there are pending changes, false otherwise
     * @throws IOException Execution of the SVN sub-process failed or the
     *          sub-process returned a exit value indicating a failure
     */
    public static boolean verifyNoPendingChanges(File directory) throws IOException {
        log.info("Checking that there are no pending changes on trunk-working copy");
        try (InputStream inStr = getPendingCheckins(directory)) {
            List<String> lines = IOUtils.readLines(inStr, "UTF-8");
            if (lines.size() > 0) {
                log.info("Found the following checkouts:\n" + ArrayUtils.toString(lines.toArray(), "\n"));
                return true;
            }
        }

        return false;
    }

	/*public static void tryMergeRevision(long revision, File TRUNK_DIR, String BRANCH_NAME) throws IOException {
		// svn merge -r 1288:1351 http://svn.example.com/myrepos/branch
		CommandLine cmdLine = new CommandLine(SVN_CMD);
		cmdLine.addArgument("merge");
		cmdLine.addArgument("--dry-run");
		cmdLine.addArgument("-c");
		cmdLine.addArgument(Long.toString(revision));
		cmdLine.addArgument(SVN_baseUrl + BRANCH_NAME);

		InputStream result = ExecutionHelper.getCommandResult(cmdLine, TRUNK_DIR, 0, 120000);
		try {
			log.info("Svn-Merge reported:\n" + IOUtils.toString(result));
		} finally {
			result.close();
		}
	}*/

    /**
     * Merge the given revision and return true if only mergeinfo changes were done on trunk.
     *
     * @param revision The revision to merge, this should be on a different branch
     * @param directory The local working directory
     * @param branch The name of the branch to merge
     * @param baseUrl       The SVN url to connect to
     * @return a MergeResult which indicates if only merge-info changes were done or if conflicts were encountered
     * @throws IOException Execution of the SVN sub-process failed or the
     *          sub-process returned a exit value indicating a failure
     */
    public static MergeResult mergeRevision(long revision, File directory, String branch, String baseUrl) throws IOException {
        // svn merge -r 1288:1351 http://svn.example.com/myrepos/branch
        CommandLine cmdLine = new CommandLine(SVN_CMD);
        cmdLine.addArgument(CMD_MERGE);
        addDefaultArguments(cmdLine, null, null);
        cmdLine.addArgument("-x");
        cmdLine.addArgument("-uw --ignore-eol-style");        // unified-diff and ignore all whitespace changes
        cmdLine.addArgument("--accept");
        cmdLine.addArgument("postpone");
        cmdLine.addArgument("-c");
        cmdLine.addArgument(Long.toString(revision));
        cmdLine.addArgument(baseUrl + branch);

        try (InputStream result = ExecutionHelper.getCommandResult(cmdLine, directory, 0, 120000)) {
            String output = extractResult(result);
            boolean foundActualMerge = false;
            boolean foundConflict = false;
            // check if only svn-properties were updated
            for (String line : output.split("\n")) {
                // ignore the comments about what is done
                if (line.startsWith("--- ")) {
                    continue;
                }

                if (line.length() >= 4 && line.substring(0, 4).contains("C")) {
                    // C..Conflict
                    foundConflict = true;
                    log.info("Found conflict!");
                    break;
                } else if (!line.startsWith(" U   ") && !line.startsWith(" G   ")) {
                    // U..Updated, G..Merged, on second position means "property", any other change is an actual merge
                    foundActualMerge = true;
                    log.info("Found actual merge: " + line);
                }
            }
            log.info("Svn-Merge reported:\n" + output);

            if (foundConflict) {
                return MergeResult.Conflicts;
            }

            if (!foundActualMerge) {
                log.info("Only mergeinfo updates found after during merge.");
                return MergeResult.OnlyMergeInfo;
            }
        }

        return MergeResult.Normal;
    }

    /**
     * Retrieve a list of all merged revisions.
     *
     * @param directory The local working directory
     * @param branches The list of branches to fetch logs for
     * @return A string listing all SVN revision numbers that were merged, formatted via List.toString()
     * @throws IOException Execution of the SVN sub-process failed or the
     *          sub-process returned a exit value indicating a failure
     */
    public static String getMergedRevisions(File directory, String... branches) throws IOException {
        // we could also use svn mergeinfo --show-revs merged ^/trunk ^/branches/test
        CommandLine cmdLine;
        cmdLine = new CommandLine(SVN_CMD);
        cmdLine.addArgument("propget");
        addDefaultArguments(cmdLine, null, null);
        cmdLine.addArgument("svn:mergeinfo");

        StringBuilder MERGED_REVISIONS_BUILD = new StringBuilder();

        try (InputStream ignoreStr = ExecutionHelper.getCommandResult(cmdLine, directory, 0, 120000)) {
            List<String> lines = IOUtils.readLines(ignoreStr, "UTF-8");
            for (String line : lines) {
                for (String source : branches) {
                    if (line.startsWith(source + ":")) {
                        log.info("Found merged revisions for branch: " + source);
                        MERGED_REVISIONS_BUILD.append(",").append(line.substring(source.length() + 1)); // + 1 for ":"
                        break;
                    }
                }
            }
        }

        String MERGED_REVISIONS = Strings.CS.removeStart(MERGED_REVISIONS_BUILD.toString(), ",");

        if (MERGED_REVISIONS.isEmpty()) {
            throw new IllegalStateException("Could not read merged revision with command " + cmdLine + " in directory " + directory);
        }

        // expand ranges r1-r2 into separate numbers for easier search in the string later on
        List<Long> revList = new ArrayList<>();
        String[] revs = MERGED_REVISIONS.split(",");
        for (String rev : revs) {
            if (rev.contains("-")) {
                String[] revRange = rev.split("-");
                if (revRange.length != 2) {
                    throw new IllegalStateException("Expected to have start and end of range, but had: " + rev);
                }

                for (long r = Long.parseLong(revRange[0]); r <= Long.parseLong(revRange[1]); r++) {
                    revList.add(r);
                }
            } else {
                // non-inheritable merge adds a "*" to the rev, see https://groups.google.com/forum/?fromgroups=#!topic/subversion-svn/ArXTv1rUk5w
                rev = Strings.CS.removeEnd(rev, "*");

                revList.add(Long.parseLong(rev));
            }
        }

        return revList.toString();
    }

    /**
     * Revert all changes pending in the given SVN Working Copy.
     *
     * @param directory The local working directory
     * @throws IOException Execution of the SVN sub-process failed or the
     *          sub-process returned a exit value indicating a failure
     */
    public static void revertAll(File directory) throws IOException {
        log.info("Reverting SVN Working copy at " + directory);

        CommandLine cmdLine = new CommandLine(SVN_CMD);
        cmdLine.addArgument(CMD_REVERT);
        addDefaultArguments(cmdLine, null, null);
        cmdLine.addArgument(OPT_DEPTH);
        cmdLine.addArgument(INFINITY);
        cmdLine.addArgument(directory.getAbsolutePath());

        try (InputStream result = ExecutionHelper.getCommandResult(cmdLine, directory, 0, 120000)) {
            log.info("Svn-RevertAll reported:\n" + extractResult(result));
        }
    }

    /**
     * Run "svn cleanup" on the given working copy.
     *
     * @param directory The local working directory
     * @throws IOException Execution of the SVN sub-process failed or the
     *          sub-process returned a exit value indicating a failure
     */
    public static void cleanup(File directory) throws IOException {
        log.info("Cleaning SVN Working copy at " + directory);

        CommandLine cmdLine = new CommandLine(SVN_CMD);
        cmdLine.addArgument("cleanup");
        addDefaultArguments(cmdLine, null, null);

        try (InputStream result = ExecutionHelper.getCommandResult(cmdLine, directory, 0, 360000)) {
            log.info("Svn-Cleanup reported:\n" + extractResult(result));
        }
    }

    /**
     * Performs a SVN Checkout of the given URL to the given directory
     *
     * @param url       The SVN URL that should be checked out
     * @param directory The location where the working copy is created.
     * @param user The SVN user or null if the default user from the machine should be used
     * @param pwd  The SVN password or null if the default user from the machine should be used   @return The contents of the file.
     * @return A stream with output from the command, should be closed by the caller
     * @throws IOException Execution of the SVN sub-process failed or the
     *          sub-process returned a exit value indicating a failure
     */
    public static InputStream checkout(String url, File directory, String user, String pwd) throws IOException {
        if (!directory.exists() && !directory.mkdirs()) {
            throw new IOException("Could not create new working copy directory at " + directory);
        }

        CommandLine cmdLine = new CommandLine(SVN_CMD);
        cmdLine.addArgument("co");
        addDefaultArguments(cmdLine, user, pwd);
        cmdLine.addArgument(url);
        cmdLine.addArgument(directory.toString());

        // allow up to two hour for new checkouts
        return ExecutionHelper.getCommandResult(cmdLine, directory, -1, 2 * 60 * 60 * 1000);
    }

    protected static String extractResult(InputStream result) throws IOException {
        String string = IOUtils.toString(result, StandardCharsets.UTF_8).trim();
        // don't include single newlines in the log-output
        return string.equals("\n") ? "" : string;
    }

    /**
     * Make a branch by calling the "svn cp" operation.
     *
     * @param base The source of the SVN copy operation
     * @param branch The name and location of the new branch
     * @param revision The revision to base the branch off
     * @param baseUrl       The SVN url to connect to
     * @throws IOException Execution of the SVN sub-process failed or the
     *          sub-process returned a exit value indicating a failure
     */
    public static void copyBranch(String base, String branch, long revision, String baseUrl) throws IOException {
        log.info("Copying branch " + base + AT_REVISION + revision + " to branch " + branch);

        CommandLine cmdLine = new CommandLine(SVN_CMD);
        cmdLine.addArgument("cp");
        addDefaultArguments(cmdLine, null, null);
        if (revision > 0) {
            cmdLine.addArgument("-r" + revision);
        }
        cmdLine.addArgument("-m");
        cmdLine.addArgument("Branch automatically created from " + base + (revision > 0 ? AT_REVISION + revision : ""));
        cmdLine.addArgument(baseUrl + base);
        cmdLine.addArgument(baseUrl + branch);

		/*
		svn copy -r123 http://svn.example.com/repos/calc/trunk \
    		http://svn.example.com/repos/calc/branches/my-calc-branch
    		 */

        try (InputStream result = ExecutionHelper.getCommandResult(cmdLine, new File("."), 0, 120000)) {
            log.info("Svn-Copy reported:\n" + extractResult(result));
        }
    }

    public static boolean checkSVNCommand() {
        log.info("Checking if SVN command is available");

        CommandLine cmdLine = new CommandLine(SVN_CMD);
        cmdLine.addArgument("--version");

        try (InputStream result = ExecutionHelper.getCommandResult(cmdLine, new File("."), 0, 360000)) {
            log.info("Svn-Version reported:\n" + extractResult(result));
            return true;
        } catch (IOException e) {
            return false;
        }
    }
}
