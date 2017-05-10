package org.dstadler.commons.date;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.Date;

import org.apache.commons.lang3.time.DateUtils;
import org.junit.Test;

import org.dstadler.commons.testing.PrivateConstructorCoverage;
import org.dstadler.commons.testing.TestHelpers;


public class DateParserTest {

	@Test
	public void testParseURLDateRelative() throws Exception {
		Date now = new Date();
		assertEquals(now, DateParser.parseURLDate(null, now));
		assertEquals(now, DateParser.parseURLDate("", now));

		assertEquals((double)now.getTime(), DateParser.parseURLDate("-0s", now).getTime(), 500);
		assertEquals((double)now.getTime(), DateParser.parseURLDate("-0hour", now).getTime(), 500);
		assertEquals((double)DateUtils.addSeconds(now, -1).getTime(), DateParser.parseURLDate("-1s", now).getTime(), 500);
		assertEquals((double)DateUtils.addSeconds(now, -1).getTime(), DateParser.parseURLDate("-1seconds", now).getTime(), 500);
		assertEquals((double)DateUtils.addSeconds(now, -523).getTime(), DateParser.parseURLDate("-523seconds", now).getTime(), 500);

		long expected = DateUtils.addDays(now, -3).getTime();
		long got = DateParser.parseURLDate("-3day", now).getTime();
		assertEquals("Expected " + new Date(expected) + " but got " + new Date(got),
				(double)expected, got,
				/* one hour because of Daylight Saving Time plus a bit of rounding-margin: */ 60*60*1000 + 500);

		// when using years/months, we do not calculate exactly as we use 365 per year
		long fiveYearsAgo = DateUtils.addDays(now, -(5*365)).getTime();
		assertEquals("Expected to get " + new Date(fiveYearsAgo) + ", but had: " + DateParser.parseURLDate("-5years", now),
				(double)fiveYearsAgo,
				DateParser.parseURLDate("-5years", now).getTime(),
				/* one hour because of Daylight Saving Time plus a bit of rounding-margin: */ 60*60*1000 + 500);
	}

	@Test
	public void testParseURLDateAbsolute() throws Exception {
		assertEquals(1304215200000L, DateParser.parseURLDate("04:00 20110501", null).getTime());
		assertEquals(1304215200000L, DateParser.parseURLDate("04:00 110501", null).getTime());
		assertEquals("Expected " + new Date(1304200800000L) + " but got: " + DateParser.parseURLDate("2011-05-01", null),
				1304200800000L, DateParser.parseURLDate("2011-05-01", null).getTime());
		assertEquals(1304215200000L, DateParser.parseURLDate("2011-05-01T04:00:00.0", null).getTime());
		assertEquals("Expected " + new Date(1304200800000L) + " but got: " + DateParser.parseURLDate("05/01/11", null),
				1304200800000L, DateParser.parseURLDate("05/01/11", null).getTime());
	}

	@Test
	public void testInvalidInput() throws Exception {
		try {
			DateParser.parseURLDate("asdflaqworthgawer", null);
			fail("Should catch exception here");
		} catch (IllegalArgumentException e) {
			TestHelpers.assertContains(e, "asdflaqworthgawer", "yyyy-MM-dd");
		}

		try {
			DateParser.parseURLDate("-asdflaqworthgawer", null);
			fail("Should catch exception here");
		} catch (IllegalArgumentException e) {
			TestHelpers.assertContains(e, "asdflaqworthgawer", "seconds", "hours", "years");
		}

		try {
			DateParser.parseURLDate("-234524ss", null);
			fail("Should catch exception here");
		} catch (IllegalArgumentException e) {
			TestHelpers.assertContains(e, "234524ss", "seconds", "hours", "years");
		}
	}

	// helper method to get coverage of the unused constructor
	@Test
	public void testPrivateConstructor() throws Exception {
		PrivateConstructorCoverage.executePrivateConstructor(DateParser.class);
	}

	@Test
	public void testComputeTimeAgoAsString() {
		assertEquals("6 min", DateParser.computeTimeAgoString(System.currentTimeMillis()- (6 * 60 * 1000), ""));
		assertEquals("2 min", DateParser.computeTimeAgoString(System.currentTimeMillis()- (2 * 60 * 1000), ""));
		assertEquals("1 h", DateParser.computeTimeAgoString(System.currentTimeMillis()- (60 * 60 * 1000), ""));
		assertEquals("3 h, 5 min", DateParser.computeTimeAgoString(System.currentTimeMillis()- (185 * 60 * 1000), ""));
		assertEquals("23 h, 59 min", DateParser.computeTimeAgoString(System.currentTimeMillis()- (23 * 60 * 60 * 1000) - (59*60*1000), ""));
		assertEquals("24 h", DateParser.computeTimeAgoString(System.currentTimeMillis()- (24 * 60 * 60 * 1000), ""));
		assertEquals("23 h, 59 min", DateParser.computeTimeAgoString(System.currentTimeMillis()- (24 * 60 * 60 * 1000)+1, ""));
		assertEquals("1 day", DateParser.computeTimeAgoString(System.currentTimeMillis()- (24 * 60 * 60 * 1000)-1, ""));
		assertEquals("1 day, 3 min", DateParser.computeTimeAgoString(System.currentTimeMillis()- (24 * 60 * 60 * 1000)- (3*60*1000), ""));
		assertEquals("1 day, 59 min", DateParser.computeTimeAgoString(System.currentTimeMillis()- (24 * 60 * 60 * 1000)- (59*60*1000), ""));
		assertEquals("1 day, 59 min", DateParser.computeTimeAgoString(System.currentTimeMillis()- (24 * 60 * 60 * 1000)- (59*60*1000) - (59*1000), ""));
		assertEquals("1 day, 1 h", DateParser.computeTimeAgoString(System.currentTimeMillis()- (25 * 60 * 60 * 1000), ""));
		assertEquals("1 day, 4 h", DateParser.computeTimeAgoString(System.currentTimeMillis()- (28 * 60 * 60 * 1000), ""));
		assertEquals("18 days, 14 h", DateParser.computeTimeAgoString(System.currentTimeMillis()- (384732 * 60 * 1000), ""));
		assertEquals("", DateParser.computeTimeAgoString(System.currentTimeMillis(), ""));
		assertEquals("34 s", DateParser.computeTimeAgoString(System.currentTimeMillis()- (34*1000), ""));
		assertEquals("19 h, 52 min", DateParser.computeTimeAgoString(System.currentTimeMillis()- (29*1000 + 52*60*1000 + 19*60*60*1000), ""));
		assertEquals("19 h", DateParser.computeTimeAgoString(System.currentTimeMillis()- (19*60*60*1000), ""));

		assertEquals("6 min ago", DateParser.computeTimeAgoString(System.currentTimeMillis()- (6 * 60 * 1000), " ago"));

		assertEquals("1 day, 1 min", DateParser.computeTimeAgoString(System.currentTimeMillis()- (24 * 60 * 60 * 1000)- (1*60*1000), ""));
	}

	@Test
	public void testReadableTime() {
		assertEquals("6 min", DateParser.timeToReadable(6 * 60 * 1000));
		assertEquals("2 min", DateParser.timeToReadable(2 * 60 * 1000));
		assertEquals("1 h", DateParser.timeToReadable(60 * 60 * 1000));
		assertEquals("3 h, 5 min", DateParser.timeToReadable(185 * 60 * 1000));
		assertEquals("1 day, 4 h", DateParser.timeToReadable(28 * 60 * 60 * 1000));
		assertEquals("18 days, 14 h", DateParser.timeToReadable(384732 * 60 * 1000));
		assertEquals("", DateParser.timeToReadable(0));
		assertEquals("34 s", DateParser.timeToReadable(34*1000));
		assertEquals("19 h, 52 min", DateParser.timeToReadable(29*1000 + 52*60*1000 + 19*60*60*1000));
		assertEquals("19 h", DateParser.timeToReadable(19*60*60*1000));

		assertEquals("6 min ago", DateParser.timeToReadable(6 * 60 * 1000, " ago"));
	}
}
