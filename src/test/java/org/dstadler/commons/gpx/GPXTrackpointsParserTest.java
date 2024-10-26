package org.dstadler.commons.gpx;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.ParseException;
import java.util.Date;
import java.util.Iterator;
import java.util.SortedMap;
import java.util.TimeZone;
import java.util.stream.Stream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.lang3.mutable.MutableInt;
import org.apache.commons.lang3.time.FastDateFormat;
import org.dstadler.commons.testing.TestHelpers;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.AttributesImpl;

public class GPXTrackpointsParserTest {
	public static final File GPX_FILE_1 = new File("src/test/data", "26624974.gpx");
	public static final File GPX_FILE_2 = new File("src/test/data", "1651066759000.gpx");
	public static final File GPX_FILE_3 = new File("src/test/data", "9266323613.gpx");
	public static final File GPX_FILE_4 = new File("src/test/data", "Ennstalerhuette_und_Tamischbachturm.gpx");
	public static final File GPX_FILE_5 = new File("src/test/data", "rte.gpx");

	@Test
    public void parse() throws Exception {
        GPXTrackpointsParser parser = new GPXTrackpointsParser();
        SortedMap<Long, TrackPoint> map =
                parser.parseContent(new ByteArrayInputStream(GPX_XML.getBytes(StandardCharsets.UTF_8)));

        assertNotNull(map);
        assertEquals(1, map.size(), "Had: " + map);
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
		assertEquals(1, map.size(), "Had: " + map);
	}

	@Test
	public void parseInvalidTime() throws Exception {
		GPXTrackpointsParser parser = new GPXTrackpointsParser();
		SortedMap<Long, TrackPoint> map =
				parser.parseContent(new ByteArrayInputStream(GPX_XML_INVALID_TIME.getBytes(StandardCharsets.UTF_8)));

		assertNotNull(map);
		assertEquals(1, map.size(), "Had: " + map);
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
        assertEquals(5, map.size(), "Had: " + map);
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
		assertEquals(1651066885000L, point.getTime(),
				"Newer files have date in UTC");
		assertEquals("15:41:25", point.getTimeString(),
				"Newer files have date in UTC");

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
    public void parseFileTourenportal() throws Exception {
		SortedMap<Long, TrackPoint> map =
				GPXTrackpointsParser.parseContent(GPX_FILE_4);
		assertNotNull(map);

		TrackPoint point = map.get(map.firstKey());
		assertEquals(1L, point.getTime());
		assertEquals("01:00:00", point.getTimeString());

		assertEquals(47.650893, point.getLatitude(), 0.001);
		assertEquals(14.694258, point.getLongitude(), 0.001);
		assertEquals(557, point.getElevation(), 0.001);
		assertEquals(0, point.getHr());
		assertEquals(0, point.getCadence());
		assertEquals(0, point.getTemp(), 0.001);
		assertEquals(0, point.getSpeed(), 0.001);
		assertEquals(0, point.getSeaLevelPressure());
    }

    @Test
    public void parseFileRTE() throws Exception {
		SortedMap<Long, TrackPoint> map =
				GPXTrackpointsParser.parseContent(GPX_FILE_5);
		assertNotNull(map);

		TrackPoint point = map.get(map.firstKey());
		assertEquals(1L, point.getTime());
		assertEquals("01:00:00", point.getTimeString());

		assertEquals(47.437445, point.getLatitude(), 0.001);
		assertEquals(14.686466, point.getLongitude(), 0.001);
		assertEquals(846.8803, point.getElevation(), 0.001);
		assertEquals(0, point.getHr());
		assertEquals(0, point.getCadence());
		assertEquals(0, point.getTemp(), 0.001);
		assertEquals(0, point.getSpeed(), 0.001);
		assertEquals(0, point.getSeaLevelPressure());
    }

	@Test
	public void parseNoTime() throws Exception {
		GPXTrackpointsParser parser = new GPXTrackpointsParser();
		SortedMap<Long, TrackPoint> map =
				parser.parseContent(new ByteArrayInputStream(GPX_XML_NO_TIME.getBytes(StandardCharsets.UTF_8)));

		assertNotNull(map);
		assertEquals(5, map.size(), "Had: " + map);

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

		point = it.next();
		assertEquals(48.556194, point.getLatitude(), 0.000001);
		assertEquals(13.89866, point.getLongitude(), 0.000001);
		assertEquals(0, point.getElevation(), 0.000001);
		assertEquals(0.412765975271989, point.getSpeed(), 0.000001);
		assertEquals(0, point.getHr());
		assertEquals(3L, point.getTime());

		point = it.next();
		assertEquals(48.556194, point.getLatitude(), 0.000001);
		assertEquals(13.89866, point.getLongitude(), 0.000001);
		assertEquals(0, point.getElevation(), 0.000001);
		assertEquals(0.412765975271989, point.getSpeed(), 0.000001);
		assertEquals(0, point.getHr());
		assertEquals(4L, point.getTime());

		point = it.next();
		assertEquals(48.556194, point.getLatitude(), 0.000001);
		assertEquals(13.89866, point.getLongitude(), 0.000001);
		assertEquals(0, point.getElevation(), 0.000001);
		assertEquals(0.412765975271989, point.getSpeed(), 0.000001);
		assertEquals(0, point.getHr());
		assertEquals(5L, point.getTime());
	}

	@Test
	public void parseEmptyLatLon() {
		GPXTrackpointsParser parser = new GPXTrackpointsParser();
		assertThrows(IllegalStateException.class,
				() -> parser.parseContent(new ByteArrayInputStream(GPX_XML_EMPTY_LAT_LON.getBytes(StandardCharsets.UTF_8))));
	}

	@Test
	public void parseInvalidXMLSyntax() throws IOException {
		GPXTrackpointsParser parser = new GPXTrackpointsParser();
		assertThrows(SAXParseException.class,
				() -> parser.parseContent(new ByteArrayInputStream(GPX_XML_INVALID_XML.getBytes(StandardCharsets.UTF_8))));

		File tempFile = File.createTempFile("GPXTrackpointsParserTest", ".gpx");
		try {
			FileUtils.writeStringToFile(tempFile, GPX_XML_INVALID_XML, "UTF-8");

			IOException exc = assertThrows(IOException.class,
					() -> GPXTrackpointsParser.parseContent(tempFile));

			TestHelpers.assertContains("Should contain file-name of invalid file",
					exc, tempFile.getName());
		} finally {
			assertTrue(!tempFile.exists() || tempFile.delete());
		}
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

	@Disabled("Calls System.exit(1)")
	@Test
	public void testMainEmpty() throws IOException {
		GPXTrackpointsParser.main(new String[0]);
	}

	@Disabled("Calls System.exit(1)")
	@Test
	public void testMainInvalid() throws IOException {
		GPXTrackpointsParser.main(new String[] { "notexists.gpx" });
	}

	@Test
	public void testMain1() throws IOException {
		GPXTrackpointsParser.main(new String[] { GPX_FILE_1.getAbsolutePath() });
	}

	@Test
	public void testMain2() throws IOException {
		GPXTrackpointsParser.main(new String[] {
				GPX_FILE_1.getAbsolutePath(),
				GPX_FILE_2.getAbsolutePath() });
	}

	@Test
	public void testParseBrokenFile() throws IOException {
		final SortedMap<Long, TrackPoint> trackPoints = GPXTrackpointsParser.parseContent(new File("src/test/data/sample_broken_1.gpx"));
		assertNotNull(trackPoints);
	}

	@Disabled
	@Test
	public void testParseLocalFile() throws IOException {
		final SortedMap<Long, TrackPoint> map = GPXTrackpointsParser.parseContent(
				new File("/tmp/test.gpx"));
		assertNotNull(map);
		assertFalse(map.isEmpty());
	}

	@Disabled("Verifies parsing of a large local corpus of GPX files if available")
	@Test
	public void testParseAllLocalFiles() throws IOException {
		MutableInt count = new MutableInt();
		try (Stream<Path> walk = Files.walk(Path.of("/usbc/CommonCrawlGPX"), FileVisitOption.FOLLOW_LINKS)) {
			walk.
				parallel().
				forEach(path -> {
					File gpxFile = path.toFile();

					if(gpxFile.isDirectory()) {
						return;
					}

					count.increment();

					if (gpxFile.length() == 0) {
						System.out.println("Skipping empty file " + gpxFile);
						return;
					}

					if (gpxFile.length() == 1048576 ||
							// exclude some invalid files
							gpxFile.getName().equals("tourenwelt.at_download.php_tourid=206&download=206_fuenfmandling.gpx") ||
							gpxFile.getName().equals("regepe.com_togpx_ffe3fb343b97b") ||
							gpxFile.getName().equals("www.naturpark-ehw.de_aktiv-im-np_wanderwege_naturparkweg-leine-werra.html_file=files_routen_naturparkweg-leine-werra.gpx") ||
							gpxFile.getName().equals("bike-alp.cz_wp-content_uploads_gpx_alpy-gravel-trasy_gravel-saalfelden.gpx")) {
						System.out.println("Skipping truncated file " + gpxFile);
						return;
					}

					System.out.println(count.getValue() + ": Processing: " + gpxFile);
					try {
						String str = FileUtils.readFileToString(gpxFile, "UTF-8").trim();
						if (str.contains("301 Moved Permanently") ||
							str.startsWith("Moved Permanently") ||
							str.startsWith("BCFZ") ||
							str.startsWith("Found") ||
							str.startsWith("302 Found") ||
							str.toLowerCase().startsWith("<!doctype html") ||
							str.toLowerCase().startsWith("<html") ||
							str.toUpperCase().startsWith("GEOMETRYCOLLECTION") ||
								StringUtils.isBlank(str)) {
							System.out.println("Skipping file with HTTP error " + gpxFile);
							return;
						}

						final SortedMap<Long, TrackPoint> trackPoints = GPXTrackpointsParser.parseContent(gpxFile);
						assertNotNull(trackPoints);
					} catch (IOException | RuntimeException e) {
						// ignore some broken files
						String stackTrace = ExceptionUtils.getStackTrace(e);
						if (e.getCause() instanceof SAXException ||
								(e.getCause() != null && e.getCause().getCause() instanceof SAXException) ||
								e instanceof IllegalStateException ||
								stackTrace.contains("Expected to have tag 'lat' and 'lon'") ||
								stackTrace.contains("For input string")) {
							System.out.println("Skipping broken file " + gpxFile + ": " + e + " - " + e.getCause());
							return;
						}
						throw new RuntimeException("Failed to process " + gpxFile, e);
					}
				});
		}
	}

	@Disabled("Allows to verify parsing of a single local files")
	@Test
	public void testParseSingleLocalFiles() throws IOException {
		final SortedMap<Long, TrackPoint> trackPoints = GPXTrackpointsParser.parseContent(new File("src/test/data/sample_broken_1.gpx"));
		assertNotNull(trackPoints);
	}

	private static final String GPX_XML =
            """
                    <gpx xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://www.topografix.com/GPX/1/1 http://www.topografix.com/GPX/1/1/gpx.xsd http://www.cluetrust.com/XML/GPXDATA/1/0 http://www.cluetrust.com/Schemas/gpxdata10.xsd http://www.garmin.com/xmlschemas/TrackPointExtension/v1 http://www.garmin.com/xmlschemas/TrackPointExtensionv1.xsd" xmlns:gpxdata="http://www.topografix.com/GPX/1/0" xmlns:gpxtpx="http://www.garmin.com/xmlschemas/TrackPointExtension/v1" version="1.1" creator="Movescount - http://www.movescount.com" xmlns="http://www.topografix.com/GPX/1/1">
                      <trk>
                        <name>Move</name>
                        <trkseg>
                          <trkpt lat="48.456194" lon="13.99866">
                            <ele>512</ele>
                            <time>2014-02-27T10:42:59.420Z</time>
                            <extensions>
                              <gpxdata:speed>0.413765975271989</gpxdata:speed>
                            </extensions>
                          </trkpt>
                        </trkseg>
                      </trk>
                    </gpx>
                    """;

	private static final String GPX_XML_METADATA =
            """
                    <gpx xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://www.topografix.com/GPX/1/1 http://www.topografix.com/GPX/1/1/gpx.xsd http://www.cluetrust.com/XML/GPXDATA/1/0 http://www.cluetrust.com/Schemas/gpxdata10.xsd http://www.garmin.com/xmlschemas/TrackPointExtension/v1 http://www.garmin.com/xmlschemas/TrackPointExtensionv1.xsd" xmlns:gpxdata="http://www.topografix.com/GPX/1/0" xmlns:gpxtpx="http://www.garmin.com/xmlschemas/TrackPointExtension/v1" version="1.1" creator="Movescount - http://www.movescount.com" xmlns="http://www.topografix.com/GPX/1/1">
                        <metadata>
                        <link href="connect.garmin.com">
                          <text>Garmin Connect</text>
                        </link>
                        <time>2022-07-09T13:32:31.000Z</time>
                      </metadata>
                    <trk>
                        <name>Move</name>
                        <trkseg>
                          <trkpt lat="48.456194" lon="13.99866">
                            <ele>512</ele>
                            <time>2014-02-27T10:42:59.420Z</time>
                            <extensions>
                              <gpxdata:speed>0.413765975271989</gpxdata:speed>
                            </extensions>
                          </trkpt>
                        </trkseg>
                      </trk>
                    </gpx>
                    """;

	private static final String GPX_XML_INVALID_TIME =
            """
                    <gpx xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://www.topografix.com/GPX/1/1 http://www.topografix.com/GPX/1/1/gpx.xsd http://www.cluetrust.com/XML/GPXDATA/1/0 http://www.cluetrust.com/Schemas/gpxdata10.xsd http://www.garmin.com/xmlschemas/TrackPointExtension/v1 http://www.garmin.com/xmlschemas/TrackPointExtensionv1.xsd" xmlns:gpxdata="http://www.topografix.com/GPX/1/0" xmlns:gpxtpx="http://www.garmin.com/xmlschemas/TrackPointExtension/v1" version="1.1" creator="Movescount - http://www.movescount.com" xmlns="http://www.topografix.com/GPX/1/1">
                     <time>2022-07-09T13:32:31.000Z</time>
                    <trk>
                        <name>Move</name>
                        <trkseg>
                          <trkpt lat="48.456194" lon="13.99866">
                            <ele>512</ele>
                            <time>2014-02-27T10:42:59.420Z</time>
                            <extensions>
                              <gpxdata:speed>0.413765975271989</gpxdata:speed>
                            </extensions>
                          </trkpt>
                        </trkseg>
                      </trk>
                    </gpx>
                    """;

	private static final String GPX_XML_BROKEN_TIME_2021 =
            """
                    <gpx xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://www.topografix.com/GPX/1/1 http://www.topografix.com/GPX/1/1/gpx.xsd http://www.cluetrust.com/XML/GPXDATA/1/0 http://www.cluetrust.com/Schemas/gpxdata10.xsd http://www.garmin.com/xmlschemas/TrackPointExtension/v1 http://www.garmin.com/xmlschemas/TrackPointExtensionv1.xsd" xmlns:gpxdata="http://www.topografix.com/GPX/1/0" xmlns:gpxtpx="http://www.garmin.com/xmlschemas/TrackPointExtension/v1" version="1.1" creator="Movescount - http://www.movescount.com" xmlns="http://www.topografix.com/GPX/1/1">
                     <time>2022-07-09T13:32:31.000Z</time>
                    <trk>
                        <name>Move</name>
                        <trkseg>
                          <trkpt lat="48.456194" lon="13.99866">
                            <ele>512</ele>
                            <time>2014-02-27Tasdagasdf10:42:59.420Z</time>
                            <extensions>
                              <gpxdata:speed>0.413765975271989</gpxdata:speed>
                            </extensions>
                          </trkpt>
                        </trkseg>
                      </trk>
                    </gpx>
                    """;

	private static final String GPX_XML_BROKEN_TIME_2022 =
            """
                    <gpx xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://www.topografix.com/GPX/1/1 http://www.topografix.com/GPX/1/1/gpx.xsd http://www.cluetrust.com/XML/GPXDATA/1/0 http://www.cluetrust.com/Schemas/gpxdata10.xsd http://www.garmin.com/xmlschemas/TrackPointExtension/v1 http://www.garmin.com/xmlschemas/TrackPointExtensionv1.xsd" xmlns:gpxdata="http://www.topografix.com/GPX/1/0" xmlns:gpxtpx="http://www.garmin.com/xmlschemas/TrackPointExtension/v1" version="1.1" creator="Movescount - http://www.movescount.com" xmlns="http://www.topografix.com/GPX/1/1">
                     <time>2022-07-09T13:32:31.000Z</time>
                    <trk>
                        <name>Move</name>
                        <trkseg>
                          <trkpt lat="48.456194" lon="13.99866">
                            <ele>512</ele>
                            <time>2023-02-27Tasdagasdf10:42:59.420Z</time>
                            <extensions>
                              <gpxdata:speed>0.413765975271989</gpxdata:speed>
                            </extensions>
                          </trkpt>
                        </trkseg>
                      </trk>
                    </gpx>
                    """;

	private static final String GPX_XML_NO_TIME =
            """
                    <gpx xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://www.topografix.com/GPX/1/1 http://www.topografix.com/GPX/1/1/gpx.xsd http://www.cluetrust.com/XML/GPXDATA/1/0 http://www.cluetrust.com/Schemas/gpxdata10.xsd http://www.garmin.com/xmlschemas/TrackPointExtension/v1 http://www.garmin.com/xmlschemas/TrackPointExtensionv1.xsd" xmlns:gpxdata="http://www.topografix.com/GPX/1/0" xmlns:gpxtpx="http://www.garmin.com/xmlschemas/TrackPointExtension/v1" version="1.1" creator="Movescount - http://www.movescount.com" xmlns="http://www.topografix.com/GPX/1/1">
                      <trk>
                        <name>Move</name>
                        <trkseg>
                          <trkpt lat="48.456194" lon="13.99866">
                            <ele>512</ele>
                            <extensions>
                              <gpxdata:speed>0.413765975271989</gpxdata:speed>
                            </extensions>
                          </trkpt>
                          <trkpt lat="48.556194" lon="13.89866">
                            <ele>511</ele>
                            <extensions>
                              <gpxdata:speed>0.412765975271989</gpxdata:speed>
                            </extensions>
                          </trkpt>
                          <trkpt lat="48.556194" lon="13.89866">
                            <ele>null</ele>
                            <time></time>
                            <extensions>
                              <gpxdata:speed>0.412765975271989</gpxdata:speed>
                            </extensions>
                          </trkpt>
                          <trkpt lat="48.556194" lon="13.89866">
                            <ele></ele>
                            <time>0</time>
                            <extensions>
                              <gpxdata:speed>0.412765975271989</gpxdata:speed>
                            </extensions>
                          </trkpt>
                          <trkpt lat="48.556194" lon="13.89866">
                            <ele>  </ele>
                            <extensions>
                              <gpxdata:speed>0.412765975271989</gpxdata:speed>
                            </extensions>
                          </trkpt>
                        </trkseg>
                      </trk>
                    </gpx>
                    """;

	private static final String GPX_XML_EMPTY_LAT_LON =
            """
                    <gpx xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://www.topografix.com/GPX/1/1 http://www.topografix.com/GPX/1/1/gpx.xsd http://www.cluetrust.com/XML/GPXDATA/1/0 http://www.cluetrust.com/Schemas/gpxdata10.xsd http://www.garmin.com/xmlschemas/TrackPointExtension/v1 http://www.garmin.com/xmlschemas/TrackPointExtensionv1.xsd" xmlns:gpxdata="http://www.topografix.com/GPX/1/0" xmlns:gpxtpx="http://www.garmin.com/xmlschemas/TrackPointExtension/v1" version="1.1" creator="Movescount - http://www.movescount.com" xmlns="http://www.topografix.com/GPX/1/1">
                      <trk>
                        <name>Move</name>
                        <trkseg>
                          <trkpt lat="48.456194" lon="13.99866">
                            <ele>512</ele>
                            <extensions>
                              <gpxdata:speed>0.413765975271989</gpxdata:speed>
                            </extensions>
                          </trkpt>
                          <trkpt lat="">
                            <ele>511</ele>
                            <extensions>
                              <gpxdata:speed>0.412765975271989</gpxdata:speed>
                            </extensions>
                          </trkpt>
                        </trkseg>
                      </trk>
                    </gpx>
                    """;

	private static final String GPX_XML_INVALID_XML =
            """
                    <gpx> xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://www.topografix.com/GPX/1/1 http://www.topografix.com/GPX/1/1/gpx.xsd http://www.cluetrust.com/XML/GPXDATA/1/0 http://www.cluetrust.com/Schemas/gpxdata10.xsd http://www.garmin.com/xmlschemas/TrackPointExtension/v1 http://www.garmin.com/xmlschemas/TrackPointExtensionv1.xsd" xmlns:gpxdata="http://www.topografix.com/GPX/1/0" xmlns:gpxtpx="http://www.garmin.com/xmlschemas/TrackPointExtension/v1" version="1.1" creator="Movescount - http://www.movescount.com" xmlns="http://www.topografix.com/GPX/1/1">
                      <trk>
                        <name>Move</name>
                        <trkseg>
                          <trkpt lat="48.456194" lon="13.99866">
                            <ele>512</ele>
                            <extensions>
                              <gpxdata:speed>0.413765975271989</gpxdata:speed>
                            </extensions>
                          </trkpt>
                          <trkpt lat="">
                            <ele>511</ele>
                            <extensions>
                              <gpxdata:speed>0.412765975271989</gpxdata:speed>
                            </extensions>
                          </trkpt>
                        </trkseg>
                      </trk>
                    </gpx>
                    """;

	@Test
	void parseTime() throws ParseException {
		Date date = FastDateFormat.getInstance("yyyy-MM-dd'T'HH:mm:ss.SSSZZ", TimeZone.getTimeZone("UTC"))
				.parse("2009-03-07T08:59:23.000+01:00");
		assertNotNull(date);

		date = FastDateFormat.getInstance("yyyy-MM-dd'T'HH:mm:ss", TimeZone.getTimeZone("UTC"))
				.parse("2018-08-05T08:36:59-07:00");
		assertNotNull(date);
	}
}
