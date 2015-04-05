package org.dstadler.commons.svn;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang3.tuple.Pair;
import org.dstadler.commons.arrays.ArrayUtils;
import org.dstadler.commons.testing.TestHelpers;
import org.junit.Test;
import org.xml.sax.SAXException;


/**
 *
 * @author dominik.stadler
 */
public class SVNLogFileParserTest {

	@Test
	public void test() throws SAXException, IOException {
		try (InputStream inStr = new FileInputStream(new File("src/test/data/svnlog.xml"))) {
			Map<Long, LogEntry> parsed = new SVNLogFileParser(new String[] {}).parseContent(inStr);
			assertNotNull(parsed);

			assertTrue("Had: " + parsed, parsed.containsKey(1798l));
			assertEquals("somebody", parsed.get(1798l).author);
			assertEquals("2015-03-31T11:03:42.600994Z", parsed.get(1798l).date);
			assertEquals("Add gradle-wrapper", parsed.get(1798l).msg);
			assertEquals(1798l, parsed.get(1798l).revision);
		}
	}

	@Test
	public void testWithPaths() throws SAXException, IOException {
		try (InputStream inStr = new FileInputStream(new File("src/test/data/svnlogwithpath.xml"))) {
			Map<Long, LogEntry> parsed = new SVNLogFileParser(new String[] {"/trunk/CommonCrawl"}).parseContent(inStr);
			assertNotNull(parsed);

			// rev. 1799: 6 paths
			LogEntry entry = parsed.get(1799l);
			assertNotNull(entry);
			assertNotNull(entry.paths);
			assertEquals("Expect 4 (3 + ....) for 6 paths, but had: " + entry.paths, 4, entry.paths.size());
			Iterator<Pair<String,String>> it = entry.paths.iterator();
			it.next();
			it.next();
			it.next();
			Pair<String,String> pair = it.next();
			String next = pair.getLeft();
			assertEquals("Last path is only '...' to indicate more paths, but had: " + ArrayUtils.toString(entry.paths.toArray(), "\n"), LogEntry.MORE, next);
			assertEquals("", pair.getRight());

			// rev. 1790: 4 paths
			entry = parsed.get(1790l);
			assertNotNull(entry);
			assertNotNull(entry.paths);
			assertEquals("Expect 4 (3 + ....) for 4 paths, but had: " + entry.paths, 4, entry.paths.size());
			it = entry.paths.iterator();
			it.next();
			it.next();
			it.next();
			pair = it.next();
			next = pair.getLeft();
			assertEquals("Last path is only '...' to indicate more paths, but had: " + ArrayUtils.toString(entry.paths.toArray(), "\n"), LogEntry.MORE, next);
			assertEquals("", pair.getRight());

			// rev. 1800: 3 paths
			entry = parsed.get(1800l);
			assertNotNull(entry);
			assertNotNull(entry.paths);
			assertEquals("Expect 3 paths, but had: " + entry.paths, 3, entry.paths.size());
			it = entry.paths.iterator();
			it.next();
			it.next();
			pair = it.next();
			next = pair.getLeft();
			assertFalse("Last path is '...', had: " + entry.paths, next.equals(LogEntry.MORE));
			assertEquals("A", pair.getRight());

			// rev. 1797: 1 path
			entry = parsed.get(1797l);
			assertNotNull(entry);
			assertNotNull(entry.paths);
			assertEquals("Expect 1 paths, but had: " + entry.paths, 1, entry.paths.size());
			assertFalse("Path is '...', had: " + entry.paths, entry.paths.iterator().next().equals(LogEntry.MORE));

			// rev. 1791: 0 paths
			entry = parsed.get(1791l);
			assertNotNull(entry);
			assertNull(entry.paths);
		}
	}

	@Test
	public void testWithDifferentPathLimit() throws SAXException, IOException {
		try (InputStream inStr = new FileInputStream(new File("src/test/data/svnlogwithpath.xml"))) {
			Map<Long, LogEntry> parsed = new SVNLogFileParser(new String[] {"/trunk/CommonCrawl"}, null, 2).parseContent(inStr);
			assertNotNull(parsed);

			// rev. 1799: 6 paths
			LogEntry entry = parsed.get(1799l);
			assertNotNull(entry);
			assertNotNull(entry.paths);
			assertEquals("Expect 3 (2 + ....) for 6 paths, but had: " + entry.paths, 3, entry.paths.size());
			Iterator<Pair<String,String>> it = entry.paths.iterator();
			it.next();
			it.next();
			Pair<String,String> pair = it.next();
			String next = pair.getLeft();
			assertEquals("Last path is only '...' to indicate more paths, but had: " + ArrayUtils.toString(entry.paths.toArray(), "\n"), LogEntry.MORE, next);
			assertEquals("", pair.getRight());
		}
	}

	@Test
	public void testWithLogEntryRunnable() throws SAXException, IOException {
		try (InputStream inStr = new FileInputStream(new File("src/test/data/svnlogwithpath.xml"))) {
		    final AtomicInteger count = new AtomicInteger(0);
			Map<Long, LogEntry> parsed = new SVNLogFileParser(new String[] {"/trunk/CommonCrawl"}, new SVNLogFileParser.LogEntryRunnable() {

				@Override
				public void run(LogEntry entry) {
					count.incrementAndGet();

					assertNotNull(entry);
					assertTrue(entry.revision > 0);
				}
			}, 10).parseContent(inStr);

			assertNotNull(parsed);
			assertEquals("When using runnable, no entries are returned", 0, parsed.size());

			assertEquals(12, count.get());
		}
	}

	@Test
	public void testInvalidXML1InvalidNesting() throws SAXException, IOException {
		try (InputStream inStr = new FileInputStream(new File("src/test/data/svnlogfail1.xml"))) {
			new SVNLogFileParser(new String[] {}).parseContent(inStr);
			fail("Should catch exception here");
		} catch (IllegalStateException e) {
			TestHelpers.assertContains(e, "Should not have tags when a config starts in the XML", "267726");
		}
	}

	@Test
	public void testInvalidXML2NoRevision() throws SAXException, IOException {
		try (InputStream inStr = new FileInputStream(new File("src/test/data/svnlogfail2.xml"))) {
			new SVNLogFileParser(new String[] {}).parseContent(inStr);
			fail("Should catch exception here");
		} catch (IllegalStateException e) {
			TestHelpers.assertContains(e, "Expected to have tag 'revision'");
		}
	}

	@Test
	public void testLogEntryPathsSorting() {
		LogEntry entry = new LogEntry();

		entry.addPath("test1", "M");
		entry.addPath("test2", null);
		entry.addPath("atest", "A");

		Iterator<Pair<String,String>> it = entry.paths.iterator();
		assertEquals("atest", it.next().getLeft());
		assertEquals("test1", it.next().getLeft());
		assertEquals("test2", it.next().getLeft());

		entry.addPath(LogEntry.MORE, "D");

		it = entry.paths.iterator();
		assertEquals("atest", it.next().getLeft());
		assertEquals("test1", it.next().getLeft());
		assertEquals("test2", it.next().getLeft());
		assertEquals(LogEntry.MORE, it.next().getLeft());
	}

	private static String XML = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
		"<log>\n" +
		"<logentry revision=\"431200\">\n" +
		"<date>2014-09-18T10:40:52.345853Z</date>\n" +
		"<msg>APM-23041 System.exit</msg>\n" +
		"</logentry>\n" +
		"</log>";

	@Test
	public void testParserProblem() throws SAXException, IOException {
		Map<Long, LogEntry> parsed = new SVNLogFileParser(new String[] {}).parseContent(new ByteArrayInputStream(XML.getBytes("UTF-8")));
		assertNotNull(parsed);

		assertTrue("Had: " + parsed, parsed.containsKey(431200l));
		assertNull(parsed.get(431200l).author);
		assertEquals("2014-09-18T10:40:52.345853Z", parsed.get(431200l).date);
		assertEquals("APM-23041 System.exit", parsed.get(431200l).msg);
		assertEquals(431200l, parsed.get(431200l).revision);
	}
}
