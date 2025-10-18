package org.dstadler.commons.svn;

import java.util.Arrays;

import org.apache.commons.lang3.Strings;
import org.dstadler.commons.xml.AbstractSimpleContentHandler;
import org.xml.sax.Attributes;


/**
 * An XML SAX Parser which converts an SVN XML-Log into a Map&lt;Long, LogEntry&gt;
 * where the key is the subversion revision number and the value provides information
 * about the SVN log entry.
 */
public class SVNLogFileParser extends AbstractSimpleContentHandler<Long, LogEntry> {
	// number of paths to store with the LogEntry, if set too high, applications will go OOM...
	private static final int PATH_LIMIT = 3;

	private final static String TAG_LOG_ENTRY = "logentry";
	private final static String TAG_AUTHOR = "author";
	private final static String TAG_DATE = "date";
	private final static String TAG_MSG = "msg";
	private final static String TAG_REVISION = "revision";
	private final static String TAG_PATH = "path";
	private final static String TAG_PATH_ACTION = "action";

	private final String[] branches;
	private final LogEntryRunnable runnable;
	private final int pathLimit;

	private String lastAction = "";

	/* Structure in the XML:
<log>
<logentry revision="251622">
<author>dominik.stadler</author>
<date>2012-07-17T07:59:00.313490Z</date>
<msg>branch for 4.3 development and bugfixing
</msg>
<path
   action="M"
   kind="file">/jloadtrace/trunk-classic/non-prod/test/integration/osgi.integrationtest.test/src/com/dynatrace/diagnostics/integrationtest/remote/performance/dotnet/iis/DotNetWithAgent50UsersOverheadPerformanceTest.java</path>
</logentry>
...
*/

	/**
	 * Construct the parser with the list of branches that we are looking at.
	 *
	 * The resulting log-entries can be queried after parsing via getConfigs().
	 *
	 * @param branches An array of branch-names.
	 */
	public SVNLogFileParser(String[] branches) {
		this(branches, null, PATH_LIMIT);
	}

	/**
	 * Construct the parser with the list of branches and a runnable that is
	 * invoked for each log-entry.
	 *
	 * This can be used for streaming parsing of log-entries, e.g. for potentially
	 * large results which would consume a large amount of memory otherwise.
	 *
	 * The found log-entries are not stored and thus getConfigs() will return
	 * an empty map after parsing in this case.
	 *
	 * @param branches An array of branch-names.
	 * @param runnable A {@link LogEntryRunnable} runnable which is invoked
	 *                    once for each log-entry.
	 * @param pathLimit The maximum number of path-elements that are stored.
	 *                     This can be used to limit the amount of memory
	 *                  that is used if the actual affected path names are
	 *                  not important.
     */
	public SVNLogFileParser(String[] branches, LogEntryRunnable runnable, int pathLimit) {
		this.branches = Arrays.copyOf(branches, branches.length);
		this.runnable = runnable;
		this.pathLimit = pathLimit;
	}

	/**
	 * Internal method used for XML parsing
     */
	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) {
		if(localName.equals(TAG_LOG_ENTRY)) {
			if (currentTags != null) {
				throw new IllegalStateException("Should not have tags when a config starts in the XML, but had: " + currentTags);
			}
			currentTags = new LogEntry();
			if(attributes.getValue(TAG_REVISION) != null) {
				currentTags.revision = Long.parseLong(attributes.getValue(TAG_REVISION));
			}
		} else if (localName.equals(TAG_PATH)) {
			lastAction = attributes.getValue(TAG_PATH_ACTION);
		}
	}

	/**
	 * Internal method used for XML parsing
	 */
	@Override
	public void endElement(String uri, String localName, String qName) {
		if(localName.equals(TAG_LOG_ENTRY)) {
			if (currentTags.revision == 0) {
				throw new IllegalStateException(
						"Expected to have tag 'revision' for svn-logentry in the XML, but did not find it in: " + currentTags);
			}

			if(runnable != null) {
				runnable.run(currentTags);
			} else {
				configs.put(currentTags.revision, currentTags);
			}
			currentTags = null;
		} else {
			String value = characters.toString().trim();
			switch (localName) {
				case TAG_AUTHOR:
					currentTags.author = value.toLowerCase();
					break;
				case TAG_DATE:
					currentTags.date = value;
					break;
				case TAG_MSG:
					currentTags.msg = value;
					break;
				case TAG_PATH:
					// remove the initial branchname to reduce screen and memory size of the paths
					for (String branch : branches) {
						value = Strings.CS.removeStart(value, branch);
					}

					// only store a few paths to not go OOM with too many paths stored
					int size = currentTags.paths == null ? 0 : currentTags.paths.size();
					if (size == pathLimit) {
						currentTags.addPath(LogEntry.MORE, "");
					} else if (size < pathLimit) {
						currentTags.addPath(value, lastAction);
					}
					lastAction = "";
					break;
			}
			characters.setLength(0);
		}
	}

	/**
	 * Callback which is invoked for each log-entry to allow
	 * streaming handling of large svn-log queries.
	 */
	public interface LogEntryRunnable {
		void run(LogEntry entry);
	}
}
