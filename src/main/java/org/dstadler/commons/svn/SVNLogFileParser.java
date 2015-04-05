package org.dstadler.commons.svn;

import java.util.Arrays;

import org.apache.commons.lang3.StringUtils;
import org.dstadler.commons.xml.AbstractSimpleContentHandler;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;


/**
 * An XML SAX Parser which converts an SVN XML-Log into a Map<Long, LogEntry> objects
 * where the key is the subversion revision number.
 *
 * @author dominik.stadler
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
	 * Construct the parser with the name of the branch that we are looking at.
	 *
	 * @param branches
	 */
	public SVNLogFileParser(String[] branches) {
		this(branches, null, PATH_LIMIT);
	}

	public SVNLogFileParser(String[] branches, LogEntryRunnable runnable, int pathLimit) {
		this.branches = Arrays.copyOf(branches, branches.length);
		this.runnable = runnable;
		this.pathLimit = pathLimit;
	}

	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
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

	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {
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
			if(localName.equals(TAG_AUTHOR)) {
				currentTags.author = value.toLowerCase();
			} else if(localName.equals(TAG_DATE)) {
				currentTags.date = value;
			} else if(localName.equals(TAG_MSG)) {
				currentTags.msg = value;
			} else if(localName.equals(TAG_PATH)) {
				// remove the initial branchname to reduce screen and memory size of the paths
				for(String branch : branches) {
					value = StringUtils.removeStart(value, branch);
				}

				// only store a few paths to not go OOM with too many paths stored
				int size = currentTags.paths == null ? 0 : currentTags.paths.size();
				if(size == pathLimit) {
					currentTags.addPath(LogEntry.MORE, "");
				} else if (size < pathLimit) {
					currentTags.addPath(value, lastAction);
				}
				lastAction = "";
			}
			characters.setLength(0);
		}
	}

	public interface LogEntryRunnable {
		public void run(LogEntry entry);
	}
}
