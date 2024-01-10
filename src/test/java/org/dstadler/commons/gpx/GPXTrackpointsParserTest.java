package org.dstadler.commons.gpx;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.SortedMap;

import org.junit.Ignore;
import org.junit.Test;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

public class GPXTrackpointsParserTest {
	public static final File GPX_FILE_1 = new File("src/test/data", "26624974.gpx");
	public static final File GPX_FILE_2 = new File("src/test/data", "1651066759000.gpx");
	public static final File GPX_FILE_3 = new File("src/test/data", "9266323613.gpx");

	@Test
    public void parse() throws Exception {
        GPXTrackpointsParser parser = new GPXTrackpointsParser();
        SortedMap<Long, TrackPoint> map =
                parser.parseContent(new ByteArrayInputStream(GPX_XML.getBytes(StandardCharsets.UTF_8)));

        assertNotNull(map);
        assertEquals("Had: " + map, 1, map.size());
		TrackPoint point = map.get(map.firstKey());
		assertEquals(48.456194, point.getLatitude(), 0.000001);
        assertEquals(13.99866, point.getLongitude(), 0.000001);
        assertEquals(512, point.getElevation(), 0.000001);
        assertEquals(0.413765975271989, point.getSpeed(), 0.000001);
        assertEquals(0, point.getHr());
		assertEquals(1393494179420L, point.getTime());
		assertEquals("10:42:59", point.getTimeString());

		assertEquals("00:00", point.formatTime(1393494179420L));
		assertEquals("-60:00", point.formatTime(1393497779420L));
		assertEquals("00:01", point.formatTime(1393494178420L));
		assertEquals("01:41", point.formatTime(1393494078420L));
		assertEquals("23224902:59", point.formatTime(0L));
    }

	@Test
	public void parseMetaData() throws Exception {
		GPXTrackpointsParser parser = new GPXTrackpointsParser();
		SortedMap<Long, TrackPoint> map =
				parser.parseContent(new ByteArrayInputStream(GPX_XML_METADATA.getBytes(StandardCharsets.UTF_8)));

		assertNotNull(map);
		assertEquals("Had: " + map, 1, map.size());
	}

	@Test
	public void parseInvalidTime() throws Exception {
		GPXTrackpointsParser parser = new GPXTrackpointsParser();
		SortedMap<Long, TrackPoint> map =
				parser.parseContent(new ByteArrayInputStream(GPX_XML_INVALID_TIME.getBytes(StandardCharsets.UTF_8)));

		assertNotNull(map);
		assertEquals("Had: " + map, 1, map.size());
	}

	@Test
	public void parseBrokenTime() {
		GPXTrackpointsParser parser = new GPXTrackpointsParser();
		assertThrows(IllegalStateException.class,
				() -> parser.parseContent(new ByteArrayInputStream(GPX_XML_BROKEN_TIME_2021.getBytes(StandardCharsets.UTF_8))));
		assertThrows(IllegalStateException.class,
				() -> parser.parseContent(new ByteArrayInputStream(GPX_XML_BROKEN_TIME_2022.getBytes(StandardCharsets.UTF_8))));
	}

	@Test
    public void parseFile() throws Exception {
        SortedMap<Long, TrackPoint> map =
                GPXTrackpointsParser.parseContent(GPX_FILE_1);

        assertNotNull(map);
        assertEquals("Had: " + map, 5, map.size());
		TrackPoint point = map.get(map.firstKey());
		assertEquals(48.456194, point.getLatitude(), 0.000001);
        assertEquals(13.99866, point.getLongitude(), 0.000001);
        assertEquals(512, point.getElevation(), 0.000001);

		assertEquals(0.413765975271989, point.getSpeed(), 0.000001);
		assertEquals(56, point.getHr());
		assertEquals(0, point.getCadence());
		assertEquals(1393494179420L, point.getTime());
		assertEquals("10:42:59", point.getTimeString());

		TrackPoint lastPoint = map.get(map.lastKey());
		assertEquals(48.456214, lastPoint.getLatitude(), 0.000001);
        assertEquals(13.99865, lastPoint.getLongitude(), 0.000001);
        assertEquals(515, lastPoint.getElevation(), 0.000001);

		assertEquals(0, lastPoint.getSpeed(), 0.000001);
		assertEquals(59, lastPoint.getHr());
		assertEquals(0, lastPoint.getCadence());
		assertEquals(1393494913840L, lastPoint.getTime());
		assertEquals("10:55:13", lastPoint.getTimeString());
    }

    @Test
    public void parseFile2022a() throws Exception {
		SortedMap<Long, TrackPoint> map =
				GPXTrackpointsParser.parseContent(GPX_FILE_2);
		assertNotNull(map);

		TrackPoint point = map.get(map.firstKey());
		assertEquals("Newer files have date in UTC",
				1651066885000L, point.getTime());
		assertEquals("Newer files have date in UTC",
				"15:41:25", point.getTimeString());

		assertEquals(48.3176978, point.getLatitude(), 0.001);
		assertEquals(14.3050261, point.getLongitude(), 0.001);
		assertEquals(261, point.getElevation(), 0.001);
		assertEquals(150, point.getHr());
		assertEquals(85, point.getCadence());
		assertEquals(23.9, point.getTemp(), 0.001);
		assertEquals(2.99, point.getSpeed(), 0.001);
		assertEquals(10215, point.getSeaLevelPressure());
	}

    @Test
    public void parseFile2022b() throws Exception {
		SortedMap<Long, TrackPoint> map =
				GPXTrackpointsParser.parseContent(GPX_FILE_3);
		assertNotNull(map);

		TrackPoint point = map.get(map.firstKey());
		assertEquals(1658675673000L, point.getTime());
		assertEquals("17:14:33", point.getTimeString());

		assertEquals(47.544777, point.getLatitude(), 0.001);
		assertEquals(12.56987, point.getLongitude(), 0.001);
		assertEquals(0, point.getElevation(), 0.001);
		assertEquals(78, point.getHr());
		assertEquals(0, point.getCadence());
		assertEquals(32, point.getTemp(), 0.001);
		assertEquals(0, point.getSpeed(), 0.001);
		assertEquals(0, point.getSeaLevelPressure());
    }

	@Test
	public void parseNoTime() throws Exception {
		GPXTrackpointsParser parser = new GPXTrackpointsParser();
		SortedMap<Long, TrackPoint> map =
				parser.parseContent(new ByteArrayInputStream(GPX_XML_NO_TIME.getBytes(StandardCharsets.UTF_8)));

		assertNotNull(map);
		assertEquals("Had: " + map, 2, map.size());

		Iterator<TrackPoint> it = map.values().iterator();

		TrackPoint point = it.next();
		assertEquals(48.456194, point.getLatitude(), 0.000001);
		assertEquals(13.99866, point.getLongitude(), 0.000001);
		assertEquals(512, point.getElevation(), 0.000001);
		assertEquals(0.413765975271989, point.getSpeed(), 0.000001);
		assertEquals(0, point.getHr());
		assertEquals(1L, point.getTime());

		point = it.next();
		assertEquals(48.556194, point.getLatitude(), 0.000001);
		assertEquals(13.89866, point.getLongitude(), 0.000001);
		assertEquals(511, point.getElevation(), 0.000001);
		assertEquals(0.412765975271989, point.getSpeed(), 0.000001);
		assertEquals(0, point.getHr());
		assertEquals(2L, point.getTime());
	}

	@Test
	public void parseInvalid() {
		assertThrows(IOException.class,
				() -> GPXTrackpointsParser.parseContent(new File("notexist.gpx")));
	}

	@Test
	public void parseInvalidXML() {
		GPXTrackpointsParser parser = new GPXTrackpointsParser();
		Attributes atts = new AttributesImpl();

		parser.startElement(null, "trkpt", null, atts);

		assertThrows(IllegalStateException.class,
				() -> parser.startElement(null, "trkpt", null, null));
	}

	@Ignore("Calls System.exit(1)")
	@Test
	public void testMainEmpty() throws IOException, SAXException {
		GPXTrackpointsParser.main(new String[0]);
	}

	@Ignore("Calls System.exit(1)")
	@Test
	public void testMainInvalid() throws IOException, SAXException {
		GPXTrackpointsParser.main(new String[] { "notexists.gpx" });
	}

	@Test
	public void testMain1() throws IOException, SAXException {
		GPXTrackpointsParser.main(new String[] { GPX_FILE_1.getAbsolutePath() });
	}

	@Test
	public void testMain2() throws IOException, SAXException {
		GPXTrackpointsParser.main(new String[] {
				GPX_FILE_1.getAbsolutePath(),
				GPX_FILE_2.getAbsolutePath() });
	}

	@Ignore
	@Test
	public void testParseLocalFile() throws IOException, SAXException {
		final SortedMap<Long, TrackPoint> map = GPXTrackpointsParser.parseContent(
				new File("/tmp/test.gpx"));
		assertNotNull(map);
		assertFalse(map.isEmpty());
	}

	private static final String GPX_XML =
			"<gpx xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.topografix.com/GPX/1/1 http://www.topografix.com/GPX/1/1/gpx.xsd http://www.cluetrust.com/XML/GPXDATA/1/0 http://www.cluetrust.com/Schemas/gpxdata10.xsd http://www.garmin.com/xmlschemas/TrackPointExtension/v1 http://www.garmin.com/xmlschemas/TrackPointExtensionv1.xsd\" xmlns:gpxdata=\"http://www.topografix.com/GPX/1/0\" xmlns:gpxtpx=\"http://www.garmin.com/xmlschemas/TrackPointExtension/v1\" version=\"1.1\" creator=\"Movescount - http://www.movescount.com\" xmlns=\"http://www.topografix.com/GPX/1/1\">\n" +
					"  <trk>\n" +
					"    <name>Move</name>\n" +
					"    <trkseg>\n" +
					"      <trkpt lat=\"48.456194\" lon=\"13.99866\">\n" +
					"        <ele>512</ele>\n" +
					"        <time>2014-02-27T10:42:59.420Z</time>\n" +
					"        <extensions>\n" +
					"          <gpxdata:speed>0.413765975271989</gpxdata:speed>\n" +
					"        </extensions>\n" +
					"      </trkpt>\n" +
					"    </trkseg>\n" +
					"  </trk>\n" +
					"</gpx>\n";

	private static final String GPX_XML_METADATA =
			"<gpx xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.topografix.com/GPX/1/1 http://www.topografix.com/GPX/1/1/gpx.xsd http://www.cluetrust.com/XML/GPXDATA/1/0 http://www.cluetrust.com/Schemas/gpxdata10.xsd http://www.garmin.com/xmlschemas/TrackPointExtension/v1 http://www.garmin.com/xmlschemas/TrackPointExtensionv1.xsd\" xmlns:gpxdata=\"http://www.topografix.com/GPX/1/0\" xmlns:gpxtpx=\"http://www.garmin.com/xmlschemas/TrackPointExtension/v1\" version=\"1.1\" creator=\"Movescount - http://www.movescount.com\" xmlns=\"http://www.topografix.com/GPX/1/1\">\n" +
					"    <metadata>\n"
					+ "    <link href=\"connect.garmin.com\">\n"
					+ "      <text>Garmin Connect</text>\n"
					+ "    </link>\n"
					+ "    <time>2022-07-09T13:32:31.000Z</time>\n"
					+ "  </metadata>\n"
					+ "<trk>\n" +
					"    <name>Move</name>\n" +
					"    <trkseg>\n" +
					"      <trkpt lat=\"48.456194\" lon=\"13.99866\">\n" +
					"        <ele>512</ele>\n" +
					"        <time>2014-02-27T10:42:59.420Z</time>\n" +
					"        <extensions>\n" +
					"          <gpxdata:speed>0.413765975271989</gpxdata:speed>\n" +
					"        </extensions>\n" +
					"      </trkpt>\n" +
					"    </trkseg>\n" +
					"  </trk>\n" +
					"</gpx>\n";

	private static final String GPX_XML_INVALID_TIME =
			"<gpx xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.topografix.com/GPX/1/1 http://www.topografix.com/GPX/1/1/gpx.xsd http://www.cluetrust.com/XML/GPXDATA/1/0 http://www.cluetrust.com/Schemas/gpxdata10.xsd http://www.garmin.com/xmlschemas/TrackPointExtension/v1 http://www.garmin.com/xmlschemas/TrackPointExtensionv1.xsd\" xmlns:gpxdata=\"http://www.topografix.com/GPX/1/0\" xmlns:gpxtpx=\"http://www.garmin.com/xmlschemas/TrackPointExtension/v1\" version=\"1.1\" creator=\"Movescount - http://www.movescount.com\" xmlns=\"http://www.topografix.com/GPX/1/1\">\n" +
					" <time>2022-07-09T13:32:31.000Z</time>\n" +
					"<trk>\n" +
					"    <name>Move</name>\n" +
					"    <trkseg>\n" +
					"      <trkpt lat=\"48.456194\" lon=\"13.99866\">\n" +
					"        <ele>512</ele>\n" +
					"        <time>2014-02-27T10:42:59.420Z</time>\n" +
					"        <extensions>\n" +
					"          <gpxdata:speed>0.413765975271989</gpxdata:speed>\n" +
					"        </extensions>\n" +
					"      </trkpt>\n" +
					"    </trkseg>\n" +
					"  </trk>\n" +
					"</gpx>\n";

	private static final String GPX_XML_BROKEN_TIME_2021 =
			"<gpx xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.topografix.com/GPX/1/1 http://www.topografix.com/GPX/1/1/gpx.xsd http://www.cluetrust.com/XML/GPXDATA/1/0 http://www.cluetrust.com/Schemas/gpxdata10.xsd http://www.garmin.com/xmlschemas/TrackPointExtension/v1 http://www.garmin.com/xmlschemas/TrackPointExtensionv1.xsd\" xmlns:gpxdata=\"http://www.topografix.com/GPX/1/0\" xmlns:gpxtpx=\"http://www.garmin.com/xmlschemas/TrackPointExtension/v1\" version=\"1.1\" creator=\"Movescount - http://www.movescount.com\" xmlns=\"http://www.topografix.com/GPX/1/1\">\n" +
					" <time>2022-07-09T13:32:31.000Z</time>\n" +
					"<trk>\n" +
					"    <name>Move</name>\n" +
					"    <trkseg>\n" +
					"      <trkpt lat=\"48.456194\" lon=\"13.99866\">\n" +
					"        <ele>512</ele>\n" +
					"        <time>2014-02-27Tasdagasdf10:42:59.420Z</time>\n" +
					"        <extensions>\n" +
					"          <gpxdata:speed>0.413765975271989</gpxdata:speed>\n" +
					"        </extensions>\n" +
					"      </trkpt>\n" +
					"    </trkseg>\n" +
					"  </trk>\n" +
					"</gpx>\n";

	private static final String GPX_XML_BROKEN_TIME_2022 =
			"<gpx xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.topografix.com/GPX/1/1 http://www.topografix.com/GPX/1/1/gpx.xsd http://www.cluetrust.com/XML/GPXDATA/1/0 http://www.cluetrust.com/Schemas/gpxdata10.xsd http://www.garmin.com/xmlschemas/TrackPointExtension/v1 http://www.garmin.com/xmlschemas/TrackPointExtensionv1.xsd\" xmlns:gpxdata=\"http://www.topografix.com/GPX/1/0\" xmlns:gpxtpx=\"http://www.garmin.com/xmlschemas/TrackPointExtension/v1\" version=\"1.1\" creator=\"Movescount - http://www.movescount.com\" xmlns=\"http://www.topografix.com/GPX/1/1\">\n" +
					" <time>2022-07-09T13:32:31.000Z</time>\n" +
					"<trk>\n" +
					"    <name>Move</name>\n" +
					"    <trkseg>\n" +
					"      <trkpt lat=\"48.456194\" lon=\"13.99866\">\n" +
					"        <ele>512</ele>\n" +
					"        <time>2023-02-27Tasdagasdf10:42:59.420Z</time>\n" +
					"        <extensions>\n" +
					"          <gpxdata:speed>0.413765975271989</gpxdata:speed>\n" +
					"        </extensions>\n" +
					"      </trkpt>\n" +
					"    </trkseg>\n" +
					"  </trk>\n" +
					"</gpx>\n";

	private static final String GPX_XML_NO_TIME =
			"<gpx xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.topografix.com/GPX/1/1 http://www.topografix.com/GPX/1/1/gpx.xsd http://www.cluetrust.com/XML/GPXDATA/1/0 http://www.cluetrust.com/Schemas/gpxdata10.xsd http://www.garmin.com/xmlschemas/TrackPointExtension/v1 http://www.garmin.com/xmlschemas/TrackPointExtensionv1.xsd\" xmlns:gpxdata=\"http://www.topografix.com/GPX/1/0\" xmlns:gpxtpx=\"http://www.garmin.com/xmlschemas/TrackPointExtension/v1\" version=\"1.1\" creator=\"Movescount - http://www.movescount.com\" xmlns=\"http://www.topografix.com/GPX/1/1\">\n" +
					"  <trk>\n" +
					"    <name>Move</name>\n" +
					"    <trkseg>\n" +
					"      <trkpt lat=\"48.456194\" lon=\"13.99866\">\n" +
					"        <ele>512</ele>\n" +
					"        <extensions>\n" +
					"          <gpxdata:speed>0.413765975271989</gpxdata:speed>\n" +
					"        </extensions>\n" +
					"      </trkpt>\n" +
					"      <trkpt lat=\"48.556194\" lon=\"13.89866\">\n" +
					"        <ele>511</ele>\n" +
					"        <extensions>\n" +
					"          <gpxdata:speed>0.412765975271989</gpxdata:speed>\n" +
					"        </extensions>\n" +
					"      </trkpt>\n" +
					"    </trkseg>\n" +
					"  </trk>\n" +
					"</gpx>\n";
}
