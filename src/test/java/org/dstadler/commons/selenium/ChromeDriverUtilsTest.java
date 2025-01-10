package org.dstadler.commons.selenium;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;
import org.dstadler.commons.exec.ExecutionHelper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

import static org.dstadler.commons.selenium.ChromeDriverUtils.PROPERTY_CHROME_DRIVER;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ChromeDriverUtilsTest {
    @AfterEach
    public void tearDown() {
        ChromeDriverUtils.cleanUp();
    }

	private void assumeGoogleChrome() {
		// we only read from the registry on Windows
		if (SystemUtils.IS_OS_WINDOWS) {
			return;
		}

		CommandLine cmdLine = new CommandLine("google-chrome-stable");
		cmdLine.addArgument("--version");

		try {
			ExecutionHelper.getCommandResult(cmdLine, new File("."), 0, 10_000);
		} catch (IOException e) {
			Assumptions.assumeTrue(false, "Command " + cmdLine + " not available: " + e.getMessage());
		}
	}

    @Test
    public void testGetGoogleChromeVersion() throws IOException {
		assumeGoogleChrome();

		String googleChromeVersion = ChromeDriverUtils.getGoogleChromeVersion();

        assertTrue(StringUtils.isNotBlank(googleChromeVersion));

        assertTrue(googleChromeVersion.matches("\\d+\\.\\d+\\.\\d+"),
				"Version did not match \\d+\\.\\d+\\.\\d+, did have version: " + googleChromeVersion);
    }

    @Test
    public void testConfigureMatchingChromeDriver() throws IOException {
		assumeGoogleChrome();

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

        ChromeDriverUtils.configureMatchingChromeDriver("125");

        String driverFile = System.getProperty(PROPERTY_CHROME_DRIVER);
        assertTrue(StringUtils.isNotBlank(driverFile), "System property for chrome-driver should be set now");

        assertTrue(new File(driverFile).exists(), "Did not find file " + driverFile);

        // running it again does not change the result
        ChromeDriverUtils.configureMatchingChromeDriver("125");
        assertEquals(driverFile, System.getProperty(PROPERTY_CHROME_DRIVER));

		ChromeDriverUtils.configureMatchingChromeDriver("126");
    }

	@Test
	public void testConfigureMatchingChromeDriverOldVersionFull() {
		assertThrows(IOException.class,
				() -> ChromeDriverUtils.configureMatchingChromeDriver("113.0.5672"),
				"Support for old versions was removed");
		/*String driverFile = System.getProperty(PROPERTY_CHROME_DRIVER);
		assertTrue(new File(driverFile).exists());
		assertTrue(driverFile.contains("113.0.5672"));*/
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
