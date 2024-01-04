package org.dstadler.commons.gpx;

import static org.junit.Assert.assertEquals;

import java.util.Date;

import org.dstadler.commons.testing.TestHelpers;
import org.junit.Test;

public class TrackPointTest {
    @Test
    public void test() {
        TrackPoint point = new TrackPoint();
        TestHelpers.ToStringTest(point);

        point.setLatitude(1.1);
        point.setLongitude(1.3);
        point.setElevation(523);
        point.setTime(new Date(123456789L));
        point.setCadence(2);
        point.setHr(234);
        point.setSpeed(234);
        point.setTemp(28.2);
		point.setSeaLevelPressure(4732);
        TestHelpers.ToStringTest(point);

        assertEquals("11:17:36", point.getTimeString());
        assertEquals("03:20", point.formatTime(123256789L));

		assertEquals(1.1, point.getLatitude(), 0.001);
		assertEquals(1.3, point.getLongitude(), 0.001);
		assertEquals(523, point.getElevation(), 0.001);
		assertEquals(234, point.getHr());
		assertEquals(2, point.getCadence());
		assertEquals(234, point.getSpeed(), 0.001);
		assertEquals(28.2, point.getTemp(), 0.001);
		assertEquals(4732, point.getSeaLevelPressure());
	}
}