package org.dstadler.commons.image;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;


public class ImageUtilsTest {
	@Test
	public void testGetTextAsPNG() throws Exception {
		byte[] pngData = ImageUtils.getTextAsPNG("some text");
		assertNotNull(pngData);
		assertTrue(pngData.length > 0);
	}

	// helper method to get coverage of the unused constructor
	@Test
	public void testPrivateConstructor() throws Exception {
		org.dstadler.commons.testing.PrivateConstructorCoverage.executePrivateConstructor(ImageUtils.class);
	}
}
