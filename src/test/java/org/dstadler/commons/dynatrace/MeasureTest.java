package org.dstadler.commons.dynatrace;

import static org.junit.Assert.*;

import org.junit.Test;

public class MeasureTest {

	@Test
	public void testSimple() {
		Measure measure = new Measure();
		assertEquals(0, measure.getValue(), 0.01);
		assertEquals(0, measure.getDynamicMeasures().size());
		assertNull(measure.getDynamicMeasureName());

		measure.incValue();
		assertEquals(1, measure.getValue(), 0.01);

		measure.addValue(2.34);
		assertEquals(3.34, measure.getValue(), 0.01);
	}

	@Test
	public void testConstructors() {
		Measure measure = new Measure();
		assertEquals(0, measure.getValue(), 0.01);

		measure = new Measure(23.4);
		assertEquals(23.4, measure.getValue(), 0.01);

		measure = new Measure("mes");
		assertEquals(0, measure.getValue(), 0.01);
		assertEquals("mes", measure.getDynamicMeasureName());

		measure = new Measure("mes2", 24.2);
		assertEquals(24.2, measure.getValue(), 0.01);
		assertEquals("mes2", measure.getDynamicMeasureName());
	}

	@Test
	public void testAdjustmentFactor() {
		Measure measure = new Measure("Test");
		measure.setAdjustmentFactor(2);

		assertEquals(0, measure.getValue(), 0.01);

		measure.setValue(3);
		assertEquals(6, measure.getValue(), 0.01);

		// add new dynamic measures
		measure.addDynamicMeasure("dyn1", 5);
		measure.addDynamicMeasure("dyn2", 3.222);
		assertEquals(10, measure.getDynamicMeasures().get("dyn1"), 0.01);
		assertEquals(6.444, measure.getDynamicMeasures().get("dyn2"), 0.01);

		// adding value to existing dynamic measures
		measure.addDynamicMeasure("dyn2", 2.123);
		assertEquals(10, measure.getDynamicMeasures().get("dyn1"), 0.01);
		assertEquals(10.690, measure.getDynamicMeasures().get("dyn2"), 0.01);
	}

	@Test
	public void testDynamicMeasureNameRequired() {
		Measure measure = new Measure();

		try {
			measure.addDynamicMeasure("dyn1", 5);
			fail("Should fail here");
		} catch (@SuppressWarnings("unused") NullPointerException e) {
			// expected here
		}
	}


	@Test
	public void testDynamicMeasureKeyRequired() {
		Measure measure = new Measure("some");

		try {
			measure.addDynamicMeasure(null, 5);
			fail("Should fail here");
		} catch (@SuppressWarnings("unused") NullPointerException e) {
			// expected here
		}
	}
}
