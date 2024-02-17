package org.dstadler.commons.selenium;

import static org.dstadler.commons.selenium.ChromeDriverUtils.PROPERTY_CHROME_DRIVER;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

public class ChromeDriverUtilsTest {
    @AfterEach
    public void tearDown() {
        ChromeDriverUtils.cleanUp();
    }

    @Test
    public void testGetGoogleChromeVersion() throws IOException {
        String googleChromeVersion = ChromeDriverUtils.getGoogleChromeVersion();

        assertTrue(StringUtils.isNotBlank(googleChromeVersion));

        assertTrue(googleChromeVersion.matches("\\d+\\.\\d+\\.\\d+"),
				"Version did not match \\d+\\.\\d+\\.\\d+, did have version: " + googleChromeVersion);
    }

    @Test
    public void testConfigureMatchingChromeDriver() throws IOException {
        assertTrue(StringUtils.isBlank(System.getProperty(PROPERTY_CHROME_DRIVER)),
				"System property for chrome-driver should not be set before starting this test");

		ChromeDriverUtils.configureMatchingChromeDriver();

        String driverFile = System.getProperty(PROPERTY_CHROME_DRIVER);
        assertTrue(StringUtils.isNotBlank(driverFile), "System property for chrome-driver should be set now");

        assertTrue(new File(driverFile).exists(), "Did not find file " + driverFile);

        // running it again does not change the result
        ChromeDriverUtils.configureMatchingChromeDriver();
        assertEquals(driverFile, System.getProperty(PROPERTY_CHROME_DRIVER));
    }

    @Test
    public void testConfigureMatchingChromeDriverWithVersion() throws IOException {
        assertTrue(StringUtils.isBlank(System.getProperty(PROPERTY_CHROME_DRIVER)),
				"System property for chrome-driver should not be set before starting this test");

        ChromeDriverUtils.configureMatchingChromeDriver("100");

        String driverFile = System.getProperty(PROPERTY_CHROME_DRIVER);
        assertTrue(StringUtils.isNotBlank(driverFile), "System property for chrome-driver should be set now");

        assertTrue(new File(driverFile).exists(), "Did not find file " + driverFile);

        // running it again does not change the result
        ChromeDriverUtils.configureMatchingChromeDriver("100");
        assertEquals(driverFile, System.getProperty(PROPERTY_CHROME_DRIVER));
    }

	@Test
	public void testConfigureMatchingChromeDriverInvalidVersion() {
		assertThrows(IOException.class,
				() -> ChromeDriverUtils.configureMatchingChromeDriver("1234567890"));
	}

	@Test
	public void testConfigureMatchingChromeDriverBlank() {
		assertThrows(IllegalStateException.class,
				() -> ChromeDriverUtils.configureMatchingChromeDriver(""));
	}

	@Test
	public void testConfigureMatchingChromeDriverNull() {
		assertThrows(IllegalStateException.class,
				() -> ChromeDriverUtils.configureMatchingChromeDriver(null));
	}

	// helper method to get coverage of the unused constructor
	@Test
	public void testPrivateConstructor() throws Exception {
		org.dstadler.commons.testing.PrivateConstructorCoverage.executePrivateConstructor(ChromeDriverUtils.class);
	}
}
