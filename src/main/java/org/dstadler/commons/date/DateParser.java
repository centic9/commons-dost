package org.dstadler.commons.date;

import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.commons.lang3.time.FastDateFormat;

import com.google.common.base.Preconditions;


/**
 * Modelled after http://graphite.readthedocs.org/en/latest/render_api.html#from-until
 *
 * Note: Passing in absolute time is expected to be stated in the European timezone!
 *
 * @author cwat-dstadler
 */
public class DateParser {
	private static final TimeZone TIME_ZONE = TimeZone.getTimeZone("GMT+2");

	private final static Pattern RELATIVE_TIME_PATTERN = Pattern.compile("([0-9]+)(s|second|seconds|min|minute|minutes|h|hour|hours|d|day|days|w|week|weeks|mon|month|months|y|year|years)");

	private final static Map<String, Integer> UNIT_CONVERSION_TABLE = new HashMap<>();
	static {
		UNIT_CONVERSION_TABLE.put("s", 1);
		UNIT_CONVERSION_TABLE.put("second", 1);
		UNIT_CONVERSION_TABLE.put("seconds", 1);
		UNIT_CONVERSION_TABLE.put("min", 60);
		UNIT_CONVERSION_TABLE.put("minute", 60);
		UNIT_CONVERSION_TABLE.put("minutes", 60);
		UNIT_CONVERSION_TABLE.put("h", 60*60);
		UNIT_CONVERSION_TABLE.put("hour", 60*60);
		UNIT_CONVERSION_TABLE.put("hours", 60*60);
		UNIT_CONVERSION_TABLE.put("d", 24*60*60);
		UNIT_CONVERSION_TABLE.put("day", 24*60*60);
		UNIT_CONVERSION_TABLE.put("days", 24*60*60);
		UNIT_CONVERSION_TABLE.put("w", 7*24*60*60);
		UNIT_CONVERSION_TABLE.put("week", 7*24*60*60);
		UNIT_CONVERSION_TABLE.put("weeks", 7*24*60*60);
		UNIT_CONVERSION_TABLE.put("mon", 30*24*60*60);
		UNIT_CONVERSION_TABLE.put("month", 30*24*60*60);
		UNIT_CONVERSION_TABLE.put("months", 30*24*60*60);
		UNIT_CONVERSION_TABLE.put("y", 365*24*60*60);
		UNIT_CONVERSION_TABLE.put("year", 365*24*60*60);
		UNIT_CONVERSION_TABLE.put("years", 365*24*60*60);
	}

	private static FastDateFormat[] DATE_PARSERS = new FastDateFormat[] {
		// note: the order here is important to not parse things incorrectly!
		// SimpleDateFormat will use a fairly matching format as well, so we
		// need to put the most complex formats in front to avoid wrong parsing.
		FastDateFormat.getInstance("yyyy-MM-dd'T'HH:mm:ss.S" /* Z */, TIME_ZONE, Locale.GERMANY),
		FastDateFormat.getInstance("yyyy-MM-dd", TIME_ZONE),
		FastDateFormat.getInstance("yyyyMMdd", TIME_ZONE),
		FastDateFormat.getInstance("HH:mm yyyyMMdd", TIME_ZONE),
		FastDateFormat.getInstance("HH:mm yyMMdd", TIME_ZONE),
		FastDateFormat.getInstance("MM/dd/yy", TIME_ZONE),
	};

	public final static long ONE_SECOND = 1000;
	public final static long ONE_MINUTE = 60 * ONE_SECOND;
	public final static long ONE_HOUR = 60 * ONE_MINUTE;
	public final static long ONE_DAY = 24 * ONE_HOUR;

	/* TODO: things that Graphite can parse in addition:

	&from=04:00_20110501&until=16:00_20110501
	(shows 4AM-4PM on May 1st, 2011)

	&from=20091201&until=20091231
	(shows December 2009)

	&from=noon+yesterday
	(shows data since 12:00pm on the previous day)

	&from=6pm+today
	(shows data since 6:00pm on the same day)

	&from=january+1
	(shows data since the beginning of the current year)

	&from=monday
	(show data since the previous monday)

	*/

	public static Date parseURLDate(String dateStr, Date defaultDate) {
		if(StringUtils.isEmpty(dateStr)) {
			return defaultDate;
		}

		/*
		There are multiple formats for these functions:

		&from=-RELATIVE_TIME
		&from=ABSOLUTE_TIME

		&from and &until can mix absolute and relative time if desired.
		*/
		if(dateStr.startsWith("-")) {
			/*
			RELATIVE_TIME is a length of time since the current time. It is always preceded my a minus sign ( - ) and follow by a unit of time. Valid units of time:
			Abbreviation 	Unit
			s 	Seconds
			min 	Minutes
			h 	Hours
			d 	Days
			w 	Weeks
			mon 	30 Days (month)
			y 	365 Days (year)
			 */

			// cut off leading minus-sign and parse value and unit
			String parse = dateStr.substring(1).toLowerCase();
			Matcher matcher = RELATIVE_TIME_PATTERN.matcher(parse);
			if(!matcher.matches()) {
				throw new IllegalArgumentException("Could not parse relative time value " + parse + ", expected to match " + RELATIVE_TIME_PATTERN);
			}

			String value = matcher.group(1);
			String unit = matcher.group(2);

			// check adjustment factor for this unit
			Integer factor = UNIT_CONVERSION_TABLE.get(unit);
			if(factor == null) {
				throw new IllegalArgumentException("Unknown unit: " + unit + " found while parsing relative time: " + parse);
			}

			// finally use the factor to calculate the resulting previous point in time
			return DateUtils.addSeconds(new Date(), (-1)*Integer.parseInt(value)*factor);
		}

/*
		ABSOLUTE_TIME is in the format HH:MM_YYMMDD, YYYYMMDD, MM/DD/YY, or any other at(1)-compatible time format.
		Abbreviation 	Meaning
		HH 	Hours, in 24h clock format. Times before 12PM must include leading zeroes.
		MM 	Minutes
		YYYY 	4 Digit Year.
		MM 	Numeric month representation with leading zero
		DD 	Day of month with leading zero
 */

		for(FastDateFormat format : DATE_PARSERS) {
			try {
				return format.parse(dateStr);
			} catch (ParseException e) {
				// expected here if the format does not match
			}
		}

		StringBuilder string = new StringBuilder();
		for(FastDateFormat format : DATE_PARSERS) {
			string.append(format.getPattern()).append(", ");
		}
		throw new IllegalArgumentException("Could not parse absolut date " + dateStr + " via any of the available parsers: " + string.toString());
	}

	/**
	 * Takes the time in milliseconds since the epoch and
	 * converts it into a string of "x days/hours/minutes/seconds"
	 * compared to the current time.
	 *
	 * @param ts The timestamp in milliseconds since the epoch
	 * @param suffix Some text that is appended only if there is a time-difference, i.e.
	 * 	it is not appended when the time is now.
	 *
	 * @return A readable string with a short description of how long ago the ts was.
	 */
	public static String computeTimeAgoString(long ts, String suffix) {
		long now = System.currentTimeMillis();

		Preconditions.checkArgument(ts <= now, "Cannot handle timestamp in the future, now: %s/%s, ts: %s/%s", now, new Date(now), ts, new Date(ts));

		long diff = now - ts;
		return timeToReadable(diff, suffix);
	}

	/**
	 * Format the given number of milliseconds as readable string.
	 *
	 * @param millis The number of milliseconds to print.
	 *
	 * @return The readable string.
	 */
	public static String timeToReadable(long millis) {
		return timeToReadable(millis, "");
	}

	/**
	 * Format the given number of milliseconds as readable string, optionally
	 * appending a suffix.
	 *
	 * @param millis The number of milliseconds to print.
	 * @param suffix A suffix that is appended if the millis is &gt; 0, specify "" if not needed.
	 *
	 * @return The readable string
	 */
	public static String timeToReadable(long millis, String suffix) {
		StringBuilder builder = new StringBuilder();
		boolean haveDays = false;
		if(millis > ONE_DAY) {
			millis = handleTime(builder, millis, ONE_DAY, "day", "s");
			haveDays = true;
		}

		boolean haveHours = false;
		if(millis >= ONE_HOUR) {
			millis = handleTime(builder, millis, ONE_HOUR, "h", "");
			haveHours = true;
		}

		if((!haveDays || !haveHours) && millis >= ONE_MINUTE) {
			millis = handleTime(builder, millis, ONE_MINUTE, "min", "");
		}

		if(!haveDays && !haveHours && millis >= ONE_SECOND) {
			/*millis =*/ handleTime(builder, millis, ONE_SECOND, "s", "");
		}

		if(builder.length() > 0) {
			// cut off trailing ", "
			builder.setLength(builder.length() - 2);
			builder.append(suffix);
		}

		return builder.toString();
	}

	private static long handleTime(StringBuilder builder, long diff, long amount, String type, String plural) {
		long days = diff / amount;
		builder.append(days).append(" ").append(type).append(days > 1 ? plural : "").append(", ");
		diff -= days*amount;
		return diff;
	}

}
