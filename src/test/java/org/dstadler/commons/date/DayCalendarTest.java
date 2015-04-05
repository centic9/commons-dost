package org.dstadler.commons.date;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class DayCalendarTest {

	@Test
	public void testDayCalendar() {
		DayCalendar day = new DayCalendar();
		day.getUnixDay();
		day.getJulianDay();
	}

	@Test
	public void testDayCalendarLong() {
		DayCalendar day = new DayCalendar(System.currentTimeMillis());
		day.getUnixDay();
		day.getJulianDay();
	}

	@Test
	public void testDayCalendarIntIntInt() {
		DayCalendar day = new DayCalendar(2010, 12, 18);
		assertEquals(14992, day.getUnixDay());
		assertEquals(2455580, day.getJulianDay());
	}

	@Test
	public void testDayCalendarIntIntIntIntIntInt() {
		DayCalendar day = new DayCalendar(2010, 12, 17, 23, 23, 12);
		assertEquals(14991, day.getUnixDay());
		assertEquals(2455579, day.getJulianDay());
	}

	@Test
	public void testDiffDayPeriods() {
		DayCalendar day = new DayCalendar(2010, 12, 18);

		DayCalendar day2 = new DayCalendar(2010, 12, 18);
		assertEquals(0, day.diffDayPeriods(day2));

		day2 = new DayCalendar(2010, 12, 19);
		assertEquals(1, day.diffDayPeriods(day2));

		day2 = new DayCalendar(2010, 12, 17);
		assertEquals(-1, day.diffDayPeriods(day2));

		day2 = new DayCalendar(2011, 11, 7);
		assertEquals(323, day.diffDayPeriods(day2));
	}
}
