package org.dstadler.commons.gpx;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Date;
import java.util.SortedMap;
import java.util.TimeZone;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Logger;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.FastDateFormat;
import org.dstadler.commons.logging.jdk.LoggerFactory;
import org.dstadler.commons.util.SuppressForbidden;
import org.dstadler.commons.xml.AbstractSimpleContentHandler;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

public class GPXTrackpointsParser extends AbstractSimpleContentHandler<Long, TrackPoint> {
    private static final Logger log = LoggerFactory.make();

	@SuppressForbidden(reason = "Uses System.exit")
    // this is an App mostly for testing arbitrary files for proper parsing
    public static void main(String[] args) throws IOException {
        LoggerFactory.initLogging();

        if (args.length < 1) {
            log.severe("Usage: GPXTrackpointsParser <gpx-file> ...");
            System.exit(1);
        }

        for (String arg : args) {
            File gpxFile = new File(arg);
            if (!gpxFile.exists()) {
                log.severe("GPX-File does not exist: " + gpxFile);
                System.exit(2);
            }

            SortedMap<Long, TrackPoint> points = GPXTrackpointsParser.parseContent(gpxFile);
            log.info("Found " + points.size() + " points in GPX-File " + gpxFile);
            log.info("First point: " + points.values().iterator().next());
        }
    }

	/*
<gpx xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
xsi:schemaLocation="http://www.topografix.com/GPX/1/1 http://www.topografix.com/GPX/1/1/gpx.xsd
http://www.cluetrust.com/XML/GPXDATA/1/0 http://www.cluetrust.com/Schemas/gpxdata10.xsd
http://www.garmin.com/xmlschemas/TrackPointExtension/v1 http://www.garmin.com/xmlschemas/TrackPointExtensionv1.xsd"
xmlns:gpxdata="http://www.topografix.com/GPX/1/0" xmlns:gpxtpx="http://www.garmin.com/xmlschemas/TrackPointExtension/v1"
version="1.1" creator="Movescount - http://www.movescount.com" xmlns="http://www.topografix.com/GPX/1/1">
<trk>
<name>Move</name>
<trkseg>
    <trkpt lat="48.3176978" lon="14.3050261">
        <ele>261</ele>
        <time>2022-04-27T13:41:25.000Z</time>
        <extensions>
            <gpxdata:hr>150</gpxdata:hr>
            <gpxdata:cadence>85</gpxdata:cadence>
            <gpxdata:temp>23.9</gpxdata:temp>
            <gpxdata:speed>2.99</gpxdata:speed>
            <gpxdata:SeaLevelPressure>10215</gpxdata:SeaLevelPressure>
        </extensions>
    </trkpt>
     */
    private static final String TAG_TRKPT = "trkpt";
    private static final String TAG_RTEPT = "rtept";
	private static final String TAG_ELE = "ele";
	private static final String TAG_TIME = "time";
	// for now parse wpt-elements the same way as trkpt
	private static final String TAG_WPT = "wpt";

    private static final String TAG_LAT = "lat";
    private static final String TAG_LON = "lon";

	private static final String TAG_HR = "hr";
	private static final String TAG_CADENCE = "cadence";    		// Suunto extension
	private static final String TAG_SPEED = "speed";        		// Suunto extension
	private static final String TAG_ATEMP = "atemp";        		// Garmin extension
	private static final String TAG_TEMP = "temp";           		// OpenAmbit
	private static final String TAG_PRESSURE = "SeaLevelPressure";	// OpenAmbit

    private static final String TAG_METADATA = "metadata";

	private static final FastDateFormat[] TIME_FORMATS = new FastDateFormat[] {
			FastDateFormat.getInstance("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", TimeZone.getTimeZone("Europe/Vienna")),
			FastDateFormat.getInstance("yyyy-MM-dd'T'HH:mm:ss'Z'", TimeZone.getTimeZone("Europe/Vienna")),
			FastDateFormat.getInstance("yyyy-MM-dd'T'HH:mm:ss.SSSZZ", TimeZone.getTimeZone("Europe/Vienna")),
			FastDateFormat.getInstance("yyyy-MM-dd'T'HH:mm:ss", TimeZone.getTimeZone("Europe/Vienna")),
	};
	private static final FastDateFormat[] TIME_FORMATS_UTC = new FastDateFormat[] {
			FastDateFormat.getInstance("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", TimeZone.getTimeZone("UTC")),
			FastDateFormat.getInstance("yyyy-MM-dd'T'HH:mm:ss'Z'", TimeZone.getTimeZone("UTC")),
			FastDateFormat.getInstance("yyyy-MM-dd'T'HH:mm:ss.SSSZZ", TimeZone.getTimeZone("UTC")),
			FastDateFormat.getInstance("yyyy-MM-dd'T'HH:mm:ss", TimeZone.getTimeZone("UTC")),
	};

	private final AtomicLong syntheticTime = new AtomicLong();

	private boolean metaData = false;

    public static SortedMap<Long, TrackPoint> parseContent(File file) throws IOException {
        GPXTrackpointsParser parser = new GPXTrackpointsParser();

        try (InputStream stream = new BufferedInputStream(new FileInputStream(file), 200*1024)) {
            return parser.parseContent(stream);
        } catch (IOException | SAXException e) {
			throw new IOException("While reading file: " + file, e);
		}
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) {
        if(localName.equals(TAG_TRKPT) || localName.equals(TAG_RTEPT) || localName.equals(TAG_WPT)) {
            if (currentTags != null) {
                throw new IllegalStateException("Should not have tags when a config starts in the XML, but had: " + currentTags);
            }
            currentTags = new TrackPoint();
			String lat = attributes.getValue(TAG_LAT);
			if(StringUtils.isNotBlank(lat)) {
                currentTags.setLatitude(Double.parseDouble(lat));
            }
			String lon = attributes.getValue(TAG_LON);
			if(StringUtils.isNotBlank(lon)) {
                currentTags.setLongitude(Double.parseDouble(lon));
            }
        } else if (localName.equals(TAG_METADATA)) {
            metaData = true;
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) {
        if(localName.equals(TAG_TRKPT) || localName.equals(TAG_RTEPT) || localName.equals(TAG_WPT)) {
			checkState(currentTags.getLatitude() != 0 && currentTags.getLongitude() != 0,
					"Expected to have tag 'lat' and 'lon' for trkpt in the XML, but did not find it for tag %s1 in: %s2",
					localName, currentTags);
			// GPX files for a planned route do not contain time-markers,
			// let's use a counter in this case to still sort trackpoints properly
			if (currentTags.time == null) {
				currentTags.setTime(new Date(syntheticTime.incrementAndGet()));
			}

			configs.put(currentTags.time.getTime(), currentTags);
            currentTags = null;
        } else if (localName.equals(TAG_METADATA)) {
            metaData = false;
        } else {
            String value = characters.toString().trim();
            switch (localName) {
				case TAG_ELE:
					// some files contain invalid elevation
					if (!"null".equals(value) && StringUtils.isNotBlank(value)) {
						currentTags.setElevation(Double.parseDouble(value));
					}
					break;
				case TAG_TIME:
					if (metaData) {
						break;
					}

					if (currentTags == null) {
						log.warning("Found " + TAG_TIME + " with value '" + value + "' outside of " + TAG_TRKPT);
						break;
					}

					if ("0".equals(value) || StringUtils.isBlank(value)) {
						break;
					}

					boolean parsed = false;
					for (FastDateFormat timeFormat :
						// we changed from non-UTC timestamp to UTC-based timestamps in the GPX
						// at some point and some external files use slightly different format, so
						// we have to try until we find the proper format
							value.length() >= 4 && Integer.parseInt(value.substring(0, 4)) >= 2022 ? TIME_FORMATS_UTC : TIME_FORMATS) {
						try {
							currentTags.setTime(timeFormat.parse(value));
							parsed = true;
							break;
						} catch (ParseException e) {
							// ignored here as we try others
						}
					}
					if (!parsed) {
						throw new IllegalStateException("Failed to parse time from: " + value +
								" with patterns " + Arrays.toString(TIME_FORMATS));
					}
					break;
				case TAG_HR:
					if (StringUtils.isNotBlank(value)) {
						currentTags.setHr(Integer.parseInt(value));
					}
					break;
				case TAG_CADENCE:
					if (StringUtils.isNotBlank(value)) {
						currentTags.setCadence(Integer.parseInt(value));
					}
					break;
				case TAG_SPEED:
					if (StringUtils.isNotBlank(value)) {
						currentTags.setSpeed(Double.parseDouble(value));
					}
					break;
				case TAG_ATEMP:
                case TAG_TEMP:
                    if (StringUtils.isNotBlank(value)) {
						currentTags.setTemp(Double.parseDouble(value));
					}
					break;
                case TAG_PRESSURE:
					if (StringUtils.isNotBlank(value)) {
						currentTags.setSeaLevelPressure(Integer.parseInt(value));
					}
					break;
			}
            characters.setLength(0);
        }
    }

	private static void checkState(boolean expression, String errorMessage, Object arg1, Object arg2) {
		if (!expression) {
			throw new IllegalStateException(errorMessage.
					replace("%s1", arg1.toString()).
					replace("%s2", arg2.toString()));
		}
	}
}
