package org.dstadler.commons.os;

import org.junit.Test;

import org.dstadler.commons.testing.PrivateConstructorCoverage;

public class WindowTitleTest {

	@Test
	public void testSetConsoleTitle() {
		// will do nothing on Linux...
		WindowTitle.setConsoleTitle("sometitle");
	}

	// helper method to get coverage of the unused constructor
	@Test
	public void testPrivateConstructor() throws Exception {
		PrivateConstructorCoverage.executePrivateConstructor(WindowTitle.class);
	}

	public static void main(String[] args) throws Exception {
		WindowTitle.setConsoleTitle("this is a test");
		Thread.sleep(50000);
	}
}
