package org.dstadler.commons.date;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.time.DateUtils;

import org.dstadler.commons.testing.PrivateConstructorCoverage;
import org.dstadler.commons.testing.TestHelpers;
import org.junit.jupiter.api.Test;


public class DateParserTest {

	@Test
	public void testParseURLDateRelative() {
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
		assertEquals((double)expected, got,
				/* one hour because of Daylight Saving Time plus a bit of rounding-margin: */ 60*60*1000 + 500,
				"Expected " + new Date(expected) + " but got " + new Date(got));

		// when using years/months, we do not calculate exactly as we use 365 per year
		long fiveYearsAgo = DateUtils.addDays(now, -(5*365)).getTime();
		assertEquals((double)fiveYearsAgo,
				DateParser.parseURLDate("-5years", now).getTime(),
				/* one hour because of Daylight Saving Time plus a bit of rounding-margin: */ 60*60*1000 + 500,
				"Expected to get " + new Date(fiveYearsAgo) + ", but had: " + DateParser.parseURLDate("-5years", now));
	}

	@Test
	public void testParseURLDateInvalid() {
		Date now = new Date();
		assertThrows(IllegalArgumentException.class,
				() -> DateParser.parseURLDate("-0any", now));
	}

	@Test
	public void testParseURLDateAbsolute() {
		assertEquals(1304215200000L, DateParser.parseURLDate("04:00 20110501", null).getTime());
		assertEquals(1304215200000L, DateParser.parseURLDate("04:00 110501", null).getTime());
		assertEquals(1304200800000L, DateParser.parseURLDate("2011-05-01", null).getTime(),
				"Expected " + new Date(1304200800000L) + " but got: " + DateParser.parseURLDate("2011-05-01", null));
		assertEquals(1304215200000L, DateParser.parseURLDate("2011-05-01T04:00:00.0", null).getTime());
		assertEquals(1304200800000L, DateParser.parseURLDate("05/01/11", null).getTime(),
				"Expected " + new Date(1304200800000L) + " but got: " + DateParser.parseURLDate("05/01/11", null));
	}

	@Test
	public void testInvalidInput() {
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
	public void testComputeTimeAgoAsStringFailsInTheFuture() {
		assertThrows(IllegalArgumentException.class,
				() -> DateParser.computeTimeAgoString(System.currentTimeMillis() + 100_000, ""));
	}

	@Test
	public void testComputeTimeAgoAsString() {
		String str = DateParser.computeTimeAgoString(System.currentTimeMillis() - 512, "");
		assertTrue("512 ms".compareTo(str) <= 0 && "600 ms".compareTo(str) >= 0,
				"Windows has a 'jumpy' time source, so we expect a range of ms as result, failed for " + str);
		assertEquals("6 min", DateParser.computeTimeAgoString(System.currentTimeMillis() - (6 * 60 * 1000), ""));
		assertEquals("2 min", DateParser.computeTimeAgoString(System.currentTimeMillis() - (2 * 60 * 1000), ""));
		assertEquals("1 h", DateParser.computeTimeAgoString(System.currentTimeMillis() - (60 * 60 * 1000), ""));
		assertEquals("3 h, 5 min", DateParser.computeTimeAgoString(System.currentTimeMillis() - (185 * 60 * 1000), ""));
		assertEquals("23 h, 59 min", DateParser.computeTimeAgoString(System.currentTimeMillis() - (23 * 60 * 60 * 1000) - (59*60*1000), ""));
		assertEquals("1 day", DateParser.computeTimeAgoString(System.currentTimeMillis() - (24 * 60 * 60 * 1000), ""));
		assertEquals("23 h, 59 min", DateParser.computeTimeAgoString(System.currentTimeMillis() - (24 * 60 * 60 * 1000)+1, ""));
		assertEquals("1 day", DateParser.computeTimeAgoString(System.currentTimeMillis() - (24 * 60 * 60 * 1000)-1, ""));
		assertEquals("1 day, 3 min", DateParser.computeTimeAgoString(System.currentTimeMillis() - (24 * 60 * 60 * 1000)- (3*60*1000), ""));
		assertEquals("1 day, 59 min", DateParser.computeTimeAgoString(System.currentTimeMillis() - (24 * 60 * 60 * 1000)- (59*60*1000), ""));
		assertEquals("1 day, 59 min", DateParser.computeTimeAgoString(System.currentTimeMillis() - (24 * 60 * 60 * 1000)- (59*60*1000) - (59*1000), ""));
		assertEquals("1 day, 1 h", DateParser.computeTimeAgoString(System.currentTimeMillis() - (25 * 60 * 60 * 1000), ""));
		assertEquals("1 day, 4 h", DateParser.computeTimeAgoString(System.currentTimeMillis() - (28 * 60 * 60 * 1000), ""));
		assertEquals("2 weeks, 4 days, 14 h", DateParser.computeTimeAgoString(System.currentTimeMillis() - TimeUnit.DAYS.toMillis(18) - TimeUnit.HOURS.toMillis(14), ""));

		str = DateParser.computeTimeAgoString(System.currentTimeMillis(), "");
		assertTrue("0 s".equals(str) || "1 ms".equals(str),
				"Sometimes 1ms elapses in the call to DateParser");

		assertEquals("34 s", DateParser.computeTimeAgoString(System.currentTimeMillis() - (34*1000), ""));
		assertEquals("19 h, 52 min", DateParser.computeTimeAgoString(System.currentTimeMillis() - (29*1000 + 52*60*1000 + 19*60*60*1000), ""));
		assertEquals("19 h", DateParser.computeTimeAgoString(System.currentTimeMillis() - (19*60*60*1000), ""));

		assertEquals("6 min ago", DateParser.computeTimeAgoString(System.currentTimeMillis() - (6 * 60 * 1000), " ago"));

		assertEquals("1 day, 1 min", DateParser.computeTimeAgoString(System.currentTimeMillis() - (24 * 60 * 60 * 1000) - (60*1000), ""));
	}

	@Test
	public void testReadableTime() {
		assertEquals("500 ms", DateParser.timeToReadable(500));
		assertEquals("1 s", DateParser.timeToReadable(1000));
		assertEquals("6 min", DateParser.timeToReadable(6 * 60 * 1000));
		assertEquals("2 min", DateParser.timeToReadable(2 * 60 * 1000));
		assertEquals("1 h", DateParser.timeToReadable(60 * 60 * 1000));
		assertEquals("3 h, 5 min", DateParser.timeToReadable(185 * 60 * 1000));
		assertEquals("1 day", DateParser.timeToReadable(DateParser.ONE_DAY));
		assertEquals("1 day, 4 h", DateParser.timeToReadable(28 * 60 * 60 * 1000));
		assertEquals("1 week", DateParser.timeToReadable(DateParser.ONE_WEEK));
		assertEquals("2 weeks, 4 days, 14 h", DateParser.timeToReadable(TimeUnit.DAYS.toMillis(18) + TimeUnit.HOURS.toMillis(14)));
		assertEquals("34 s", DateParser.timeToReadable(34*1000));
		assertEquals("19 h, 52 min", DateParser.timeToReadable(29*1000 + 52*60*1000 + 19*60*60*1000));
		assertEquals("19 h", DateParser.timeToReadable(19*60*60*1000));

		// try a few "unusual" values
		assertEquals("-15250284452 weeks, 3 days, 7 h", DateParser.timeToReadable(Long.MIN_VALUE));
		assertEquals("15250284452 weeks, 3 days, 7 h", DateParser.timeToReadable(Long.MAX_VALUE));
		assertEquals("-1 ms", DateParser.timeToReadable(-1));
		assertEquals("0 s", DateParser.timeToReadable(0));
		assertEquals("15250284452 weeks, 3 days, 7 h", DateParser.timeToReadable((long)Double.POSITIVE_INFINITY));
		assertEquals("-15250284452 weeks, 3 days, 7 h", DateParser.timeToReadable((long)Double.NEGATIVE_INFINITY));
		//noinspection ConstantConditions
		assertEquals("0 s", DateParser.timeToReadable((long)Double.MIN_VALUE));
		assertEquals("15250284452 weeks, 3 days, 7 h", DateParser.timeToReadable((long)Double.MAX_VALUE));

		assertEquals("6 min ago", DateParser.timeToReadable(6 * 60 * 1000, " ago"));
	}
}
