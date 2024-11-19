package org.dstadler.commons.date;

import java.io.Serial;
import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 * Taken from http://www.xmission.com/~goodhill/dates/deltaDates.html
 *
 * Demonstration of delta day calculations.
 * @author Paul Hill
 * copyright 2004 Paul Hill
 */
public class DayCalendar extends GregorianCalendar {
	@Serial
	private static final long serialVersionUID = -9054523308590467657L;

	/**
     * All minutes have this many milliseconds except the last minute of the day on a day defined with
     * a leap second.
     */
    public static final long MILLISECS_PER_MINUTE = 60*1000;

    /**
     * Number of milliseconds per hour, except when a leap second is inserted.
     */
    public static final long MILLISECS_PER_HOUR   = 60*MILLISECS_PER_MINUTE;

    /**
     * Number of leap seconds per day expect on
     * 1. days when a leap second has been inserted, e.g. 1999 JAN  1.
     * 2. Daylight-savings "spring forward" or "fall back" days.
     */
    protected static final long MILLISECS_PER_DAY = 24*MILLISECS_PER_HOUR;

    /****
     * Value to add to the day number returned by this calendar to find the Julian Day number.
     * This is the Julian Day number for 1/1/1970.
     * Note: Since the unix Day number is the same from local midnight to local midnight adding
     * JULIAN_DAY_OFFSET to that value results in the chronologist, historians, or calenderists
     * Julian Day number.
     * @see <a href="http://www.hermetic.ch/cal_stud/jdn.htm">here</a>
     */
    public static final long EPOCH_UNIX_ERA_DAY = 2440588L;

    /**
     * @see java.util.GregorianCalendar#GregorianCalendar()
     */
    public DayCalendar() {
        super();
    }
    /**
     * @param millisecondTime - time as a binary Unix/Java time value.
     * @see java.util.GregorianCalendar
     */
    public DayCalendar( long millisecondTime ) {
        super();
        this.setTimeInMillis( millisecondTime);
    }

    /**
     * Constructs a <code>GregorianCalendar</code> with the given date set
     * in the default time zone with the default locale.
     *
     * @param year the value used to set the <code>YEAR</code> calendar field in the calendar.
     * @param month the value used to set the <code>MONTH</code> calendar field in the calendar.
     * Month value is 0-based. e.g., 0 for January.
     * @param dayOfMonth the value used to set the <code>DAY_OF_MONTH</code> calendar field in the calendar.

     * @see java.util.GregorianCalendar#GregorianCalendar(int, int, int)
     */
    public DayCalendar( int year, int month, int dayOfMonth ) {
        super( year, month, dayOfMonth );
    }

    /**
     * Constructs a GregorianCalendar with the given date
     * and time set for the default time zone with the default locale.
     *
     * @param year the value used to set the <code>YEAR</code> calendar field in the calendar.
     * @param month the value used to set the <code>MONTH</code> calendar field in the calendar.
     * Month value is 0-based. e.g., 0 for January.
     * @param dayOfMonth the value used to set the <code>DAY_OF_MONTH</code> calendar field in the calendar.
     * @param hourOfDay the value used to set the <code>HOUR_OF_DAY</code> calendar field
     * in the calendar.
     * @param minute the value used to set the <code>MINUTE</code> calendar field
     * in the calendar.
     * @param second the value used to set the <code>SECOND</code> calendar field
     * in the calendar.

     * @see java.util.GregorianCalendar#GregorianCalendar(int, int, int, int, int, int)
     */
    public DayCalendar( int year, int month, int dayOfMonth, int hourOfDay, int minute, int second ) {
        super( year, month, dayOfMonth, hourOfDay, minute, second );
    }

    /**
     * @return Day number where day 0 is 1/1/1970, as per the Unix/Java date/time epoch.
     */
    public long getUnixDay() {
        long offset = get(Calendar.ZONE_OFFSET) + get(Calendar.DST_OFFSET);
        return (long)Math.floor( (double)(getTime().getTime() + offset ) / ((double)MILLISECS_PER_DAY) );
    }

    /**
     * @return LOCAL Chronologists Julian day number each day starting from midnight LOCAL TIME.
     * See <a href="http://tycho.usno.navy.mil/mjd.html">here</a> for more information about local C-JDN
     */
    public long getJulianDay() {
        return getUnixDay() + EPOCH_UNIX_ERA_DAY;
    }

    /**
     * find the number of days from this date to the given end date.
     * later end dates result in positive values.
     * Note this is not the same as subtracting day numbers.  Just after midnight subtracted from just before
     * midnight is 0 days for this method while subtracting day numbers would yields 1 day.
     * @param end - any Calendar representing the moment of time at the end of the interval for calculation.
     *
     * @return The difference in days
     */
    public long diffDayPeriods(Calendar end) {
        long endL   =  end.getTimeInMillis() +  end.getTimeZone().getOffset(  end.getTimeInMillis() );
        long startL = this.getTimeInMillis() + this.getTimeZone().getOffset( this.getTimeInMillis() );
        return (endL - startL) / MILLISECS_PER_DAY;
    }
}
